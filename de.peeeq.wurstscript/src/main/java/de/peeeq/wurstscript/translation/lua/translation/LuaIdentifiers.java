package de.peeeq.wurstscript.translation.lua.translation;

/**
 * Lua's rule for what a generated name may look like, kept in one place.
 *
 * <p>Names from the intermediate language are not constrained to Lua's identifier syntax;
 * specialised generics, for example, are named after their type arguments. Sanitising in the
 * backend keeps that rule where it belongs rather than requiring every earlier pass to know
 * about Lua. Any collisions the mapping introduces are resolved by the usual uniquing.
 */
public final class LuaIdentifiers {

    /** Lua's vararg parameter, which is a legal parameter name but not an identifier. */
    public static final String VARARG = "...";

    /**
     * Whether {@code name} can be used as-is as a Lua identifier or table key.
     *
     * <p>A keyword is spelled like an identifier and is not one. Wurst reserves a different set, so
     * a method can be declared {@code repeat} or {@code goto} and reach the backend under that
     * name; emitted as a table key it is a syntax error rather than a wrong result, but this is the
     * check that is supposed to catch it first.
     */
    public static boolean isValid(String name) {
        if (name == null || name.isEmpty() || isDigit(name.charAt(0))) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!isIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        return !LuaReservedNames.LUA_KEYWORDS.contains(name);
    }

    /** Maps any name onto a Lua identifier, leaving names that already are one untouched. */
    public static String toIdentifier(String name) {
        if (isValid(name)) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            sb.append(isIdentifierPart(c) ? c : '_');
        }
        if (sb.length() == 0 || isDigit(sb.charAt(0))) {
            sb.insert(0, '_');
        }
        // A trailing underscore rather than a counter, so the name a keyword maps to is the same
        // wherever it is derived - call sites and class tables have to agree without consulting
        // each other.
        while (LuaReservedNames.LUA_KEYWORDS.contains(sb.toString())) {
            sb.append('_');
        }
        return sb.toString();
    }

    private static boolean isIdentifierPart(char c) {
        return c == '_' || (c < 128 && Character.isLetterOrDigit(c));
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private LuaIdentifiers() {}
}
