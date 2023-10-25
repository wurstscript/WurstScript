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
    public void testSimpleCompiletimeReal() {
        test().executeProg(true)
            .runCompiletimeFunctions(true)
            .lines("package Test",
                "native testSuccess()",
                "function compiletime(real r) returns real",
                "    return r",
                "real r = 0.",
                "function next() returns real",
                "    r++",
                "    return r",
                "constant a = compiletime(next())",
                "constant b = compiletime(next())",
                "init",
                "    if a == 1. and b == 2.",
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
    public void testCompiletimeHashtableReal() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("type agent extends handle",
                        "type hashtable extends agent",
                        "package Test",
                        "native testSuccess()",
                        "@extern native InitHashtable() returns hashtable",
                        "@extern native LoadReal(hashtable h, int p, int c) returns real",
                        "@extern native SaveReal(hashtable h, int p, int c, real i)",
                        "function compiletime(hashtable h) returns hashtable",
                        "    return h",
                        "let h = compiletime(InitHashtable())",
                        "@compiletime",
                        "function foo()",
                        "    SaveReal(h, 2, 3, 3.14)",
                        "init",
                        "    if LoadReal(h, 2, 3) == 3.14",
                        "        testSuccess()");
    }

    @Test
    public void testCompiletimeHashtableStr() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("type agent extends handle",
                        "type hashtable extends agent",
                        "package Test",
                        "native testSuccess()",
                        "@extern native InitHashtable() returns hashtable",
                        "@extern native LoadStr(hashtable h, int p, int c) returns string",
                        "@extern native SaveStr(hashtable h, int p, int c, string i)",
                        "function compiletime(hashtable h) returns hashtable",
                        "    return h",
                        "let h = compiletime(InitHashtable())",
                        "@compiletime",
                        "function foo()",
                        "    SaveStr(h, 2, 3, \"salami\")",
                        "init",
                        "    if LoadStr(h, 2, 3) == \"salami\"",
                        "        testSuccess()");
    }

    @Test
    public void testCompiletimeHashtableBool() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("type agent extends handle",
                        "type hashtable extends agent",
                        "package Test",
                        "native testSuccess()",
                        "@extern native InitHashtable() returns hashtable",
                        "@extern native LoadBoolean(hashtable h, int p, int c) returns bool",
                        "@extern native SaveBoolean(hashtable h, int p, int c, bool i)",
                        "function compiletime(hashtable h) returns hashtable",
                        "    return h",
                        "let h = compiletime(InitHashtable())",
                        "@compiletime",
                        "function foo()",
                        "    SaveBoolean(h, 2, 3, true)",
                        "init",
                        "    if LoadBoolean(h, 2, 3) == true",
                        "        testSuccess()");
    }

    @Test
    public void testPersistCompiletimeClass() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "native testSuccess()",
                        "class A",
                        "    string x",
                        "    int y",
                        "function compiletime(A a) returns A",
                        "    return a",
                        "let a = compiletime(new A)",
                        "@compiletime",
                        "function foo()",
                        "    a.x = \"schwardemage\"",
                        "    a.y = 42",
                        "init",
                        "    if a.x == \"schwardemage\" and a.y == 42",
                        "        testSuccess()");
    }

    @Test
    public void testPersistCompiletimeClassCycle() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "native testSuccess()",
                        "class A",
                        "    A x",
                        "    int y",
                        "function compiletime<T:>(T t) returns T",
                        "    return t",
                        "let a = compiletime(new A)",
                        "@compiletime",
                        "function foo()",
                        "    a.x = new A",
                        "    a.x.x = new A",
                        "    a.x.x.x = a",
                        "    a.y = 42",
                        "    a.x.y = 43",
                        "    a.x.x.y = 43",
                        "init",
                        "    if a.x.x.x.y == 42",
                        "        testSuccess()");
    }


    @Test
    public void testPersistCompiletimeClassTuple() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "native testSuccess()",
                        "class A",
                        "    int y",
                        "function compiletime<T:>(T t) returns T",
                        "    return t",
                        "tuple pair(A a, A b)",
                        "let a = compiletime(pair(new A, new A))",
                        "@compiletime",
                        "function foo()",
                        "    a.a.y = 42",
                        "    a.b.y = 43",
                        "init",
                        "    if a.a.y == 42 and a.b.y == 43",
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


    @Test
    public void nullBug() {
        testAssertOkLinesWithStdLib(true,
                "package Hello",
                "import LinkedList",
                "",
                "function myFunction(int i) returns string",
                "	if 0 == i",
                "		return null // This causes the bug",
                "	else",
                "		return \"i=\" + i.toString()",
                "",
                "init",
                "	let original = new LinkedList<int>..add(0, 1, 2)",
                "	let mapped = original.map(i -> myFunction(i))",
                "	println(mapped.get(0))",
                "	println(mapped.get(1))",
                "	if mapped.get(1) == \"i=1\"",
                "		testSuccess()");

    }

}
