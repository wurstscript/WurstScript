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


    @Test
    public void testCompiletimeHashtable() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("type agent extends handle",
                        "type hashtable extends agent",
                        "package Test",
                        "native testSuccess()",
                        "@extern native InitHashtable() returns hashtable",
                        "@extern native LoadInteger(hashtable h, int p, int c) returns int",
                        "@extern native SaveInteger(hashtable h, int p, int c, int i)",
                        "function compiletime(hashtable h) returns hashtable",
                        "    return h",
                        "let h = compiletime(InitHashtable())",
                        "@compiletime",
                        "function foo()",
                        "    SaveInteger(h, 2, 3, 42)",
                        "init",
                        "    if LoadInteger(h, 2, 3) == 42",
                        "        testSuccess()");
    }

    @Test
    public void checkCompiletimeAnnotation1() {
        testAssertErrorsLines(false, "Functions annotated '@compiletime' may not take parameters.",
                "package test",
                "@compiletime",
                "function foo(int x)");
    }

    @Test
    public void checkCompiletimeAnnotation2() {
        testAssertErrorsLines(false, "Functions annotated '@compiletime' must be static.",
                "package test",
                "class C",
                "    @compiletime",
                "    function foo()");

    }


}
