package de.peeeq.wurstscript.translation.lua.translation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static assertion helpers for the Lua backend.
 *
 * These are called after Lua code has been emitted to catch backend invariant
 * violations early (e.g. leaked raw native calls that should have been rewritten).
 */
public class LuaAssertions {

    private LuaAssertions() {}

    /**
     * Asserts that {@code luaCode} contains no raw call to {@code GetHandleId}.
     *
     * In Lua mode handle IDs can desync, so all uses of {@code GetHandleId} must
     * be rewritten to {@code __wurst_GetHandleId} which uses a stable table-based
     * counter instead.
     */
    public static void assertNoLeakedGetHandleIdCalls(String luaCode) {
        Set<String> called = collectCalledFunctionNames(luaCode);
        if (called.contains("GetHandleId")) {
            throw new RuntimeException(
                "Wurst Lua backend assertion failed: raw GetHandleId() call found in generated Lua. "
                + "Use the __wurst_GetHandleId polyfill (table-based) instead to avoid desync.");
        }
    }

    /**
     * Asserts that {@code luaCode} contains no raw call to any of the Jass hashtable
     * natives (SaveInteger, LoadBoolean, …) that should have been rewritten to their
     * {@code __wurst_} prefixed counterparts, and that every {@code __wurst_} hashtable
     * helper that is called is also defined in the output.
     */
    public static void assertNoLeakedHashtableNativeCalls(String luaCode) {
        List<String> leaked = new ArrayList<>();
        List<String> missingHelpers = new ArrayList<>();
        Set<String> calledFunctionNames = collectCalledFunctionNames(luaCode);
        Set<String> definedFunctionNames = collectDefinedFunctionNames(luaCode);
        for (String nativeName : LuaTranslator.allHashtableNativeNames()) {
            if (calledFunctionNames.contains(nativeName)) {
                leaked.add(nativeName);
            }
            String helperName = "__wurst_" + nativeName;
            boolean helperCalled = calledFunctionNames.contains(helperName);
            boolean helperDefined = definedFunctionNames.contains(helperName);
            if (helperCalled && !helperDefined) {
                missingHelpers.add(helperName);
            }
        }
        if (!leaked.isEmpty()) {
            throw new RuntimeException("Wurst Lua backend assertion failed: leaked raw hashtable native calls in generated Lua: "
                + String.join(", ", leaked));
        }
        if (!missingHelpers.isEmpty()) {
            throw new RuntimeException("Wurst Lua backend assertion failed: missing __wurst hashtable helper definitions in generated Lua: "
                + String.join(", ", missingHelpers));
        }
    }

    /**
     * Collects all function names that appear as CALLS in the Lua source.
     *
     * Skips string literals, comments, and function declaration names (including
     * method-syntax declarations like {@code function Foo:bar()} or
     * {@code function Foo.bar()}) to avoid false positives.
     */
    static Set<String> collectCalledFunctionNames(String text) {
        Set<String> result = new HashSet<>();
        int length = text.length();
        int index = 0;
        while (index < length) {
            char ch = text.charAt(index);

            // Skip Lua comments: -- short or --[[ long ]]
            if (ch == '-' && index + 1 < length && text.charAt(index + 1) == '-') {
                int longLevel = countLongBracketLevel(text, index + 2);
                if (longLevel >= 0) {
                    index = skipLongString(text, index + 2, longLevel);
                } else {
                    // Short comment: skip to end of line
                    while (index < length && text.charAt(index) != '\n') {
                        index++;
                    }
                }
                continue;
            }

            // Skip string literals: "..." or '...'
            if (ch == '"' || ch == '\'') {
                index = skipQuotedString(text, index, ch);
                continue;
            }

            // Skip long strings: [[...]] or [=[...]=]
            if (ch == '[') {
                int longLevel = countLongBracketLevel(text, index);
                if (longLevel >= 0) {
                    index = skipLongString(text, index, longLevel);
                    continue;
                }
            }

            // Skip function declarations: after the 'function' keyword the name tokens
            // (including A.B or A:B method syntax) are NOT calls.
            if (matchesWord(text, index, "function")) {
                index = skipFunctionDeclarationName(text, index + "function".length());
                continue;
            }

            // Check identifier followed by '(' → function call
            if (isIdentifierStart(ch)) {
                int end = scanIdentifierEnd(text, index + 1);
                int next = skipWhitespace(text, end);
                if (next < length && text.charAt(next) == '(') {
                    result.add(text.substring(index, end));
                }
                index = end;
                continue;
            }

            index++;
        }
        return result;
    }

