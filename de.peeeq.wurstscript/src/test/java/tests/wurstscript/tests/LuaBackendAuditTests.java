package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarargLoop;
import de.peeeq.wurstscript.jassIm.ImVarargLoopVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
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

    private String luaFunctionBody(String compiled, String functionName) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "function " + java.util.regex.Pattern.quote(functionName)
                    + "\\([^)]*\\)\\s*\\R(.*?)\\Rend",
                java.util.regex.Pattern.DOTALL)
            .matcher(compiled);
        assertTrue("expected generated Lua function " + functionName, matcher.find());
        return matcher.group(1);
    }

    private String compileOptimizedLua(String testName, String... lines) {
        RunArgs runArgs = new RunArgs().with("-lua", "-inline", "-localOptimizations");
        return compileLuaWithRunArgs(testName, runArgs, false, lines);
    }

    private String compileOptimizedLuaWithStdLib(String testName, String... lines) {
        RunArgs runArgs = new RunArgs().with("-lua", "-inline", "-localOptimizations",
            "-runcompiletimefunctions", "-lib", StdLib.getLib());
        return compileLuaWithRunArgs(testName, runArgs, true, lines);
    }

    private String compileLuaWithRunArgs(String testName, RunArgs runArgs, String... lines) {
        return compileLuaWithRunArgs(testName, runArgs, false, lines);
    }

    private String compileLuaWithRunArgs(String testName, RunArgs runArgs, boolean withStdLib, String... lines) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU(testName + ".wurst", String.join("\n", lines))), withStdLib, compiler);
        assertTrue("unexpected parse/type errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.checkProg(model);
        assertTrue("unexpected compile errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.translateProgToIm(model);
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        LuaCompilationUnit luaCode = compiler.transformProgToLua();
        compiler.getImProg().accept(new ImVarargLoop.DefaultVisitor() {
            @Override
            public void visit(ImVarargLoop loop) {
                de.peeeq.wurstscript.jassIm.Element owner = loop;
                while (owner != null && !(owner instanceof de.peeeq.wurstscript.jassIm.ImFunction)) {
                    owner = owner.getParent();
                }
                for (ImVarargLoopVar loopVar : loop.getLoopVars()) {
                    assertTrue("lowered vararg loop variable must remain attached to its declaration list: "
                            + loopVar.getVar() + " in " + owner,
                        loopVar.getVar().getParent() != null);
                }
                super.visit(loop);
            }
        });
        StringBuilder result = new StringBuilder();
        luaCode.print(result, 0);
        return result.toString();
    }

    @Test
    public void classInstancesUseRecycledIntegerIdsAndStaticFieldStorage() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int destroyed = 0",
            "class Base",
            "    int scalar",
            "    Child reference",
            "    int array[8] values",
            "    function score() returns int",
            "        return scalar",
            "    ondestroy",
            "        destroyed++",
            "class Child extends Base",
            "    int extra",
            "    override function score() returns int",
            "        return scalar + extra",
            "init",
            "    let first = new Child()",
            "    let firstId = first castTo int",
            "    first.scalar = 7",
            "    first.reference = first",
            "    first.values[3] = 99",
            "    first.extra = 5",
            "    Base polymorphic = first",
            "    Base stale = first",
            "    let dispatched = polymorphic.score()",
            "    destroy first",
            "    let destroyedIsInvalid = not (stale instanceof Child)",
            "    let second = new Child()",
            "    Base secondBase = second",
            "    if second castTo int == firstId and dispatched == 12 and destroyed == 1",
            "        and second.scalar == 0 and second.reference == null",
            "        and second.values[3] == 0 and second.extra == 0",
            "        and destroyedIsInvalid and stale == second",
            "        and secondBase instanceof Child and second.typeId == Child.typeId",
            "        testSuccess()"
        );

        String compiled = compiledLua("classInstancesUseRecycledIntegerIdsAndStaticFieldStorage");
        assertTrue("integer-ID lowering must emit the live-object class map",
            compiled.contains("__wurst_objectClass"));
        assertTrue("integer-ID lowering must emit an explicit free-ID pool",
            compiled.contains("__wurst_objectFree"));
        assertFalse("class allocation must not construct a table per instance",
            compiled.contains("local new_inst = {"));
        assertFalse("class allocation must not attach an instance metatable",
            compiled.contains("setmetatable(new_inst"));
        assertTrue("class-to-int casts must use the integer object id directly",
            compiled.contains("__wurst_classToIndex(first)"));
        assertFalse("class casts must not allocate boxed-number identity wrappers",
            compiled.contains("firstId = __wurst_objectToIndex(first)"));
        assertFalse("deallocation must preserve field values just like Jass storage",
            compiled.contains("Base_reference_storage[object] = nil"));
    }

    @Test
    public void legacyGenericHandleCastsUseObjectIndexMap() throws IOException {
        test().testLua(true).executeProg().lines(
            "type timer extends handle",
            "package Test",
            "native testSuccess()",
            "native CreateTimer() returns timer",
            "function timerToIndex(timer value) returns int",
            "    return 0",
            "function timerFromIndex(int value) returns timer",
            "    return null",
            "function toIndex<T>(T value) returns int",
            "    return value castTo int",
            "init",
            "    let value = CreateTimer()",
            "    let first = toIndex(value)",
            "    let second = toIndex(value)",
            "    if first > 0 and first == second",
            "        testSuccess()"
        );

        String compiled = compiledLua("legacyGenericHandleCastsUseObjectIndexMap");
        assertTrue(java.util.regex.Pattern.compile(
            "function toIndex\\((\\w+)\\)\\s*\\R\\s*return __wurst_objectToIndex\\(\\1\\)")
            .matcher(compiled).find());
        assertFalse(java.util.regex.Pattern.compile(
            "function toIndex\\((\\w+)\\)\\s*\\R\\s*return __wurst_classToIndex\\(\\1\\)")
            .matcher(compiled).find());
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
    public void tupleFieldReadsOnlyLoadTheSelectedStorageComponent() throws IOException {
        String[] source = {
            "package Test",
            "native testSuccess()",
            "tuple vec3(real x, real y, real z)",
            "tuple segment(vec3 start, vec3 finish)",
            "vec3 array points",
            "segment array segments",
            "int indexCalls",
            "@noinline function nextIndex() returns int",
            "    indexCalls++",
            "    return 2",
            "@noinline function readAt(int index) returns real",
            "    return points[index].z",
            "@noinline function readAtNext() returns real",
            "    return points[nextIndex()].z",
            "@noinline function readNested(int index) returns real",
            "    return segments[index].finish.y",
            "class Entity",
            "    vec3 pos",
            "@noinline function readPos(Entity entity) returns real",
            "    return entity.pos.z",
            "init",
            "    points[2] = vec3(1., 2., 3.)",
            "    segments[2] = segment(vec3(4., 5., 6.), vec3(7., 8., 9.))",
            "    let entity = new Entity()",
            "    entity.pos = vec3(4., 5., 6.)",
            "    if readAt(2) == 3. and readAtNext() == 3. and indexCalls == 1",
            "        and readNested(2) == 8. and readPos(entity) == 6.",
            "        testSuccess()"
        };
        test().testLua(true).executeProg().lines(source);

        String compiled = compileOptimizedLua(
            "tupleFieldReadsOnlyLoadTheSelectedStorageComponentOptimized", source);
        String plainArrayRead = luaFunctionBody(compiled, "readAt");
        String effectfulArrayRead = luaFunctionBody(compiled, "readAtNext");
        String nestedArrayRead = luaFunctionBody(compiled, "readNested");
        String memberRead = luaFunctionBody(compiled, "readPos");

        assertTrue(plainArrayRead.contains("points_z["));
        assertFalse(plainArrayRead.contains("points_x["));
        assertFalse(plainArrayRead.contains("points_y["));
        assertTrue(effectfulArrayRead.contains("points_z["));
        assertFalse(effectfulArrayRead.contains("points_x["));
        assertFalse(effectfulArrayRead.contains("points_y["));
        assertEquals("a selected tuple-array field must evaluate its index exactly once",
            1, effectfulArrayRead.split("nextIndex\\(", -1).length - 1);
        assertTrue(nestedArrayRead.contains("segments_finish_y["));
        assertFalse(nestedArrayRead.contains("segments_start_"));
        assertFalse(nestedArrayRead.contains("segments_finish_x["));
        assertFalse(nestedArrayRead.contains("segments_finish_z["));
        assertTrue(memberRead.contains("Entity_pos_z_storage["));
        assertFalse(memberRead.contains("Entity_pos_x_storage["));
        assertFalse(memberRead.contains("Entity_pos_y_storage["));
        assertFalse("selected tuple storage reads must not need discard helpers",
            plainArrayRead.contains("__wurst_tuple_discard_")
                || effectfulArrayRead.contains("__wurst_tuple_discard_")
                || nestedArrayRead.contains("__wurst_tuple_discard_")
                || memberRead.contains("__wurst_tuple_discard_"));
    }

    @Test
    public void tupleFieldAssignmentsOnlyTouchTheSelectedStorageComponent() throws IOException {
        String[] source = {
            "package Test",
            "native testSuccess()",
            "tuple vec3(real x, real y, real z)",
            "tuple segment(vec3 start, vec3 finish)",
            "vec3 array points",
            "segment array segments",
            "int indexCalls",
            "int trace",
            "@noinline function nextIndex() returns int",
            "    indexCalls++",
            "    trace = trace * 10 + 1",
            "    return 2",
            "@noinline function nextValue() returns real",
            "    trace = trace * 10 + 2",
            "    return 21.",
            "@noinline function setAt(int index, real value)",
            "    points[index].z = value",
            "@noinline function setAtNext()",
            "    points[nextIndex()].y = nextValue()",
            "@noinline function addAt(int index, real value)",
            "    points[index].x += value",
            "@noinline function setNested(int index, real value)",
            "    segments[index].finish.y = value",
            "@noinline function replaceAt(int index, vec3 value)",
            "    points[index] = value",
            "class Entity",
            "    vec3 pos",
            "@noinline function setPos(Entity entity, real value)",
            "    entity.pos.z = value",
            "init",
            "    points[1] = vec3(1., 2., 3.)",
            "    points[2] = vec3(4., 5., 6.)",
            "    segments[1] = segment(vec3(7., 8., 9.), vec3(10., 11., 12.))",
            "    let entity = new Entity()",
            "    entity.pos = vec3(13., 14., 15.)",
            "    setAt(1, 20.)",
            "    setAtNext()",
            "    addAt(1, 22.)",
            "    setNested(1, 23.)",
            "    setPos(entity, 24.)",
            "    replaceAt(2, vec3(25., 26., 27.))",
            "    if points[1] == vec3(23., 2., 20.) and points[2] == vec3(25., 26., 27.)",
            "        and segments[1] == segment(vec3(7., 8., 9.), vec3(10., 23., 12.))",
            "        and entity.pos == vec3(13., 14., 24.) and indexCalls == 1 and trace == 12",
            "        testSuccess()"
        };
        test().testLua(true).executeProg().lines(source);

        String compiled = compileOptimizedLua(
            "tupleFieldAssignmentsOnlyTouchTheSelectedStorageComponentOptimized", source);
        String plainWrite = luaFunctionBody(compiled, "setAt");
        String effectfulWrite = luaFunctionBody(compiled, "setAtNext");
        String readModifyWrite = luaFunctionBody(compiled, "addAt");
        String nestedWrite = luaFunctionBody(compiled, "setNested");
        String memberWrite = luaFunctionBody(compiled, "setPos");
        String wholeTupleWrite = luaFunctionBody(compiled, "replaceAt");

        assertTrue(plainWrite.contains("points_z["));
        assertFalse(plainWrite.contains("points_x["));
        assertFalse(plainWrite.contains("points_y["));
        assertTrue(effectfulWrite.contains("points_y["));
        assertFalse(effectfulWrite.contains("points_x["));
        assertFalse(effectfulWrite.contains("points_z["));
        assertEquals("a selected tuple-array field must evaluate its write index exactly once",
            1, effectfulWrite.split("nextIndex\\(", -1).length - 1);
        assertTrue(readModifyWrite.contains("points_x["));
        assertFalse(readModifyWrite.contains("points_y["));
        assertFalse(readModifyWrite.contains("points_z["));
        assertTrue(nestedWrite.contains("segments_finish_y["));
        assertFalse(nestedWrite.contains("segments_start_"));
        assertFalse(nestedWrite.contains("segments_finish_x["));
        assertFalse(nestedWrite.contains("segments_finish_z["));
        assertTrue(memberWrite.contains("Entity_pos_z_storage["));
        assertFalse(memberWrite.contains("Entity_pos_x_storage["));
        assertFalse(memberWrite.contains("Entity_pos_y_storage["));
        assertTrue("whole-tuple assignment must still write x", wholeTupleWrite.contains("points_x["));
        assertTrue("whole-tuple assignment must still write y", wholeTupleWrite.contains("points_y["));
        assertTrue("whole-tuple assignment must still write z", wholeTupleWrite.contains("points_z["));
        assertFalse("selected tuple storage writes must not need discard helpers",
            plainWrite.contains("__wurst_tuple_discard_")
                || effectfulWrite.contains("__wurst_tuple_discard_")
                || readModifyWrite.contains("__wurst_tuple_discard_")
                || nestedWrite.contains("__wurst_tuple_discard_")
                || memberWrite.contains("__wurst_tuple_discard_"));
    }

    @Test
    public void tupleMemberArrayAssignmentCapturesIndex() throws IOException {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int first, int second)",
            "constant SIZE = 10",
            "class Container",
            "    pair array[SIZE] values",
            "    int used",
            "    function add(pair value)",
            "        values[used] = value",
            "        used++",
            "init",
            "    let container = new Container()",
            "    container.add(pair(1, 2))",
            "    if container.used == 1 and container.values[0] == pair(1, 2)",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleMemberArrayAssignmentCapturesIndex");
        assertTrue("tuple member-array index must be captured into a declared Lua local",
            compiled.contains("local tuple_lvalue_index = 0"));
        assertTrue("tuple member-array index capture must survive dead-store elimination",
            compiled.contains("tuple_lvalue_index = Container_used_storage[this]"));
    }

    @Test
    public void optimizedTupleCommonPathIsOnlyScalarCode() {
        String compiled = compileOptimizedLua(
            "optimizedTupleCommonPathIsOnlyScalarCode",
            "package Test",
            "tuple vec2(real x, real y)",
            "native consume(real x, real y)",
            "@noinline function add(vec2 left, vec2 right) returns vec2",
            "    return vec2(left.x + right.x, left.y + right.y)",
            "init",
            "    let result = add(vec2(1., 2.), vec2(3., 4.))",
            "    consume(result.x, result.y)"
        );

        java.util.regex.Matcher add = java.util.regex.Pattern
            .compile("function add\\(([^)]*)\\)\\s*\\n(.*?)\\nend", java.util.regex.Pattern.DOTALL)
            .matcher(compiled);
        assertTrue("optimized tuple function must remain for shape inspection", add.find());
        assertEquals("both vec2 parameters must unfold into four scalar parameters",
            4, add.group(1).split(",").length);
        assertFalse("the optimized tuple function body must not allocate a Lua table",
            add.group(2).contains("{"));
        assertFalse(compiled.contains("tupleCopy"));
        assertFalse(compiled.contains("tupleEquals"));
    }

    @Test
    public void optimizedTupleVarargLoopUsesAttachedScalarLocals() {
        String compiled = compileOptimizedLua(
            "optimizedTupleVarargLoopUsesAttachedScalarLocals",
            "package Test",
            "nativetype framehandle extends handle",
            "native makeFrame() returns framehandle",
            "tuple handles(framehandle first, framehandle second)",
            "class Bag<T:>",
            "    private static T array store",
            "    int size = 0",
            "    @noinline function add(vararg T elems)",
            "        for elem in elems",
            "            store[size] = elem",
            "            size++",
            "init",
            "    let bag = new Bag<handles>()",
            "    bag.add(handles(makeFrame(), makeFrame()))"
        );
        assertFalse("a static-arity vararg call must not pack a table on Lua", compiled.contains("table.pack(...)"));
        assertFalse(compiled.contains("tupleCopy"));
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
    public void randomizedTupleEvaluationMatchesInterpreterAndLua() throws IOException {
        Random random = new Random(0x0D1FF3A7L);
        List<String> source = new ArrayList<>();
        source.add("package Test");
        source.add("native testSuccess()");
        source.add("tuple pair(int x, int y)");
        source.add("tuple nested(pair left, pair right)");
        source.add("int trace");
        source.add("int calls");
        source.add("int mutable");
        source.add("class Holder");
        source.add("    pair value");
        source.add("Holder current");
        source.add("Holder replacement");
        source.add("int currentIndex");
        source.add("pair array values");
        source.add("function mark(int value) returns int");
        source.add("    trace = trace * 37 + value");
        source.add("    return value");
        source.add("@noinline function produce(int seed) returns pair");
        source.add("    calls++");
        source.add("    return pair(mark(seed), mark(seed + 1))");
        source.add("@noinline function recursive(int seed) returns pair");
        source.add("    if seed == 0");
        source.add("        return pair(mark(7), recursive(1).x)");
        source.add("    return pair(mark(seed), mark(seed + 10))");
        source.add("function retarget(int x, int y) returns pair");
        source.add("    current = replacement");
        source.add("    currentIndex = 2");
        source.add("    return pair(mark(x), mark(y))");
        source.add("function mutatingPair(int replacementValue, int x, int y) returns pair");
        source.add("    mutable = replacementValue");
        source.add("    return pair(mark(x), mark(y))");
        source.add("function scorePairs(pair first, pair second) returns int");
        source.add("    return first.x * 41 + first.y * 43 + second.x * 47 + second.y * 53");
        source.add("init");
        source.add("    int checksum = 0");

        int expected = 0;
        for (int i = 0; i < 96; i++) {
            int a = random.nextInt(9) + 1;
            int b = random.nextInt(9) + 1;
            int c = random.nextInt(9) + 1;
            int d = random.nextInt(9) + 1;
            switch (random.nextInt(8)) {
                case 0 -> {
                    boolean selectFirst = random.nextBoolean();
                    source.add("    trace = 0");
                    source.add("    let selected" + i + " = pair(mark(" + a + "), mark(" + b + "))."
                        + (selectFirst ? "x" : "y"));
                    source.add("    checksum += trace + selected" + i + " * 13");
                    expected += a * 37 + b + (selectFirst ? a : b) * 13;
                }
                case 1 -> {
                    int selection = random.nextInt(4);
                    String[] paths = {"left.x", "left.y", "right.x", "right.y"};
                    int[] values = {a, b, c, d};
                    source.add("    trace = 0");
                    source.add("    let selected" + i + " = nested(pair(mark(" + a + "), mark(" + b
                        + ")), pair(mark(" + c + "), mark(" + d + ")))." + paths[selection]);
                    source.add("    checksum += trace + selected" + i + " * 17");
                    expected += (((a * 37 + b) * 37 + c) * 37 + d) + values[selection] * 17;
                }
                case 2 -> {
                    source.add("    trace = 0");
                    source.add("    calls = 0");
                    source.add("    let selected" + i + " = produce(" + a + ").y");
                    source.add("    checksum += trace + selected" + i + " * 19 + calls * 23");
                    expected += a * 37 + (a + 1) + (a + 1) * 19 + 23;
                }
                case 3 -> {
                    source.add("    trace = 0");
                    source.add("    calls = 0");
                    source.add("    if produce(" + a + ") != produce(" + b + ")");
                    source.add("        checksum += " + (a == b ? 29 : 31));
                    source.add("    else");
                    source.add("        checksum += " + (a == b ? 31 : 29));
                    source.add("    checksum += trace + calls * 37");
                    expected += 31
                        + (((a * 37 + (a + 1)) * 37 + b) * 37 + (b + 1)) + 2 * 37;
                }
                case 4 -> {
                    source.add("    let original" + i + " = new Holder()");
                    source.add("    replacement = new Holder()");
                    source.add("    current = original" + i);
                    source.add("    trace = 0");
                    source.add("    current.value = retarget(" + a + ", " + b + ")");
                    source.add("    checksum += original" + i + ".value.x * 41 + original" + i
                        + ".value.y * 43 + replacement.value.x + trace");
                    expected += a * 41 + b * 43 + a * 37 + b;
                }
                case 5 -> {
                    source.add("    values[1] = pair(0, 0)");
                    source.add("    values[2] = pair(0, 0)");
                    source.add("    replacement = new Holder()");
                    source.add("    currentIndex = 1");
                    source.add("    trace = 0");
                    source.add("    values[currentIndex] = retarget(" + a + ", " + b + ")");
                    source.add("    checksum += values[1].x * 47 + values[1].y * 53 + values[2].x + trace");
                    expected += a * 47 + b * 53 + a * 37 + b;
                }
                case 6 -> {
                    source.add("    mutable = " + a);
                    source.add("    trace = 0");
                    source.add("    if pair(mutable, mark(" + a + ")) == mutatingPair("
                        + c + ", " + a + ", " + a + ")");
                    source.add("        checksum += 71");
                    source.add("    else");
                    source.add("        checksum += 67");
                    source.add("    checksum += trace + mutable * 73");
                    expected += 71 + ((a * 37 + a) * 37 + a) + c * 73;
                }
                case 7 -> {
                    source.add("    mutable = " + a);
                    source.add("    trace = 0");
                    source.add("    checksum += scorePairs(pair(mutable, mark(" + a
                        + ")), mutatingPair(" + c + ", " + b + ", " + d + "))");
                    source.add("    checksum += trace + mutable * 79");
                    expected += a * 41 + a * 43 + b * 47 + d * 53
                        + ((a * 37 + b) * 37 + d) + c * 79;
                }
            }
        }
        source.add("    trace = 0");
        source.add("    let recursiveResult = recursive(0)");
        source.add("    checksum += recursiveResult.x * 59 + recursiveResult.y * 61 + trace");
        expected += 7 * 59 + 61 + ((7 * 37 + 1) * 37 + 11);
        source.add("    if checksum == " + expected);
        source.add("        testSuccess()");

        // executeProg validates the source-level IM interpreter; testLua additionally runs the
        // scalarized output in Lua 5.3, making the generated program a deterministic differential test.
        test().testLua(true).executeProg().lines(source.toArray(new String[0]));
        String compiled = compiledLua("randomizedTupleEvaluationMatchesInterpreterAndLua");
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
    public void tupleSpecializedClassBindsNongenericInterfaceDispatch() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "interface Producer",
            "    function produce() returns pair",
            "class GenericProducer<T:> implements Producer",
            "    pair stored",
            "    construct(pair value)",
            "        stored = value",
            "    function produce() returns pair",
            "        return stored",
            "init",
            "    Producer producer = new GenericProducer<pair>(pair(4, 5))",
            "    if producer.produce() == pair(4, 5)",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleSpecializedClassBindsNongenericInterfaceDispatch");
        assertTrue(compiled.contains("GenericProducer_specialized"));
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleSpecializedClassPreservesRuntimeTypeOperations() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T value",
            "    construct(T initial)",
            "        value = initial",
            "init",
            "    Marker box = new Box<pair>(pair(6, 7))",
            "    Marker plain = new Box<int>(1)",
            "    if box instanceof Box and box.typeId == plain.typeId",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleSpecializedClassPreservesRuntimeTypeOperations");
        assertTrue(compiled.contains("Box_specialized"));
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleSpecializedClassPreservesGenericInstanceofIdentity() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T value",
            "    construct(T initial)",
            "        value = initial",
            "    function get() returns T",
            "        return value",
            "function isIntBox<T:>(Marker value) returns bool",
            "    return value instanceof Box<T>",
            "class Child extends Box<int>",
            "    construct(int initial)",
            "        super(initial)",
            "class GenericChild<U:> extends Box<U>",
            "    construct(U initial)",
            "        super(initial)",
            "class GrandChild extends GenericChild<int>",
            "    construct(int initial)",
            "        super(initial)",
            "init",
            "    Marker tupleBox = new Box<pair>(pair(6, 7))",
            "    Box<int> intBox = new Box<int>(1)",
            "    Marker intMarker = intBox",
            "    Marker child = new Child(2)",
            "    Marker genericChild = new GenericChild<int>(3)",
            "    Marker grandChild = new GrandChild(4)",
            "    if tupleBox instanceof Box<pair>",
            "        and not (tupleBox instanceof Box<int>)",
            "        and intMarker instanceof Box<int>",
            "        and not (intMarker instanceof Box<pair>)",
            "        and intBox.get() == 1",
            "        and intBox.value == 1",
            "        and isIntBox<int>(intMarker)",
            "        and not isIntBox<int>(tupleBox)",
            "        and child instanceof Box<int>",
            "        and not (child instanceof Box<pair>)",
            "        and genericChild instanceof Box<int>",
            "        and not (genericChild instanceof Box<pair>)",
            "        and grandChild instanceof Box<int>",
            "        and not (grandChild instanceof Box<pair>)",
            "        testSuccess()"
        );
    }

    @Test
    public void tupleSpecializedClassRetainsNominalMetadataWhenItIsTheOnlyReachableForm() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T value",
            "    construct(T initial)",
            "        value = initial",
            "init",
            "    Marker box = new Box<pair>(pair(6, 7))",
            "    if box != null",
            "        testSuccess()"
        );

        String compiled = compiledLua(
            "tupleSpecializedClassRetainsNominalMetadataWhenItIsTheOnlyReachableForm");
        assertTrue(compiled.contains("Box_specialized"));
        assertFalse(compiled.contains("tupleCopy"));
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
    public void tupleReturningCallArgumentsAreStagedInOrder() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int trace",
            "@noinline function produce(int seed) returns pair",
            "    trace = trace * 10 + seed",
            "    return pair(seed, seed + 10)",
            "@noinline function consume(pair first, int middle, pair second) returns bool",
            "    return first == pair(1, 11) and middle == 7 and second == pair(2, 12)",
            "function mark(int value) returns int",
            "    trace = trace * 10 + value",
            "    return value",
            "init",
            "    if consume(produce(1), mark(7), produce(2)) and trace == 172",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleReturningCallArgumentsAreStagedInOrder");
        assertTrue("tuple arguments must be materialized before the scalar call",
            compiled.contains("tuple_argument"));
        assertFalse(compiled.contains("tupleCopy"));
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
    public void tupleReturnStagesComponentsBeforeRecursiveSlotWrites() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "class Producer",
            "    @noinline function produce(int seed) returns pair",
            "        if seed == 0",
            "            return pair(7, produce(1).x)",
            "        return pair(seed, 99)",
            "init",
            "    let result = new Producer().produce(0)",
            "    if result == pair(7, 1)",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleReturnStagesComponentsBeforeRecursiveSlotWrites");
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleBundleCapturesEarlierReadsBeforeLaterPreludes() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int mutable",
            "@noinline function mutate() returns pair",
            "    mutable = 9",
            "    return pair(5, 2)",
            "@noinline function makeResult() returns pair",
            "    mutable = 7",
            "    return pair(mutable, mutate().y)",
            "init",
            "    mutable = 5",
            "    pair assigned = pair(mutable, mutate().y)",
            "    let returned = makeResult()",
            "    mutable = 5",
            "    let compared = pair(mutable, 2) == mutate()",
            "    if assigned == pair(5, 2) and returned == pair(7, 2)",
            "        and compared and mutable == 9",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleBundleCapturesEarlierReadsBeforeLaterPreludes");
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleSelectionPreservesLeftToRightEvaluation() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int trace",
            "function mark(int value) returns int",
            "    trace = trace * 10 + value",
            "    return value",
            "init",
            "    let selected = pair(mark(1), mark(2)).x",
            "    if selected == 1 and trace == 12",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleSelectionPreservesLeftToRightEvaluation");
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void unusedTotalTupleComponentDoesNotSurviveLuaLowering() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "function selectFirst(int left, int right) returns int",
            "    return pair(left + 1, right + 2).x",
            "init",
            "    if selectFirst(4, 10) == 5",
            "        testSuccess()"
        );

        String compiled = compiledLua("unusedTotalTupleComponentDoesNotSurviveLuaLowering");
        assertFalse("unused total tuple components should not require a Lua discard call",
            compiled.contains("__wurst_tuple_discard_"));
        assertFalse("unused total tuple components should not be evaluated",
            compiled.contains("right + 2"));
    }

    @Test
    public void unreadTupleReturnSlotsAreRemoved() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "@noinline function makePair(int left, int right) returns pair",
            "    return pair(left + 1, right + 2)",
            "init",
            "    if makePair(4, 10).x == 5",
            "        testSuccess()"
        );

        String compiled = compiledLua("unreadTupleReturnSlotsAreRemoved");
        assertFalse("unread scalar return slots should be removed after tuple lowering",
            compiled.contains("makePair_return_y"));
        assertFalse("the producer for an unread scalar return slot should be removed",
            compiled.contains("right + 2"));
    }

    @Test
    public void discardedTupleComponentsThatCanFailAreStillEvaluated() {
        String compiled = compileLuaWithRunArgs(
            "discardedTupleComponentsThatCanFailAreStillEvaluated",
            new RunArgs().with("-lua"),
            "package Test",
            "tuple pair(int x, int y)",
            "class Box",
            "    int value",
            "Box nullable",
            "init",
            "    let selected = pair(1, nullable.value).x"
        );

        assertTrue("discarded member access must still be evaluated so null access can fail",
            java.util.regex.Pattern.compile(
                "__wurst_tuple_discard_\\d+\\([^\\n]*Box_value_storage\\[[^]]*nullable]\\)")
                .matcher(compiled).find());
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleComparisonEagerlyEvaluatesPotentiallyFailingComponents() {
        String compiled = compileLuaWithRunArgs(
            "tupleComparisonEagerlyEvaluatesPotentiallyFailingComponents",
            new RunArgs().with("-lua"),
            "package Test",
            "tuple pair(int x, int y)",
            "class Box",
            "    int value",
            "Box nullable",
            "init",
            "    let equal = pair(1, nullable.value) == pair(2, 0)"
        );

        assertTrue("comparison operands must cross the eager-evaluation barrier before and/or",
            java.util.regex.Pattern.compile(
                "__wurst_tuple_discard_\\d+\\([^\\n]*tuple_compare[^\\n]*\\)")
                .matcher(compiled).find());
        assertFalse(compiled.contains("tupleCopy"));
        assertFalse(compiled.contains("tupleEquals"));
    }

    @Test
    public void tupleAssignmentCapturesLvalueBeforeRhs() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "class Holder",
            "    pair value",
            "Holder current",
            "Holder replacement",
            "int currentIndex = 1",
            "pair array values",
            "function changeTargets() returns pair",
            "    current = replacement",
            "    currentIndex = 2",
            "    return pair(3, 4)",
            "init",
            "    let original = new Holder()",
            "    replacement = new Holder()",
            "    current = original",
            "    current.value = changeTargets()",
            "    current = original",
            "    currentIndex = 1",
            "    values[currentIndex] = changeTargets()",
            "    if original.value == pair(3, 4) and replacement.value == pair(0, 0)",
            "        and values[1] == pair(3, 4) and values[2] == pair(0, 0)",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleAssignmentCapturesLvalueBeforeRhs");
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleFieldReadCapturesReceiverBeforeEffectfulIndex() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "class Holder",
            "    pair array[8] values",
            "Holder current",
            "Holder replacement",
            "function retarget() returns int",
            "    current = replacement",
            "    return 1",
            "init",
            "    let original = new Holder()",
            "    replacement = new Holder()",
            "    original.values[1] = pair(3, 4)",
            "    replacement.values[1] = pair(8, 9)",
            "    current = original",
            "    let result = current.values[retarget()]",
            "    if result == pair(3, 4) and current == replacement",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleFieldReadCapturesReceiverBeforeEffectfulIndex");
        java.util.regex.Matcher capture = java.util.regex.Pattern.compile(
            "(tupleReceiver\\w*) = Test_current\\s+(tupleIndex\\w*) = retarget\\(\\)"
                + "\\s+[^\\n]*_values_x_storage\\[\\1]\\[\\2]")
            .matcher(compiled);
        assertTrue("receiver must be captured before the index retargets it", capture.find());
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleSpecializationPreservesExplicitGenericStaticOwner() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "class Box<T:>",
            "    static int counter",
            "    static function setCounter(int value)",
            "        counter = value",
            "    static function incrementCounter()",
            "        counter++",
            "    static function getCounter() returns int",
            "        return counter",
            "function touch<T:>(T value)",
            "    Box<int>.incrementCounter()",
            "init",
            "    Box<int>.setCounter(10)",
            "    Box<pair>.setCounter(100)",
            "    touch<pair>(pair(1, 2))",
            "    if Box<int>.getCounter() == 11 and Box<pair>.getCounter() == 100",
            "        testSuccess()"
        );

        String compiled = compiledLua("tupleSpecializationPreservesExplicitGenericStaticOwner");
        assertFalse(compiled.contains("tupleCopy"));
    }

    @Test
    public void tupleSpecializedStaticsEmitDeterministically() {
        String[] source = {
            "package Test",
            "tuple pair(int x, int y)",
            "class Box<T:>",
            "    static T first",
            "    static T second",
            "    static T third",
            "    static function set(T a, T b, T c)",
            "        first = a",
            "        second = b",
            "        third = c",
            "init",
            "    Box<pair>.set(pair(1, 2), pair(3, 4), pair(5, 6))"
        };
        RunArgs runArgs = new RunArgs().with("-lua");
        String first = compileLuaWithRunArgs("tupleSpecializedStaticsEmitDeterministically1",
            runArgs, source);
        String second = compileLuaWithRunArgs("tupleSpecializedStaticsEmitDeterministically2",
            runArgs, source);
        assertEquals("tuple-specialized statics must emit byte-identically", first, second);
    }

    @Test
    public void tupleSpecializedStaticInitializerRunsOnceWithoutErasedInstantiation() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    static function get() returns int",
            "        return value",
            "init",
            "    if Box<pair>.get() == 1 and bumps == 1",
            "        testSuccess()"
        );

        String compiled = compiledLua(
            "tupleSpecializedStaticInitializerRunsOnceWithoutErasedInstantiation");
        assertEquals("only the live tuple instantiation may call the static initializer",
            2, countOccurrences(compiled, "bump()")); // one function declaration plus one call
    }

    @Test
    public void tupleSpecializedTypedLocalDoesNotRootErasedInitializer() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    construct()",
            "init",
            "    let box = new Box<pair>()",
            "    if bumps == 1",
            "        testSuccess()"
        );

        String compiled = compiledLua(
            "tupleSpecializedTypedLocalDoesNotRootErasedInitializer");
        assertEquals("a tuple-specialized local type must not retain the erased initializer",
            2, countOccurrences(compiled, "bump()")); // one function declaration plus one call
    }

    @Test
    public void tupleSpecializedStaticKeepsAllLiveInitializers() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    static function get() returns int",
            "        return value",
            "init",
            "    if Box<int>.get() == 1 and Box<pair>.get() == 2 and bumps == 2",
            "        testSuccess()"
        );
    }

    @Test
    public void tupleSpecializedStaticKeepsInitializerForConstructedErasedClass() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    construct()",
            "    static function get() returns int",
            "        return value",
            "init",
            "    new Box<int>()",
            "    new Box<pair>()",
            "    if Box<int>.get() == 1 and Box<pair>.get() == 2 and bumps == 2",
            "        testSuccess()"
        );
    }

    @Test
    public void tupleSpecializedInterfaceDispatchDoesNotRootErasedStaticInitializer() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "interface Reader",
            "    function read() returns int",
            "class Box<T:> implements Reader",
            "    static int value = bump()",
            "    construct()",
            "    function read() returns int",
            "        return value",
            "init",
            "    Reader reader = new Box<pair>()",
            "    if reader.read() == 1 and bumps == 1",
            "        testSuccess()"
        );
    }

    @Test
    public void nestedConcreteGenericStaticStorageCompilesInLua() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "class List<T:>",
            "    static T array store",
            "    int size",
            "    construct()",
            "    function add(T value)",
            "        store[size] = value",
            "        size++",
            "    function get(int index) returns T",
            "        return store[index]",
            "class Item",
            "    int value",
            "    construct(int value)",
            "        this.value = value",
            "init",
            "    let nested = new List<List<Item>>()",
            "    let inner = new List<Item>()",
            "    nested.add(inner)",
            "    inner.add(new Item(7))",
            "    let pairs = new List<pair>()",
            "    pairs.add(pair(2, 3))",
            "    if nested.get(0) == inner and inner.get(0).value == 7 and pairs.get(0).x == 2",
            "        testSuccess()"
        );

        String compiled = compiledLua("nestedConcreteGenericStaticStorageCompilesInLua");
        java.util.regex.Matcher storageDeclarations = java.util.regex.Pattern
            .compile("(?m)^(List_store\\S*) = nil$")
            .matcher(compiled);
        List<String> storageNames = new ArrayList<>();
        while (storageDeclarations.find()) {
            storageNames.add(storageDeclarations.group(1));
        }
        assertEquals("each concrete List instantiation needs independent static storage",
            3, storageNames.size());
        assertEquals("each structural List specialization must emit one storage slot",
            3L, storageNames.stream().distinct().count());
    }

    @Test
    public void genericStaticsAreIndependentWithoutTupleInstantiation() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Slot<T:>",
            "    static T value",
            "    static function set(T newValue)",
            "        value = newValue",
            "    static function get() returns T",
            "        return value",
            "init",
            "    Slot<int>.set(7)",
            "    Slot<string>.set(\"ok\")",
            "    if Slot<int>.get() == 7 and Slot<string>.get() == \"ok\"",
            "        testSuccess()"
        );

        String compiled = compiledLua("genericStaticsAreIndependentWithoutTupleInstantiation");
        java.util.regex.Matcher storageDeclarations = java.util.regex.Pattern
            .compile("(?m)^Slot_value_\\S* = nil$")
            .matcher(compiled);
        int storages = 0;
        while (storageDeclarations.find()) {
            storages++;
        }
        assertEquals("each concrete Slot instantiation needs its own static",
            2, storages);
    }

    @Test
    public void detachedGenericStaticInitializerRemainsMetadataOnly() {
        RunArgs runArgs = new RunArgs().with("-lua");
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(Collections.emptyList(), Collections.singletonList(new CU(
            "detachedGenericStaticInitializerRemainsMetadataOnly.wurst", String.join("\n",
                "package Test",
                "native testSuccess()",
                "class Slot<T:>",
                "    static T value",
                "    static function set(T newValue)",
                "        value = newValue",
                "    static function get() returns T",
                "        return value",
                "init",
                "    Slot<int>.set(7)",
                "    Slot<string>.set(\"ok\")",
                "    if Slot<int>.get() == 7 and Slot<string>.get() == \"ok\"",
                "        testSuccess()"
            ))), false, compiler);
        assertTrue("unexpected parse/type errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.checkProg(model);
        assertTrue("unexpected compile errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        ImProg prog = compiler.translateProgToIm(model);
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);

        ImVar genericStatic = prog.getGlobals().stream()
            .filter(global -> global.getName().contains("value"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected the translated Slot.value global"));
        ImSet detached = JassIm.ImSet(genericStatic.attrTrace(), JassIm.ImVarAccess(genericStatic),
            ImHelper.nullExpr());
        prog.getGlobalInits().put(genericStatic, Collections.singletonList(detached));
        assertTrue("default initializer must remain detached metadata", detached.getParent() == null);

        LuaCompilationUnit luaCode = compiler.transformProgToLua();
        StringBuilder output = new StringBuilder();
        luaCode.print(output, 0);
        java.util.regex.Matcher declarations = java.util.regex.Pattern
            .compile("(?m)^Slot_value_\\S* = nil$")
            .matcher(output);
        int storages = 0;
        while (declarations.find()) {
            storages++;
        }
        assertEquals("both concrete static specializations must survive detached initialization",
            2, storages);
    }

    @Test
    public void constructedErasedInstantiationDoesNotDuplicateSpecializedStaticInitializer() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    construct()",
            "    static function get() returns int",
            "        return value",
            "init",
            "    new Box<int>()",
            "    if Box<int>.get() == 1 and bumps == 1",
            "        testSuccess()"
        );
    }

    @Test
    public void eachConstructedErasedInstantiationGetsItsOwnStaticInitializer() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    construct()",
            "    static function get() returns int",
            "        return value",
            "init",
            "    new Box<string>()",
            "    new Box<real>()",
            "    if Box<string>.get() == 1 and Box<real>.get() == 2 and Box<int>.get() == 3 and bumps == 3",
            "        testSuccess()"
        );
    }

    @Test
    public void genericFactoryAllocationSpecializesStaticOwningClass() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    construct()",
            "    static function get() returns int",
            "        return value",
            "function make<T:>() returns Box<T>",
            "    return new Box<T>()",
            "function forward<T:>() returns Box<T>",
            "    return make<T>()",
            "class Maker<T:>",
            "    construct()",
            "    function makeBox() returns Box<T>",
            "        return new Box<T>()",
            "init",
            "    let first = forward<int>()",
            "    let second = forward<string>()",
            "    let third = new Maker<real>().makeBox()",
            "    if first != null and second != null and third != null",
            "        and Box<int>.get() == 1 and Box<string>.get() == 2",
            "        and Box<real>.get() == 3 and bumps == 3",
            "        testSuccess()"
        );

        String compiled = compiledLua("genericFactoryAllocationSpecializesStaticOwningClass");
        assertEquals("the shared constructor must allocate ordinary objects on the erased Lua class",
            1, countOccurrences(compiled, "= Box:create()"));
        assertFalse("static specialization must not create specialized object classes",
            java.util.regex.Pattern.compile("(?m)^Box_specialized\\S* = \\(\\{\\}\\)$")
                .matcher(compiled).find());
    }

    @Test
    public void inheritedGenericStaticUsesDeclaringOwnerSpecialization() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Base<T:>",
            "    static T value",
            "class Child<Unused:, T:> extends Base<T>",
            "    construct()",
            "    function set(T newValue)",
            "        value = newValue",
            "    function get() returns T",
            "        return value",
            "init",
            "    let ints = new Child<real, int>()",
            "    let strings = new Child<int, string>()",
            "    ints.set(7)",
            "    strings.set(\"ok\")",
            "    if ints.get() == 7 and strings.get() == \"ok\"",
            "        testSuccess()"
        );

        String compiled = compiledLua("inheritedGenericStaticUsesDeclaringOwnerSpecialization");
        java.util.regex.Matcher declarations = java.util.regex.Pattern
            .compile("(?m)^Base_value_\\S* = nil$").matcher(compiled);
        int storages = 0;
        while (declarations.find()) {
            storages++;
        }
        assertEquals("each inherited Base<T> static needs independent storage", 2, storages);
    }

    @Test
    public void constructedSubclassInitializesInheritedGenericStatic() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Base<T:>",
            "    static int value = bump()",
            "class Child<T:> extends Base<T>",
            "    construct()",
            "init",
            "    new Child<int>()",
            "    new Child<string>()",
            "    if bumps == 2",
            "        testSuccess()"
        );
    }

    @Test
    public void fixedConcreteConstructionDoesNotSpecializeUnrelatedGenericCaller() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    construct()",
            "    static function get() returns int",
            "        return value",
            "function helper<T:>() returns Box<int>",
            "    return new Box<int>()",
            "init",
            "    let first = helper<string>()",
            "    let second = helper<real>()",
            "    if first != null and second != null and Box<string>.get() == 2 and bumps == 2",
            "        testSuccess()"
        );

        String compiled = compiledLua("fixedConcreteConstructionDoesNotSpecializeUnrelatedGenericCaller");
        assertFalse("fixed Box<int> construction must not clone helper<T>",
            compiled.contains("helper_specialized"));
    }

    @Test
    public void inheritedGenericStaticInitializerUsesDeclaringOwnerMapping() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Base<T:>",
            "    static int serial = bump()",
            "class Child<Unused:, T:> extends Base<T>",
            "    static int copied = serial",
            "    static function get() returns int",
            "        return copied",
            "init",
            "    if Child<real, int>.get() == 1 and Child<int, string>.get() == 2",
            "        and bumps == 2",
            "        testSuccess()"
        );
    }

    @Test
    public void fixedAllocationInsideErasedGenericMethodIsRegistered() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    construct()",
            "    static function get() returns int",
            "        return value",
            "class Factory<T:>",
            "    construct()",
            "    function make() returns Box<int>",
            "        return new Box<int>()",
            "init",
            "    let factory = new Factory<real>()",
            "    let made = factory.make()",
            "    let fresh = new Factory<string>().make()",
            "    if made != null and fresh != null and Box<string>.get() == 2 and bumps == 2",
            "        testSuccess()"
        );

        String compiled = compiledLua("fixedAllocationInsideErasedGenericMethodIsRegistered");
        assertFalse("fixed generic method body must not be cloned for its class argument",
            compiled.contains("Factory_make_specialized"));
    }

    @Test
    public void fixedStaticCalleeDoesNotSpecializeUnrelatedGenericCaller() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int value = bump()",
            "    static function get() returns int",
            "        return value",
            "function helper<T:>() returns int",
            "    return Box<int>.get()",
            "init",
            "    if helper<string>() == 1 and helper<real>() == 1",
            "        and Box<string>.get() == 2 and bumps == 2",
            "        testSuccess()"
        );

        String compiled = compiledLua("fixedStaticCalleeDoesNotSpecializeUnrelatedGenericCaller");
        assertFalse("fixed Box<int> static call must not clone helper<T>",
            compiled.contains("helper_specialized"));
    }

    @Test
    public void randomizedNestedGenericTupleClassShapesMatchAllBackends() {
        record Shape(String type, String value, int constructions) {}

        Random random = new Random(0x6E3571A9L);
        List<String> declarations = new ArrayList<>();
        List<Shape> shapes = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            int first = random.nextInt(17) + 1;
            int second = random.nextInt(17) + 1;
            Shape shape = i % 2 == 0
                ? new Shape("int", Integer.toString(first), 0)
                : new Shape("pair", "pair(" + first + ", " + second + ")",
                    0);
            int depth = 2 + random.nextInt(3);
            for (int d = 0; d < depth; d++) {
                switch ((i + d + random.nextInt(3)) % 3) {
                    case 0 -> shape = new Shape("Box<" + shape.type() + ">",
                        "new Box<" + shape.type() + ">(" + shape.value() + ")",
                        shape.constructions() + 1);
                    case 1 -> shape = new Shape("Child<" + shape.type() + ">",
                        "new Child<" + shape.type() + ">(" + shape.value() + ")",
                        shape.constructions() + 1);
                    case 2 -> {
                        String tupleName = "Wrapped" + i + "_" + d;
                        int tag = random.nextInt(11) + 1;
                        declarations.add("tuple " + tupleName + "(" + shape.type()
                            + " value, int tag)");
                        shape = new Shape(tupleName,
                            tupleName + "(" + shape.value() + ", " + tag + ")",
                            shape.constructions());
                    }
                }
            }
            shapes.add(shape);
        }

        List<String> source = new ArrayList<>();
        source.add("package Test");
        source.add("native testSuccess()");
        source.add("tuple pair(int x, int y)");
        source.add("int constructions");
        source.add("int writes");
        source.add("interface Marker<T:>");
        source.add("class Box<T:> implements Marker<T>");
        source.add("    T value");
        source.add("    construct(T value)");
        source.add("        this.value = value");
        source.add("        constructions++");
        source.add("class Child<T:> extends Box<T>");
        source.add("    construct(T value)");
        source.add("        super(value)");
        source.add("class Vault<T:>");
        source.add("    static T value");
        source.add("    static function set(T newValue)");
        source.add("        value = newValue");
        source.add("        writes++");
        source.addAll(declarations);
        source.add("init");
        int expectedConstructions = 0;
        for (Shape shape : shapes) {
            source.add("    Vault<" + shape.type() + ">.set(" + shape.value() + ")");
            expectedConstructions += shape.constructions();
        }
        source.add("    if writes == " + shapes.size()
            + " and constructions == " + expectedConstructions);
        source.add("        testSuccess()");

        test().testLua(true).executeProg().lines(source.toArray(new String[0]));
    }

    @Test
    public void randomizedClassInterfaceModuleDispatchMatchesAllBackends() {
        Random random = new Random(0xD15A7C4L);
        List<String> source = new ArrayList<>();
        Collections.addAll(source,
            "package Test",
            "native testSuccess()",
            "int destroyed",
            "interface Primary",
            "    function score() returns int",
            "interface Secondary",
            "    function bonus() returns int",
            "module Payload",
            "    int moduleValue",
            "    function payload() returns int",
            "        return moduleValue * 3",
            "    ondestroy",
            "        destroyed++",
            "class Root implements Primary",
            "    use Payload",
            "    int base",
            "    construct(int base)",
            "        this.base = base",
            "        moduleValue = base + 1",
            "    override function score() returns int",
            "        return base + payload()",
            "class Alpha extends Root implements Secondary",
            "    construct(int base)",
            "        super(base)",
            "    override function score() returns int",
            "        return super.score() + 11",
            "    override function bonus() returns int",
            "        return base * 5 + 1",
            "class AlphaLeaf extends Alpha",
            "    construct(int base)",
            "        super(base)",
            "    override function score() returns int",
            "        return super.score() * 2",
            "    override function bonus() returns int",
            "        return super.bonus() + 5",
            "class Beta extends Root implements Secondary",
            "    construct(int base)",
            "        super(base)",
            "    override function score() returns int",
            "        return super.score() - 7",
            "    override function bonus() returns int",
            "        return base * 7 + 2",
            "module ScoreContract",
            "    abstract function score() returns int",
            "module StandaloneScore",
            "    use ScoreContract",
            "    use Payload",
            "    override function score() returns int",
            "        return moduleValue * 9 + 4",
            "class ModuleOnly implements Primary",
            "    use StandaloneScore",
            "    construct(int base)",
            "        moduleValue = base",
            "function viaPrimary(Primary value) returns int",
            "    return value.score()",
            "function viaSecondary(Secondary value) returns int",
            "    return value.bonus()",
            "init",
            "    int checksum = 0");

        int expected = 0;
        int objectCount = 40;
        for (int i = 0; i < objectCount; i++) {
            int value = random.nextInt(30) + 1;
            int kind = random.nextInt(5);
            String className;
            int score;
            Integer bonus = null;
            switch (kind) {
                case 0 -> {
                    className = "Root";
                    score = 4 * value + 3;
                }
                case 1 -> {
                    className = "Alpha";
                    score = 4 * value + 14;
                    bonus = value * 5 + 1;
                }
                case 2 -> {
                    className = "AlphaLeaf";
                    score = (4 * value + 14) * 2;
                    bonus = value * 5 + 6;
                }
                case 3 -> {
                    className = "Beta";
                    score = 4 * value - 4;
                    bonus = value * 7 + 2;
                }
                default -> {
                    className = "ModuleOnly";
                    score = value * 9 + 4;
                }
            }
            source.add("    let object" + i + " = new " + className + "(" + value + ")");
            source.add("    Primary primary" + i + " = object" + i);
            source.add("    checksum += viaPrimary(primary" + i + ")");
            expected += score;
            if (bonus != null) {
                source.add("    Secondary secondary" + i + " = object" + i);
                source.add("    checksum += viaSecondary(secondary" + i + ")");
                expected += bonus;
            }
            source.add("    destroy object" + i);
        }
        source.add("    if checksum == " + expected + " and destroyed == " + objectCount);
        source.add("        testSuccess()");

        test().testLua(true).executeProg().lines(source.toArray(new String[0]));
    }

    /**
     * Differential dispatch corpus: every generated case executes the same generic interface
     * calls through the Jass interpreter and the Lua runtime. Keeping the seed fixed makes a
     * failure reproducible while varying specialization order, tuple shapes, nesting, and two
     * unrelated interfaces whose method names deliberately match.
     */
    @Test
    public void randomizedGenericTupleDispatchMatchesJassAndLua() {
        Random random = new Random(0x71A9D15CL);
        for (int caseIndex = 0; caseIndex < 8; caseIndex++) {
            EnumSet<GenericDispatchShape> covered = EnumSet.noneOf(GenericDispatchShape.class);
            List<String> source = genericTupleDispatchCase(random, covered);
            assertEquals("dispatch fuzz case must cover every semantic specialization shape",
                EnumSet.allOf(GenericDispatchShape.class), covered);
            String[] lines = source.toArray(new String[0]);
            try {
                test().executeProg().lines(lines);
            } catch (Exception | Error e) {
                throw new AssertionError("dispatch fuzz case " + caseIndex + " failed in Jass:\n"
                    + String.join("\n", lines), e);
            }
            try {
                test().testLua(true).executeProg().lines(lines);
            } catch (org.testng.SkipException e) {
                // Lua is optional on developer/CI hosts; preserve the framework's visible skip
                // instead of turning it into a dispatch failure through the diagnostic wrapper.
                throw e;
            } catch (Exception | Error e) {
                throw new AssertionError("dispatch fuzz case " + caseIndex + " failed in Lua:\n"
                    + String.join("\n", lines), e);
            }
        }
    }

    private enum GenericDispatchShape {
        TUPLE_INT,
        TUPLE_TEXT,
        NESTED_TUPLE_INT,
        NESTED_TUPLE_TEXT,
        CLASS_PREDICATE,
        OTHER_CLASS_PREDICATE,
        OTHER_CLASS_PREDICATE_SECOND_TYPE,
        CLASS_IMPLEMENTATION
    }

    /** A user method beginning with {@code destroy} is ordinary virtual dispatch, not lifecycle
     * destruction.  The lifecycle slot is identified from the generated OnDestroy function. */
    @Test
    public void ordinaryDestroyNamedMethodDoesNotUseLifecycleDispatchSlot() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Destroyer",
            "    function destroyValue() returns int",
            "class Implementation implements Destroyer",
            "    override function destroyValue() returns int",
            "        return 7",
            "init",
            "    Destroyer value = new Implementation()",
            "    if value.destroyValue() == 7",
            "        testSuccess()",
            "    destroy value"
        );
        String compiled = compiledLua("ordinaryDestroyNamedMethodDoesNotUseLifecycleDispatchSlot");
        int dispatchStart = compiled.indexOf("function dispatch_Destroyer_destroyValue");
        int dispatchEnd = compiled.indexOf("\nend", dispatchStart);
        assertTrue(dispatchStart >= 0 && dispatchEnd > dispatchStart);
        String dispatchBody = compiled.substring(dispatchStart, dispatchEnd);
        assertTrue(dispatchBody.contains(".Destroyer_destroyValue"));
        assertFalse(dispatchBody.contains(".__wurst_destroy"));
    }

    private List<String> genericTupleDispatchCase(Random random,
                                                  EnumSet<GenericDispatchShape> covered) {
        List<String> source = new ArrayList<>();
        Collections.addAll(source,
            "package Test",
            "native testSuccess()",
            "tuple PairInt(int a, int b)",
            "tuple PairText(string a, int b)",
            "interface Predicate<T:>",
            "    function test(T value) returns boolean",
            "interface OtherPredicate<T:>",
            "    function test(T value) returns boolean",
            "class Box<T:>",
            "    T value",
            "    construct(T value)",
            "        this.value = value",
            "    function matches(Predicate<T> predicate) returns boolean",
            "        let result = predicate.test(value)",
            "        destroy predicate",
            "        return result",
            "class OtherBox<T:>",
            "    T value",
            "    construct(T value)",
            "        this.value = value",
            "    function matches(OtherPredicate<T> predicate) returns boolean",
            "        let result = predicate.test(value)",
            "        destroy predicate",
            "        return result",
            "class Nested<T:>",
            "    Box<T> inner",
            "    construct(T value)",
            "        inner = new Box<T>(value)",
            "    function matches(Predicate<T> predicate) returns boolean",
            "        return inner.matches(predicate)",
            "    ondestroy",
            "        destroy inner",
            "class Foo",
            "class Bar",
            "class FooPredicate implements Predicate<Foo>",
            "    function test(Foo value) returns boolean",
            "        return value != null",
            "init",
            "    int successes = 0");

        int expected = 0;
        List<GenericDispatchShape> shapes = new ArrayList<>(
            EnumSet.allOf(GenericDispatchShape.class));
        Collections.shuffle(shapes, random);
        for (int i = 0; i < 16; i++) {
            int value = random.nextInt(100) + 1;
            GenericDispatchShape shape = i < shapes.size()
                ? shapes.get(i)
                : shapes.get(random.nextInt(shapes.size()));
            covered.add(shape);
            switch (shape) {
                case TUPLE_INT -> {
                    source.add("    let value" + i + " = new Box<PairInt>(PairInt(" + value + ", " + (value + 1) + "))");
                    source.add("    if value" + i + ".matches(x -> x.a == " + value + ")");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
                case TUPLE_TEXT -> {
                    source.add("    let value" + i + " = new Box<PairText>(PairText(\"v" + value + "\", " + value + "))");
                    source.add("    if value" + i + ".matches(x -> x.a == \"v" + value + "\")");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
                case NESTED_TUPLE_INT -> {
                    source.add("    let value" + i + " = new Nested<PairInt>(PairInt(" + value + ", " + (value + 2) + "))");
                    source.add("    if value" + i + ".matches(x -> x.b == " + (value + 2) + ")");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
                case NESTED_TUPLE_TEXT -> {
                    source.add("    let value" + i + " = new Nested<PairText>(PairText(\"v" + value + "\", " + value + "))");
                    source.add("    if value" + i + ".matches(x -> x.a == \"v" + value + "\")");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
                case CLASS_PREDICATE -> {
                    source.add("    let value" + i + " = new Box<Foo>(new Foo())");
                    source.add("    if value" + i + ".matches(x -> x != null)");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
                case OTHER_CLASS_PREDICATE -> {
                    source.add("    let value" + i + " = new OtherBox<Foo>(new Foo())");
                    source.add("    if value" + i + ".matches(x -> x != null)");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
                case OTHER_CLASS_PREDICATE_SECOND_TYPE -> {
                    source.add("    let value" + i + " = new OtherBox<Bar>(new Bar())");
                    source.add("    if value" + i + ".matches(x -> x != null)");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
                case CLASS_IMPLEMENTATION -> {
                    source.add("    let value" + i + " = new Box<Foo>(new Foo())");
                    source.add("    if value" + i + ".matches(new FooPredicate())");
                    source.add("        successes++");
                    source.add("    destroy value" + i);
                }
            }
            expected++;
        }
        source.add("    if successes == " + expected);
        source.add("        testSuccess()");
        return source;
    }

    @Test
    public void tupleSpecializedStaticInitializerCycleDoesNotRootErasedCopy() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int bumps",
            "function bump() returns int",
            "    bumps++",
            "    return bumps",
            "class Box<T:>",
            "    static int a = b + bump()",
            "    static int b = a",
            "    static function get() returns int",
            "        return a + b",
            "init",
            "    if Box<pair>.get() == 2 and bumps == 1",
            "        testSuccess()"
        );

        String compiled = compiledLua(
            "tupleSpecializedStaticInitializerCycleDoesNotRootErasedCopy");
        assertEquals("an unreachable erased initializer cycle must not execute",
            2, countOccurrences(compiled, "bump()")); // one function declaration plus one call
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
            int assignmentsInFunction = (int) java.util.regex.Pattern
                .compile("Box_store[^\\[]*\\[")
                .matcher(replayBody.group(1)).results().count();
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

    /**
     * Lua function calls are expensive enough that tiny leaf helpers should inline regardless of
     * how many distinct callers use them. The old size * (callerCount - 1) rating did the opposite:
     * a commonly reused one-expression helper quickly became less likely to inline.
     */
    @Test
    public void tinyPopularLuaHelpersInlineWithoutAnnotations() {
        String compiled = compileOptimizedLua(
            "tinyPopularLuaHelpersInlineWithoutAnnotations",
            tinyPopularLuaHelpersProgram()
        );

        assertFalse("tiny nil-safe wrapper must inline at every Lua call site:\n" + compiled,
            compiled.contains("safeCoordinate("));
        assertFalse("tiny arithmetic helper must inline at every Lua call site:\n" + compiled,
            compiled.contains("arithmetic("));
    }

    @Test
    public void tinyMonomorphicMethodsInlineOnLua() {
        String compiled = compileOptimizedLua(
            "tinyMonomorphicMethodsInlineOnLua",
            "package Test",
            "native consume(int value)",
            "class Accumulator",
            "    int offset",
            "    construct(int offset)",
            "        this.offset = offset",
            "    function add(int value) returns int",
            "        return value + offset",
            "abstract class Operation",
            "    abstract function apply(int value) returns int",
            "class DoubleOperation extends Operation",
            "    override function apply(int value) returns int",
            "        return value * 2",
            "@noinline function hotLoop(Accumulator accumulator)",
            "    var i = 0",
            "    while i < 16",
            "        consume(accumulator.add(i))",
            "        i++",
            "@noinline function dynamicCall(Operation operation)",
            "    consume(operation.apply(3))",
            "init",
            "    hotLoop(new Accumulator(2))",
            "    dynamicCall(new DoubleOperation())"
        );

        String body = topLevelFunctionBodyWithPrefix(compiled, "hotLoop");
        assertFalse("a tiny method with exactly one implementation must inline in optimized Lua:\n" + body,
            body.contains("Accumulator_add("));
        java.util.regex.Matcher offsetAlias = java.util.regex.Pattern
            .compile("local (\\w+) = Accumulator_offset_storage").matcher(body);
        assertTrue("the inlined method's field storage must be localized:\n" + body,
            offsetAlias.find());
        assertTrue("the inlined method must retain its field read through the local alias:\n" + body,
            body.contains(offsetAlias.group(1) + "["));
        String dynamicBody = topLevelFunctionBodyWithPrefix(compiled, "dynamicCall");
        assertTrue("a genuinely virtual method call must retain dispatch:\n" + dynamicBody,
            dynamicBody.contains("dispatch_"));
    }

    @Test
    public void monomorphicMethodInliningEvaluatesReceiverOnce() {
        test().testLua(true).luaOnly(false).optimize().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int receiverEvaluations = 0",
            "class Accumulator",
            "    int offset",
            "    construct(int offset)",
            "        this.offset = offset",
            "    function add(int value) returns int",
            "        return value + offset",
            "function makeAccumulator() returns Accumulator",
            "    receiverEvaluations++",
            "    return new Accumulator(2)",
            "function evaluate() returns int",
            "    var result = 0",
            "    var i = 0",
            "    while i < 1",
            "        result = makeAccumulator().add(5)",
            "        i++",
            "    return result",
            "init",
            "    if evaluate() == 7 and receiverEvaluations == 1",
            "        testSuccess()"
        );
    }

    @Test
    public void monomorphicMethodInliningKeepsCallbackBoundary() {
        String compiled = compileOptimizedLua(
            "monomorphicMethodInliningKeepsCallbackBoundary",
            "package Test",
            "native consume(code callback)",
            "function callback()",
            "class Registrar",
            "    function install()",
            "        consume(function callback)",
            "@noinline function hotPath(Registrar registrar)",
            "    var i = 0",
            "    while i < 1",
            "        registrar.install()",
            "        i++",
            "init",
            "    hotPath(new Registrar())"
        );

        assertFunctionBodyContains(compiled, "hotPath", "Registrar_install(", true);
    }

    @Test
    public void monomorphicMethodInliningKeepsLocalPlayerBoundary() {
        String compiled = compileOptimizedLua(
            "monomorphicMethodInliningKeepsLocalPlayerBoundary",
            "type player extends handle",
            "package Test",
            "@extern native GetLocalPlayer() returns player",
            "native consume(bool value)",
            "class Probe",
            "    function isLocal() returns bool",
            "        return GetLocalPlayer() != null",
            "@noinline function hotPath(Probe probe)",
            "    var i = 0",
            "    while i < 1",
            "        consume(probe.isLocal())",
            "        i++",
            "init",
            "    hotPath(new Probe())"
        );

        assertFunctionBodyContains(compiled, "hotPath", "Probe_isLocal(", true);
    }

    /**
     * Measured after Lua native lowering: unit_getX = 59 IM nodes,
     * unit_getAbilityLevel = 63, real_floor = 35, and __wurst_intDiv = 31. Each helper is called
     * from eight retained functions so the normal popularity rating exceeds the inline threshold;
     * Lua's unconditional small-body rule must still remove every call.
     */
    @Test
    public void stdlibHotLeafSizesDefineLuaAlwaysInlineCutoff() {
        String compiled = compileOptimizedLuaWithStdLib(
            "stdlibHotLeafSizesDefineLuaAlwaysInlineCutoff",
            popularStdlibHelpersProgram()
        );
        for (int i = 1; i <= 8; i++) {
            String caller = "caller" + i;
            assertFunctionBodyContains(compiled, caller, "unit_getX(", false);
            assertFunctionBodyContains(compiled, caller, "unit_getAbilityLevel(", false);
            assertFunctionBodyContains(compiled, caller, "real_floor(", false);
            assertFunctionBodyContains(compiled, caller, "__wurst_intDiv(", false);
        }
    }

    @Test
    public void optimizedUnitSpatialIndexInnerLoopUsesRawLuaOperations() {
        String compiled = compileOptimizedLuaWithStdLib(
            "optimizedUnitSpatialIndexInnerLoopUsesRawLuaOperations",
            "package Test",
            "import SpatialIndexForUnits",
            "@noinline function query(vec2 center)",
            "    let result = unitsInRange(center, 512.)",
            "    destroy result",
            "init",
            "    query(vec2(0., 0.))"
        );

        // Register-pressure-aware inlining may keep the range helper as the hot-loop owner instead
        // of folding it into query. Inspect whichever function actually retains the loop.
        String body = topLevelFunctionBodyWithPrefix(compiled, "query");
        if (!body.contains("= UnitSpatialIndex_nextInCell")) {
            assertTrue("query must call the retained range helper:\n" + body,
                body.contains("addRangeMatches("));
            body = topLevelFunctionBodyWithPrefix(compiled, "addRangeMatches");
        }
        java.util.regex.Matcher nextAlias = java.util.regex.Pattern
            .compile("local (\\w+) = UnitSpatialIndex_nextInCell").matcher(body);
        java.util.regex.Matcher xAlias = java.util.regex.Pattern
            .compile("local (\\w+) = UnitSpatialIndex_lastX").matcher(body);
        java.util.regex.Matcher yAlias = java.util.regex.Pattern
            .compile("local (\\w+) = UnitSpatialIndex_lastY").matcher(body);
        assertTrue("spatial-index hot loop must localize the next-link array:\n" + body, nextAlias.find());
        assertTrue("spatial-index hot loop must localize cached X:\n" + body, xAlias.find());
        assertTrue("spatial-index hot loop must localize cached Y:\n" + body, yAlias.find());
        assertTrue("spatial-index hot loop must index the localized next-link array:\n" + body,
            body.contains(nextAlias.group(1) + "["));
        assertTrue("spatial-index hot loop must index localized cached X:\n" + body,
            body.contains(xAlias.group(1) + "["));
        assertTrue("spatial-index hot loop must index localized cached Y:\n" + body,
            body.contains(yAlias.group(1) + "["));
        assertFalse("spatial-index loop must not retain global next-link lookups:\n" + body,
            body.contains("UnitSpatialIndex_nextInCell["));
        assertFalse("spatial-index loop must not retain global cached-X lookups:\n" + body,
            body.contains("UnitSpatialIndex_lastX["));
        assertFalse("spatial-index loop must not retain global cached-Y lookups:\n" + body,
            body.contains("UnitSpatialIndex_lastY["));
        assertFalse("typed array reads must not retain assurance calls:\n" + body,
            body.contains("__wurst_ensure"));
        assertFalse("static-arity helpers must not allocate vararg packs:\n" + body,
            body.contains("table.pack"));
        assertFalse("hot query arithmetic must not retain portable div helpers:\n" + body,
            body.contains("__wurst_intDiv("));
    }

    @Test
    public void hotLoopStorageTablesAreLocalizedOnLua() {
        String compiled = compileOptimizedLua(
            "hotLoopStorageTablesAreLocalizedOnLua",
            "package Test",
            "int array values",
            "class Counter",
            "    int total",
            "@noinline function hotLoop(Counter counter, int limit)",
            "    var i = 0",
            "    while i < limit",
            "        values[i] = values[i] + counter.total",
            "        counter.total = values[i + 1] + counter.total",
            "        i++",
            "@noinline function coldPath(Counter counter) returns int",
            "    return values[0] + counter.total",
            "init",
            "    let counter = new Counter()",
            "    hotLoop(counter, 16)",
            "    coldPath(counter)"
        );

        String hotBody = topLevelFunctionBodyWithPrefix(compiled, "hotLoop");
        java.util.regex.Matcher arrayAlias = java.util.regex.Pattern
            .compile("local (\\w+) = Test_values").matcher(hotBody);
        assertTrue("the loop's generated array table must be cached in a local:\n" + hotBody,
            arrayAlias.find());
        String arrayAliasName = arrayAlias.group(1);
        assertTrue("the localized array must be used in the loop:\n" + hotBody,
            hotBody.contains(arrayAliasName + "["));
        assertFalse("loop indexes must not keep resolving the array through a global:\n" + hotBody,
            hotBody.contains("Test_values["));

        java.util.regex.Matcher fieldAlias = java.util.regex.Pattern
            .compile("local (\\w+) = Counter_total_storage").matcher(hotBody);
        assertTrue("the loop's generated field-storage table must be cached in a local:\n" + hotBody,
            fieldAlias.find());
        String fieldAliasName = fieldAlias.group(1);
        assertTrue("the localized field storage must be used in the loop:\n" + hotBody,
            hotBody.contains(fieldAliasName + "["));
        assertFalse("loop field accesses must not keep resolving storage through a global:\n" + hotBody,
            hotBody.contains("Counter_total_storage["));

        String coldBody = topLevelFunctionBodyWithPrefix(compiled, "coldPath");
        assertFalse("cold array access must not consume a local alias:\n" + coldBody,
            coldBody.contains("local "));
        assertTrue("cold array access must keep the generated global table:\n" + coldBody,
            coldBody.contains("Test_values["));
        assertTrue("cold field access must keep the generated global storage:\n" + coldBody,
            coldBody.contains("Counter_total_storage["));
    }

    @Test
    public void localizedHotStorageTablesPreserveLuaBehavior() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int array values",
            "class Counter",
            "    int total",
            "function hotLoop(Counter counter, int limit)",
            "    var i = 0",
            "    while i < limit",
            "        values[i] = values[i] + counter.total",
            "        counter.total = values[i + 1] + counter.total",
            "        i++",
            "init",
            "    values[0] = 1",
            "    let counter = new Counter()",
            "    counter.total = 2",
            "    hotLoop(counter, 3)",
            "    if values[0] == 3 and values[1] == 2 and counter.total == 2",
            "        testSuccess()"
        );
    }

    @Test
    public void hotStorageLocalizationRespectsLuaRegisterHeadroom() {
        List<String> lines = new ArrayList<>();
        lines.add("package Test");
        lines.add("native consume(int value)");
        lines.add("int array values");
        lines.add("@noinline function crowded(int limit)");
        lines.add("    var sum = 0");
        for (int i = 0; i < 187; i++) {
            lines.add("    let v" + i + " = " + i);
            lines.add("    sum += v" + i);
        }
        lines.add("    var i = 0");
        lines.add("    while i < limit");
        lines.add("        sum += values[i]");
        lines.add("        i++");
        lines.add("    consume(sum)");
        lines.add("init");
        lines.add("    crowded(1)");

        String compiled = compileLuaWithRunArgs(
            "hotStorageLocalizationRespectsLuaRegisterHeadroom",
            new RunArgs().with("-lua"), lines.toArray(new String[0]));
        String body = topLevelFunctionBodyWithPrefix(compiled, "crowded");
        assertFalse("localization must retain headroom below Lua's hard local limit:\n" + body,
            body.contains("= Test_values"));
        assertTrue("a skipped alias must leave the generated array access intact:\n" + body,
            body.contains("Test_values[i]"));
        assertFalse("the optimization must not force the locals-table fallback:\n" + body,
            body.contains("__wurst_locals"));
    }

    /**
     * On Lua a vararg function used to keep its {@code ...} parameter and pack it into a table on
     * every call, and the inliner refused it. With a static argument count at the call site the
     * call is redirected to a fixed-arity copy, as Jass has always done, so the pack is gone and
     * the copy inlines like any other small function.
     */
    @Test
    public void staticArityVarargCallsAreFixedArityOnLua() {
        String compiled = compileOptimizedLua(
            "staticArityVarargCallsAreFixedArityOnLua",
            "package Test",
            "native consume(int i)",
            "int array values",
            "function biggest(vararg int xs) returns int",
            "    var best = -2147483648",
            "    for x in xs",
            "        if x > best",
            "            best = x",
            "    return best",
            "@noinline function query(int a, int b, int c)",
            "    var i = 0",
            "    while i < 16",
            "        consume(biggest(a, values[i]))",
            "        consume(biggest(a, b, c))",
            "        i++",
            "init",
            "    query(1, 2, 3)"
        );
        assertFalse("no vararg call site may pack a table:\n" + compiled, compiled.contains("table.pack"));
        assertFalse("the vararg original must not survive with a ... parameter:\n" + compiled,
            compiled.contains("function biggest(...)"));
        assertFunctionBodyContains(compiled, "query", "biggest(", false);
    }

    @Test
    public void fixedArityVarargLoweringKeepsSemanticsOnLua() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "function sum(vararg int xs) returns int",
            "    var total = 0",
            "    for x in xs",
            "        total += x",
            "    return total",
            "function count(vararg int xs) returns int",
            "    var n = 0",
            "    for x in xs",
            "        n++",
            "    return n",
            "function firstOr(vararg int xs) returns int",
            "    for x in xs",
            "        return x",
            "    return -1",
            "function pairs(vararg pair ps) returns int",
            "    var result = 0",
            "    for p in ps",
            "        result = result * 100 + p.x * 10 + p.y",
            "    return result",
            "class Bag",
            "    int total = 0",
            "    function add(vararg int xs)",
            "        for x in xs",
            "            total += x",
            "init",
            "    let bag = new Bag()",
            "    bag.add(1)",
            "    bag.add(2, 3)",
            "    bag.add()",
            "    if sum() == 0 and sum(5) == 5 and sum(1, 2, 3, 4) == 10",
            "        and count() == 0 and count(9, 9, 9) == 3",
            "        and firstOr() == -1 and firstOr(4, 5) == 4",
            "        and pairs(pair(1, 2), pair(3, 4)) == 1234",
            "        and bag.total == 6",
            "        testSuccess()"
        );
        String compiled = compiledLua("fixedArityVarargLoweringKeepsSemanticsOnLua");
        assertFalse("every call above has a static arity, so nothing may pack:\n" + compiled,
            compiled.contains("table.pack"));
    }

    @Test
    public void varargCallAboveTheLuaArityBoundKeepsThePackedPath() throws IOException {
        StringBuilder args = new StringBuilder();
        int n = 150;
        for (int i = 1; i <= n; i++) {
            if (i > 1) {
                args.append(", ");
            }
            args.append(i);
        }
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function sum(vararg int xs) returns int",
            "    var total = 0",
            "    for x in xs",
            "        total += x",
            "    return total",
            "init",
            "    if sum(" + args + ") == " + (n * (n + 1) / 2) + " and sum(1, 2) == 3",
            "        testSuccess()"
        );
        String compiled = compiledLua("varargCallAboveTheLuaArityBoundKeepsThePackedPath");
        assertTrue("a call above the bound keeps the vararg original:\n" + compiled,
            compiled.contains("table.pack"));
    }

    /**
     * A vararg constructor is reached through a compiler-generated `new_C` wrapper which forwards its
     * vararg placeholder to `construct_C`. When a call above the bound keeps that wrapper as the
     * retained original, its body still holds the forwarding call, and the placeholder is one node
     * standing for however many arguments the caller passed. Specialising by node count would rewrite
     * it to a fixed-arity constructor and silently drop every argument after the first.
     */
    @Test
    public void varargConstructorAboveTheLuaArityBoundKeepsThePackedPath() {
        StringBuilder args = new StringBuilder();
        int n = 70;
        for (int i = 1; i <= n; i++) {
            if (i > 1) {
                args.append(", ");
            }
            args.append(i);
        }
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Tally",
            "    int total = 0",
            "    construct(vararg int xs)",
            "        for x in xs",
            "            total += x",
            "init",
            "    let big = new Tally(" + args + ")",
            "    let small = new Tally(1, 2)",
            "    if big.total == " + (n * (n + 1) / 2) + " and small.total == 3",
            "        testSuccess()"
        );
    }

    /**
     * Jass erases generics long before this pass, so `redirectCall` could build the replacement with
     * an empty type-argument list. Lua only specialises concrete operations at that point and leaves
     * generics live, so dropping them leaves the redirected call typed by an unresolved type variable.
     * `LuaNativeLowering` decides string concatenation from each operand's type, so a generic vararg
     * returning its type parameter silently became a numeric addition on strings.
     */
    @Test
    public void genericVarargCallKeepsItsTypeArgumentsOnLua() {
        test().testLua(true).withStdLib().executeProg().lines(
            "package Test",
            "function lastOf<T>(vararg T xs) returns T",
            "    T result = null",
            "    for x in xs",
            "        result = x",
            "    return result",
            "init",
            "    let joined = \"a\" + lastOf<string>(\"b\", \"c\")",
            "    if joined == \"ac\" and lastOf<int>(1, 2) == 2",
            "        testSuccess()"
        );
    }

    @Test
    public void virtuallyDispatchedVarargMethodKeepsThePackedPath() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Summer",
            "    function sum(vararg int xs) returns int",
            "class Plain implements Summer",
            "    override function sum(vararg int xs) returns int",
            "        var total = 0",
            "        for x in xs",
            "            total += x",
            "        return total",
            "class Doubling implements Summer",
            "    override function sum(vararg int xs) returns int",
            "        var total = 0",
            "        for x in xs",
            "            total += 2 * x",
            "        return total",
            "init",
            "    Summer a = new Plain()",
            "    Summer b = new Doubling()",
            "    if a.sum(1, 2, 3) == 6 and b.sum(1, 2, 3) == 12",
            "        testSuccess()"
        );
    }

    /**
     * A vararg function may call itself with a different static arity. Copying the function for one
     * arity must not retarget that inner call to the copy, which has the wrong parameter count; the
     * call has to be mapped to its own arity like every other call.
     */
    @Test
    public void recursiveVarargCallsAreMappedToTheirOwnArity() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function depth(vararg int xs) returns int",
            "    var n = 0",
            "    for x in xs",
            "        n++",
            "    if n == 3",
            "        return 100 + depth(1, 2)",
            "    if n == 2",
            "        return 10 + depth(1)",
            "    return n",
            "init",
            "    if depth(1, 2, 3) == 111 and depth(5) == 1 and depth() == 0",
            "        testSuccess()"
        );
    }

    /**
     * The Lua arity bound is about emitted parameters, which tuple elimination multiplies: twenty
     * four-field tuples are eighty formal parameters. Such a call keeps the packed path rather than
     * emitting a function Lua refuses to load.
     */
    @Test
    public void wideTupleVarargCallCountsFlattenedParametersAgainstTheLuaBound() throws IOException {
        StringBuilder args = new StringBuilder();
        int n = 20;
        for (int i = 1; i <= n; i++) {
            if (i > 1) {
                args.append(", ");
            }
            args.append("quad(").append(i).append(", 0, 0, 1)");
        }
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple quad(int a, int b, int c, int d)",
            "@noinline function sumFirst(vararg quad qs) returns int",
            "    var total = 0",
            "    for q in qs",
            "        total += q.a + q.d",
            "    return total",
            "init",
            "    if sumFirst(" + args + ") == " + (n * (n + 1) / 2 + n) + " and sumFirst(quad(1, 2, 3, 4)) == 5",
            "        testSuccess()"
        );
        String compiled = compiledLua("wideTupleVarargCallCountsFlattenedParametersAgainstTheLuaBound");
        assertTrue("eighty flattened parameters must keep the packed original:\n" + compiled,
            compiled.contains("table.pack"));
    }

    /**
     * A vararg parameter cannot be passed on, to a function or to a method. Both halves matter here:
     * a vararg function may have only the one parameter, so a receiver cannot be a second one, and
     * the argument itself does not type as the element type. The eliminator's forwarding branch is
     * therefore reachable only from calls it generated itself, which are always ImFunctionCall.
     */
    @Test
    public void varargParameterCannotBeForwardedToAMethod() {
        testAssertErrorsLines(false, "Found vararg integer",
            "package Test",
            "class Sink",
            "    static Sink instance = null",
            "    function consume(vararg int xs)",
            "        skip",
            "function relay(vararg int xs)",
            "    Sink.instance.consume(xs)"
        );
    }


    /**
     * `@preserveName` and `ExecuteFunc` mark a function's emitted name as part of the map's
     * WC3-facing API, and `LuaTranslator.collectPredefinedNames()` resets every function carrying
     * that flag to its trace's source name. A generated copy shares the original's trace, so an
     * inherited flag would emit the original and every copy under one name and let the last
     * definition win. The preserved name belongs to the retained original: that is the one external
     * code calls, at an arity this pass never gets to see.
     */
    @Test
    public void preservedNameStaysOnTheVarargOriginalNotItsCopies() {
        String compiled = compileOptimizedLua(
            "preservedNameStaysOnTheVarargOriginalNotItsCopies",
            "package Test",
            "native consume(int i)",
            "@preserveName @noinline public function tally(vararg int xs) returns int",
            "    var sum = 0",
            "    for x in xs",
            "        sum += x",
            "    return sum",
            "init",
            "    consume(tally(1, 2))"
        );
        assertTrue("the fixed-arity copy must keep its own suffixed name:\n" + compiled,
            compiled.contains("function tally_2("));
        int definitions = 0;
        for (int at = compiled.indexOf("function tally("); at >= 0;
             at = compiled.indexOf("function tally(", at + 1)) {
            definitions++;
        }
        assertEquals("the preserved name must name exactly one function:\n" + compiled,
            1, definitions);
    }

    /**
     * The inliner used to refuse every function whose return fact the local-player analysis had
     * marked, and that fact fires for anything reachable from a client-local branch anywhere in the
     * program. In a stdlib-linked map that is most of the call graph, so pure index arithmetic in
     * hot loops stayed as calls. Only functions which transitively invoke a client-local native are
     * an inlining barrier.
     */
    @Test
    public void pureHelpersReachableFromLocalPlayerBranchInlineIntoLuaHotLoops() {
        String compiled = compileOptimizedLua(
            "pureHelpersReachableFromLocalPlayerBranchInlineIntoLuaHotLoops",
            "type player extends handle",
            "package Test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native consume(int i)",
            "native consumePlayer(player p)",
            "int array cells",
            "int offset = 0",
            "@inline function slotOf(int cell, int group) returns int",
            "    return cell * 8 + group",
            "@inline function chainHead(int cell, int group) returns int",
            "    return cells[slotOf(cell, group)]",
            "@inline function localWrapper() returns player",
            "    return GetLocalPlayer()",
            "@noinline function query(int group)",
            "    var cell = 0",
            "    while cell < 16",
            "        consume(chainHead(cell, group))",
            "        cell++",
            "init",
            "    if GetLocalPlayer() == Player(0)",
            "        consume(slotOf(offset, 1))",
            "    consumePlayer(localWrapper())",
            "    query(2)"
        );

        assertFunctionBodyContains(compiled, "query", "slotOf", false);
        assertFunctionBodyContains(compiled, "query", "chainHead", false);
        assertFunctionBodyContains(compiled, "query", "* 8", true);
        assertTrue("a wrapper which itself calls GetLocalPlayer must stay an explicit call",
            compiled.contains("consumePlayer(localWrapper())"));
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
     * array slot) would read as "not nil", skip normalization, and come out as
     * the literal string "nil" via tostring() instead of "" - or, for
     * stringConcat, get passed straight into raw ".." concatenation.
     * LuaEnsureFunctions#notNull tags its ImNull sentinel with ImAnyType
     * specifically to stay exempt from that rewrite.
     */
    @Test
    public void stringConcatNilCheckSurvivesEliminateLocalTypes() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "native print(string value)",
            "string array names",
            "function join(string a, string b) returns string",
            "    return a + b",
            "init",
            "    if names[5] == \"\" and join(\"a\", \"b\") == \"ab\"",
            "        print(names[5])",
            "        testSuccess()"
        );
        String compiled = compiledLua("stringConcatNilCheckSurvivesEliminateLocalTypes");
        assertNilCheckNotCorruptedToEmptyStringCheck(compiled, "__wurst_stringConcat(");
    }

    @Test
    public void genericNormalizationIsKeptAtNativeBoundaryOnly() {
        String compiled = compileLuaWithRunArgs(
            "LuaBackendAuditTests_genericNormalizationIsKeptAtNativeBoundaryOnly",
            new RunArgs().with("-lua"),
            "package Test",
            "native print(string value)",
            "native consumeBool(bool value)",
            "string array values",
            "function identity<T:>(T value) returns T",
            "    return value",
            "function forward<T:>(T value) returns T",
            "    return identity<T>(value)",
            "init",
            "    print(forward<string>(\"value\"))",
            "    consumeBool(forward<bool>(false))",
            "    print(values[1])"
        );

        assertFunctionBodyContains(compiled, "forward", "__wurst_ensure", false);
        assertTrue("boolean normalization should be a direct true comparison:\n" + compiled,
            compiled.contains("consumeBool((forward(false) == true))"));
        assertFalse("boolean normalization must not call the ensure helper",
            compiled.contains("__wurst_ensureBool(forward(false))"));
        assertTrue("typed primitive arrays must cross native boundaries as raw reads:\n" + compiled,
            compiled.contains("print(Test_values[1])"));
    }

    @Test
    public void erasedGenericPrimitiveDefaultsAreNormalizedAtConcreteUse() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "native testFail(string message)",
            "int array values",
            "class Box<T:>",
            "    T value",
            "    function get() returns T",
            "        return value",
            "init",
            "    let box = new Box<int>",
            "    values[box.get()] = 7",
            "    if box.get() + 1 == 1",
            "        testSuccess()",
            "    for i = 1 to box.get()",
            "        testSuccess()",
            "    switch box.get()",
            "        case 0",
            "            testSuccess()",
            "        default",
            "            testFail(\"switch\")",
            "    testSuccess()"
        );
        String compiled = compiledLua("erasedGenericPrimitiveDefaultsAreNormalizedAtConcreteUse");
        assertTrue("concrete generic use must normalize an erased integer",
            compiled.contains("__wurst_ensureInt"));
    }

    @Test
    public void erasedGenericPrimitiveDefaultsAreNormalizedInTypedClosures() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface IntSupplier",
            "    function get() returns int",
            "class Box<T:>",
            "    T value",
            "    function get() returns T",
            "        return value",
            "init",
            "    let box = new Box<int>",
            "    IntSupplier supplier = () -> box.get()",
            "    if supplier.get() + 1 == 1",
            "        testSuccess()"
        );
        String compiled = compiledLua("erasedGenericPrimitiveDefaultsAreNormalizedInTypedClosures");
        assertTrue("typed closure implementations must normalize erased primitive results",
            compiled.contains("return __wurst_ensureInt(Box_Box_get("));
    }

    @Test
    public void erasedGenericPrimitiveDefaultsAreNormalizedInTypedStatementBlocks() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Box<T:>",
            "    T value",
            "    function get() returns T",
            "        return value",
            "init",
            "    let box = new Box<int>",
            "    int value = begin",
            "        return box.get()",
            "    end",
            "    if value + 1 == 1",
            "        testSuccess()"
        );
        String compiled = compiledLua("erasedGenericPrimitiveDefaultsAreNormalizedInTypedStatementBlocks");
        assertTrue("typed statement blocks must normalize erased primitive results",
            compiled.contains("value = __wurst_ensureInt(Box_Box_get(box))"));
    }

    @Test
    public void erasedGenericPrimitiveDefaultsAreNormalizedInCompositeRangeBounds() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Box<T:>",
            "    T value",
            "    function get() returns T",
            "        return value",
            "init",
            "    let box = new Box<int>",
            "    bool useBox = true",
            "    int iterations = 0",
            "    for i = 1 to (useBox ? box.get() : 0)",
            "        iterations++",
            "    if iterations == 0",
            "        testSuccess()"
        );
        String compiled = compiledLua("erasedGenericPrimitiveDefaultsAreNormalizedInCompositeRangeBounds");
        assertTrue("composite range bounds must normalize erased primitive branches",
            compiled.contains("__wurst_ensureInt(Box_Box_get(box))"));
    }

    @Test
    public void erasedGenericPrimitiveDefaultsUseTheSelectedOverload() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Box<T:>",
            "    T value",
            "    function get() returns T",
            "        return value",
            "function consume(int value)",
            "    if value == 0",
            "        testSuccess()",
            "function consume(string value)",
            "init",
            "    let box = new Box<int>",
            "    consume(box.get())"
        );
        String compiled = compiledLua("erasedGenericPrimitiveDefaultsUseTheSelectedOverload");
        assertTrue("selected integer overload arguments must normalize erased primitive values",
            compiled.contains("__wurst_ensureInt(Box_Box_get(box))"));
    }

    @Test
    public void erasedGenericPrimitiveDefaultsPropagateThroughCompositeContexts() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Box<T:>",
            "    T value",
            "    function get() returns T",
            "        return value",
            "class Addable",
            "    function op_plus(int value) returns int",
            "        return value",
            "class Constructed",
            "    int value",
            "    construct(int value)",
            "        this.value = value",
            "    construct(string value)",
            "        this.value = -1",
            "    function get() returns int",
            "        return value",
            "function consume(int value) returns int",
            "    return value",
            "function consume(string value) returns int",
            "    return -1",
            "interface IntSupplier",
            "    function get() returns int",
            "int array values",
            "function readArrayValue() returns int",
            "    return values[0]",
            "init",
            "    let box = new Box<int>",
            "    let addable = new Addable()",
            "    bool useBox = true",
            "    values[0] = 7",
            "    let sum = addable + box.get()",
            "    let builtinSum = box.get() + box.get()",
            "    let overloaded = consume(useBox ? box.get() : 0)",
            "    let blockOverloaded = consume(begin",
            "        return (useBox ? box.get() : 0)",
            "    end)",
            "    let indexed = values[useBox ? box.get() : 0]",
            "    IntSupplier supplier = () -> (useBox ? box.get() : 0)",
            "    IntSupplier unarySupplier = () -> -box.get()",
            "    IntSupplier blockSupplier = () -> begin",
            "        return (useBox ? box.get() : 0)",
            "    end",
            "    let constructed = new Constructed(useBox ? box.get() : 0)",
            "    int blockValue = begin",
            "        return (useBox ? box.get() : 0)",
            "    end",
            "    int switchValue = -1",
            "    switch (useBox ? box.get() : 1)",
            "        case 0",
            "            switchValue = 0",
            "    if sum == 0 and builtinSum == 0 and overloaded == 0 and blockOverloaded == 0",
            "        and indexed == 7 and readArrayValue() == 7 and supplier.get() == 0",
            "        and unarySupplier.get() == 0 and blockSupplier.get() == 0",
            "        and blockValue == 0 and switchValue == 0 and constructed.get() == 0",
            "        testSuccess()"
        );
        String compiled = compiledLua("erasedGenericPrimitiveDefaultsPropagateThroughCompositeContexts");
        assertEquals("each concrete integer consumer must normalize its erased generic input", 12,
            countOccurrences(compiled, "__wurst_ensureInt(Box_Box_get("));
        assertTrue("global primitive array reads must be raw table indexes",
            compiled.contains("return Test_values[0]"));
        assertFalse("typed array reads must not use erased-generic normalization",
            compiled.contains("ensureInt(Test_values"));
    }

    @Test
    public void erasedGenericDefaultsUseResolvedAssignmentAndDelegationTargets() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Box<T:>",
            "    T value",
            "    function get() returns T",
            "        return value",
            "class Delegating",
            "    int value",
            "    construct(int value)",
            "        this.value = value + 1",
            "    construct(Box<int> box)",
            "        this(box.get())",
            "    function get() returns int",
            "        return value",
            "class Parent",
            "    int sum",
            "    construct(int fixed, vararg int rest)",
            "        sum = fixed",
            "        for value in rest",
            "            sum += value",
            "class Child extends Parent",
            "    construct(Box<int> box)",
            "        super(1, box.get(), box.get())",
            "    function get() returns int",
            "        return sum",
            "class Indexed",
            "    bool assignedDefault",
            "    function op_index(int index) returns string",
            "        return \"\"",
            "    function op_indexAssign(int index, int value)",
            "        assignedDefault = value == 0",
            "init",
            "    let box = new Box<int>",
            "    let delegating = new Delegating(box)",
            "    let child = new Child(box)",
            "    let indexed = new Indexed",
            "    indexed[0] = box.get()",
            "    if delegating.get() == 1 and child.get() == 1 and indexed.assignedDefault",
            "        testSuccess()"
        );
        String compiled = compiledLua("erasedGenericDefaultsUseResolvedAssignmentAndDelegationTargets");
        assertEquals("resolved primitive consumers must normalize their erased generic input", 4,
            countOccurrences(compiled, "__wurst_ensureInt(Box_Box_get("));
    }

    /**
     * Seeded boundary corpus for the type-assurance change. Each case varies
     * the primitive type, literal value, and array slot while checking the two
     * unsafe paths independently: erased generic propagation and a raw array
     * read. The intermediate generic functions and typed array reads must stay
     * free of assurance calls, while erased generic values keep normalization
     * at concrete uses. This is intentionally compile-only: the
     * generated native sinks have no Warcraft runtime implementation.
     */
    @Test
    public void seededTypeAssuranceBoundaryFuzz() {
        Random random = new Random(0x7A55_BA5EL);
        String[] types = {"int", "bool", "real", "string"};
        String[] suffixes = {"Int", "Bool", "Real", "Str"};
        for (int caseIndex = 0; caseIndex < 32; caseIndex++) {
            int typeIndex = (caseIndex + random.nextInt(types.length)) % types.length;
            String type = types[typeIndex];
            String suffix = suffixes[typeIndex];
            int arrayIndex = random.nextInt(16) + 1;
            String literal = switch (type) {
                case "int" -> Integer.toString(random.nextInt(51));
                case "bool" -> random.nextBoolean() ? "true" : "false";
                case "real" -> random.nextInt(51) + ".5";
                case "string" -> "\"fuzz_" + caseIndex + "\"";
                default -> throw new AssertionError(type);
            };
            String sink = "consume" + suffix;
            String testName = "LuaBackendAuditTests_seededTypeAssuranceBoundaryFuzz_" + caseIndex;
            String compiled = compileLuaWithRunArgs(
                testName,
                new RunArgs().with("-lua"),
                "package TypeAssuranceFuzz",
                "native " + sink + "(" + type + " value)",
                type + " array values",
                "function identity<T:>(T value) returns T",
                "    return value",
                "function forward<T:>(T value) returns T",
                "    return identity<T>(value)",
                "function read() returns " + type,
                "    return values[" + arrayIndex + "]",
                "init",
                "    " + sink + "(forward<" + type + ">(" + literal + "))",
                "    " + sink + "(values[" + arrayIndex + "])",
                "    " + sink + "(read())",
                "    " + sink + "(" + literal + ")"
            );

            assertFunctionBodyContains(compiled, "forward", "__wurst_ensure", false);
            String rawArrayRead = "TypeAssuranceFuzz_values[" + arrayIndex + "]";
            assertFunctionBodyContains(compiled, "read", rawArrayRead, true);
            assertFunctionBodyContains(compiled, "read", "__wurst_ensure", false);
            assertFunctionBodyContains(compiled, "read", "== true", false);
            String genericArgument = type.equals("bool")
                ? "(forward(" + literal + ") == true)"
                : "__wurst_ensure" + suffix + "(forward(" + literal + "))";
            assertTrue("generic boundary case " + caseIndex + " was not normalized:\n" + compiled,
                compiled.contains(sink + "(" + genericArgument + ")"));
            assertTrue("array boundary case " + caseIndex + " was not emitted raw:\n" + compiled,
                compiled.contains(sink + "(" + rawArrayRead + ")"));
            assertTrue("ordinary typed values must not be normalized at the boundary:\n" + compiled,
                compiled.contains(sink + "(" + literal + ")"));
        }
    }

    @Test
    public void typedPrimitiveArrayReadsAreRawInOptimizedLua() {
        String compiled = compileOptimizedLua(
            "LuaBackendAuditTests_typedPrimitiveArrayReadsAreRawInOptimizedLua",
            primitiveArrayReadShapeLines()
        );
        assertFalse("typed array reads must not call assurance helpers:\n" + compiled,
            compiled.contains("__wurst_ensure"));
        assertFalse("boolean array reads must not be normalized with a true comparison:\n" + compiled,
            compiled.contains("== true)"));
    }

    @Test
    public void typedPrimitiveArrayDefaultsComeFromMetatables() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int array ints",
            "real array reals",
            "bool array bools",
            "string array strings",
            "function localDefault() returns int",
            "    int array[8] localInts",
            "    return localInts[7]",
            "init",
            "    if ints[0] == 0 and ints[1000000] == 0",
            "        and reals[0] == 0. and reals[1000000] == 0.",
            "        and not bools[0] and not bools[1000000]",
            "        and strings[0] == \"\" and strings[1000000] == \"\"",
            "        and localDefault() == 0",
            "        ints[3] = 0",
            "        reals[3] = 0.",
            "        bools[3] = false",
            "        strings[3] = \"\"",
            "        if ints[3] == 0 and reals[3] == 0. and not bools[3] and strings[3] == \"\"",
            "            testSuccess()"
        );
    }

    @Test
    public void typedPrimitiveArrayReadsAreRawWithStacktraces() {
        String compiled = compileLuaWithRunArgs(
            "LuaBackendAuditTests_typedPrimitiveArrayReadsAreRawWithStacktraces",
            new RunArgs().with("-lua", "-stacktraces"),
            primitiveArrayReadShapeLines()
        );
        assertFalse("stacktrace mode must not restore typed-array assurance calls:\n" + compiled,
            compiled.contains("__wurst_ensure"));
        assertFalse("stacktrace mode must not normalize boolean reads with a true comparison:\n" + compiled,
            compiled.contains("== true)"));
    }

    private static String[] primitiveArrayReadShapeLines() {
        return new String[]{
            "package Test",
            "native consumeInt(int value)",
            "int array ints",
            "real array reals",
            "bool array bools",
            "string array strings",
            "function scan(int limit) returns real",
            "    int array[8] localInts",
            "    int i = 0",
            "    real sum = 0.",
            "    while i < limit",
            "        sum += ints[i] + reals[i] + localInts[i]",
            "        if bools[i] and strings[i] == \"\"",
            "            sum += 1",
            "        consumeInt(ints[i])",
            "        i++",
            "    return sum",
            "init",
            "    scan(8)"
        };
    }

    private static String[] tinyPopularLuaHelpersProgram() {
        List<String> lines = new ArrayList<>(List.of(
            "type unit extends handle",
            "package Test",
            "@extern native GetUnitX(unit u) returns real",
            "native consumeReal(real value)",
            "native consumeInt(int value)",
            "function safeCoordinate(unit u) returns real",
            "    return u == null ? 0. : GetUnitX(u)",
            "function arithmetic(int value) returns int",
            "    return (value * 31 + 17) div 4"
        ));
        for (int i = 1; i <= 8; i++) {
            lines.add("@noinline function caller" + i + "(unit u, int value)");
            lines.add("    consumeReal(safeCoordinate(u))");
            lines.add("    consumeInt(arithmetic(value))");
        }
        lines.add("init");
        for (int i = 1; i <= 8; i++) {
            lines.add("    caller" + i + "(null, " + i + ")");
        }
        return lines.toArray(String[]::new);
    }

    private static String[] popularStdlibHelpersProgram() {
        List<String> lines = new ArrayList<>(List.of(
            "package Test",
            "native consumeReal(real value)",
            "native consumeInt(int value)"
        ));
        for (int i = 1; i <= 8; i++) {
            lines.add("@noinline function caller" + i + "(unit u, real value, int divisor)");
            lines.add("    consumeReal(u.getX())");
            lines.add("    consumeInt(u.getAbilityLevel('A000'))");
            lines.add("    consumeInt(value.floor())");
            lines.add("    consumeInt(17 div divisor)");
        }
        lines.add("init");
        for (int i = 1; i <= 8; i++) {
            lines.add("    caller" + i + "(null, -1.5, 2)");
        }
        return lines.toArray(String[]::new);
    }

    private static void assertFunctionBodyContains(String compiled, String functionName,
                                                    String text, boolean expected) {
        boolean found = functionBody(compiled, functionName).contains(text);
        assertEquals("unexpected occurrence of " + text + " in " + functionName,
            expected, found);
    }

    private static String functionBody(String compiled, String functionName) {
        int start = compiled.indexOf("function " + functionName + "(");
        assertTrue("expected function " + functionName, start >= 0);
        int end = compiled.indexOf("\nend", start);
        assertTrue("unterminated function " + functionName, end >= 0);
        return compiled.substring(start, end);
    }

    private static String topLevelFunctionBodyWithPrefix(String compiled, String functionNamePrefix) {
        int start = compiled.indexOf("function " + functionNamePrefix);
        assertTrue("expected function starting with " + functionNamePrefix, start >= 0);
        int end = compiled.indexOf("\nfunction ", start + 1);
        if (end < 0) {
            end = compiled.length();
        }
        return compiled.substring(start, end);
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
     * Div/mod are lowered to portable IM functions before the optimizer runs
     * (see LuaNativeLowering#lowerDivMod). Their raw primitive calls are Lua
     * backend intrinsics, so emitted code uses // and math.fmod directly.
     */
    @Test
    public void integerDivModMatchJassSemanticsInLua() throws IOException {
        test().testLua(true).executeProg().lines(DIV_MOD_PROG);
        String compiled = compiledLua("integerDivModMatchJassSemanticsInLua");
        assertTrue("div must use Lua floor division inside the truncating correction",
            compiled.contains(" // "));
        assertTrue("mod must use math.fmod inside the ModuloInteger-compatible correction",
            compiled.contains("math.fmod("));
        assertFalse("raw numeric primitive calls must be intrinsic",
            compiled.contains("__wurst_rawF"));
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

    @Test
    public void optimizedIntegerDivModUsesLuaPrimitivesInLoop() {
        String compiled = compileOptimizedLua(
            "LuaBackendAuditTests_optimizedIntegerDivModUsesLuaPrimitivesInLoop",
            "package Test",
            "native consumeInt(int value)",
            "function run(int limit)",
            "    int x = limit",
            "    while x > 0",
            "        consumeInt(x div 8)",
            "        consumeInt(x mod 8)",
            "        x--",
            "init",
            "    run(32)"
        );
        assertTrue("optimized div must contain Lua floor division:\n" + compiled,
            compiled.contains(" // 8"));
        assertTrue("optimized mod must contain math.fmod:\n" + compiled,
            compiled.contains("math.fmod("));
        assertFalse("optimized loop must not call raw numeric helpers:\n" + compiled,
            compiled.contains("__wurst_raw"));
    }

    @Test
    public void numericIntrinsicRecognitionUsesFunctionIdentity() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function __wurst_rawFmodInt(int a, int b) returns int",
            "    return 123",
            "init",
            "    if __wurst_rawFmodInt(7, 2) == 123",
            "        testSuccess()"
        );
        String compiled = compiledLua("numericIntrinsicRecognitionUsesFunctionIdentity");
        assertTrue("an ordinary same-named function must keep its definition:\n" + compiled,
            compiled.contains("function __wurst_rawFmodInt("));
        assertFalse("an ordinary same-named call must not lower to fmod:\n" + compiled,
            compiled.contains("math.fmod("));
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
    public void optimizedStringConcatHasNoDanglingHelperCalls() {
        String compiled = compileOptimizedLua(
            "LuaBackendAuditTests_optimizedStringConcatHasNoDanglingHelperCalls",
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
        assertFalse("the tiny lowered concat helper should inline without leaving dangling calls",
            compiled.contains("__wurst_stringConcat("));
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
            "native consumeInt(int value)",
            "native consumeBool(bool value)",
            "native consumeReal(real value)",
            "native consumeString(string value)",
            "int array ints",
            "bool array bools",
            "real array reals",
            "string array strings",
            "function intDiv(int a, int b) returns int",
            "    return a div b",
            "function intMod(int a, int b) returns int",
            "    return a mod b",
            "function realMod(real a, real b) returns real",
            "    return a % b",
            "init",
            "    consumeInt(ints[1])",
            "    consumeBool(bools[1])",
            "    consumeReal(reals[1])",
            "    consumeString(strings[1])",
            "    print(\"value=\" + I2S(intDiv(7, 2)))",
            "    print(I2S(intMod(7, 2)))",
            "    print(R2S(realMod(7.5, 2.)))"
        );

        String[] helperNames = {
            "__wurst_stringConcat", "__wurst_intDiv", "__wurst_modInt", "__wurst_modReal",
            "__wurst_rawConcat"
        };
        for (String helperName : helperNames) {
            assertHelperDefinedWhenCalled(compiled, helperName);
        }
        assertTrue("repro must exercise string concat lowering", compiled.contains("__wurst_rawConcat"));
        assertTrue("repro must exercise integer div lowering", compiled.contains(" // "));
        assertTrue("repro must exercise integer and real mod lowering", compiled.contains("math.fmod("));
        assertFalse("raw numeric primitive calls must not survive Lua emission", compiled.contains("__wurst_rawF"));
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
            "native consumeInt(int value)",
            "native consumeString(string value)",
            "int array values",
            "string array names",
            "function readValue(int index) returns int",
            "    return values[index]",
            "function join(string left, string right) returns string",
            "    return left + right",
            "init",
            "    values[1] = 7",
            "    consumeInt(values[1])",
            "    consumeString(names[1])",
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

    /** Class construction allocates only a scalar id; class descriptors and field storage are static. */
    @Test
    public void classInstancesAllocateIdsWithoutInstanceTables() throws IOException {
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
        String compiled = compiledLua("classInstancesAllocateIdsWithoutInstanceTables");
        assertTrue("expected static field storage indexed by object id",
            compiled.contains("Foo_v_storage[new_inst] = 0"));
        assertTrue("expected each live id to point at its static class descriptor",
            compiled.contains("__wurst_objectClass[new_inst] = Foo"));
        assertFalse("create must not allocate an instance table", compiled.contains("local new_inst = {"));
        assertFalse("create must not attach an instance metatable", compiled.contains("setmetatable(new_inst"));
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
