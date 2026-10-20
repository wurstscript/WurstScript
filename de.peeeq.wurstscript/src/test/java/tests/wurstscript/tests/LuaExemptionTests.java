package tests.wurstscript.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * The harness under {@code -PallBackends}: an exempt test passes only when its Lua run fails its own way, with
 * the failure its exemption expects and of the kind its reason produces.
 */
public class LuaExemptionTests extends WurstScriptTest {

    /** Holds on Jass, where == on reals allows 0.001, and calls testFail on Lua, which compares exactly. */
    private static final String[] EXACT_ON_LUA = {
        "package Test",
        "native testSuccess()",
        "native testFail(string msg)",
        "@noinline function r(real x) returns real",
        "    return x",
        "init",
        "    if not (r(1.0) == r(1.0005))",
        "        testFail(\"exact on Lua\")",
        "    testSuccess()"
    };

    /** Holds on Jass and stops on Lua at a native the Lua test runtime lacks. */
    private static final String[] CALLS_A_MISSING_NATIVE = {
        "package Test",
        "init",
        "    if GetPlayerSlotState(Player(0)) == ConvertPlayerSlotState(1)",
        "        skip",
        "    testSuccess()"
    };

    @Test
    public void acceptsItsOwnFailure() {
        test().executeProg(true).runLuaAsAllBackends()
            .luaExempt(LuaExemption.REAL_EQUALITY_TOLERANCE, "exact on Lua").lines(EXACT_ON_LUA);
    }

    @Test
    public void rejectsAFailureWithoutItsExpectedText() {
        AssertionError e = expectThrows(AssertionError.class, () -> test().executeProg(true).runLuaAsAllBackends()
            .luaExempt(LuaExemption.REAL_EQUALITY_TOLERANCE, "another message").lines(EXACT_ON_LUA));
        assertTrue(e.getMessage().contains("expected: another message")
            && e.getMessage().contains("actual: ") && e.getMessage().contains("exact on Lua"), e.getMessage());
    }

    /** The failure names the expected text, but a Lua error is no answer of the program, which the reason needs. */
    @Test
    public void rejectsAFailureOfAnotherKind() {
        AssertionError e = expectThrows(AssertionError.class, () -> test().withStdLib().executeProg(true)
            .runLuaAsAllBackends()
            .luaExempt(LuaExemption.REAL_EQUALITY_TOLERANCE, "GetPlayerSlotState").lines(CALLS_A_MISSING_NATIVE));
        assertTrue(e.getMessage().contains("the Lua run raised an error"), e.getMessage());
    }

    @Test
    public void acceptsTheMissingNativeItNames() {
        test().withStdLib().executeProg(true).runLuaAsAllBackends()
            .luaExempt(LuaExemption.MISSING_TEST_NATIVE, "GetPlayerSlotState").lines(CALLS_A_MISSING_NATIVE);
    }

    @Test
    public void rejectsAnotherMissingNative() {
        AssertionError e = expectThrows(AssertionError.class, () -> test().withStdLib().executeProg(true)
            .runLuaAsAllBackends()
            .luaExempt(LuaExemption.MISSING_TEST_NATIVE, "R2S").lines(CALLS_A_MISSING_NATIVE));
        assertTrue(e.getMessage().contains("expected: 'R2S'"), e.getMessage());
    }

    @Test
    public void rejectsAStaleExemption() {
        AssertionError e = expectThrows(AssertionError.class, () -> test().executeProg(true).runLuaAsAllBackends()
            .luaExempt(LuaExemption.REAL_EQUALITY_TOLERANCE, "exact on Lua").lines(
                "package Test",
                "native testSuccess()",
                "init",
                "    testSuccess()"));
        assertTrue(e.getMessage().contains("is stale"), e.getMessage());
    }
}
