package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jass has no forward declaration, so mutually recursive functions cannot be emitted as themselves:
 * a cycle is merged into one function which takes a choice of which body to run plus the parameters
 * of all of them. That merged signature has to fit in a Jass function, and nothing used to check that
 * it did.
 */
public class CyclicFunctionTests extends WurstScriptTest {

    /** A Jass function takes at most 31 parameters, and a tuple counts as one per component. */
    private static final int JASS_MAX_PARAMETERS = 31;

    /**
     * Two functions in a cycle taking the same parameters in a different order.
     * <p>
     * Sharing a slot used to be looked for from a position which only moved forwards, so a parameter
     * matching a slot late in the union pushed every parameter after it past everything already there:
     * {@code pong} matched its {@code int} against the last slot and then had five tuples left with
     * nowhere behind it to go. Eleven slots for six parameters, 32 Jass parameters for a signature
     * which needs 17, and the emitted function was over the limit while the IM function looked fine.
     * <p>
     * Reordering arguments between two functions which call each other is ordinary, so this is not a
     * corner: it is two functions and one swap.
     */
    @Test
    public void mutuallyRecursiveFunctionsSharingParametersFitTheJassLimit() throws IOException {
        test().executeProg().lines(
            "package test",
            "native testSuccess()",
            "tuple vec3(real x, real y, real z)",
            "function ping(vec3 a, vec3 b, vec3 c, vec3 d, vec3 e, int n) returns int",
            "    if n <= 0",
            "        return 0",
            "    return pong(n - 1, a, b, c, d, e)",
            "function pong(int n, vec3 a, vec3 b, vec3 c, vec3 d, vec3 e) returns int",
            "    if n <= 0",
            "        return 1",
            "    return ping(a, b, c, d, e, n - 1)",
            "init",
            "    vec3 v = vec3(1., 2., 3.)",
            "    if ping(v, v, v, v, v, 3) == 1",
            "        testSuccess()"
        );
        String jass = compiledJass("mutuallyRecursiveFunctionsSharingParametersFitTheJassLimit");
        assertNoFunctionExceedsTheJassLimit(jass);
        // choice, one int, and five tuples of three reals: what the wider of the two needs.
        assertMergedCycleTakes(jass, 17);
    }

    /**
     * The same shape across more of the cycle, since the union is built one function at a time and a
     * slot the next function cannot reach is a slot every function after it cannot reach either.
     */
    @Test
    public void aLongerCycleReusesSlotsRatherThanAccumulatingThem() throws IOException {
        test().executeProg().lines(
            "package test",
            "native testSuccess()",
            "tuple pair(real x, real y)",
            "function first(pair a, pair b, pair c, int n, string s) returns int",
            "    if n <= 0",
            "        return 7",
            "    return second(s, a, n - 1, b, c)",
            "function second(string s, pair a, int n, pair b, pair c) returns int",
            "    if n <= 0",
            "        return 7",
            "    return third(a, n - 1, b, s, c)",
            "function third(pair a, int n, pair b, string s, pair c) returns int",
            "    if n <= 0",
            "        return 7",
            "    return first(a, b, c, n - 1, s)",
            "init",
            "    pair p = pair(1., 2.)",
            "    if first(p, p, p, 6, \"x\") == 7",
            "        testSuccess()"
        );
        String jass = compiledJass("aLongerCycleReusesSlotsRatherThanAccumulatingThem");
        assertNoFunctionExceedsTheJassLimit(jass);
        // choice, three tuples of two reals, one int and one string. It took fifteen before.
        assertMergedCycleTakes(jass, 9);
    }

    private String compiledJass(String testName) throws IOException {
        return Files.toString(
            new File(TEST_OUTPUT_PATH + "CyclicFunctionTests_" + testName + "_no_opts.j"),
            Charsets.UTF_8);
    }

    private static final Pattern FUNCTION_HEADER =
        Pattern.compile("(?m)^function\\s+(\\w+)\\s+takes\\s+(.*?)\\s+returns\\s");

    /**
     * Counts the parameters the emitted Jass actually declares, which is the number the game applies
     * its limit to - the IM function's parameter count is smaller wherever a tuple is passed.
     */
    private static void assertNoFunctionExceedsTheJassLimit(String jass) {
        Matcher header = FUNCTION_HEADER.matcher(jass);
        while (header.find()) {
            String parameters = header.group(2);
            if (parameters.equals("nothing")) {
                continue;
            }
            int count = parameters.split(",").length;
            if (count > JASS_MAX_PARAMETERS) {
                throw new AssertionError("function '" + header.group(1) + "' takes " + count
                    + " parameters, and Jass allows " + JASS_MAX_PARAMETERS
                    + "\n" + header.group());
            }
        }
    }

    private static final Pattern MERGED_CYCLE =
        Pattern.compile("(?m)^function\\s+(cyc_\\w+)\\s+takes\\s+(.*?)\\s+returns\\s");

    /**
     * The merged function takes exactly the parameters the cycle needs live at once.
     * <p>
     * Staying under the limit is the requirement, but a signature which is merely under it is how this
     * got here: the slack was being spent one duplicate slot at a time, per function, and only a long
     * enough cycle made it visible. The count is asserted so that spending starts failing again.
     */
    private static void assertMergedCycleTakes(String jass, int expectedParameters) {
        Matcher merged = MERGED_CYCLE.matcher(jass);
        if (!merged.find()) {
            throw new AssertionError("no merged cycle function in the emitted Jass");
        }
        int count = merged.group(2).equals("nothing") ? 0 : merged.group(2).split(",").length;
        if (count != expectedParameters) {
            throw new AssertionError("merged cycle '" + merged.group(1) + "' takes " + count
                + " parameters, expected " + expectedParameters + "\n" + merged.group());
        }
    }
}
