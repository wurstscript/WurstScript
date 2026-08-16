package tests.wurstscript.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for the harness that runs emitted Lua, rather than for anything it compiles.
 * <p>
 * Both cases here used to stop the whole suite instead of failing a test: the runner drained the
 * spawned interpreter's stderr to the end before reading stdout, and waited for the process without
 * a bound. A program that fills the stdout pipe blocks writing while the runner blocks reading the
 * other pipe, and neither side ever moves. There is nothing to see when that happens - no output,
 * no failing test, no timeout - so it is worth holding onto tests that would notice it coming back.
 * <p>
 * Both are Lua only. Run through the Jass configurations as well, the non-terminating one would
 * hang the interpreter instead, which is the same problem in a place this fix does not reach.
 */
public class LuaRunnerTests extends WurstScriptTest {

    /** Long enough that a program which does terminate still does, short enough to wait for. */
    @Override
    protected int luaExecutionTimeoutSeconds() {
        return 5;
    }

    @Test
    public void aProgramThatDoesNotTerminateFailsItsOwnTest() {
        try {
            test().testLua(true).luaOnly(true).executeProg().lines(
                "package test",
                "native testSuccess()",
                "init",
                "    var i = 0",
                "    while true",
                "        i += 1",
                "    testSuccess()"
            );
            fail("expected the runner to give up on a program that does not terminate");
        } catch (Error e) {
            assertTrue(e.getMessage() != null && e.getMessage().contains("did not terminate"),
                "expected a timeout, got: " + e.getMessage());
        }
    }

    /**
     * Enough lines to fill the pipe buffer several times over. With the streams read one after the
     * other this deadlocks: the program blocks writing stdout, the runner blocks reading stderr.
     */
    @Test
    public void aProgramThatFloodsStdoutStillFinishes() {
        test().testLua(true).luaOnly(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "native println(string s)",
            "init",
            "    for i = 1 to 20000",
            "        println(\"filling the pipe buffer with a reasonably long line of output\")",
            "    testSuccess()"
        );
    }

    /**
     * The same drain with the output arriving as one line rather than many. This checks that it
     * finishes, which is what the two cases above check too — it does not observe how much of the
     * line was retained, and would pass against a collector that kept all of it. Keeping a bounded
     * amount is worth doing regardless, but a program printing a megabyte without a newline is not
     * something this suite expects, so it is not worth test-only machinery to assert.
     */
    @Test
    public void aProgramPrintingWithoutNewlinesStillFinishes() {
        test().testLua(true).luaOnly(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "native println(string s)",
            "init",
            "    var line = \"\"",
            "    for i = 1 to 4000",
            "        line += \"this line keeps growing and is never broken by a newline \"",
            "    println(line)",
            "    testSuccess()"
        );
    }
}
