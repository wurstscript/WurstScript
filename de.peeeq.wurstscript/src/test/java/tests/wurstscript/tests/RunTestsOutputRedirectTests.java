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
 * Tests that RunTests.redirectInterpreterOutput actually captures what the tests print,
 * instead of it going to System.err unnoticed.
 */
public class RunTestsOutputRedirectTests extends WurstScriptTest {

    private static final String[] PROGRAM = {
        "package test",
        "native testFail(string msg)",
        "native println(string msg)",
        "@test function passingTest()",
        "	println(\"output of the passing test\")",
        "@test function failingTest()",
        "	println(\"output of the failing test\")",
        "	testFail(\"assertion message\")",
    };

    @Test
    public void interpreterOutputIsRedirected() {
        String output = runTests(false);
        assertTrue(output.contains("output of the passing test"), output);
        assertTrue(output.contains("output of the failing test"), output);
    }

    @Test
    public void interpreterOutputIsSuppressedWithCompactOutput() {
        String output = runTests(true);
        assertFalse(output.contains("output of the passing test"), output);
        assertFalse(output.contains("output of the failing test"), output);
    }

    @Test
    public void testResultsAreStillReported() {
        String output = runTests(false);
        assertTrue(output.contains("Tests succeeded: 1/2"), output);
        assertTrue(output.contains("assertion message"), output);
    }

    private String runTests(boolean compactOutput) {
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
        RunTests runTests = new RunTests(Optional.empty(), 0, 0, Optional.empty(), 20, Optional.empty(), compactOutput) {
            @Override
            protected void print(String message) {
                output.append(message);
            }
        };
        runTests.runTests(compiler.getImTranslator(), imProg, Optional.empty(), Optional.empty());
        return output.toString();
    }
}
