package tests.wurstscript.tests;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import org.testng.annotations.Test;

import static org.testng.Assert.expectThrows;
import static org.testng.AssertJUnit.assertTrue;

/**
 * The compile-time check a library makes that the compiler lowers {@code CodeList}: the probe's
 * source body says no, this compiler says yes on both targets, so a build which reaches the check
 * with an older compiler stops in {@code compileError}.
 */
public class CodeListSupportTests extends WurstScriptTest {

    private static String[] check(String annotation) {
        return new String[] {
            "package Test",
            annotation + "function codeListLowered() returns boolean",
            "    return false",
            "@compiletime function requireCodeList()",
            "    if not codeListLowered()",
            "        compileError(\"needs a newer compiler\")",
            "init",
            "    print(\"built\")",
            "endpackage"};
    }

    @Test
    public void thisCompilerAnswersTheProbeOnJass() {
        test().withStdLib().lines(check("@compilerintrinsic "));
    }

    @Test
    public void thisCompilerAnswersTheProbeOnLua() {
        test().testLua(true).withStdLib().lines(check("@compilerintrinsic "));
    }

    /** Without the declaration the compiler recognises, the source body answers and the build stops. */
    @Test
    public void aFunctionOfThatNameWithoutTheAnnotationStopsTheBuild() {
        InterpreterException failure = expectThrows(InterpreterException.class,
            () -> test().withStdLib().lines(check("")));
        assertTrue("the build says why it stopped: " + failure.getMessage(),
            failure.getMessage().contains("needs a newer compiler"));
    }
}
