package tests.wurstscript.tests;

/**
 * Why an executing test does not hold on the Lua target, given with
 * {@code test().luaExempt(reason)} or {@code luaExempt(reason, detail)}.
 *
 * <p>Each reason names the record which explains it. An exempt test runs no Lua in an ordinary run. Under
 * {@code -PallBackends} it runs Lua after its Jass runs like every other executing test, and its Lua run
 * must fail: one that passes has a stale exemption and fails the test.
 */
public enum LuaExemption {
    /** Jass {@code ==} on reals allows a difference of 0.001; Lua compares exactly, by design. */
    REAL_EQUALITY_TOLERANCE("AGENTS.md §7, \"Reference semantics for arithmetic\"", false),
    /** A method combining its class's type parameters with its own is not supported on Lua. */
    CLASS_AND_METHOD_TYPE_PARAMS("AGENTS.md §9, \"Lowering and backend rules\"", false),
    /**
     * Lua keeps objects and classes otherwise than Jass, as documented: one id space for every class, one erased
     * class for the instantiations of a new generic, and only the compile-time objects it makes again counted.
     */
    OBJECT_BOOKKEEPING("docs/WC3_RUNTIME.md, \"Objects and memory\"", false),
    /** The test Lua computes reals in double; the game and the interpreters do not. */
    TEST_LUA_COMPUTES_IN_DOUBLE("docs/WC3_RUNTIME.md, \"Numbers\"", false),
    /** The test checks the interpreter's model of a native, which the Lua test runtime does not share. */
    INTERPRETER_MOCK_ONLY("AGENTS.md §7, \"Interpreter native mocks\"", false),
    /** The Lua test runtime lacks a native the program calls; the detail names it. */
    MISSING_TEST_NATIVE("src/test/resources/luaruntime/wc3shim.lua", true),
    /** A Lua defect not fixed yet; the detail is the title of its BACKLOG.md item. */
    KNOWN_LUA_DEFECT("BACKLOG.md", true);

    private final String record;
    private final boolean needsDetail;

    LuaExemption(String record, boolean needsDetail) {
        this.record = record;
        this.needsDetail = needsDetail;
    }

    /** Where this reason is written down. */
    public String record() {
        return record;
    }

    /** Whether an exemption for this reason names the native or backlog item it is about. */
    public boolean needsDetail() {
        return needsDetail;
    }
}