    /**
     * Collects function names that appear as DEFINITIONS in the Lua source.
     *
     * Handles both simple ({@code function name(}) and method-syntax
     * ({@code function A:name(} or {@code function A.name(}) declarations.
     * Skips string literals and comments.
     */
    static Set<String> collectDefinedFunctionNames(String text) {
        Set<String> result = new HashSet<>();
        int length = text.length();
        int index = 0;
        while (index < length) {
            char ch = text.charAt(index);

            // Skip comments
            if (ch == '-' && index + 1 < length && text.charAt(index + 1) == '-') {
                int longLevel = countLongBracketLevel(text, index + 2);
                if (longLevel >= 0) {
                    index = skipLongString(text, index + 2, longLevel);
                } else {
                    while (index < length && text.charAt(index) != '\n') {
                        index++;
                    }
                }
                continue;
            }

            // Skip string literals
            if (ch == '"' || ch == '\'') {
                index = skipQuotedString(text, index, ch);
                continue;
            }
            if (ch == '[') {
                int longLevel = countLongBracketLevel(text, index);
                if (longLevel >= 0) {
                    index = skipLongString(text, index, longLevel);
                    continue;
                }
            }

            if (!matchesWord(text, index, "function")) {
                index++;
                continue;
            }

            // Skip past 'function', then scan the name
            int pos = skipWhitespace(text, index + "function".length());
            if (pos >= length || !isIdentifierStart(text.charAt(pos))) {
                index++;
                continue;
            }

            // Walk A.B.C or A:B chains, keeping track of the last identifier
            String lastName = null;
            while (pos < length && isIdentifierStart(text.charAt(pos))) {
                int nameEnd = scanIdentifierEnd(text, pos + 1);
                lastName = text.substring(pos, nameEnd);
                pos = nameEnd;
                if (pos < length && (text.charAt(pos) == '.' || text.charAt(pos) == ':')) {
                    pos++; // consume '.' or ':'
                } else {
                    break;
                }
            }

            int next = skipWhitespace(text, pos);
            if (lastName != null && next < length && text.charAt(next) == '(') {
                result.add(lastName);
            }
            index = pos;
        }
        return result;
    }

    /**
     * After the {@code function} keyword, skip past the declaration name
     * (which may include {@code A.B} or {@code A:B} qualifiers) and return
     * the position after the opening {@code (}.
     *
     * If there is no valid name, returns the position just after the keyword.
     */
    private static int skipFunctionDeclarationName(String text, int index) {
        int length = text.length();
        int pos = skipWhitespace(text, index);

        if (pos >= length || !isIdentifierStart(text.charAt(pos))) {
            // Anonymous function: 'function(' — no name to skip
            return pos;
        }

        // Walk A.B.C or A:B chains
        while (pos < length && isIdentifierStart(text.charAt(pos))) {
            pos = scanIdentifierEnd(text, pos + 1);
            if (pos < length && (text.charAt(pos) == '.' || text.charAt(pos) == ':')) {
                pos++; // consume '.' or ':'
            } else {
                break;
            }
        }

        // Skip to just after '(' so the outer loop doesn't re-examine the '('
        pos = skipWhitespace(text, pos);
        if (pos < length && text.charAt(pos) == '(') {
            pos++;
        }
        return pos;
    }

    /**
     * Returns the long-bracket level of a {@code [=..=[} opener at {@code index},
     * or -1 if there is no valid long-bracket opener at that position.
     */
    private static int countLongBracketLevel(String text, int index) {
        int length = text.length();
        if (index >= length || text.charAt(index) != '[') {
            return -1;
        }
        int level = 0;
        int pos = index + 1;
        while (pos < length && text.charAt(pos) == '=') {
            level++;
            pos++;
        }
        if (pos < length && text.charAt(pos) == '[') {
            return level;
        }
        return -1;
    }

    /**
     * Skips past a long string starting with {@code [=..=[} at {@code index}.
     * The {@code level} is the number of {@code =} signs in the bracket.
     * Returns the index after the closing {@code ]=..=]}.
     */
    private static int skipLongString(String text, int index, int level) {
        int length = text.length();
        // Skip the opening bracket [=..=[  (1 + level + 1 chars)
        int pos = index + 1 + level + 1;
        String close = "]" + "=".repeat(level) + "]";
        int closeIdx = text.indexOf(close, pos);
        if (closeIdx < 0) {
            return length;
        }
        return closeIdx + close.length();
    }

    /**
     * Skips a quoted string starting at {@code index} with quote character {@code quote}.
     * Handles backslash escapes. Returns the index after the closing quote.
     */
    private static int skipQuotedString(String text, int index, char quote) {
        int length = text.length();
        int pos = index + 1; // skip opening quote
        while (pos < length) {
            char ch = text.charAt(pos);
            if (ch == '\\') {
                pos += 2; // skip escaped character
            } else if (ch == quote) {
                return pos + 1;
            } else if (ch == '\n') {
                // Unfinished string literal — treat as ended
                return pos;
            } else {
                pos++;
            }
        }
        return pos;
    }

    private static int skipWhitespace(String text, int index) {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int scanIdentifierEnd(String text, int index) {
        while (index < text.length() && isIdentifierPart(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static boolean matchesWord(String text, int index, String word) {
        int end = index + word.length();
        if (end > text.length() || !text.regionMatches(index, word, 0, word.length())) {
            return false;
        }
        return (index == 0 || !isIdentifierPart(text.charAt(index - 1)))
            && (end == text.length() || !isIdentifierPart(text.charAt(end)));
    }

    private static boolean isIdentifierStart(char ch) {
        return ch == '_' || Character.isLetter(ch);
    }

    private static boolean isIdentifierPart(char ch) {
        return ch == '_' || Character.isLetterOrDigit(ch);
    }
}
