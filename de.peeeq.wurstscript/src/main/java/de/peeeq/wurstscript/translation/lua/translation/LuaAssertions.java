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

    static Set<String> collectCalledFunctionNames(String text) {
        Set<String> result = new HashSet<>();
        int length = text.length();
        int index = 0;
        while (index < length) {
            if (!isIdentifierStart(text.charAt(index))) {
                index++;
                continue;
            }
            int end = scanIdentifierEnd(text, index + 1);
            int next = skipWhitespace(text, end);
            if (next < length && text.charAt(next) == '(') {
                result.add(text.substring(index, end));
            }
            index = end;
        }
        return result;
    }

    static Set<String> collectDefinedFunctionNames(String text) {
        Set<String> result = new HashSet<>();
        int length = text.length();
        int index = 0;
        while (index < length) {
            if (!matchesWord(text, index, "function")) {
                index++;
                continue;
            }
            int nameStart = skipWhitespace(text, index + "function".length());
            if (nameStart >= length || !isIdentifierStart(text.charAt(nameStart))) {
                index++;
                continue;
            }
            int nameEnd = scanIdentifierEnd(text, nameStart + 1);
            int next = skipWhitespace(text, nameEnd);
            if (next < length && text.charAt(next) == '(') {
                result.add(text.substring(nameStart, nameEnd));
            }
            index = nameEnd;
        }
        return result;
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
