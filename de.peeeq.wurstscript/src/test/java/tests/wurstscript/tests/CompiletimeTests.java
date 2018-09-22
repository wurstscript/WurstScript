package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class CompiletimeTests extends WurstScriptTest {


    @Test
    public void testSimpleCompiletime() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .lines("package Test",
                        "native testSuccess()",
                        "function compiletime(int i) returns int",
                        "    return i",
                        "int i = 0",
                        "function next() returns int",
                        "    i++",
                        "    return i",
                        "constant a = compiletime(next())",
                        "constant b = compiletime(next())",
                        "init",
                        "    if a == 1 and b == 2",
                        "        testSuccess()");
    }

    @Test
    public void testCompiletimeArray() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .lines("package Test",
                        "native testSuccess()",
                        "function compiletime(int i) returns int",
                        "    return i",
                        "int i = 0",
                        "function next(int a) returns int",
                        "    i++",
                        "    return i",
                        "constant ar = [compiletime(next(55)), compiletime(next(66))]",
                        "constant x = compiletime(ar[1])",
                        "init",
                        "    if ar[0] == 1 and ar[1] == 2 and x == 2",
                        "        testSuccess()");
    }


}
