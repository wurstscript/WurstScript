package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.requests.RunTests;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Optional;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the -testQuiet option, which suppresses the output of passing tests
 * while keeping the full output of failing tests.
 */
public class RunTestsQuietTests extends WurstScriptTest {

    private static final String[] PROGRAM = {
        "package test",
        "native testFail(string msg)",
        "native println(string msg)",
        "@test function passingTest()",
        "	println(\"noise from the passing test\")",
        "@test function failingTest()",
        "	println(\"context from the failing test\")",
        "	testFail(\"assertion message\")",
    };

    @Test
    public void quietHidesPassingTests() {
        String output = runTestsWith(true);
        assertFalse(output.contains("passingTest"), output);
        assertFalse(output.contains("noise from the passing test"), output);
        assertFalse(output.contains("OK!"), output);
    }

    @Test
    public void quietKeepsFailingTests() {
        String output = runTestsWith(true);
        assertTrue(output.contains("failingTest"), output);
        assertTrue(output.contains("context from the failing test"), output);
        assertTrue(output.contains("FAILED assertion"), output);
        assertTrue(output.contains("assertion message"), output);
    }

    @Test
    public void quietKeepsSummary() {
        String output = runTestsWith(true);
        assertTrue(output.contains("Tests succeeded: 1/2"), output);
        assertTrue(output.contains("1 Tests have failed!"), output);
    }

    @Test
    public void defaultReportsPassingTests() {
        String output = runTestsWith(false);
        assertTrue(output.contains("passingTest"), output);
        assertTrue(output.contains("noise from the passing test"), output);
        assertTrue(output.contains("OK!"), output);
    }

    @Test
    public void quietFlagIsParsed() {
        assertTrue(new RunArgs("-testQuiet").isTestQuiet());
        assertTrue(new RunArgs("-tq").isTestQuiet());
        assertFalse(new RunArgs("-runtests").isTestQuiet());
    }

    private String runTestsWith(boolean testQuiet) {
        RunArgs runArgs = new RunArgs();
        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(null,
            Collections.singletonList(new CU("test", Utils.join(PROGRAM, "\n") + "\n")), false, compiler);
        compiler.checkProg(model);
        if (!gui.getErrorList().isEmpty()) {
            throw gui.getErrorList().get(0);
        }
        ImProg imProg = compiler.translateProgToIm(model);

        StringBuilder output = new StringBuilder();
        RunTests runTests = new RunTests(Optional.empty(), 0, 0, Optional.empty(), 20, Optional.empty(), false, testQuiet) {
            @Override
            protected void print(String message) {
                output.append(message);
            }
        };
        runTests.runTests(compiler.getImTranslator(), imProg, Optional.empty(), Optional.empty());
        return output.toString();
    }
}
