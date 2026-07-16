package de.peeeq.wurstscript.translation.lua.translation;

import java.util.HashSet;
import java.util.Set;

/**
 * Names that generated Lua identifiers must never take.
 *
 * <p>{@link #LUA_KEYWORDS} would produce syntax errors if used as identifiers
 * or field names. {@link #PROTECTED_GLOBALS} are Lua standard library globals
 * (and the fixed WC3 entry points) that the emitted runtime helpers depend on;
 * a user identifier with one of these names would clobber the library at map
 * load and break helpers like {@code math.floor} or {@code table.pack}.
 */
public final class LuaReservedNames {

    public static final Set<String> LUA_KEYWORDS = Set.of(
        "and", "break", "do", "else", "elseif", "end", "false", "for",
        "function", "goto", "if", "in", "local", "nil", "not", "or",
        "repeat", "return", "then", "true", "until", "while"
    );

    public static final Set<String> PROTECTED_GLOBALS = Set.of(
        // WC3 script entry points
        "main", "config",
        // base library functions used by emitted helpers or user shims
        "print", "tostring", "tonumber", "type", "error", "assert",
        "pairs", "ipairs", "next", "select",
        "rawget", "rawset", "rawequal", "rawlen",
        "setmetatable", "getmetatable",
        "pcall", "xpcall", "load", "dofile", "require", "collectgarbage",
        // standard library tables
        "math", "table", "string", "os", "io", "coroutine", "debug", "utf8", "_G"
    );

    /** Union of {@link #LUA_KEYWORDS} and {@link #PROTECTED_GLOBALS}. */
    public static Set<String> all() {
        Set<String> result = new HashSet<>(LUA_KEYWORDS);
        result.addAll(PROTECTED_GLOBALS);
        return result;
    }

    private LuaReservedNames() {}
}
