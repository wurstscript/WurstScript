package tests.wurstscript.tests;


import java.util.regex.Pattern;

/**
 * Why an executing test does not hold on the Lua target, given with {@code test().luaExempt(...)}.
 *
 * <p>Each reason names the record which explains it. An exempt test runs no Lua in an ordinary run. Under
 * {@code -PallBackends} it runs Lua after its Jass runs like every other executing test, and its Lua run
 * must fail, and fail its own way: with the failure the exemption expects, and of the kind its reason
 * produces. A Lua run that passes has a stale exemption, and one that fails otherwise has a new defect;
 * both fail the test.
 */
public enum LuaExemption {
    /** Jass {@code ==} on reals allows a difference of 0.001; Lua compares exactly, by design. */
    REAL_EQUALITY_TOLERANCE("AGENTS.md §7, \"Reference semantics for arithmetic\"", Kind.PROGRAM_OUTCOME),
    /**
     * Lua keeps objects and classes otherwise than Jass, as documented: one id space for every class, one erased
     * class for the instantiations of a new generic, and only the compile-time objects it makes again counted.
     */
    OBJECT_BOOKKEEPING("docs/WC3_RUNTIME.md, \"Objects and memory\"", Kind.PROGRAM_OUTCOME),
    /** The test Lua computes reals in double; the game and the interpreters do not. */
    TEST_LUA_COMPUTES_IN_DOUBLE("docs/WC3_RUNTIME.md, \"Numbers\"", Kind.PROGRAM_OUTCOME),
    /** The test checks the interpreter's model of a native, which the Lua test runtime does not share. */
    INTERPRETER_MOCK_ONLY("AGENTS.md §7, \"Interpreter native mocks\"", Kind.PROGRAM_OUTCOME),
    /** The Lua test runtime lacks a native the program calls; the exemption names it, and the failure must. */
    MISSING_TEST_NATIVE("src/test/resources/luaruntime/wc3shim.lua", Kind.MISSING_NATIVE),
    /** A Lua defect not fixed yet; the exemption gives the title of its BACKLOG.md item. */
    KNOWN_LUA_DEFECT("BACKLOG.md", Kind.ANY);

    /** What kind of Lua failure a reason produces, checked on top of the test's own expected failure. */
    enum Kind {
        /** The program ran to its end on Lua and answered otherwise: testFail, or no testSuccess, with no Lua error. */
        PROGRAM_OUTCOME,
        /** Lua called the missing native the exemption names. */
        MISSING_NATIVE,
        /** Whatever the defect does; only the expected failure is checked. */
        ANY
    }

    /** A Lua error raised at a place in a script ({@code file.lua:12: message}), which testFail never is. */
    private static final Pattern LUA_ERROR = Pattern.compile("\\.lua:\\d+: ");

    private final String record;
    private final Kind kind;

    LuaExemption(String record, Kind kind) {
        this.record = record;
        this.kind = kind;
    }

    /** Where this reason is written down. */
    public String record() {
        return record;
    }

    /** Whether an exemption for this reason names the backlog item it is about, besides its expected failure. */
    public boolean needsBacklogItem() {
        return this == KNOWN_LUA_DEFECT;
    }

    /**
     * Why {@code failure} is not this reason's kind of failure, or null when it is. {@code text} is the messages of
     * the failure and its causes; {@code expected} is the exemption's expected failure (for a missing native, its name).
     */
    String mismatch(Throwable failure, String text, String expected) {
        switch (kind) {
            case PROGRAM_OUTCOME:
                if (!text.contains("Lua testFail was called") && !text.contains("Succeed function not called")) {
                    return "the program did not run to its end on Lua";
                }
                if (LUA_ERROR.matcher(text).find()) {
                    return "the Lua run raised an error";
                }
                return null;
            case MISSING_NATIVE:
                if (text.contains("attempt to call a nil value (global '" + expected + "')")
                    || text.contains("The native '" + expected + "' is not implemented")) {
                    return null;
                }
                return "the Lua run did not call the missing native " + expected;
            default:
                return null;
        }
    }

    /** The text the failure must contain: the expected failure, or for a missing native the call Lua reports. */
    String signature(String expected) {
        return kind == Kind.MISSING_NATIVE ? "'" + expected + "'" : expected;
    }
}
