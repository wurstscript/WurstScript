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
    public void testCompiletimeObjectIdMigration() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "native testSuccess()",
                        "class A",
                        "    int storedId",
                        "tuple BuildResult(A obj, int maxId)",
                        "function compiletime<T:>(T t) returns T",
                        "    return t",
                        "@compiletime function build() returns BuildResult",
                        "    let keep = new A",
                        "    keep.storedId = keep castTo int",
                        "    let temp1 = new A",
                        "    let temp2 = new A",
                        "    let maxId = temp2 castTo int",
                        "    destroy temp1",
                        "    destroy temp2",
                        "    return BuildResult(keep, maxId)",
                        "let result = compiletime(build())",
                        "init",
                        "    let newA = new A",
                        "    if result.obj castTo int == result.obj.storedId",
                        "        and newA castTo int == result.maxId + 1",
                        "        testSuccess()");
    }

    @Test
    public void testCompiletimeObjectIdMigrationStress() {
        test().executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "native testSuccess()",
                        "class A",
                        "    int idSnapshot",
                        "class B",
                        "    int idSnapshot",
                        "tuple BuildResult(A keepA, B keepB, int maxA, int maxB)",
                        "function compiletime<T:>(T t) returns T",
                        "    return t",
                        "@compiletime function build() returns BuildResult",
                        "    A keepA = null",
                        "    B keepB = null",
                        "    int maxA = 0",
                        "    int maxB = 0",
                        "    for i = 1 to 200",
                        "        let a = new A",
                        "        a.idSnapshot = a castTo int",
                        "        maxA = a castTo int",
                        "        if i % 25 == 0",
                        "            keepA = a",
                        "        else",
                        "            destroy a",
                        "        let b = new B",
                        "        b.idSnapshot = b castTo int",
                        "        maxB = b castTo int",
                        "        if i % 30 == 0",
                        "            keepB = b",
                        "        else",
                        "            destroy b",
                        "    return BuildResult(keepA, keepB, maxA, maxB)",
                        "let result = compiletime(build())",
                        "init",
                        "    let newA = new A",
                        "    let newB = new B",
                        "    if result.keepA.idSnapshot == result.keepA castTo int",
                        "        and result.keepB.idSnapshot == result.keepB castTo int",
                        "        and newA castTo int == result.maxA + 1",
                        "        and newB castTo int == result.maxB + 1",
                        "        testSuccess()");
    }

    @Test
    public void testInterpreterIdRecyclingWithHashMap() {
        test().withStdLib()
                .executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Hello",
                        "import HashMap",
                        "class MyClass",
                        "    int payload",
                        "",
                        "    construct(int payload)",
                        "        this.payload = payload",
                        "",
                        "function buildSequence(string label) returns string",
                        "    let map = new HashMap<int, MyClass>",
                        "",
                        "    let a = new MyClass(11)",
                        "    let b = new MyClass(23)",
                        "    let c = new MyClass(37)",
                        "",
                        "    let idA = a castTo int",
                        "    let idB = b castTo int",
                        "    let idC = c castTo int",
                        "",
                        "    map.put(idA, a)",
                        "    map.put(idB, b)",
                        "    map.put(idC, c)",
                        "    print(label + \" create a id=\" + idA.toString() + \" payload=11\")",
                        "    print(label + \" create b id=\" + idB.toString() + \" payload=23\")",
                        "    print(label + \" create c id=\" + idC.toString() + \" payload=37\")",
                        "",
                        "    map.remove(idB)",
                        "    print(label + \" remove b-from-map id=\" + idB.toString())",
                        "    destroy b",
                        "    print(label + \" destroy b id=\" + idB.toString())",
                        "",
                        "    let d = new MyClass(41)",
                        "    let idD = d castTo int",
                        "    map.put(idD, d)",
                        "    print(label + \" create d id=\" + idD.toString() + \" payload=41\")",
                        "    print(label + \" recycled b->d = \" + (idB == idD).toString())",
                        "",
                        "    var prev = new MyClass(100)",
                        "    var prevId = prev castTo int",
                        "    print(label + \" cycle seed id=\" + prevId.toString())",
                        "    for i = 1 to 4",
                        "        destroy prev",
                        "        print(label + \" cycle \" + i.toString() + \" destroy id=\" + prevId.toString())",
                        "        let next = new MyClass(100 + i)",
                        "        let nextId = next castTo int",
                        "        print(label + \" cycle \" + i.toString() + \" create id=\" + nextId.toString() + \" recycled=\" + (nextId == prevId).toString())",
                        "        prev = next",
                        "        prevId = nextId",
                        "    destroy prev",
                        "    print(label + \" final destroy id=\" + prevId.toString())",
                        "",
                        "    map.get(idA).payload.assertEquals(11)",
                        "    map.get(idC).payload.assertEquals(37)",
                        "    map.get(idD).payload.assertEquals(41)",
                        "    map.size().assertEquals(3)",
                        "",
                        "    let seqA = map.get(idA).payload * 100 + map.get(idC).payload",
                        "    let seqB = map.get(idD).payload * 10 + map.size()",
                        "    let recycled = (idB == idD).toInt()",
                        "",
                        "    let sequence = seqA.toString() + \":\" + seqB.toString() + \":\" + recycled.toString()",
                        "    print(label + \" \" + sequence)",
                        "    return sequence",
                        "",
                        "init",
                        "    let sequence = buildSequence(\"RT\")",
                        "    if sequence == \"1137:413:1\"",
                        "        testSuccess()",
                        "",
                        "@compiletime",
                        "function interpreterIdRecyclingCompiletime()",
                        "    let sequence = buildSequence(\"CT\")",
                        "    print(\"CT-VERIFY \" + sequence)",
                        "    sequence.assertEquals(\"1137:413:1\")");
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
                "	if mapped.get(1) == \"i=1\"",
                "		testSuccess()");

    }

}
