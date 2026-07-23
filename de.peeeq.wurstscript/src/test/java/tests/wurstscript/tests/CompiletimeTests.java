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

    @Test
    public void testCompiletimeSQLite() {
        test().withStdLib()
                .executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "import LinkedList",
                        "@extern native sqlite_open(string path) returns int",
                        "@extern native sqlite_prepare(int conn, string q) returns int",
                        "@extern native sqlite_step(int stmt) returns boolean",
                        "@extern native sqlite_column_string(int stmt, int idx) returns string",
                        "@extern native sqlite_column_int(int stmt, int idx) returns int",
                        "@extern native sqlite_column_real(int stmt, int idx) returns real",
                        "@extern native sqlite_column_count(int stmt) returns int",
                        "@extern native sqlite_exec(int conn, string q)",
                        "@extern native sqlite_bind_int(int stmt, int idx, int value)",
                        "@extern native sqlite_bind_real(int stmt, int idx, real value)",
                        "@extern native sqlite_bind_string(int stmt, int idx, string value)",
                        "@extern native sqlite_reset(int stmt)",
                        "@extern native sqlite_finalize(int stmt)",
                        "@extern native sqlite_close(int conn)",
                        "",
                        "function testFullSQLiteApi() returns int",
                        "    let db = sqlite_open(\":memory:\")",
                        "    sqlite_exec(db, \"CREATE TABLE Items (id INTEGER, name TEXT, price REAL)\")",
                        "    let insert = sqlite_prepare(db, \"INSERT INTO Items VALUES (?, ?, ?)\")",
                        "    sqlite_bind_int(insert, 1, 101)",
                        "    sqlite_bind_string(insert, 2, \"Sword\")",
                        "    sqlite_bind_real(insert, 3, 15.5)",
                        "    let s1 = sqlite_step(insert)",
                        "    let s2 = sqlite_step(insert)",
                        "    sqlite_reset(insert)",
                        "    sqlite_bind_int(insert, 1, 102)",
                        "    sqlite_bind_string(insert, 2, \"Shield\")",
                        "    sqlite_bind_real(insert, 3, 25.0)",
                        "    let s3 = sqlite_step(insert)",
                        "    sqlite_finalize(insert)",
                        "    let query = sqlite_prepare(db, \"SELECT id, name, price FROM Items ORDER BY id ASC\")",
                        "    let cols = sqlite_column_count(query)",
                        "    int count = 0",
                        "    if not s1 and not s2 and not s3 and cols == 3 and sqlite_step(query)",
                        "        if sqlite_column_int(query, 0) == 101 and sqlite_column_string(query, 1) == \"Sword\" and sqlite_column_real(query, 2) == 15.5",
                        "            count++",
                        "    if sqlite_step(query)",
                        "        if sqlite_column_int(query, 0) == 102 and sqlite_column_string(query, 1) == \"Shield\" and sqlite_column_real(query, 2) == 25.0",
                        "            count++",
                        "    sqlite_finalize(query)",
                        "    sqlite_close(db)",
                        "    return count",
                        "",
                        "let c = compiletime(testFullSQLiteApi())",
                        "init",
                        "    if c == 2",
                        "        testSuccess()");
    }

    @Test
    public void testCompiletimeSQLiteResetPreservesBindings() {
        test().withStdLib()
                .executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "@extern native sqlite_open(string path) returns int",
                        "@extern native sqlite_prepare(int conn, string q) returns int",
                        "@extern native sqlite_step(int stmt) returns boolean",
                        "@extern native sqlite_column_int(int stmt, int idx) returns int",
                        "@extern native sqlite_exec(int conn, string q)",
                        "@extern native sqlite_bind_int(int stmt, int idx, int value)",
                        "@extern native sqlite_bind_string(int stmt, int idx, string value)",
                        "@extern native sqlite_reset(int stmt)",
                        "@extern native sqlite_clear_bindings(int stmt)",
                        "@extern native sqlite_finalize(int stmt)",
                        "@extern native sqlite_close(int conn)",
                        "",
                        "function testResetPreservesBindings() returns int",
                        "    let db = sqlite_open(\":memory:\")",
                        "    sqlite_exec(db, \"CREATE TABLE Items (id INTEGER, name TEXT)\")",
                        "    let insert = sqlite_prepare(db, \"INSERT INTO Items VALUES (?, ?)\")",
                        "    sqlite_bind_int(insert, 1, 101)",
                        "    sqlite_bind_string(insert, 2, \"Sword\")",
                        "    let s1 = sqlite_step(insert)",
                        // reset then step again WITHOUT rebinding: bindings must be preserved,
                        // so the same row (101, Sword) is inserted a second time.
                        "    sqlite_reset(insert)",
                        "    let s2 = sqlite_step(insert)",
                        "    sqlite_finalize(insert)",
                        "    let count = sqlite_prepare(db, \"SELECT COUNT(*) FROM Items WHERE id = 101 AND name = 'Sword'\")",
                        "    int preserved = 0",
                        "    if not s1 and not s2 and sqlite_step(count)",
                        "        preserved = sqlite_column_int(count, 0)",
                        "    sqlite_finalize(count)",
                        // sqlite_clear_bindings clears parameters back to NULL, so the next insert
                        // writes a NULL id that will not match the id = 999 filter.
                        "    let insert2 = sqlite_prepare(db, \"INSERT INTO Items VALUES (?, ?)\")",
                        "    sqlite_bind_int(insert2, 1, 999)",
                        "    sqlite_bind_string(insert2, 2, \"Cleared\")",
                        "    sqlite_clear_bindings(insert2)",
                        "    let s3 = sqlite_step(insert2)",
                        "    sqlite_finalize(insert2)",
                        "    let cleared = sqlite_prepare(db, \"SELECT COUNT(*) FROM Items WHERE id = 999\")",
                        "    int clearedCount = 1",
                        "    if not s3 and sqlite_step(cleared)",
                        "        clearedCount = sqlite_column_int(cleared, 0)",
                        "    sqlite_finalize(cleared)",
                        "    sqlite_close(db)",
                        // expect 2 rows preserved by reset and 0 rows for id 999 after clear_bindings
                        "    if preserved == 2 and clearedCount == 0",
                        "        return 1",
                        "    return 0",
                        "",
                        "let c = compiletime(testResetPreservesBindings())",
                        "init",
                        "    if c == 1",
                        "        testSuccess()");
    }
}
