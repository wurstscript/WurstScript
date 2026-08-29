package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Regression tests for the Wurst -> Lua backend audit.
 *
 * Each test is a minimal repro for a concrete emitted-code bug. All tests use
 * {@code testLua(true)}, which syntax-checks the generated Lua with luac;
 * several bugs manifest directly as luac syntax errors. Behavioral properties
 * are additionally asserted structurally on the generated source.
 */
public class LuaBackendAuditTests extends WurstScriptTest {

    private String compiledLua(String testName) throws IOException {
        return Files.toString(new File("test-output/lua/LuaBackendAuditTests_" + testName + ".lua"), Charsets.UTF_8);
    }

    private String compileOptimizedLua(String testName, String... lines) {
        RunArgs runArgs = new RunArgs().with("-lua", "-inline", "-localOptimizations");
        return compileLuaWithRunArgs(testName, runArgs, lines);
    }

    private String compileLuaWithRunArgs(String testName, RunArgs runArgs, String... lines) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU(testName + ".wurst", String.join("\n", lines))), false, compiler);
        assertTrue("unexpected parse/type errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.checkProg(model);
        assertTrue("unexpected compile errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.translateProgToIm(model);
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        LuaCompilationUnit luaCode = compiler.transformProgToLua();
        StringBuilder result = new StringBuilder();
        luaCode.print(result, 0);
        return result.toString();
    }

    @Test
    public void tuplesAreScalarizedWithoutLuaAllocations() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple vec2(real x, real y)",
            "tuple segment(vec2 start, vec2 finish)",
            "vec2 array points",
            "abstract class Producer",
            "    vec2 offset",
            "    abstract function produce(real x) returns vec2",
            "class Concrete extends Producer",
            "    override function produce(real x) returns vec2",
            "        return vec2(x + offset.x, x + offset.y)",
            "function shifted(segment s, vec2 delta) returns segment",
            "    return segment(vec2(s.start.x + delta.x, s.start.y + delta.y),",
            "        vec2(s.finish.x + delta.x, s.finish.y + delta.y))",
            "init",
            "    Producer producer = new Concrete()",
            "    producer.offset = vec2(3., 4.)",
            "    points[2] = producer.produce(5.)",
            "    let result = shifted(segment(points[2], vec2(10., 20.)), vec2(1., 2.))",
            "    if points[2] == vec2(8., 9.) and result.start == vec2(9., 11.)",
            "        and result.finish == vec2(11., 22.)",
            "        testSuccess()"
        );

        String compiled = compiledLua("tuplesAreScalarizedWithoutLuaAllocations");
        assertFalse("tuple assignment must not allocate through a copy helper", compiled.contains("tupleCopy"));
        assertFalse("tuple comparison must be lowered to scalar comparisons", compiled.contains("tupleEquals"));
        assertFalse("tuple arrays must be split into scalar arrays", compiled.contains("__wurst_arrIndex("));
    }

    @Test
    public void randomizedTupleValueSemanticsStayScalar() throws IOException {
        Random random = new Random(0x5CA1A2L);
        List<String> source = new ArrayList<>();
        source.add("package Test");
        source.add("native testSuccess()");
        source.add("tuple pair(int x, int y)");
        source.add("init");
        source.add("    int checksum = 0");
        int expected = 0;
        for (int i = 0; i < 64; i++) {
            int ax = random.nextInt(101) - 50;
            int ay = random.nextInt(101) - 50;
            int bx = random.nextInt(101) - 50;
            int by = random.nextInt(101) - 50;
            int resultX = ay + bx;
            int resultY = ax - by;
            source.add("    pair a" + i + " = pair(" + ax + ", " + ay + ")");
            source.add("    let b" + i + " = pair(" + bx + ", " + by + ")");
            source.add("    a" + i + " = pair(a" + i + ".y + b" + i + ".x, a" + i + ".x - b" + i + ".y)");
            source.add("    checksum += a" + i + ".x * " + (i + 1) + " + a" + i + ".y");
            expected += resultX * (i + 1) + resultY;
        }
        source.add("    if checksum == " + expected);
        source.add("        testSuccess()");

        test().testLua(true).executeProg().lines(source.toArray(new String[0]));
        String compiled = compiledLua("randomizedTupleValueSemanticsStayScalar");
        assertFalse(compiled.contains("tupleCopy"));
        assertFalse(compiled.contains("tupleEquals"));
    }

    @Test
    public void tupleReturnSlotsAreSharedAcrossMultipleInterfaceRoots() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "interface First",
            "    function value(int seed) returns pair",
            "interface Second",
            "    function value(int seed) returns pair",
            "class Both implements First, Second",
            "    function value(int seed) returns pair",
            "        return pair(seed, seed + 1)",
            "@noinline function fromFirst(First value) returns pair",
            "    return value.value(10)",
            "@noinline function fromSecond(Second value) returns pair",
            "    return value.value(20)",
            "init",
            "    let both = new Both()",
            "    let first = fromFirst(both)",
            "    let second = fromSecond(both)",
            "    if first == pair(10, 11) and second == pair(20, 21)",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleReturnSlotsAreSharedAcrossMultipleInterfaceRoots");
        assertFalse(compiled.contains("tupleCopy"));
        assertFalse(compiled.contains("tupleEquals"));
    }

    @Test
    public void tupleReturningCallsAreCapturedBeforeComparison() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "@noinline function value(int seed) returns pair",
            "    return pair(0, seed)",
            "init",
            "    if value(1) != value(2) and not (value(1) == value(2))",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleReturningCallsAreCapturedBeforeComparison");
        assertFalse(compiled.contains("tupleCopy"));
        assertFalse(compiled.contains("tupleEquals"));
    }

    @Test
    public void selectingLaterTupleComponentStillInvokesProducer() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "class Producer",
            "    int calls",
            "    @noinline function produce(int seed) returns pair",
            "        calls++",
            "        return pair(seed, seed + calls)",
            "init",
            "    let producer = new Producer()",
            "    let selected = producer.produce(5).y",
            "    if selected == 6 and producer.calls == 1",
            "        testSuccess()"
        );

        String compiled = compiledLua("selectingLaterTupleComponentStillInvokesProducer");
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void compiletimeGenericArrayReplayLeavesAreSplit() {
        String compiled = compileLuaWithRunArgs(
            "compiletimeGenericArrayReplayLeavesAreSplit",
            new RunArgs().with("-lua", "-runcompiletimefunctions", "-functionSplitLimit", "1"),
            "package Test",
            "class Box<T:>",
            "    @compiletime static T array store",
            "    static function set(int index, T value)",
            "        store[index] = value",
            "    static function get(int index) returns T",
            "        return store[index]",
            "@compiletime function fill()",
            "    Box<int>.set(0, 10)",
            "    Box<int>.set(1, 20)",
            "    Box<int>.set(2, 30)",
            "native testSuccess()",
            "init",
            "    if Box<int>.get(0) + Box<int>.get(1) + Box<int>.get(2) == 60",
            "        testSuccess()"
        );

        java.util.regex.Matcher replayBody = java.util.regex.Pattern
            .compile("function initCompiletimeArrayState[^\\n]*\\n(.*?)\\nend", java.util.regex.Pattern.DOTALL)
            .matcher(compiled);
        int persistedAssignments = 0;
        while (replayBody.find()) {
            int assignmentsInFunction = countOccurrences(replayBody.group(1), "Box_store[");
            assertTrue("each generic replay leaf must honor the configured split limit:\n" + replayBody.group(),
                assignmentsInFunction <= 1);
            persistedAssignments += assignmentsInFunction;
        }
        assertEquals("all generic compiletime array entries must still be emitted", 3, persistedAssignments);
    }

    @Test
    public void compiletimeArrayReplaySplittingIsDeterministicAcrossPackages() {
        RunArgs runArgs = new RunArgs().with(
            "-lua", "-runcompiletimefunctions", "-functionSplitLimit", "1");
        String[] source = {
            "package A", "@compiletime public int array a = [1]", "@compiletime function fillA()", "    a[0] = 10", "endpackage",
            "package B", "@compiletime public int array b = [1]", "@compiletime function fillB()", "    b[0] = 20", "endpackage",
            "package C", "@compiletime public int array c = [1]", "@compiletime function fillC()", "    c[0] = 30", "endpackage",
            "package D", "@compiletime public int array d = [1]", "@compiletime function fillD()", "    d[0] = 40", "endpackage",
            "package Test", "import A", "import B", "import C", "import D", "native testSuccess()", "init",
            "    if a[0] + b[0] + c[0] + d[0] == 100", "        testSuccess()"
        };

        String first = compileLuaWithRunArgs("compiletimeArrayReplaySplittingIsDeterministicAcrossPackages", runArgs, source);
        String second = compileLuaWithRunArgs("compiletimeArrayReplaySplittingIsDeterministicAcrossPackages", runArgs, source);
        assertEquals("compiletime replay splitting must not depend on identity-hash iteration", first, second);
    }

    @Test
    public void compiletimeScalarReplaySplittingIsDeterministicAcrossPackages() {
        RunArgs runArgs = new RunArgs().with(
            "-lua", "-runcompiletimefunctions", "-functionSplitLimit", "1");
        String[] source = {
            "package A", "@compiletime public int a", "@compiletime function fillA()", "    a = 10", "endpackage",
            "package B", "@compiletime public int b", "@compiletime function fillB()", "    b = 20", "endpackage",
            "package C", "@compiletime public int c", "@compiletime function fillC()", "    c = 30", "endpackage",
            "package D", "@compiletime public int d", "@compiletime function fillD()", "    d = 40", "endpackage",
            "package Test", "import A", "import B", "import C", "import D", "native testSuccess()", "init",
            "    if a + b + c + d == 100", "        testSuccess()"
        };

        String first = compileLuaWithRunArgs("compiletimeScalarReplaySplittingIsDeterministicAcrossPackages", runArgs, source);
        String second = compileLuaWithRunArgs("compiletimeScalarReplaySplittingIsDeterministicAcrossPackages", runArgs, source);
        assertEquals("compiletime scalar replay splitting must be deterministic", first, second);

        java.util.regex.Matcher replayBody = java.util.regex.Pattern
            .compile("function initCompiletimeScalarState[^\\n]*\\n(.*?)\\nend", java.util.regex.Pattern.DOTALL)
            .matcher(first);
        int persistedAssignments = 0;
        while (replayBody.find()) {
            int assignmentsInFunction = 0;
            for (int value : new int[]{10, 20, 30, 40}) {
                assignmentsInFunction += countOccurrences(replayBody.group(1), " = " + value);
            }
            assertTrue("each scalar replay leaf must honor the configured split limit:\n" + replayBody.group(),
                assignmentsInFunction <= 1);
            persistedAssignments += assignmentsInFunction;
        }
        assertEquals("all compiletime scalar values must still be emitted", 4, persistedAssignments);
    }

    @Test
    public void compiletimeScalarMigrationIsDisabledByDefault() {
        String compiled = compileLuaWithRunArgs(
            "compiletimeScalarMigrationIsDisabledByDefault",
            new RunArgs().with("-lua", "-runcompiletimefunctions"),
            "package Test",
            "int source = 1",
            "@compiletime function fill()",
            "    source = 42",
            "native testSuccess()",
            "init",
            "    if source == 1",
            "        testSuccess()"
        );

        assertFalse("scalar compiletime state must not be emitted without the opt-in flag", compiled.contains("initCompiletimeScalarState"));
    }

    @Test
    public void compiletimeArrayMigrationIsDisabledByDefault() {
        String compiled = compileLuaWithRunArgs(
            "compiletimeArrayMigrationIsDisabledByDefault",
            new RunArgs().with("-lua", "-runcompiletimefunctions"),
            "package Test",
            "int array source = [1]",
            "@compiletime function fill()",
            "    source[0] = 42",
            "native testSuccess()",
            "init",
            "    if source[0] == 1",
            "        testSuccess()"
        );

        assertFalse("array compiletime state must not be emitted without the opt-in annotation", compiled.contains("initCompiletimeArrayState"));
    }

    @Test
    public void compiletimeInterpreterSeesLuaTarget() {
        String compiled = compileLuaWithRunArgs(
            "compiletimeInterpreterSeesLuaTarget",
            new RunArgs().with("-lua", "-runcompiletimefunctions"),
            "package MagicFunctions",
            "public constant isLua = false",
            "endpackage",
            "package Test",
            "import MagicFunctions",
            "@compiletime int observedBackend",
            "@compiletime function detectBackend()",
            "    if isLua",
            "        observedBackend = 1",
            "    else",
            "        observedBackend = 2",
            "native testSuccess()",
            "init",
            "    if observedBackend == 1",
            "        testSuccess()"
        );

        assertTrue("compiletime execution must take the Lua branch:\n" + compiled,
            compiled.contains("Test_observedBackend = 1"));
        assertFalse("compiletime execution must not persist the Jass branch:\n" + compiled,
            compiled.contains("Test_observedBackend = 2"));
    }

    @Test
    public void localPlayerEffectfulBooleanOperandSurvivesOptimization() {
        String compiled = compileOptimizedLua(
            "localPlayerEffectfulBooleanOperandSurvivesOptimization",
            "type player extends handle",
            "package Test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "integer calls = 0",
            "@noinline function localProbe() returns boolean",
            "    calls++",
            "    return GetLocalPlayer() == Player(0)",
            "init",
            "    if localProbe() or true",
            "        print(calls)"
        );

        int definitionOrCall = compiled.indexOf("localProbe()");
        assertTrue("optimized Lua must emit the local-player probe",
            definitionOrCall >= 0);
        assertTrue("optimized Lua must retain the call to the local-player probe",
            compiled.indexOf("localProbe()", definitionOrCall + 1) >= 0);
    }

    @Test
    public void localPlayerTaintFlowsThroughVarargLoopValues() {
        String compiled = compileOptimizedLua(
            "localPlayerTaintFlowsThroughVarargLoopValues",
            "type player extends handle",
            "package Test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "player selected",
            "integer result = 0",
            "@noinline function selectLast(vararg player players)",
            "    for p in players",
            "        selected = p",
            "init",
            "    selectLast(Player(1), GetLocalPlayer())",
            "    if selected == Player(0)",
            "        result = 31",
            "    else",
            "        result = 31",
            "    print(result)"
        );

        assertTrue("vararg loop values must retain local-player dependence",
            countOccurrences(compiled, "result = 31") >= 2);
    }

    /**
     * A bare {@code return} inside a vararg loop used to truncate the literal
     * {@code end} that closed the loop, producing unparseable Lua
     * (caught here by the luac syntax check).
     */
    @Test
    public void varargLoopWithBareReturn() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function firstOr(vararg int xs) returns int",
            "    for x in xs",
            "        return x",
            "    return -1",
            "init",
            "    if firstOr(7, 8) == 7",
            "        testSuccess()"
        );
        String compiled = compiledLua("varargLoopWithBareReturn");
        // the loop must be built from real AST nodes, not from literal for/end lines
        assertFalse("vararg loop must not be emitted via literal 'for' lines",
            compiled.contains("for i=1,"));
    }

    /**
     * All fields of a class hierarchy are flattened into one instance table.
     * A closure-captured local whose name collides with an (IM-prefixed)
     * superclass field name used to silently alias that field: both ended up
     * as the same table key, so writes to one clobbered the other.
     */
    @Test
    public void hierarchyFieldsDoNotAlias() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "abstract class Cb",
            "    int y = 100",
            "    abstract function run() returns int",
            "    function base() returns int",
            "        return y",
            "init",
            "    let Cb_y = 5",
            "    Cb c = () -> Cb_y",
            "    if c.run() == 5 and c.base() == 100",
            "        testSuccess()"
        );
        String compiled = compiledLua("hierarchyFieldsDoNotAlias");
        // no allocation table may contain the same key twice
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\\(\\{([^}]*)\\}\\)").matcher(compiled);
        while (m.find()) {
            String[] keys = m.group(1).split(",");
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (String entry : keys) {
                int eq = entry.indexOf('=');
                if (eq > 0) {
                    String key = entry.substring(0, eq).trim();
                    assertTrue("duplicate field key '" + key + "' in allocation table: " + m.group(0),
                        seen.add(key));
                }
            }
        }
    }

    /**
     * Constructor helper methods are named create, create1, create2, ... in
     * class-translation order, while method dispatch slots use (normalized)
     * user method names. Both live in the same class-table key namespace, so
     * a user method whose dispatch slot name equals a class's constructor
     * name used to overwrite the constructor at main() time: allocations then
     * called the user method instead of the constructor.
     */
    @Test
    public void dispatchSlotMustNotStompConstructor() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface I",                       // class 0 -> constructor "create"
            "    function create2() returns int", // bare slot name "create2"
            "        return 42",
            "class A",                           // class 1 -> constructor "create1"
            "class C implements I",              // class 2 -> constructor "create2"
            "    int v = 7",
            "init",
            "    let a = new A()",
            "    let c = new C()",
            "    if c.v == 7 and c.create2() == 42 and a != null",
            "        testSuccess()"
        );
        String compiled = compiledLua("dispatchSlotMustNotStompConstructor");
        // no class may have a dispatch slot assigned over its own constructor:
        // for every "function X:NAME(" definition there must be no "X.NAME =" assignment
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("function\\s+([A-Za-z0-9_]+):(create\\d*)\\s*\\(").matcher(compiled);
        while (m.find()) {
            String cls = m.group(1);
            String ctor = m.group(2);
            assertFalse("constructor " + cls + ":" + ctor + " is overwritten by a dispatch slot assignment",
                compiled.contains(cls + "." + ctor + " ="));
        }
    }

    /**
     * Dispatch alias generation derives a "semantic name" from everything
     * after the last underscore of a method name. A user method containing
     * an underscore (my_x) therefore produced phantom alias slots
     * (Base_x, Child_x) that collided with the real dispatch slot of an
     * unrelated method named x — and could win the slot, so calls to x()
     * dispatched into my_x(int) with a missing argument.
     */
    @Test
    public void underscoreMethodNamesDoNotStompUnrelatedDispatchSlots() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Base",
            "    function x() returns int",
            "        return 1",
            "class Child extends Base",
            "    function my_x(int i) returns int",
            "        return i + 100",
            "class Child2 extends Base",
            "    override function x() returns int",
            "        return 2",
            "init",
            "    Base b = new Child()",
            "    Base b2 = new Child2()",
            "    let c = new Child()",
            "    if b.x() == 1 and b2.x() == 2 and c.my_x(1) == 101",
            "        testSuccess()"
        );
        String compiled = compiledLua("underscoreMethodNamesDoNotStompUnrelatedDispatchSlots");
        assertFalse("phantom semantic slot must not bind my_x over the real x dispatch slot",
            compiled.contains("Child.Base_x = Child_Child_my_x"));
    }

    private static final String[] DIV_MOD_PROG = {
        "package Test",
        "native testSuccess()",
        "function d(int a, int b) returns int",
        "    return a div b",
        "function m(int a, int b) returns int",
        "    return a mod b",
        "init",
        "    if d(-7, 2) == -3 and m(-7, 2) == 1 and d(7, -2) == -3 and m(7, -2) == 1",
        "        if d(7, 2) == 3 and m(7, 2) == 1 and d(-8, 2) == -4 and m(-8, 2) == 0",
        "            testSuccess()"
    };

    /**
     * Reference semantics: integer div truncates toward zero and mod follows
     * Blizzard.j's ModuloInteger (truncated remainder, plus divisor when
     * negative). This guards the interpreter/Jass behavior the Lua backend
     * must match.
     */
    @Test
    public void integerDivModReferenceSemanticsInInterpreter() {
        test().executeProg().lines(DIV_MOD_PROG);
    }

    /**
     * EliminateLocalTypes#transformProgram runs after LuaEnsureFunctions
     * builds __wurst_ensureStr's/__wurst_stringConcat's bodies, and
     * unconditionally rewrites every string-typed ImNull node in the whole
     * program - including inside those functions themselves - into
     * ImStringVal(""). If their own "x ~= nil" checks were tagged with the
     * string type, that rewrite would silently turn them into "x ~= \"\"",
     * so a genuinely nil Lua value (e.g. an unset bound-generic string
     * field) would read as "not nil", skip normalization, and come out as
     * the literal string "nil" via tostring() instead of "" - or, for
     * stringConcat, get passed straight into raw ".." concatenation.
     * LuaEnsureFunctions#notNull tags its ImNull sentinel with ImAnyType
     * specifically to stay exempt from that rewrite.
     */
    @Test
    public void ensureStrAndStringConcatNilChecksSurviveEliminateLocalTypes() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "string array names",
            "function join(string a, string b) returns string",
            "    return a + b",
            "init",
            "    if names[5] == \"\" and join(\"a\", \"b\") == \"ab\"",
            "        testSuccess()"
        );
        String compiled = compiledLua("ensureStrAndStringConcatNilChecksSurviveEliminateLocalTypes");
        assertNilCheckNotCorruptedToEmptyStringCheck(compiled, "__wurst_ensureStr(");
        assertNilCheckNotCorruptedToEmptyStringCheck(compiled, "__wurst_stringConcat(");
    }

    private void assertNilCheckNotCorruptedToEmptyStringCheck(String compiled, String functionNamePrefix) {
        int fnStart = compiled.indexOf("function " + functionNamePrefix);
        assertTrue("expected " + functionNamePrefix + " to be present", fnStart >= 0);
        int fnEnd = compiled.indexOf("\nend", fnStart);
        String fnBody = compiled.substring(fnStart, fnEnd);
        assertTrue(functionNamePrefix + " must check for real nil", fnBody.contains("== nil"));
        assertFalse(functionNamePrefix + "'s nil check must not be corrupted into an empty-string check",
            fnBody.contains("== \"\""));
    }

    /**
     * The Lua backend used to emit floored {@code //} for div and
     * {@code math.floor(a % b)} for mod, which disagree with the Jass
     * backend and the interpreter for negative operands
     * (e.g. -7 div 2 was -4 instead of -3, and 7 mod -2 was -1 instead of 1).
     *
     * Div/mod are now lowered to portable IM functions before the optimizer
     * runs (see LuaNativeLowering#lowerDivMod), so calls with constant
     * arguments - like the ones below - may get inlined away entirely rather
     * than showing up as a helper call in the output. The floor-div/fmod
     * *native* they delegate to (Wurst has no such IM operator) always
     * survives somewhere in the output, inlined or not, so checking for it
     * is robust regardless of the inliner's decision.
     */
    @Test
    public void integerDivModMatchJassSemanticsInLua() throws IOException {
        test().testLua(true).executeProg().lines(DIV_MOD_PROG);
        String compiled = compiledLua("integerDivModMatchJassSemanticsInLua");
        assertTrue("div must go through the truncating floor-div correction",
            compiled.contains("__wurst_rawFloorDivInt("));
        assertTrue("mod must go through the ModuloInteger-compatible fmod correction",
            compiled.contains("__wurst_rawFmodInt("));
        assertFalse("mod/div must not use math.floor directly",
            compiled.contains("math.floor"));
    }

    /**
     * ErrorHandling.error()'s deliberate {@code I2S(1 div 0)} crash trap
     * relies on {@code lua.translation.ExprTranslation#isIntentionalThreadAbortCall}
     * pattern-matching the exact {@code ImOperatorCall(DIV_INT, [1, 0])} shape
     * as I2S's sole argument, to turn it into the {@code __wurst_abort_thread}
     * sentinel every callback xpcall handler ignores. The div/mod lowering
     * (LuaNativeLowering#lowerDivMod) runs before that check and rewrites
     * every DIV_INT it sees into a __wurst_intDiv(...) call - which would
     * destroy that exact shape and replace the sentinel with a real Lua
     * {@code n//0} runtime error, breaking every callback error handler's
     * "was this an intentional abort" check. This guards that the lowering
     * carves out an exception for precisely this pattern.
     */
    @Test
    public void i2sDivByZeroAbortTrapSurvivesDivModLowering() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native testSuccess()",
            "native I2S(int i) returns string",
            "native print(string s)",
            "function crashTrap() returns string",
            "    return I2S(1 div 0)",
            "init",
            "    print(crashTrap())",
            "    testSuccess()"
        );
        String compiled = compiledLua("i2sDivByZeroAbortTrapSurvivesDivModLowering");
        assertTrue("the abort trap must still produce the sentinel error call",
            compiled.contains("error(\"__wurst_abort_thread\", 0)"));
        assertFalse("the abort trap's 1 div 0 must not be lowered to a helper call",
            compiled.contains("__wurst_intDiv(1, 0)"));
    }

    /**
     * Div/mod helpers are real IM functions now, created only when the
     * program actually uses div/mod, instead of unconditionally-emitted Lua
     * source (as before) - so a program that never divides/mods must not
     * carry any of this machinery in its output.
     */
    @Test
    public void divModHelpersAreOmittedWhenUnused() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "init",
            "    if 1 + 1 == 2",
            "        testSuccess()"
        );
        String compiled = compiledLua("divModHelpersAreOmittedWhenUnused");
        assertFalse("unused div helper must not be emitted",
            compiled.contains("__wurst_intDiv"));
        assertFalse("unused mod helpers must not be emitted",
            compiled.contains("__wurst_mod"));
        assertFalse("unused raw floor-div/fmod natives must not be emitted",
            compiled.contains("__wurst_rawF"));
    }

    /**
     * A non-constant div/mod call (parameters, not literals) has no
     * constant-folding upside, so the inliner leaves it as a call to the
     * shared helper - proving div/mod is optimizable at all now (it used to
     * be opaque, always-emitted Lua source the IM optimizer never saw).
     */
    @Test
    public void nonConstantDivModCallsUseSharedHelper() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function d(int a, int b) returns int",
            "    return a div b",
            "function m(int a, int b) returns int",
            "    return a mod b",
            "init",
            "    int x = 7",
            "    int y = -2",
            "    if d(x, y) == -3 and m(x, y) == 1 and d(y, x) == 0 and m(y, x) == 5",
            "        testSuccess()"
        );
        String compiled = compiledLua("nonConstantDivModCallsUseSharedHelper");
        assertEquals("expected exactly one shared int-div helper definition",
            1, countOccurrences(compiled, "function __wurst_intDiv("));
        assertEquals("expected exactly one shared int-mod helper definition",
            1, countOccurrences(compiled, "function __wurst_modInt("));
    }

    /**
     * String concatenation is lowered to a synthetic stringConcat IM function.
     * The polyfill and its call sites used to be linked only by both happening
     * to print the same Lua name "stringConcat" - a user function also named
     * stringConcat could hijack it, or get renamed away by the collision. The
     * polyfill now has its own permanently distinct internal name
     * (__wurst_stringConcat, see LuaEnsureFunctions), so a user-defined
     * stringConcat no longer collides with it at all and needs no renaming.
     */
    @Test
    public void userFunctionNamedStringConcatDoesNotBreakConcatenation() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function stringConcat(string a, string b) returns string",
            "    return \"wrong\"",
            "function join(string a, string b) returns string",
            "    return a + b",
            "init",
            "    if join(\"a\", \"b\") == \"ab\" and stringConcat(\"a\", \"b\") == \"wrong\"",
            "        testSuccess()"
        );
        String compiled = compiledLua("userFunctionNamedStringConcatDoesNotBreakConcatenation");
        assertTrue("user stringConcat no longer collides with the internal polyfill and keeps its own name",
            compiled.contains("function stringConcat("));
        assertFalse("the internal polyfill must not leak the bare 'stringConcat' name",
            compiled.contains("function __wurst_stringConcat1("));
    }

    /**
     * Inlining starts with garbage collection, while string PLUS used to be
     * lowered into __wurst_stringConcat only after inlining. Once the helper
     * became an ordinary IM function, that ordering removed its definition
     * before its first call was introduced.
     */
    @Test
    public void optimizedStringConcatKeepsItsHelperDefinition() {
        String compiled = compileOptimizedLua(
            "LuaBackendAuditTests_optimizedStringConcatKeepsItsHelperDefinition",
            "package Test",
            "native print(string message)",
            "function join1(string a, string b) returns string",
            "    return a + b",
            "function join2(string a, string b) returns string",
            "    return a + b",
            "function join3(string a, string b) returns string",
            "    return a + b",
            "function join4(string a, string b) returns string",
            "    return a + b",
            "function join5(string a, string b) returns string",
            "    return a + b",
            "function join6(string a, string b) returns string",
            "    return a + b",
            "init",
            "    print(join1(\"a\", \"b\"))",
            "    print(join2(\"a\", \"b\"))",
            "    print(join3(\"a\", \"b\"))",
            "    print(join4(\"a\", \"b\"))",
            "    print(join5(\"a\", \"b\"))",
            "    print(join6(\"a\", \"b\"))"
        );
        assertTrue("optimized Lua must retain the helper called by lowered string concatenation",
            compiled.contains("function __wurst_stringConcat("));
        assertTrue("repro must contain calls in addition to the helper definition",
            countOccurrences(compiled, "__wurst_stringConcat(") > 1);
    }

    /**
     * All portable Lua helpers now live in IM and may be inlined or removed
     * when unused. Any helper call that remains, however, must still target a
     * function rooted in the final IM program. LuaTranslator checks that
     * invariant for every function call/reference before emitting source.
     */
    @Test
    public void optimizedMovedImHelpersHaveNoDanglingReferences() {
        String compiled = compileOptimizedLua(
            "LuaBackendAuditTests_optimizedMovedImHelpersHaveNoDanglingReferences",
            "package Test",
            "native print(string message)",
            "native I2S(int value) returns string",
            "native R2S(real value) returns string",
            "int array ints",
            "bool array bools",
            "real array reals",
            "string array strings",
            "function readInt(int index) returns int",
            "    return ints[index]",
            "function readBool(int index) returns bool",
            "    return bools[index]",
            "function readReal(int index) returns real",
            "    return reals[index]",
            "function readString(int index) returns string",
            "    return strings[index]",
            "function intDiv(int a, int b) returns int",
            "    return a div b",
            "function intMod(int a, int b) returns int",
            "    return a mod b",
            "function realMod(real a, real b) returns real",
            "    return a % b",
            "init",
            "    ints[1] = 7",
            "    bools[1] = true",
            "    reals[1] = 7.5",
            "    strings[1] = \"value=\"",
            "    if readBool(1)",
            "        print(readString(1) + I2S(intDiv(readInt(1), 2)))",
            "        print(I2S(intMod(readInt(1), 2)))",
            "        print(R2S(realMod(readReal(1), 2.)))"
        );

        String[] helperNames = {
            "__wurst_ensureInt", "__wurst_ensureBool", "__wurst_ensureReal", "__wurst_ensureStr",
            "__wurst_stringConcat", "__wurst_intDiv", "__wurst_modInt", "__wurst_modReal",
            "__wurst_rawToNumberInt", "__wurst_rawToInteger", "__wurst_rawToNumberReal",
            "__wurst_rawToString", "__wurst_rawConcat", "__wurst_rawFloorDivInt",
            "__wurst_rawFmodInt", "__wurst_rawFmodReal"
        };
        for (String helperName : helperNames) {
            assertHelperDefinedWhenCalled(compiled, helperName);
        }
        assertTrue("repro must exercise integer ensure lowering", compiled.contains("__wurst_rawToNumberInt"));
        assertTrue("repro must exercise real ensure lowering", compiled.contains("__wurst_rawToNumberReal"));
        assertTrue("repro must exercise string ensure lowering", compiled.contains("__wurst_rawToString"));
        assertTrue("repro must exercise string concat lowering", compiled.contains("__wurst_rawConcat"));
        assertTrue("repro must exercise integer div lowering", compiled.contains("__wurst_rawFloorDivInt"));
        assertTrue("repro must exercise integer mod lowering", compiled.contains("__wurst_rawFmodInt"));
        assertTrue("repro must exercise real mod lowering", compiled.contains("__wurst_rawFmodReal"));
    }

    /**
     * Stacktrace injection runs before Lua-native lowering. The lowering pass
     * introduces fresh calls to existing IM helpers, so those calls must carry
     * the injected stacktrace argument when the helper signature already has it.
     */
    @Test
    public void stacktracedLuaLoweringPassesHelperStacktraceArguments() {
        compileLuaWithRunArgs(
            "LuaBackendAuditTests_stacktracedLuaLoweringPassesHelperStacktraceArguments",
            new RunArgs().with("-lua", "-inline", "-localOptimizations", "-stacktraces"),
            "package Test",
            "native print(string message)",
            "native I2S(int value) returns string",
            "int array values",
            "function readValue(int index) returns int",
            "    return values[index]",
            "function join(string left, string right) returns string",
            "    return left + right",
            "init",
            "    values[1] = 7",
            "    print(join(\"value=\", I2S(readValue(1))))"
        );
    }

    private void assertHelperDefinedWhenCalled(String compiled, String helperName) {
        int definitions = countOccurrences(compiled, "function " + helperName + "(");
        int calls = countOccurrences(compiled, helperName + "(") - definitions;
        assertTrue("dangling call to " + helperName, calls == 0 || definitions == 1);
    }

    /**
     * Deferred bootstrap (global defaults, class dispatch tables, typecasting
     * maps) used to be prepended only to main(). WC3 calls config() before
     * main(), so any translated global or class metadata reachable from
     * config saw nil. Both entry points must run the bootstrap first.
     */
    @Test
    public void deferredInitRunsForConfigToo() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class C",
            "    function f() returns int",
            "        return 1",
            "int array counts",
            "init",
            "    let c = new C()",
            "    counts[3] = c.f()",
            "    if counts[3] == 1",
            "        testSuccess()"
        );
        String compiled = compiledLua("deferredInitRunsForConfigToo");
        java.util.regex.Matcher main = java.util.regex.Pattern
            .compile("function main\\(\\)\\s*\\n\\s*([A-Za-z0-9_]+)\\(").matcher(compiled);
        assertTrue("main must start with the bootstrap call", main.find());
        String bootstrapCall = main.group(1);
        assertTrue("bootstrap helper must be a __wurst function but was " + bootstrapCall,
            bootstrapCall.startsWith("__wurst"));
        java.util.regex.Matcher config = java.util.regex.Pattern
            .compile("function config\\(\\)\\s*\\n\\s*" + java.util.regex.Pattern.quote(bootstrapCall) + "\\(").matcher(compiled);
        assertTrue("config must start with the same bootstrap call", config.find());
    }

    /**
     * Instances used to get a fresh {@code {__index = Class}} metatable per
     * allocation — pure garbage. All instances of a class share one metatable.
     */
    @Test
    public void classInstancesShareOneMetatablePerClass() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Foo",
            "    int v = 1",
            "init",
            "    let a = new Foo()",
            "    let b = new Foo()",
            "    if a.v + b.v == 2 and a != b",
            "        testSuccess()"
        );
        String compiled = compiledLua("classInstancesShareOneMetatablePerClass");
        assertTrue("expected a shared per-class metatable variable",
            compiled.contains("Foo_mt = ({__index=Foo, })"));
        assertFalse("create must not allocate a metatable per instance",
            compiled.contains("setmetatable(new_inst, ({"));
    }

    /**
     * Reading a never-written array slot must not permanently store an entry
     * for it - merely probing a sparse array would otherwise grow it
     * unboundedly. Immutable (primitive) defaults are served by a dedicated
     * index function that never writes back into the array table; only
     * table-typed defaults (tuples, nested arrays) store on first read,
     * because each slot needs its own, independently mutable default value.
     */
    @Test
    public void primitiveArrayReadsDoNotMaterializeEntries() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int array counts",
            "tuple pair(int x, int y)",
            "pair array ps",
            "init",
            "    counts[5] = 2",
            "    ps[2] = pair(4, 5)",
            "    let unset = ps[7]",
            "    if counts[3] + counts[5] == 2 and ps[2].x == 4 and unset.y == 0",
            "        testSuccess()"
        );
        String compiled = compiledLua("primitiveArrayReadsDoNotMaterializeEntries");
        int fnStart = compiled.indexOf("function __wurst_arrIndex_integer(");
        assertTrue("expected a dedicated index function for int array defaults", fnStart >= 0);
        int fnEnd = compiled.indexOf("\nend", fnStart);
        assertTrue("could not find end of int array index function", fnEnd >= 0);
        assertFalse("primitive array default reads must not write back into the array table",
            compiled.substring(fnStart, fnEnd).contains("="));

        assertFalse("tuple arrays are value types and must be split into scalar arrays",
            compiled.contains("function __wurst_arrIndex("));
    }

    /**
     * Array-default infrastructure (metatable + index function) is shared
     * across every array with the same entry type, not allocated fresh per
     * array instance - mirrors the shared per-class instance metatable
     * ({@link #classInstancesShareOneMetatablePerClass}).
     */
    @Test
    public void arraysOfSameEntryTypeShareDefaultMetatable() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int array a",
            "int array b",
            "int array c",
            "init",
            "    a[1] = 1",
            "    b[1] = 2",
            "    c[1] = 3",
            "    if a[1] + b[1] + c[1] == 6 and a[9] == 0 and b[9] == 0",
            "        testSuccess()"
        );
        String compiled = compiledLua("arraysOfSameEntryTypeShareDefaultMetatable");
        assertEquals("expected exactly one shared metatable declaration for all int arrays",
            1, countOccurrences(compiled, "__wurst_arrMt_integer = "));
        assertEquals("expected exactly one shared index function for all int arrays",
            1, countOccurrences(compiled, "function __wurst_arrIndex_integer("));
        assertEquals("expected one setmetatable call per array instance",
            3, countOccurrences(compiled, "setmetatable(({}), __wurst_arrMt_integer)"));
    }

    /**
     * Handle-typed arrays (WC3 native types, not user classes) default to
     * nil, same as an untouched Lua table key - they must not pay for any
     * metatable machinery at all.
     */
    @Test
    public void handleTypedArraysAllocateNoMetatable() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "nativetype customHandle",
            "customHandle array hs",
            "init",
            "    if hs[3] == null",
            "        testSuccess()"
        );
        String compiled = compiledLua("handleTypedArraysAllocateNoMetatable");
        assertFalse("a nil-default array must not allocate any array-default metatable",
            compiled.contains("__wurst_arrMt"));
        assertFalse("a nil-default array must not allocate any array-default index function",
            compiled.contains("__wurst_arrIndex"));
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) >= 0) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    /**
     * The wc3shim's GetLocalPlayer used to return a generic enum-handle table
     * ({handleKind, value}) while the generated Player/GetPlayerId fallbacks
     * use {id = x} with their own cache — so GetPlayerId(GetLocalPlayer())
     * returned nil and Player(0) was not identical to GetLocalPlayer().
     */
    @Test
    public void localPlayerHandleIsCompatibleWithPlayerAndGetPlayerId() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "nativetype player",
            "native Player(int id) returns player",
            "native GetPlayerId(player p) returns int",
            "native GetLocalPlayer() returns player",
            "init",
            "    if GetPlayerId(GetLocalPlayer()) == 0 and Player(0) == GetLocalPlayer()",
            "        testSuccess()"
        );
    }

    /**
     * {@code goto} is a reserved word in Lua 5.3 but was missing from the
     * translator's reserved-name list, so a Wurst local named {@code goto}
     * produced {@code local goto = ...} (a Lua syntax error).
     */
    @Test
    public void gotoIsRenamed() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "init",
            "    let goto = 1",
            "    if goto == 1",
            "        testSuccess()"
        );
    }

    /**
     * The emitted runtime helpers depend on Lua standard library globals
     * (math, table, string, setmetatable, ...). User identifiers with those
     * names used to keep their name and clobber the library at map load.
     */
    @Test
    public void luaStdlibGlobalsAreNotClobbered() throws IOException {
        // package globals are package-prefixed by the IM translator, but
        // top-level functions and locals keep their plain names
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function math(int x) returns int",
            "    let table = x",
            "    return table",
            "init",
            "    if math(4) mod 3 == 1",
            "        testSuccess()"
        );
        String compiled = compiledLua("luaStdlibGlobalsAreNotClobbered");
        assertFalse("user function must not shadow Lua's math library",
            compiled.contains("function math("));
        assertFalse("user local must not shadow Lua's table library",
            compiled.contains("local table ="));
    }

    /**
     * Field names are printed verbatim as table keys / field accesses.
     * Regular class fields are scope-prefixed by the IM translator, but
     * closure-captured locals become fields with their raw source name, so a
     * captured local named after a Lua keyword (until, local, ...) used to
     * produce {@code obj.until} — a syntax error.
     */
    @Test
    public void luaKeywordFieldNamesAreRenamed() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface IntProvider",
            "    function get() returns int",
            "init",
            "    let until = 3",
            "    let local = 4",
            "    IntProvider p = () -> until + local",
            "    if p.get() == 7",
            "        testSuccess()"
        );
    }
}
