package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;


/**
 * These tests are supposed to check, whether the compiler is determinisitic
 */
public class DeterministicChecks extends WurstScriptTest {


    @Test
    public void simple() throws IOException {
        run(this::exampleCode, "exampleCode");
    }

    private void run(Runnable example, String name) throws IOException {
        example.run();
        File exampleFile1 = new File("test-output/DeterministicChecks_"+ name + ".j");
        String script1 = Files.toString(exampleFile1, Charsets.UTF_8);
        Files.move(exampleFile1, new File("test-output/det1.j"));
        Files.move(new File("test-output/im 1.im"), new File("test-output/im1.j"));
        example.run();
        String script2 = Files.toString(exampleFile1, Charsets.UTF_8);
        Files.move(exampleFile1, new File("test-output/det2.j"));
        Files.move(new File("test-output/im 1.im"), new File("test-output/im2.j"));
        assertEquals(script1, script2);
    }

    private void exampleCode() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "interface I",
            "    function foo() returns int",
            "class B implements I",
            "    function foo() returns int",
            "        return 2",
            "class C implements I",
            "    function foo() returns int",
            "        return 3",
            "init",
            "    I i1 = new B()",
            "    I i2 = new C()",
            "    if i1.foo() == 2 and i2.foo() == 3",
            "        testSuccess()"
        );
    }

    @Test
    public void cyclicFunctionCall() throws IOException {
        run(this::cycleExample, "cycleExample");
    }

    private void cycleExample() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "function a(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return b(i div 2)",
            "function b(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return c(i div 2)",
            "function c(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return a(i div 2)",
            "init",
            "    if a(42) == 0",
            "        testSuccess()"
        );
    }
}
