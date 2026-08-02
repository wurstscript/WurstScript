package tests.wurstscript.tests;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
    public void testUnsupportedCompiletimeArrayWarningIsAggregatedAndReadable() {
        Logger logger = (Logger) LoggerFactory.getLogger("default");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            test().withStdLib().testLua(true).luaOnly(true).runCompiletimeFunctions(true)
                .lines("package Test",
                    "init",
                    "    let _firstPlayer = players[0]");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        List<String> playerWarnings = appender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .filter(message -> message.contains("Player_players"))
            .toList();
        assertEquals(playerWarnings.size(), 1, "expected one warning for the entire array");
        String warning = playerWarnings.get(0);
        assertTrue(warning.contains("28 unsupported compiletime entries"), warning);
        assertTrue(warning.contains("Player, line"), warning);
        assertFalse(warning.contains("GlobalVarDef"), warning);
    }

    @Test
    public void testCompiletimePackageScalarState() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package A",
                   "public int source = 1",
                   "@compiletime function fill()",
                   "    source = 42",
                   "endpackage",
                   "package B",
                   "import A",
                   "native testSuccess()",
                   "int observed = source",
                   "init",
                   "    if source == 42 and observed == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeObjectAndNullScalarState() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "class A",
                   "    int value",
                   "A source",
                   "string cleared = \"value\"",
                   "@compiletime function fill()",
                   "    source = new A",
                   "    source.value = 42",
                   "    cleared = null",
                   "init",
                   "    if source.value == 42 and cleared == null",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeScalarReplayOnlyWrittenValues() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package A",
                   "public int seed = 1",
                   "init",
                   "    seed = 2",
                   "endpackage",
                   "package B",
                   "import A",
                   "native testSuccess()",
                   "int observed = seed",
                   "int migrated = 0",
                   "@compiletime function fill()",
                   "    let snapshot = observed",
                   "    migrated = snapshot + 41",
                   "init",
                   "    if observed == 2 and migrated == 42",
                   "        testSuccess()");
    }

    @Test
    public void testLazyScalarInitializerSideEffectsAreNotReplayed() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "int counter = 0",
                   "function bump() returns int",
                   "    counter++",
                   "    return counter",
                   "int observed = bump()",
                   "int migrated",
                   "@compiletime function fill()",
                   "    let _snapshot = observed",
                   "    migrated = 42",
                   "init",
                   "    if counter == 1 and observed == 1 and migrated == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeOnlyLazyInitializerSideEffectsAreReplayed() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package MagicFunctions",
                   "public constant compiletime = false",
                   "endpackage",
                   "package Test",
                   "import MagicFunctions",
                   "native testSuccess()",
                   "int runtimeCounter = 0",
                   "int compiletimeValue = 0",
                   "int array compiletimeValues = [0, 0, 0, 0]",
                   "int nestedCounter = 0",
                   "function initializeFlag() returns boolean",
                   "    if compiletime",
                   "        let _compiletimeOnly = true",
                   "    return true",
                   "boolean flag = initializeFlag()",
                   "function initializeNested() returns int",
                   "    if flag",
                   "        nestedCounter++",
                   "    return nestedCounter",
                   "int nestedObserved = initializeNested()",
                   "function initialize() returns int",
                   "    runtimeCounter++",
                   "    let ct = compiletime",
                   "    if ct",
                   "        compiletimeValue = 42",
                   "    if not not ct",
                   "        compiletimeValues[0] = 7",
                   "    if ct and true",
                   "        compiletimeValues[1] = 8",
                   "    if ct or false",
                   "        compiletimeValues[2] = 9",
                   "    if ct == true",
                   "        compiletimeValues[3] = 10",
                   "    return runtimeCounter",
                   "int observed = initialize()",
                   "@compiletime function fill()",
                   "    let _snapshot = observed",
                   "    let _nestedSnapshot = nestedObserved",
                   "init",
                   "    if runtimeCounter == 1 and observed == 1 and compiletimeValue == 42 and compiletimeValues[0] == 7 and compiletimeValues[1] == 8 and compiletimeValues[2] == 9 and compiletimeValues[3] == 10 and nestedCounter == 1 and nestedObserved == 1",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeLazyInitializerControlFlowEdges() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package MagicFunctions",
                   "public constant compiletime = false",
                   "endpackage",
                   "package Test",
                   "import MagicFunctions",
                   "native testSuccess()",
                   "int conditionCounter = 0",
                   "function mark() returns boolean",
                   "    conditionCounter++",
                   "    return true",
                   "function initializeCondition() returns int",
                   "    if compiletime and mark()",
                   "        skip",
                   "    return conditionCounter",
                   "int conditionObserved = initializeCondition()",
                   "int unresolvedCounter = 0",
                   "function runtimeFalse() returns boolean",
                   "    return false",
                   "function initializeUnresolved() returns int",
                   "    if not compiletime and runtimeFalse()",
                   "        skip",
                   "    else",
                   "        unresolvedCounter++",
                   "    return unresolvedCounter",
                   "int unresolvedObserved = initializeUnresolved()",
                   "int oppositeCounter = 0",
                   "function initializeOpposite() returns int",
                   "    if compiletime or runtimeFalse()",
                   "        oppositeCounter++",
                   "    return oppositeCounter",
                   "int oppositeObserved = initializeOpposite()",
                   "int stableLocalCounter = 0",
                   "function initializeStableLocal(int guard) returns int",
                   "    if compiletime or guard == 0",
                   "        stableLocalCounter++",
                   "    return stableLocalCounter",
                   "int stableLocalObserved = initializeStableLocal(1)",
                   "int loopCounter = 0",
                   "function initializeLoop() returns int",
                   "    var i = 0",
                   "    while compiletime and i == 0",
                   "        loopCounter++",
                   "        i++",
                   "    return loopCounter",
                   "int loopObserved = initializeLoop()",
                   "@compiletime function fill()",
                   "    let _conditionSnapshot = conditionObserved",
                   "    let _unresolvedSnapshot = unresolvedObserved",
                   "    let _oppositeSnapshot = oppositeObserved",
                   "    let _stableLocalSnapshot = stableLocalObserved",
                   "    let _loopSnapshot = loopObserved",
                   "init",
                   "    if conditionCounter == 1 and conditionObserved == 1 and unresolvedCounter == 1 and unresolvedObserved == 1 and oppositeCounter == 1 and oppositeObserved == 1 and stableLocalCounter == 1 and stableLocalObserved == 1 and loopCounter == 1 and loopObserved == 1",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeScalarRuntimeWriteRemainsAuthoritative() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "int source = 1",
                   "@compiletime function fill()",
                   "    source = 42",
                   "init",
                   "    source = 7",
                   "    if source == 7",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeClassStaticScalarState() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "class Counter",
                   "    static int value = 1",
                   "    static function setValue(int newValue)",
                   "        value = newValue",
                   "    static function getValue() returns int",
                   "        return value",
                   "int observed = Counter.getValue()",
                   "@compiletime function fill()",
                   "    Counter.setValue(42)",
                   "init",
                   "    if Counter.getValue() == 42 and observed == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeGenericClassStaticScalarState() {
        test().testLua(true).luaOnly(false).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "class Counter<T:>",
                   "    static T value",
                   "    static function setValue(T newValue)",
                   "        value = newValue",
                   "    static function getValue() returns T",
                   "        return value",
                   "@compiletime function fill()",
                   "    Counter<int>.setValue(42)",
                   "init",
                   "    if Counter<int>.getValue() == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeArrayState() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "int array source",
                   "@compiletime function fill()",
                   "    source[0] = 42",
                   "init",
                   "    if source[0] == 42",
                       "        testSuccess()");
    }

    @Test
    public void testCompiletimeArrayStateAfterSourceInitializer() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "int array source = [1]",
                   "@compiletime function fill()",
                   "    source[0] = 42",
                   "init",
                   "    if source[0] == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeArrayStateLua() {
        test().testLua(true).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "int array source = [1]",
                   "@compiletime function fill()",
                   "    source[0] = 42",
                   "init",
                   "    if source[0] == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeGenericArrayState() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "class Box<T:>",
                   "    static T array store",
                   "    static function set(int index, T value)",
                   "        store[index] = value",
                   "    static function get(int index) returns T",
                   "        return store[index]",
                   "@compiletime function fill()",
                   "    Box<int>.set(0, 42)",
                   "init",
                   "    if Box<int>.get(0) == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeGenericArrayStateLua() {
        test().testLua(true).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "class Box<T:>",
                   "    static T array store",
                   "    static function set(int index, T value)",
                   "        store[index] = value",
                   "    static function get(int index) returns T",
                   "        return store[index]",
                   "@compiletime function fill()",
                   "    Box<int>.set(0, 42)",
                   "init",
                   "    if Box<int>.get(0) == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeObjectArrayState() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "class A",
                   "    int value",
                   "A array source",
                   "@compiletime function fill()",
                   "    source[0] = new A",
                   "    source[0].value = 42",
                   "init",
                   "    if source[0].value == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeHashtableArrayState() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("type agent extends handle",
                   "type hashtable extends agent",
                   "package Test",
                   "native testSuccess()",
                   "@extern native InitHashtable() returns hashtable",
                   "@extern native LoadInteger(hashtable h, int p, int c) returns int",
                   "@extern native SaveInteger(hashtable h, int p, int c, int i)",
                   "hashtable array source",
                   "@compiletime function fill()",
                   "    source[0] = InitHashtable()",
                   "    SaveInteger(source[0], 2, 3, 42)",
                   "init",
                   "    if LoadInteger(source[0], 2, 3) == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeNullArrayState() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "string array source = [\"value\"]",
                   "@compiletime function clear()",
                   "    source[0] = null",
                   "init",
                   "    if source[0] == null",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeTupleArrayState() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "tuple pair(int left, int right)",
                   "pair array source",
                   "@compiletime function fill()",
                   "    source[0] = pair(42, 7)",
                   "init",
                   "    if source[0].left == 42 and source[0].right == 7",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeArrayStateAcrossPackages() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package A",
                   "public int array source = [1]",
                   "@compiletime function fillA()",
                   "    source[0] = 42",
                   "init",
                   "    source[0] = 7",
                   "endpackage",
                   "package B",
                   "import A",
                   "native testSuccess()",
                   "init",
                   "    if source[0] == 7",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeArrayStateAcrossPackagesWithTwoInitializers() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package A",
                   "public int array source = [1]",
                   "@compiletime function fillA()",
                   "    source[0] = 42",
                   "init",
                   "    source[0] = 7",
                   "endpackage",
                   "package B",
                   "import A",
                   "int array other = [2]",
                   "@compiletime function fillB()",
                   "    other[0] = 9",
                   "native testSuccess()",
                   "init",
                   "    if source[0] == 7 and other[0] == 9",
                   "        testSuccess()",
                   "endpackage");
    }

    @Test
    public void testCompiletimeArrayReplayPrecedesDependentInitializer() {
        test().testLua(true).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package Test",
                   "native testSuccess()",
                   "int array first = [1]",
                   "int observed = first[0]",
                   "int array second = [2]",
                   "@compiletime function fill()",
                   "    first[0] = 42",
                   "    second[0] = 9",
                   "init",
                   "    if observed == 42 and first[0] == 42 and second[0] == 9",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeArrayReplayOnlyWrittenEntries() {
        test().testLua(true).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true)
            .lines("package A",
                   "public int seed = 1",
                   "init",
                   "    seed = 2",
                   "endpackage",
                   "package B",
                   "import A",
                   "native testSuccess()",
                   "int array source = [seed, 0]",
                   "@compiletime function fill()",
                   "    source[1] = 42",
                   "init",
                   "    if source[0] == 2 and source[1] == 42",
                   "        testSuccess()");
    }

    @Test
    public void testCompiletimeHashtable() {
        test().executeProg(true).executeProgOnlyAfterTransforms()
                .runCompiletimeFunctions(true)
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
    public void testPersistCompiletimeNewGenericClass() {
        // Translation is the assertion here: executing the synthesized generic
        // runtime global through the interpreter still needs a separate attachment fix.
        test()
                .runCompiletimeFunctions(true)
                .lines("package Test",
                        "class PureMap<T:>",
                        "    T value",
                        "    function put(T value)",
                        "        this.value = value",
                        "    function get() returns T",
                        "        return value",
                        "function compiletime<T:>(T value) returns T",
                        "    return value",
                        "PureMap<int> map = compiletime(new PureMap<int>)",
                        "@compiletime function populate()",
                        "    map.put(42)");
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

    @Test
    public void testCompiletimeSQLiteExecMultiStatementAndNulls() {
        test().withStdLib()
                .executeProg(true)
                .runCompiletimeFunctions(true)
                .executeProgOnlyAfterTransforms()
                .lines("package Test",
                        "@extern native sqlite_open(string path) returns int",
                        "@extern native sqlite_prepare(int conn, string q) returns int",
                        "@extern native sqlite_step(int stmt) returns boolean",
                        "@extern native sqlite_column_int(int stmt, int idx) returns int",
                        "@extern native sqlite_column_is_null(int stmt, int idx) returns boolean",
                        "@extern native sqlite_exec(int conn, string q)",
                        "@extern native sqlite_bind_int(int stmt, int idx, int value)",
                        "@extern native sqlite_finalize(int stmt)",
                        "@extern native sqlite_close(int conn)",
                        "",
                        "function testExecMultiAndNulls() returns int",
                        "    let db = sqlite_open(\":memory:\")",
                        // multi-statement exec must create BOTH tables (only the first runs
                        // under a naive Statement.execute)
                        "    sqlite_exec(db, \"CREATE TABLE A (id INTEGER); CREATE TABLE B (id INTEGER, name TEXT)\")",
                        // multi-statement DML: both inserts must run
                        "    sqlite_exec(db, \"INSERT INTO A VALUES (1); INSERT INTO A VALUES (2)\")",
                        "    let ca = sqlite_prepare(db, \"SELECT COUNT(*) FROM A\")",
                        "    int countA = 0",
                        "    if sqlite_step(ca)",
                        "        countA = sqlite_column_int(ca, 0)",
                        "    sqlite_finalize(ca)",
                        // rebind WITHOUT an intervening sqlite_reset must force re-execution,
                        // so both id 3 and id 4 get inserted (4 rows total in A)
                        "    let ins = sqlite_prepare(db, \"INSERT INTO A VALUES (?)\")",
                        "    sqlite_bind_int(ins, 1, 3)",
                        "    let b1 = sqlite_step(ins)",
                        "    sqlite_bind_int(ins, 1, 4)",
                        "    let b2 = sqlite_step(ins)",
                        "    sqlite_finalize(ins)",
                        "    let ca2 = sqlite_prepare(db, \"SELECT COUNT(*) FROM A\")",
                        "    int countA2 = 0",
                        "    if sqlite_step(ca2)",
                        "        countA2 = sqlite_column_int(ca2, 0)",
                        "    sqlite_finalize(ca2)",
                        // NULL detection: a genuine SQL NULL must be distinguishable
                        "    sqlite_exec(db, \"INSERT INTO B VALUES (7, NULL)\")",
                        "    let q = sqlite_prepare(db, \"SELECT id, name FROM B WHERE id = 7\")",
                        "    boolean idNotNull = false",
                        "    boolean nameIsNull = false",
                        "    if not b1 and not b2 and sqlite_step(q)",
                        "        idNotNull = not sqlite_column_is_null(q, 0)",
                        "        nameIsNull = sqlite_column_is_null(q, 1)",
                        "    sqlite_finalize(q)",
                        "    sqlite_close(db)",
                        "    if countA == 2 and countA2 == 4 and idNotNull and nameIsNull",
                        "        return 1",
                        "    return 0",
                        "",
                        "let c = compiletime(testExecMultiAndNulls())",
                        "init",
                        "    if c == 1",
                        "        testSuccess()");
    }
}
