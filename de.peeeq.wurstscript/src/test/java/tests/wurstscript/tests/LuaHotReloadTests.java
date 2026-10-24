package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.testng.Assert.expectThrows;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * The opt-in Lua body hot reload ({@code -luaHotReload}, docs/LUA_HOT_RELOAD.md): a payload compiled from an edited
 * program replaces bodies in the running script, which keeps its globals, objects and allocator.
 */
public class LuaHotReloadTests extends WurstScriptTest {

    private static final RunArgs HOT_RELOAD = new RunArgs().with("-lua", "-luaHotReload");

    private static final String[] PROGRAM = {
        "package Test",
        "native testSuccess()",
        "native testFail(string msg)",
        "native hotReload()",
        "int calls = 0",
        "abstract class Shape",
        "    abstract function area() returns int",
        "class Square extends Shape",
        "    int side",
        "    construct(int side)",
        "        this.side = side",
        "    override function area() returns int",
        "        return side * side",
        "class Circle extends Shape",
        "    override function area() returns int",
        "        return 3",
        "function score(int x) returns int",
        "    calls++",
        "    return x + 1",
    };

    private static String[] program(String scoreBody, String areaBody, String... rest) {
        String[] lines = PROGRAM.clone();
        lines[lines.length - 1] = "    return " + scoreBody;
        lines[12] = "        return " + areaBody;
        String[] result = new String[lines.length + rest.length];
        System.arraycopy(lines, 0, result, 0, lines.length);
        System.arraycopy(rest, 0, result, lines.length, rest.length);
        return result;
    }

    private static final String[] INIT = {
        "init",
        "    Shape square = new Square(3)",
        "    Shape circle = new Circle()",
        "    if score(1) != 2 or square.area() != 9 or circle.area() != 3",
        "        testFail(\"the original bodies did not run\")",
        "    hotReload()",
        "    if score(1) != 11",
        "        testFail(\"a call by name still runs the old body\")",
        "    if square.area() != 90",
        "        testFail(\"dispatch through the class table still runs the old body\")",
        "    if circle.area() != 3",
        "        testFail(\"a sibling's slot was rebound\")",
        "    if calls != 2",
        "        testFail(\"a global lost its value\")",
        "    Shape later = new Square(2)",
        "    if (later castTo int) != (circle castTo int) + 1 or later.area() != 40",
        "        testFail(\"the allocator restarted or a new object runs the old body\")",
        "    testSuccess()",
    };

    /** The edit replaces a function called by name and an override reached through its class table. */
    @Test
    public void replacedBodiesRunAndStateSurvives() {
        test().testLua(true).executeProg()
            .luaHotReload(program("x + 10", "side * side * 10", INIT))
            .lines(program("x + 1", "side * side", INIT));
    }

    @Test
    public void payloadCarriesTheChangedFunctionsAndTheirSlots() throws Exception {
        LuaHotReload.Manifest baseline = manifest(program("x + 1", "side * side", INIT));
        LuaHotReload.Manifest edited = manifest(program("x + 10", "side * side", INIT));
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, edited, Set.of(), 1);
        assertEquals(List.of("score"), sourceNames(payload.functions()));
        assertTrue(payload.source(), payload.source().contains("(x + 10)"));

        LuaHotReload.Payload area = LuaHotReload.payload(baseline,
            manifest(program("x + 1", "side * side * 10", INIT)), Set.of(), 1);
        assertEquals(1, area.functions().size());
        String areaFunction = area.functions().get(0);
        assertTrue("the override's class-table slot is rebound:\n" + area.source(),
            Pattern.compile("(?m)^Square_[0-9a-f]{12}\\.\\w+ = " + areaFunction + "$").matcher(area.source()).find());
        assertFalse("a sibling's slot is left alone:\n" + area.source(),
            Pattern.compile("(?m)^Circle_[0-9a-f]{12}\\.").matcher(area.source()).find());

        assertTrue("an ordinary body edit warns of nothing", payload.warnings().isEmpty());
        LuaHotReload.Payload initEdit = LuaHotReload.payload(baseline,
            manifest(program("x + 1", "side * side", initWithSquare(4))), Set.of(), 1);
        assertEquals("a changed init body ships, with a warning that it does not run again", 1, initEdit.warnings().size());
        assertTrue(initEdit.warnings().get(0), initEdit.warnings().get(0).contains("restart the map"));
        assertTrue("an unchanged program needs an empty payload",
            LuaHotReload.payload(baseline, baseline, Set.of(), 1).functions().isEmpty());
        assertEquals("a function an earlier payload replaced is replaced again, here by its original body",
            List.of("score"), sourceNames(LuaHotReload.payload(baseline, baseline, Set.of(luaName(baseline, "score")), 2).functions()));
    }

    /**
     * A type variable in a signature is part of a function's id, and the IM printer adds an identity hash to it, which
     * differs between compilations: the same program must still have the same fingerprint.
     */
    @Test
    public void genericSignaturesHaveTheSameIdInEveryCompilation() throws Exception {
        String[] generic = {
            "package Test",
            "class Box<T:>",
            "    T value",
            "function unbox<T:>(Box<T> box) returns T",
            "    return box.value",
            "init",
            "    let box = new Box<int>",
            "    box.value = 3",
            "    unbox(box)",
        };
        LuaHotReload.Manifest first = manifest(generic);
        assertTrue(first.bindings().toString(), first.bindings().stream().anyMatch(b -> b.contains("Box<T>")));
        LuaHotReload.Manifest second = manifest(generic);
        assertEquals(first.bindings(), second.bindings());
        assertEquals(first.abi(), second.abi());
        assertTrue(LuaHotReload.payload(first, second, Set.of(), 1).functions().isEmpty());
    }

    /**
     * Lua names are numbered in one namespace for functions and locals, so a new local named like a function which is
     * named later must not take that function's name: the edit only changes a body.
     */
    @Test
    public void newLocalNamedLikeALaterFunctionIsABodyEdit() throws Exception {
        String[] base = {
            "package Test",
            "function first() returns int",
            "    return 1",
            "function helper() returns int",
            "    return 2",
            "init",
            "    first()",
            "    helper()",
        };
        String[] edited = base.clone();
        edited[2] = "    let helper = 5\n    return helper";
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, manifest(edited), Set.of(), 1);
        assertEquals(List.of("first"), sourceNames(payload.functions()));
    }

    /**
     * A function a running version stores as a value keeps that body, so it is not reloadable even when the edit no
     * longer stores it: whether the baseline stored it or an earlier payload did.
     */
    @Test
    public void functionStoredAsAValueByTheRunningMapIsNotReloadable() {
        LuaHotReload.Function stored = new LuaHotReload.Function("f", "Test:f()void", "function f() return 1 end",
            List.of(), List.of("in g"), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Function edited = new LuaHotReload.Function("f", "Test:f()void", "function f() return 2 end",
            List.of(), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Function plain = new LuaHotReload.Function("f", "Test:f()void", "function f() return 1 end",
            List.of(), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Manifest current = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", edited), List.of(), List.of(),
            List.of());
        LuaHotReload.Rejected byBaseline = expectThrows(LuaHotReload.Rejected.class, () -> LuaHotReload.payload(
            new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", stored), List.of(), List.of(), List.of()), current, Set.of(), 1));
        assertTrue(byBaseline.getMessage(), byBaseline.getMessage().contains("in g"));
        LuaHotReload.Manifest plainBaseline = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", plain), List.of(), List.of(),
            List.of());
        expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(plainBaseline, current,
                new LuaHotReload.History(Set.of(), java.util.Map.of("f", LuaHotReload.definitionHash(plain))), 2));
    }

    /**
     * Removing a callback or a stored function leaves what the map registered before with its old body, which the new
     * code cannot reach: the payload applies, and warns of it, whether the baseline stored it or an earlier payload did.
     */
    @Test
    public void removedStoredFunctionWarnsFromBaselineAndHistory() throws Exception {
        LuaHotReload.Function stored = new LuaHotReload.Function("f", "Test:f()void", "function f() end",
            List.of(), List.of("in register"), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Manifest empty = new LuaHotReload.Manifest("abi", "v", java.util.Map.of(), List.of(), List.of(), List.of());
        LuaHotReload.Manifest baseline = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", stored),
            List.of(), List.of(), List.of());
        LuaHotReload.Payload original = LuaHotReload.payload(baseline, empty, LuaHotReload.History.NONE, 1);
        assertEquals(List.of("function f was removed, but what the map made before keeps its old body (in register):"
            + " restart the map to drop it."), original.warnings());
        LuaHotReload.Payload added = LuaHotReload.payload(empty, empty,
            new LuaHotReload.History(Set.of("f"), java.util.Map.of("f", LuaHotReload.definitionHash(stored))), 2);
        assertEquals(List.of("function f was removed, but what the map made before keeps its old body (by an earlier payload):"
            + " restart the map to drop it."), added.warnings());
    }

    @Test
    public void removedCallbackAdapterWarns() throws Exception {
        String[] callback = concat(MAIN_PACKAGE, new String[] {"native register(code callback)",
            "function callback()", "    bump()", "function setup()", "    register(function callback)",
            "init", "    setup()"});
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(callback), manifest(MAIN_PACKAGE), LuaHotReload.History.NONE, 1);
        // The adapter registered when the map started stays registered, with its old body, which calls its target by name.
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w ->
            w.startsWith("function __wurst_callback_callback_")
            && w.contains(" was removed, but what the map made before keeps its old body (in setup_")
            && w.endsWith("restart the map to drop it.")));
    }

    /**
     * A callback adapter gives xpcall its error handler by name on every call, as it gives its target, so a change to
     * the handler reloads: here the edit leaves ErrorHandling's error unreachable, and the handler, which called it,
     * prints instead.
     */
    @Test
    public void callbackErrorHandlerReloads() throws Exception {
        CU errorHandling = new CU("ErrorHandling", String.join("\n", "package ErrorHandling",
            "public function error(string msg)", "    skip", ""));
        String[] base = {"package Main", "import ErrorHandling", "native register(code callback)",
            "function callback()", "    skip", "function check(int x)", "    if x < 0", "        error(\"negative\")",
            "init", "    register(function callback)", "    check(1)", ""};
        String[] edited = {"package Main", "import ErrorHandling", "native register(code callback)",
            "function callback()", "    skip", "init", "    register(function callback)", ""};
        LuaHotReload.Manifest before = compileLuaHotReloadManifest(false, HOT_RELOAD, errorHandling,
            new CU("callbackErrorHandlerReloads", String.join("\n", base)));
        LuaHotReload.Manifest after = compileLuaHotReloadManifest(false, HOT_RELOAD, errorHandling,
            new CU("callbackErrorHandlerReloads", String.join("\n", edited)));
        String handler = before.functions().keySet().stream().filter(n -> n.startsWith("__wurst_callback_error"))
            .findFirst().orElseThrow();
        assertFalse("the edit changes the handler", before.functions().get(handler).definition()
            .equals(after.functions().get(handler).definition()));
        LuaHotReload.Payload payload = LuaHotReload.payload(before, after, LuaHotReload.History.NONE, 1);
        assertTrue(payload.functions().toString(), payload.functions().contains(handler));
    }

    @Test
    public void removedMethodWarnsUnlessItsSlotIsRebound() throws Exception {
        LuaHotReload.Function method = new LuaHotReload.Function("f", "Test:f()void", "function f() end",
            List.of("C.slot"), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Manifest baseline = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", method),
            List.of(), List.of(), List.of());
        LuaHotReload.Manifest empty = new LuaHotReload.Manifest("abi", "v", java.util.Map.of(), List.of(), List.of(), List.of());
        assertEquals(List.of("function f was removed, but what the map made before keeps its old body (in class-table slot"
                + " C.slot): restart the map to drop it."),
            LuaHotReload.payload(baseline, empty, LuaHotReload.History.NONE, 1).warnings());
        LuaHotReload.Function replacement = new LuaHotReload.Function("g", "Test:g()void", "function g() end",
            List.of("C.slot"), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Manifest rebound = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("g", replacement),
            List.of(), List.of(), List.of());
        assertEquals(List.of(), LuaHotReload.payload(baseline, rebound, LuaHotReload.History.NONE, 2).warnings());
    }

    @DataProvider
    public Object[][] survivingSlotCases() {
        return new Object[][] {{false, false}, {true, false}, {false, true}, {true, true}};
    }

    /**
     * A slot may outlive its relationship while the function itself remains in the edited program: objects of a class
     * which no longer binds the slot keep the body it held, which the new code cannot reach, so a changed body warns.
     */
    @Test(dataProvider = "survivingSlotCases")
    public void survivingFunctionWhichAbandonsOldSlotsWarns(boolean fromHistory, boolean rebound) throws Exception {
        LuaHotReload.Function old = new LuaHotReload.Function("f", "Test:f()int", "function f() return 1 end",
            List.of("C.slot"), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Function edited = new LuaHotReload.Function("f", "Test:f()int", "function f() return 2 end",
            List.of(), List.of(), LuaHotReload.Kind.PROGRAM);
        var before = new LuaHotReload.Manifest("abi", "v", fromHistory ? java.util.Map.of() : java.util.Map.of("f", old),
            List.of(), List.of(), List.of());
        var functions = new java.util.LinkedHashMap<String, LuaHotReload.Function>();
        functions.put("f", edited);
        if (rebound) {
            functions.put("g", new LuaHotReload.Function("g", "Test:g()int", "function g() return 3 end",
                List.of("C.slot"), List.of(), LuaHotReload.Kind.PROGRAM));
        }
        var current = new LuaHotReload.Manifest("abi", "v", functions, List.of(), List.of(), List.of());
        var history = fromHistory ? new LuaHotReload.History(Set.of("f"), java.util.Map.of(),
            java.util.Map.of("f", List.of("C.slot")), java.util.Map.of(), Set.of()) : LuaHotReload.History.NONE;
        List<String> warnings = LuaHotReload.payload(before, current, history, 2).warnings();
        assertEquals(rebound ? List.of() : List.of("function f changed, but what the map made before keeps its old body"
            + " (in class-table slot C.slot): restart the map to drop it."), warnings);
    }

    /** A function which abandons a slot but keeps its body leaves that slot holding the very body it has. */
    @Test
    public void unchangedFunctionWhichAbandonsASlotWarnsOfNothing() throws Exception {
        LuaHotReload.Function old = new LuaHotReload.Function("f", "Test:f()int", "function f() return 1 end",
            List.of("C.slot"), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Function unbound = new LuaHotReload.Function("f", "Test:f()int", "function f() return 1 end",
            List.of(), List.of(), LuaHotReload.Kind.PROGRAM);
        var before = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", old), List.of(), List.of(), List.of());
        var current = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", unbound), List.of(), List.of(), List.of());
        LuaHotReload.Payload payload = LuaHotReload.payload(before, current, LuaHotReload.History.NONE, 1);
        assertEquals(List.of(), payload.warnings());
        assertEquals(List.of(), payload.functions());
    }

    /** The session keeps the slots an earlier payload bound, so the edit which drops them says what keeps them. */
    @Test
    public void sessionRemembersSlotsOfAnEarlierPayload() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadSlots");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadSlotDelivery");
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        LuaHotReload.Manifest added = manifest(concat(MAIN_PACKAGE, new String[] {
            "interface Value", "    function get() returns int", "class Holder implements Value",
            "    override function get() returns int", "        return 3",
            "function read(Value value) returns int", "    return value.get()",
            "init", "    made += read(new Holder())",
        }));
        LuaHotReloadSession.start(build, baseline);
        LuaHotReloadSession.send(build, delivery, added);
        List<String> warnings = LuaHotReloadSession.send(build, delivery, baseline).payload().warnings();
        assertTrue(warnings.toString(), warnings.stream().anyMatch(w -> w.contains(" was removed, but what the map made before"
            + " keeps its old body (in class-table slot Holder_")));
    }

    /**
     * A closure kept in a global and removed from the source: the payload applies, the object made before runs its old
     * body through its class table, and the closures which remain work.
     */
    @Test
    public void removedClosureKeepsRunningItsOldBodyInTheMap() throws Exception {
        String[] values = {
            "public interface Value", "    function get() returns int",
            "Value kept = null",
            // Each bound to a variable of its own: closures of one signature are otherwise told apart by their order.
            "public function makeOther() returns Value", "    Value other = () -> 1", "    return other",
            "public function makeThird() returns Value", "    Value third = () -> 2", "    return third",
            "public function callKept() returns int", "    return kept.get()",
            "public function callOther() returns int", "    return makeOther().get()",
        };
        String[] base = concat(concat(MAIN_PACKAGE, values), new String[] {
            "function makeKept() returns Value", "    Value seven = () -> 7", "    return seven",
            "init", "    kept = makeKept()", "    made += callOther() + makeThird().get() + callKept()",
        });
        String[] edited = concat(concat(MAIN_PACKAGE, values), new String[] {
            "init", "    kept = makeOther()", "    made += callOther() + makeThird().get() + callKept()",
        });
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_removedClosureKeepsRunningItsOldBodyInTheMap.lua";
        LuaHotReload.Manifest current = manifest(edited);
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(base), current, LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w -> w.contains(" was removed, but what"
            + " the map made before keeps its old body (in class-table slot ") && w.endsWith("restart the map to drop it.")));
        String output = applyInLua(script, List.of(payload.source()), globalName(current, "made"),
            luaName(current, "callKept") + "()", luaName(current, "callOther") + "()");
        assertEquals(output, "true 11 7 1 nil", output);
    }

    @Test
    public void removedGlobalInitializerWarnsOfKeptValue() throws Exception {
        String[] initialized = mainWithExtra("bump()");
        String[] removed = withoutExtraInitializer(initialized);
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(initialized), manifest(removed), LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w -> w.contains("extra") && w.contains("Restart")));
    }

    @Test
    public void removedInitializerOfAnEarlierPayloadWarnsInTheMap() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_removedInitializerOfAnEarlierPayloadWarnsInTheMap.lua";
        var build = java.nio.file.Files.createTempDirectory("hotReloadInitializers");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadInitializerDelivery");
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        String[] initialized = mainWithExtra("bump()");
        LuaHotReload.Manifest added = manifest(initialized);
        LuaHotReloadSession.start(build, baseline);
        var first = LuaHotReloadSession.send(build, delivery, added).payload();
        var second = LuaHotReloadSession.send(build, delivery, manifest(withoutExtraInitializer(initialized))).payload();
        String output = applyInLua(script, List.of(first.source(), second.source()), true, globalName(added, "extra"));
        assertTrue(output, output.contains("removed initializer") && output.contains("restart the map"));
        assertEquals(output, 2, occurrences(output, "true 2 nil"));
    }

    @Test
    public void removedPackageInitStatementWarnsOfARestart() throws Exception {
        String[] baseline = mainWithExtra("bump()");
        String[] edited = baseline.clone();
        for (int i = 0; i < edited.length; i++) {
            if (edited[i].equals("    bump()")) {
                edited[i] = "";
            }
        }
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(baseline), manifest(edited), LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w -> w.contains("package init")));
    }

    @DataProvider
    public Object[][] initSequenceCases() {
        return new Object[][] {
            {"bump()\nbump()", "bump()", true},
            {"bump()", "bump()\nbump()", true},
            {"bump()\nlower()", "lower()\nbump()", true},
            {"bump()", "bump()", false},
        };
    }

    @Test(dataProvider = "initSequenceCases")
    public void packageInitDiagnosticsCompareSequences(String before, String after, boolean changed) throws Exception {
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(withInitSequence(before)), manifest(withInitSequence(after)),
            LuaHotReload.History.NONE, 1);
        assertEquals(payload.warnings().toString(), changed, payload.warnings().stream().anyMatch(w -> w.contains("package init")));
    }

    @Test
    public void reorderedExistingGlobalInitializersWarnOfARestart() throws Exception {
        String[] baseline = mainWithExtra("bump()");
        for (int i = 0; i < baseline.length; i++) {
            if (baseline[i].equals("int extra = bump()")) {
                baseline[i] += "\nint another = bump()";
            } else if (baseline[i].equals("    return extra")) {
                baseline[i] += " + another";
            }
        }
        String[] edited = baseline.clone();
        for (int i = 0; i < edited.length; i++) {
            if (edited[i].startsWith("int extra = bump()")) {
                edited[i] = "int another = bump()\nint extra = bump()";
            }
        }
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(baseline), manifest(edited), LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w -> w.contains("package init")));
    }

    /** mainWithExtra with a second global after extra, or before it. */
    private static String[] extraAndAnother(boolean swapped) {
        String[] lines = mainWithExtra("bump()");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].equals("int extra = bump()")) {
                lines[i] = swapped ? "int another = bump()\nint extra = bump()" : "int extra = bump()\nint another = bump()";
            } else if (lines[i].equals("    return extra")) {
                lines[i] += " + another";
            }
        }
        return lines;
    }

    /** Initializers which an edit swaps ran in the old order when the map started: the map warns that it keeps it. */
    @Test
    public void swappedInitializersOfTheStartedMapWarnInTheMap() throws Exception {
        test().testLua(true).luaHotReload(extraAndAnother(false)).lines(extraAndAnother(false));
        String script = "test-output/lua/LuaHotReloadTests_swappedInitializersOfTheStartedMapWarnInTheMap.lua";
        String applied = applyInLua(script, List.of(LuaHotReload.payload(manifest(extraAndAnother(false)),
            manifest(extraAndAnother(true)), LuaHotReload.History.NONE, 1).source()), true);
        assertEquals(applied, 1, occurrences(applied, "global initializer order changed"));
    }

    private static String[] tupleProgram(String value) {
        return concat(MAIN_PACKAGE, new String[] {
            "tuple Pair(int a, int b)", "int array xs = [bump(), bump()]", "Pair pair = Pair(bump(), bump())",
            "function total() returns int", "    return xs[0] + xs[1] + pair.a + pair.b + " + value, "init", "    total()"});
    }

    /**
     * A payload checks what an edit can have changed, not the whole program: an unchanged program's payload, and one for
     * an edited body, carry no check of the order of the initializers the map ran when it started, nor of storage which
     * existed then. On a large map those checks were most of every payload.
     */
    @Test
    public void payloadChecksNoUnchangedInitializers() throws Exception {
        LuaHotReload.Manifest baseline = manifest(tupleProgram("1"));
        for (LuaHotReload.Manifest current : List.of(baseline, manifest(tupleProgram("2")))) {
            String source = LuaHotReload.payload(baseline, current, LuaHotReload.History.NONE, 1).source();
            assertFalse(source, source.contains("global initializer order changed"));
            assertFalse(source, source.contains("spans old and new storage"));
        }
    }

    /** Ordering warnings follow the map's completed work, including payload-added units and missed deliveries. */
    @Test
    public void mapRemembersOrderOfPayloadAddedInitializers() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_mapRemembersOrderOfPayloadAddedInitializers.lua";
        String[] added = mainWithExtra("bump()");
        for (int i = 0; i < added.length; i++) {
            if (added[i].equals("int extra = bump()")) {
                added[i] += "\nint another = bump()";
            } else if (added[i].equals("    return extra")) {
                added[i] += " + another";
            }
        }
        String[] reordered = added.clone();
        for (int i = 0; i < reordered.length; i++) {
            if (reordered[i].startsWith("int extra = bump()")) {
                reordered[i] = "int another = bump()\nint extra = bump()";
            }
        }
        var baseline = manifest(MAIN_PACKAGE);
        var first = manifest(added);
        var second = manifest(reordered);
        String p1 = LuaHotReload.payload(baseline, first, LuaHotReload.History.NONE, 1).source();
        String p2 = LuaHotReload.payload(baseline, second, LuaHotReload.History.NONE, 2).source();
        String[] globals = {globalName(first, "made"), globalName(first, "extra"), globalName(first, "another")};
        String applied = applyInLua(script, List.of(p1, p2), true, globals);
        assertEquals(applied, 1, occurrences(applied, "global initializer order changed"));
        assertEquals(applied, 2, occurrences(applied, "true 3 2 3 nil"));
        String missed = applyInLua(script, List.of(p2), true, globals);
        assertFalse(missed, missed.contains("global initializer order changed"));
        assertEquals(missed, "true 3 3 2 nil", missed);
    }

    private static String[] mainAndLater(boolean extra) {
        String[] main = extra ? mainWithExtra("bump()") : MAIN_PACKAGE;
        return concat(concat(main, new String[] {"endpackage"}), new String[] {
            "package Later", "import Main", "int late = bump()", "init", "    bump()", "endpackage"});
    }

    /**
     * A global added to a package which the map initialised before another runs when the payload comes, after the
     * other package's globals: that is no change of their order, and the map warns of none.
     */
    @Test
    public void addedGlobalBeforeALaterPackageIsNoReorder() throws Exception {
        test().testLua(true).luaHotReload(mainAndLater(false)).lines(mainAndLater(false));
        String script = "test-output/lua/LuaHotReloadTests_addedGlobalBeforeALaterPackageIsNoReorder.lua";
        LuaHotReload.Manifest baseline = manifest(mainAndLater(false));
        LuaHotReload.Manifest added = manifest(mainAndLater(true));
        String applied = applyInLua(script, List.of(LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 2).source()), true, globalName(added, "extra"));
        assertFalse(applied, applied.contains("global initializer order changed"));
        assertEquals(applied, 2, occurrences(applied, "true 4 nil"));
    }

    @DataProvider
    public Object[][] uninitializedGlobalCases() {
        return new Object[][] {
            {"int extra", "int extra = bump()", "extra = 42", "extra"},
            {"int array xs", "int array xs = [bump(), bump()]", "xs[0] = 42", "xs[0] + xs[1]"},
            {"Pair extra", "Pair extra = Pair(bump(), bump())", "extra = Pair(40, 2)", "extra.a + extra.b"},
        };
    }

    @Test(dataProvider = "uninitializedGlobalCases")
    public void existingUninitializedGlobalsKeepTheirValues(String declaration, String initialized, String store, String read) throws Exception {
        String[] prefix = java.util.Arrays.copyOf(MAIN_PACKAGE, 5);
        String[] base = concat(prefix, new String[] {"tuple Pair(int a, int b)", declaration,
            "function readExtra() returns int", "    return " + read, "init", "    bump()", "    " + store, "    readExtra()"});
        String[] edited = base.clone();
        for (int i = 0; i < edited.length; i++) {
            if (edited[i].equals(declaration)) {
                edited[i] = initialized;
            }
        }
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_existingUninitializedGlobalsKeepTheirValues.lua";
        var current = manifest(edited);
        var payload = LuaHotReload.payload(manifest(base), current, LuaHotReload.History.NONE, 1);
        String output = applyInLua(script, List.of(payload.source()), globalName(current, "made"), luaName(current, "readExtra") + "()");
        assertEquals(output, "true 1 42 nil", output);
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w -> w.contains("keeps") || w.contains("kept") || w.contains("keeps the values")));
    }

    @Test
    public void packageWithoutAnInitKeepsItsExistingGlobal() throws Exception {
        String[] base = {"package Main", "public int extra", "public function readExtra() returns int", "    return extra"};
        String[] edited = base.clone();
        edited[1] = "public int extra = 7";
        // A separate entry unit keeps Main's global live without giving Main its own package init.
        String entry = "endpackage\npackage Entry\nimport Main\ninit\n    extra = 42\n    readExtra()\nendpackage";
        base = concat(base, new String[] {entry});
        edited = concat(edited, new String[] {entry});
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_packageWithoutAnInitKeepsItsExistingGlobal.lua";
        var current = manifest(edited);
        var payload = LuaHotReload.payload(manifest(base), current, LuaHotReload.History.NONE, 1);
        String output = applyInLua(script, List.of(payload.source()), globalName(current, "extra"));
        assertEquals(output, "true 42 nil", output);
    }

    @Test
    public void earlierPayloadsUninitializedGlobalKeepsItsValue() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_earlierPayloadsUninitializedGlobalKeepsItsValue.lua";
        CU main = new CU("Main", String.join("\n", MAIN_PACKAGE).replace("package Main", "package Main\nimport Store")
            + "\ninit\n    getExtra()\n");
        CU uninitialized = new CU("Store", "package Store\npublic int extra\npublic function getExtra() returns int\n    return extra\n");
        CU initialized = new CU("Store", "package Store\npublic int extra = 7\npublic function getExtra() returns int\n    return extra\n");
        var baseline = manifest(MAIN_PACKAGE);
        var first = compileLuaHotReloadManifest(false, HOT_RELOAD, main, uninitialized);
        var second = compileLuaHotReloadManifest(false, HOT_RELOAD, main, initialized);
        String output = applyInLua(script, List.of(
            LuaHotReload.payload(baseline, first, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, second, LuaHotReload.History.NONE, 2).source()), true, globalName(first, "extra"));
        assertEquals(output, 2, occurrences(output, "true 0 nil"));
        assertTrue(output, output.contains("keeps its value"));
    }

    @Test
    public void genericStaticWithoutAnInitializerKeepsItsValues() {
        String[] base = genericStaticProgram(false);
        test().testLua(true).executeProg().luaHotReload(genericStaticProgram(true)).lines(base);
    }

    /**
     * A new specialisation of a generic static in a package an earlier payload added: its storage is a unit of its own,
     * so it is initialised alone and the specialisations the earlier payload installed keep their values. (One unit
     * used to initialise every specialisation, so this needed a restart.)
     */
    @Test
    public void newGenericSpecializationInAnEarlierPayloadsPackage() throws Exception {
        String[] first = concat(java.util.Arrays.copyOf(genericStaticProgram(true), 14), new String[] {
            "init", "    let ints = new Store<int>()", "    let strings = new Store<string>()",
            "    ints.put(42)", "    strings.put(\"saved\")",
            "    if ints.read() != 42 or strings.read() != \"saved\"", "        testFail(\"values changed\")",
            "    destroy ints", "    destroy strings"});
        String[] second = first.clone();
        second[second.length - 1] += "\n    let reals = new Store<real>()\n    reals.put(3.5)\n"
            + "    if reals.read() != 3.5\n        testFail(\"value changed\")\n    destroy reals";
        LuaHotReload.payload(manifest(first), manifest(second), LuaHotReload.History.NONE, 1);

        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_newGenericSpecializationInAnEarlierPayloadsPackage.lua";
        var baseline = manifest(MAIN_PACKAGE);
        var edited = manifest(second);
        String output = applyInLua(script, List.of(
            LuaHotReload.payload(baseline, manifest(first), LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, edited, LuaHotReload.History.NONE, 2).source()), true,
            globalName(edited, "Store_value_integer_"), globalName(edited, "Store_value_string_"),
            globalName(edited, "Store_value_real_"));
        // The erased initialize<real>() answers nil on Lua, as it does in a restarted map.
        assertEquals(output, 2, occurrences(output, "true 42 saved nil nil"));
        assertFalse(output, output.contains("false"));
    }

    private static String[] genericStaticProgram(boolean initialized) {
        return new String[] {"package Test", "native hotReload()", "native testSuccess()", "native testFail(string msg)",
            "int initialized = 0", "function initialize<T:>() returns T", "    initialized++", "    return null",
            "class Store<T:>", initialized ? "    static T value = initialize<T>()" : "    static T value",
            "    function put(T v)", "        value = v", "    function read() returns T", "        return value",
            "init", "    let ints = new Store<int>()", "    let strings = new Store<string>()",
            "    ints.put(42)", "    strings.put(\"saved\")", "    hotReload()",
            "    if initialized != 0 or ints.read() != 42 or strings.read() != \"saved\"",
            "        testFail(\"generic static values were reinitialized\")", "    destroy ints", "    destroy strings", "    testSuccess()"};
    }

    /**
     * An initializer added to a generic static, in the edit which also adds a specialisation: the specialisation the map
     * has keeps its value (with the warning a global gets whose initializer was added), and only the new one is
     * initialised. Each specialisation is a unit of its own, so no unit spans old and new storage; when one unit
     * initialised both, this was refused.
     */
    @Test
    public void newlyInitializedGenericStorageInitializesOnlyTheNewSpecialization() throws Exception {
        var uninitialized = manifest(genericStorageProgram(false, false));
        var initialized = manifest(genericStorageProgram(true, true));
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_newlyInitializedGenericStorageInitializesOnlyTheNewSpecialization.lua";
        var baseline = manifest(MAIN_PACKAGE);
        String first = LuaHotReload.payload(baseline, uninitialized, LuaHotReload.History.NONE, 1).source();
        String second = LuaHotReload.payload(baseline, initialized, LuaHotReload.History.NONE, 2).source();
        String value = globalName(uninitialized, "Store_value_integer_");
        String initializedCount = globalName(initialized, "initialized");
        String newStorage = globalName(initialized, "Store_value_real_");
        String output = applyInLua(script, List.of(first, second), true, value, initializedCount, newStorage);
        // The erased initialize<real>() answers nil on Lua; the count shows it ran once, for the new specialisation.
        assertTrue(output, output.startsWith("true 42 0 nil nil"));
        assertTrue(output, output.endsWith("true 42 1 nil nil"));
        assertTrue(output, output.contains("keeps its value"));
        String skipped = applyInLua(script, List.of(second), initializedCount);
        assertEquals(skipped, "true 2 nil", skipped);
        var direct = LuaHotReload.payload(uninitialized, initialized, LuaHotReload.History.NONE, 1);
        assertTrue(direct.warnings().toString(), direct.warnings().stream().anyMatch(w -> w.contains(value)));
    }

    /**
     * A first use of a generic class with another type argument initialises only the new specialisation's static, as a
     * restarted map would: each specialisation is an initializer unit of its own (on Jass each is a copy with its own
     * initializer). The specialisation the map has keeps its value, and the initializer runs once, for the new one.
     */
    @Test
    public void newSpecializationOfAGenericStaticIsInitializedAlone() throws Exception {
        String[] base = specializationProgram(false);
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_newSpecializationOfAGenericStaticIsInitializedAlone.lua";
        var baseline = manifest(base);
        var current = manifest(specializationProgram(true));
        var payload = LuaHotReload.payload(baseline, current, LuaHotReload.History.NONE, 1);
        String counter = globalName(current, "counter");
        String ints = globalName(baseline, "Store_made_integer_");
        String reals = globalName(current, "Store_made_real_");
        assertEquals(ints, globalName(current, "Store_made_integer_"));
        String output = applyInLua(script, List.of(payload.source()), true, counter, ints, reals);
        // The map started with counter 1 and the int store's 1, bumped to 101; the payload runs count() once more.
        assertTrue(output, output.endsWith("true 2 101 2 nil"));
        assertFalse(output, output.contains("keeps") || output.contains("restart"));
    }

    /**
     * Each element of an array initializer of a generic static is computed for every specialisation before the next
     * element, as in a build without -luaHotReload; units of their own would compute all of one specialisation first.
     * So an initializer with an effect stays one unit, and its counter reads the same in a hot-reload build.
     */
    @Test
    public void effectfulGenericArrayInitializerKeepsItsOrder() {
        String[] program = {
            "package Test", "native testSuccess()", "native testFail(string msg)",
            "int counter = 0", "function count() returns int", "    counter++", "    return counter",
            "class Store<T:>", "    static int array values = [count(), count()]",
            "    function first() returns int", "        return values[0]",
            "    function second() returns int", "        return values[1]",
            "init", "    let a = new Store<int>()", "    let b = new Store<real>()",
            // Interleaved, the specialisations' first elements are 1 and 2, their second 3 and 4.
            "    if a.first() - b.first() != 1 and b.first() - a.first() != 1",
            "        testFail(\"first elements not computed one after the other\")",
            "    if a.second() - b.second() != 1 and b.second() - a.second() != 1",
            "        testFail(\"second elements not computed one after the other\")",
            "    destroy a", "    destroy b", "    testSuccess()"};
        test().testLua(true).executeProg().luaHotReload(program).lines(program);
    }

    /** The removed specialisation's storage stays, unused; the other keeps its value and nothing runs again. */
    @Test
    public void removedSpecializationOfAGenericStaticKeepsTheOthers() throws Exception {
        String[] base = specializationProgram(true);
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_removedSpecializationOfAGenericStaticKeepsTheOthers.lua";
        var baseline = manifest(base);
        var current = manifest(specializationProgram(false));
        var payload = LuaHotReload.payload(baseline, current, LuaHotReload.History.NONE, 1);
        String output = applyInLua(script, List.of(payload.source()), true, globalName(current, "counter"),
            globalName(current, "Store_made_integer_"), globalName(baseline, "Store_made_real_"));
        assertTrue(output, output.endsWith("true 2 101 2 nil"));
    }

    private static String[] specializationProgram(boolean reals) {
        String[] program = {"package Test", "native testFail(string msg)",
            "int counter = 0", "function count() returns int", "    counter++", "    return counter",
            "class Store<T:>", "    static int made = count()", "    function bump()", "        made += 100",
            "    function read() returns int", "        return made",
            "init", "    let ints = new Store<int>()", "    ints.bump()",
            "    if ints.read() < 100", "        testFail(\"not bumped\")", "    destroy ints"};
        if (reals) {
            program = concat(program, new String[] {"    let reals = new Store<real>()",
                "    if reals.read() <= 0", "        testFail(\"not initialized\")", "    destroy reals"});
        }
        return program;
    }

    private static String[] genericStorageProgram(boolean initialized, boolean secondSpecialization) {
        String[] declarations = java.util.Arrays.copyOf(genericStaticProgram(initialized), 14);
        String[] init = {"init", "    let ints = new Store<int>()", "    ints.put(42)",
            "    if ints.read() != 42", "        testFail(\"value changed\")", "    destroy ints"};
        if (secondSpecialization) {
            // A real specialization supplies a second physical store while sharing the same source initializer.
            init = concat(init, new String[] {"    let reals = new Store<real>()", "    reals.put(3.5)",
                "    if reals.read() != 3.5", "        testFail(\"value changed\")", "    destroy reals"});
        }
        return concat(concat(declarations, init), new String[] {
            "    if initialized < 0", "        testFail(\"initializer count changed\")"});
    }

    private static String[] withInitSequence(String statements) {
        String[] lines = mainWithExtra("bump()");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].equals("    bump()")) {
                lines[i] = "    " + statements.replace("\n", "\n    ");
            }
        }
        return concat(lines, new String[] {"function lower() returns int", "    made--", "    return made"});
    }

    @Test
    public void removedEarlierPackageInitWarnsInTheMap() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_removedEarlierPackageInitWarnsInTheMap.lua";
        var build = java.nio.file.Files.createTempDirectory("hotReloadPackageRemoval");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadPackageRemovalDelivery");
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        LuaHotReload.Manifest added = compileLuaHotReloadManifest(false, HOT_RELOAD,
            new CU("Main", String.join("\n", MAIN_PACKAGE) + "\n"),
            new CU("Added", "package Added\nimport Main\ninit\n    bump()\n"));
        LuaHotReloadSession.start(build, baseline);
        var first = LuaHotReloadSession.send(build, delivery, added).payload();
        var second = LuaHotReloadSession.send(build, delivery, baseline).payload();
        String output = applyInLua(script, List.of(first.source(), second.source()), true, globalName(baseline, "made"));
        assertTrue(output, output.contains("removed package init") && output.contains("restart the map"));
        assertEquals(output, 2, occurrences(output, "true 2 nil"));
    }

    private static String[] withoutExtraInitializer(String[] initialized) {
        String[] removed = initialized.clone();
        for (int i = 0; i < removed.length; i++) {
            if (removed[i].startsWith("int extra =")) {
                removed[i] = "int extra";
            }
        }
        return removed;
    }

    /**
     * A hot-reload build names each declaration from its identity, so declarations added in front of others, even ones
     * of the same names in another package, rename nothing that the running script has.
     */
    @Test
    public void addedDeclarationsRenameNothing() {
        CU other = new CU("Other", String.join("\n", "package Other", "public int calls = 0",
            "public function score(int x) returns int", "    return x", "public class Square", "init", "    score(calls)", "    new Square()", ""));
        String[] base = program("x + 1", "side * side", INIT);
        String[] added = base.clone();
        added[4] = "int extra = 3\nfunction helper(int x) returns int\n    return x * extra\nclass Extra\n    int f\n" + base[4];
        LuaHotReload.Manifest before = compileLuaHotReloadManifest(false, HOT_RELOAD,
            new CU("addedDeclarationsRenameNothing", String.join("\n", base) + "\n"));
        LuaHotReload.Manifest after = compileLuaHotReloadManifest(false, HOT_RELOAD, other,
            new CU("addedDeclarationsRenameNothing", String.join("\n", added) + "\n"));
        java.util.List<String> lost = new java.util.ArrayList<>(before.bindings());
        lost.removeAll(after.bindings());
        assertTrue("every name the first build had means the same in the second: " + lost, lost.isEmpty());
    }

    /**
     * Locals are numbered within their function: with stack traces every function has the position parameter, and a
     * function added in front of others must not renumber theirs, which would change every body and initialisation.
     */
    @Test
    public void addedFunctionLeavesOtherFunctionsAlone() throws Exception {
        RunArgs traced = HOT_RELOAD.with("-stacktraces");
        String[] base = program("x + 1", "side * side", INIT);
        // At the end of the file: a function in front would move the source positions of the ones after it.
        String[] added = concat(program("added(x)", "side * side", INIT),
            new String[] {"function added(int x) returns int", "    let y = x + 1", "    return y"});
        LuaHotReload.Manifest before = compileLuaHotReloadManifest(false, traced,
            new CU("addedFunctionLeavesOtherFunctionsAlone", String.join("\n", base) + "\n"));
        LuaHotReload.Manifest after = compileLuaHotReloadManifest(false, traced,
            new CU("addedFunctionLeavesOtherFunctionsAlone", String.join("\n", added) + "\n"));
        List<String> changed = new java.util.ArrayList<>();
        for (LuaHotReload.Function f : before.functions().values()) {
            LuaHotReload.Function now = after.functions().get(f.name());
            if (now != null && !now.definition().equals(f.definition())) {
                changed.add(f.name());
            }
        }
        assertEquals("only the edited function's text changes", List.of("score"), sourceNames(changed));
    }

    /**
     * The flatten names its temporaries within their function: a temporary added to one function must not renumber
     * those of the functions flattened after it, which would change their text and ship them in every payload.
     */
    @Test
    public void addedTemporaryLeavesOtherFunctionsAlone() {
        LuaHotReload.Manifest before = compileLuaHotReloadManifest(false, HOT_RELOAD,
            new CU("addedTemporaryLeavesOtherFunctionsAlone", String.join("\n", temporaries("x + 1")) + "\n"));
        // The nested call is evaluated into a temporary, because the conditional after it has statements.
        LuaHotReload.Manifest after = compileLuaHotReloadManifest(false, HOT_RELOAD,
            new CU("addedTemporaryLeavesOtherFunctionsAlone",
                String.join("\n", temporaries("add(add(x, 1), x > 0 ? 1 : 2)")) + "\n"));
        String second = before.functions().get(luaName(before, "second")).definition();
        assertTrue("the other function has temporaries of both kinds: " + second,
            second.contains("andLeft") && second.contains("temp"));
        List<String> changed = new java.util.ArrayList<>();
        for (LuaHotReload.Function f : before.functions().values()) {
            LuaHotReload.Function now = after.functions().get(f.name());
            if (now != null && !now.definition().equals(f.definition())) {
                changed.add(f.name());
            }
        }
        assertEquals("only the edited function's text changes", List.of("first"), sourceNames(changed));
    }

    private static String[] temporaries(String firstBody) {
        return new String[] {
            "package Test",
            "function add(int a, int b) returns int",
            "    return a + b",
            "function first(int x) returns int",
            "    return " + firstBody,
            "function second(int x) returns int",
            "    if x > 0 and add(add(x, 1), x > 1 ? 1 : 2) > 2",
            "        return 1",
            "    return 0",
            "init",
            "    first(1)",
            "    second(2)",
        };
    }

    /** Overloads of a method are separate declarations: each dispatch stub is named from its own signature. */
    @Test
    public void overloadedMethodsHaveTheirOwnNames() throws IOException {
        String[] lines = {
            "package Test",
            "native testSuccess()",
            "native hotReload()",
            "abstract class A",
            "    abstract function f(int x) returns int",
            "    abstract function f(string x) returns int",
            "class B extends A",
            "    override function f(int x) returns int",
            "        return 1",
            "    override function f(string x) returns int",
            "        return 2",
            "function getA() returns A",
            "    return new B()",
            "init",
            "    if getA().f(1) == 1 and getA().f(\"s\") == 2",
            "        testSuccess()",
        };
        test().testLua(true).executeProg().luaHotReload(lines).lines(lines);
        String compiled = compiledLua("overloadedMethodsHaveTheirOwnNames");
        java.util.regex.Matcher stubs = Pattern.compile("(?m)^function (dispatch_\\w+)\\(").matcher(compiled);
        List<String> names = new java.util.ArrayList<>();
        while (stubs.find()) {
            names.add(stubs.group(1));
        }
        assertEquals(compiled, 2, names.size());
        for (String name : names) {
            assertTrue("named from its identity, not numbered: " + names, name.matches(".*_[0-9a-f]{12}"));
        }
    }

    /**
     * {@code P4769:C.C_x} and {@code P8706:C.C_x} share the first six hex digits of their hashes. Adding the second
     * renames nothing of the first, and the names do not depend on the order of the compilation units.
     */
    @Test
    public void addedDeclarationWithSimilarHashRenamesNothing() {
        CU first = new CU("P4769", String.join("\n", "package P4769", "public class C", "    int x = 1", "init", "    let c = new C()", "    c.x = c.x + 1", ""));
        CU second = new CU("P8706", String.join("\n", "package P8706", "public class C", "    int x = 2", "init", "    let c = new C()", "    c.x = c.x + 1", ""));
        LuaHotReload.Manifest alone = compileLuaHotReloadManifest(false, HOT_RELOAD, first);
        LuaHotReload.Manifest forward = compileLuaHotReloadManifest(false, HOT_RELOAD, first, second);
        LuaHotReload.Manifest backward = compileLuaHotReloadManifest(false, HOT_RELOAD, second, first);
        assertEquals(new java.util.TreeSet<>(forward.bindings()), new java.util.TreeSet<>(backward.bindings()));
        assertTrue(alone.bindings().toString(), forward.bindings().containsAll(alone.bindings()));
        java.util.Set<String> prefixes = forward.bindings().stream()
            .filter(b -> b.matches("C_x_storage_[0-9a-f]{12}=P(4769|8706):C\\.C_x.*"))
            .map(b -> b.substring("C_x_storage_".length(), "C_x_storage_".length() + 6))
            .collect(java.util.stream.Collectors.toSet());
        assertEquals("both stores, sharing six digits: " + forward.bindings(), 1, prefixes.size());
    }

    /** A class of the same name from another package is another type. */
    @Test
    public void fieldTypeOfAnotherPackagesClassIsRejected() {
        CU first = new CU("First", String.join("\n", "package First", "public class Item", "init", "    new Item()", ""));
        CU second = new CU("Second", String.join("\n", "package Second", "public class Item", "init", "    new Item()", ""));
        String holder = String.join("\n", "package Test", "import %s", "class Holder", "    Item held = null",
            "function read(Holder h) returns int", "    return h.held castTo int", "init", "    read(new Holder())", "");
        LuaHotReload.Manifest baseline = compileLuaHotReloadManifest(false, HOT_RELOAD, first, second,
            new CU("Test", String.format(holder, "First")));
        LuaHotReload.Manifest changed = compileLuaHotReloadManifest(false, HOT_RELOAD, first, second,
            new CU("Test", String.format(holder, "Second")));
        expectThrows(LuaHotReload.Rejected.class, () -> LuaHotReload.payload(baseline, changed, Set.of(), 1));
    }

    /** A field which holds objects of another class now: objects which exist keep values of the old one. */
    @Test
    public void fieldTypeChangeIsRejected() {
        String[] base = {
            "package Test",
            "class A",
            "class B",
            "class Holder",
            "    A held = null",
            "function read(Holder h) returns int",
            "    return h.held castTo int",
            "init",
            "    read(new Holder())",
        };
        String[] edited = base.clone();
        edited[4] = "    B held = null";
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReload.Manifest changed = manifest(edited);
        assertFalse(baseline.abi().equals(changed.abi()));
        expectThrows(LuaHotReload.Rejected.class, () -> LuaHotReload.payload(baseline, changed, Set.of(), 1));
    }

    /** A variable-bound closure has its own source identity, so a sibling addition preserves existing bindings. */
    @Test
    public void namedClosureAddedBesideAnotherOfTheSameMethodIsAccepted() throws Exception {
        String[] base = {
            "package Test",
            "interface Op",
            "    function apply(int v) returns int",
            "function run() returns int",
            "    Op a = (int v) -> v + 1",
            "    Op b = (int v) -> v + 2",
            "    return a.apply(1) + b.apply(1)",
            "init",
            "    run()",
        };
        String[] edited = {
            "package Test",
            "interface Op",
            "    function apply(int v) returns int",
            "function run() returns int",
            "    Op z = (int v) -> v + 3",
            "    Op a = (int v) -> v + 1",
            "    Op b = (int v) -> v + 2",
            "    return z.apply(1) + a.apply(1) + b.apply(1)",
            "init",
            "    run()",
        };
        var baseline = manifest(base);
        var inserted = manifest(edited);
        assertTrue(inserted.functions().keySet().containsAll(baseline.functions().keySet()));
        LuaHotReload.payload(baseline, inserted, Set.of(), 1);
        String swap = edited[5];
        edited[5] = edited[6];
        edited[6] = swap;
        var reordered = manifest(edited);
        assertTrue(reordered.functions().keySet().containsAll(baseline.functions().keySet()));
        LuaHotReload.payload(baseline, reordered, Set.of(), 1);
    }

    @DataProvider
    public Object[][] namedClosureCases() {
        return new Object[][] {{"apply", false}, {"_apply", false}, {"apply", true}, {"_apply", true}};
    }

    @Test(dataProvider = "namedClosureCases")
    public void namedClosureAdditionPreservesOldCapturesAndUpdatesTheirBodies(String method, boolean nested) {
        String[] base = namedClosureProgram(false, nested);
        String[] edited = namedClosureProgram(true, nested);
        for (int i = 0; i < base.length; i++) {
            base[i] = base[i].replace("apply(", method + "(");
            edited[i] = edited[i].replace("apply(", method + "(");
        }
        test().testLua(true).executeProg().luaHotReload(edited).lines(base);
    }

    private static String[] namedClosureProgram(boolean edited, boolean nested) {
        return new String[] {"package Test", "native hotReload()", "native testSuccess()", "native testFail(string msg)",
            "interface Op", "    function apply(int v) returns int", "Op savedA", "Op savedB", "int siblingScore",
            "function install(int seed)",
            edited ? "    Op z = " + closureExpression("seed + v + 300", nested) : "",
            "    Op a = " + closureExpression("seed + v" + (edited ? " + 100" : ""), nested),
            "    Op b = " + closureExpression("seed * 10 + v" + (edited ? " + 200" : ""), nested),
            "    savedA = a", "    savedB = b",
            edited ? "    siblingScore = z.apply(2)\n    destroy z" : "",
            "init", "    install(3)", "    let oldA = savedA", "    let oldB = savedB",
            "    if oldA.apply(1) != 4 or oldB.apply(1) != 31", "        testFail(\"original closures failed\")",
            "    hotReload()", "    if oldA.apply(1) != 104 or oldB.apply(1) != 231",
            "        testFail(\"existing closure bodies or captures changed identity\")", "    install(5)",
            "    if savedA.apply(1) != 106 or savedB.apply(1) != 251 or siblingScore != 307",
            "        testFail(\"new closures failed\")", "    destroy oldA", "    destroy oldB",
            "    destroy savedA", "    destroy savedB", "    testSuccess()"};
    }

    private static String closureExpression(String expression, boolean nested) {
        return nested ? "(int v) -> begin\n        Op inner = (int x) -> " + expression.replace("v", "x")
            + "\n        let result = inner.apply(v)\n        destroy inner\n        return result\n    end"
            : "(int v) -> " + expression;
    }

    @Test
    public void reorderedOverloadsKeepTheirNamedClosuresAndCaptures() {
        test().testLua(true).executeProg().luaHotReload(overloadedClosureProgram(true)).lines(overloadedClosureProgram(false));
    }

    private static String[] overloadedClosureProgram(boolean edited) {
        String first = "function make(int seed) returns Op\n    Op action = (int v) -> seed + v"
            + (edited ? " + 100" : "") + "\n    return action";
        String second = "function make(string text, int seed) returns Op\n    Op action = (int v) -> seed * 10 + v"
            + (edited ? " + 200" : "") + "\n    return action";
        return new String[] {"package Test", "native hotReload()", "native testSuccess()", "native testFail(string msg)",
            "interface Op", "    function apply(int v) returns int", edited ? second : first, edited ? first : second,
            "init", "    let a = make(3)", "    let b = make(\"old\", 4)",
            "    if a.apply(1) != 4 or b.apply(1) != 41", "        testFail(\"original overload closures failed\")",
            "    hotReload()", "    if a.apply(1) != 104 or b.apply(1) != 241",
            "        testFail(\"overloaded closure identities swapped\")", "    let c = make(5)", "    let d = make(\"new\", 6)",
            "    if c.apply(1) != 106 or d.apply(1) != 261", "        testFail(\"new overloaded closures failed\")",
            "    destroy a", "    destroy b", "    destroy c", "    destroy d", "    testSuccess()"};
    }

    @Test
    public void closureOwnerDistinguishesSameNamedNestedParameterTypes() throws Exception {
        var baseline = manifest(nestedParameterClosure(false));
        var edited = manifest(nestedParameterClosure(true));
        // The closure of make(B.N) is another one than that of make(A.N), not an edit of it: the old one is removed, and
        // the object made from it keeps its body (one identity for both would bind a name to another type instead).
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, edited, LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w -> w.contains(" was removed, but what"
            + " the map made before keeps its old body (in class-table slot ")));
    }

    private static String[] nestedParameterClosure(boolean edited) {
        String type = edited ? "B.N" : "A.N";
        return new String[] {"package Test", "interface Op", "    function apply(int v) returns int",
            "class A", "    static class N", "        int value\n    static function create() returns N\n        return new N()",
            "class B", "    static class N", "        int value\n    static function create() returns N\n        return new N()",
            "A.N a", "B.N b", "Op saved", "function source() returns " + type, edited ? "    return b" : "    return a",
            "function make(" + type + " item) returns Op", "    Op action = (int v) -> item.value + v" + (edited ? " + 100" : ""),
            "    return action", "function read() returns int", "    return saved.apply(1)", "init",
            "    a = A.create()", "    b = B.create()", "    a.value = 3", "    b.value = 4",
            "    saved = make(source())", "    read()"};
    }

    @Test
    public void nestedClassFieldTypeChangesRequireARestart() throws Exception {
        String[] base = nestedParameterClosure(false);
        base[0] += "\nclass Holder\n    A.N held\nfunction useHolder(Holder h) returns int\n    return h.held.value";
        base[base.length - 1] += "\n    let h = new Holder()\n    h.held = a\n    useHolder(h)\n    destroy h";
        String[] edited = base.clone();
        edited[0] = base[0].replace("A.N held", "B.N held");
        edited[edited.length - 1] = base[base.length - 1].replace("h.held = a", "h.held = b");
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(manifest(base), manifest(edited), LuaHotReload.History.NONE, 1));
        // Two inner classes N in one package are told apart by the classes they are declared in.
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("Holder.Holder_held:Test:B.N")
            && rejected.getMessage().contains("instead of Test:Holder.Holder_held:Test:A.N"));
    }

    /**
     * Closures told apart by their order alone (callbacks of one interface in one function) are matched by that order:
     * one added after them reloads; one added before them would move the others, and the map refuses that.
     */
    @Test
    public void siblingClosureAppendedOrInsertedBeforeReloads() throws Exception {
        String[] base = {"package Test", "interface Op", "    function apply(int v) returns int",
            "function consume(Op op) returns int", "    let result = op.apply(1)", "    destroy op", "    return result",
            "function run() returns int", "    return consume((int v) -> v + 1) + consume((int v) -> v + 2)",
            "init", "    run()"};
        String[] appended = base.clone();
        appended[8] += " + consume((int v) -> v + 3)";
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(base), manifest(appended), Set.of(), 1);
        assertTrue(payload.warnings().toString(), payload.warnings().isEmpty());
        assertFalse("an appended closure moves no other's tables:\n" + payload.source(),
            payload.source().contains("__wurst_hotreload_lineages"));
        String[] inserted = base.clone();
        inserted[8] = "    return consume((int v) -> v + 9) + consume((int v) -> v + 1) + consume((int v) -> v + 2)";
        LuaHotReload.Payload insertion = LuaHotReload.payload(manifest(base), manifest(inserted), Set.of(), 1);
        assertTrue(insertion.warnings().toString(), insertion.warnings().isEmpty());
        assertTrue("the names of the others take their tables along:\n" + insertion.source(),
            insertion.source().contains("__wurst_hotreload_lineages"));
    }

    /**
     * A dispatch slot is named after its group, not after the order the groups are named in. Interfaces with a method
     * of one name (`run`) had their slots numbered in the order of their classes' names (`run`, `run1`, ...), so an
     * interface added before another renumbered its slot, the closures bound to it lost their binding, and the edit
     * was refused: in a large map's history it was most of the closure refusals.
     */
    @Test
    public void addedInterfaceWithAMethodOfTheSameNameKeepsTheOthersSlots() throws Exception {
        String[] base = {"package Test", "interface Alpha", "    function run() returns int",
            "interface Zeta", "    function run() returns int",
            "function callA(Alpha a) returns int", "    return a.run()",
            "function callZ(Zeta z) returns int", "    return z.run()",
            // Two closures of each, so the calls dispatch through the interfaces' slots.
            "function combine() returns int", "    return callA(() -> 1) + callA(() -> 2) + callZ(() -> 3) + callZ(() -> 4)",
            "init", "    combine()"};
        String[] edited = base.clone();
        edited[10] = "    return callA(() -> 1) + callA(() -> 2) + callZ(() -> 3) + callZ(() -> 4) + callB(() -> 5) + callB(() -> 6)";
        edited = concat(edited, new String[] {"interface Beta", "    function run() returns int",
            "function callB(Beta b) returns int", "    return b.run()"});
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(base), manifest(edited), LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().isEmpty());
    }

    /**
     * A group is named after its roots, the methods none of its others override: a class added which implements an
     * interface and sorts before it (`AaaImpl` before `Zeta`) leaves the interface's slot as it was.
     */
    @Test
    public void addedImplementationSortingFirstKeepsTheSlot() throws Exception {
        String[] base = {"package Test", "interface Zeta", "    function run() returns int",
            "function callZ(Zeta z) returns int", "    return z.run()",
            "function combine() returns int", "    return callZ(() -> 1) + callZ(() -> 2)", "init", "    combine()"};
        String[] edited = base.clone();
        edited[6] = "    return callZ(() -> 1) + callZ(() -> 2) + callZ(new AaaImpl())";
        edited = concat(edited, new String[] {"class AaaImpl implements Zeta", "    function run() returns int",
            "        return 3"});
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(base), manifest(edited), LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().isEmpty());
    }

    /**
     * A closure's owner is its function, named: two functions of one signature in one package (`makeKept()` and
     * `makeOther()`, both `returns Value`) made their closures one group told apart by order, so a closure added to the
     * first moved the second's and the edit was refused.
     */
    @Test
    public void closuresOfTwoFunctionsOfOneSignatureAreTwoGroups() throws Exception {
        String[] base = {"package Test", "interface Value", "    function get() returns int",
            "function makeKept(boolean first) returns Value", "    return () -> 1",
            "function makeOther(boolean first) returns Value", "    return () -> 2",
            "init", "    makeKept(true).get()", "    makeOther(true).get()"};
        String[] edited = base.clone();
        edited[4] = "    if first\n        return () -> 1\n    return () -> 3";
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(base), manifest(edited), LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().isEmpty());
    }

    /**
     * The editor shows a payload's warnings as one notification: an edit which removes a class warns once for each of its
     * methods, which were as many notifications.
     */
    @Test
    public void payloadWarningsAreOneNote() {
        LuaHotReload.Payload none = new LuaHotReload.Payload(3, List.of(), "", List.of(), java.util.Map.of(), java.util.Map.of(),
            java.util.Map.of(), Set.of(), Set.of(), java.util.Map.of(), Set.of());
        assertEquals(null, LuaHotReloadSession.warningNote(none));
        LuaHotReload.Payload one = new LuaHotReload.Payload(3, List.of(), "", List.of("a"), java.util.Map.of(), java.util.Map.of(),
            java.util.Map.of(), Set.of(), Set.of(), java.util.Map.of(), Set.of());
        assertEquals("a", LuaHotReloadSession.warningNote(one));
        LuaHotReload.Payload two = new LuaHotReload.Payload(3, List.of(), "", List.of("a", "b"), java.util.Map.of(),
            java.util.Map.of(), java.util.Map.of(), Set.of(), Set.of(), java.util.Map.of(), Set.of());
        assertEquals("Hot reload payload 3 applies with 2 notes:\na\nb", LuaHotReloadSession.warningNote(two));
    }

    private static String[] consumers(String closures) {
        return new String[] {"package Test", "interface Op", "    function apply(int v) returns int",
            "function consume(Op op) returns int", "    let result = op.apply(1)", "    destroy op", "    return result",
            "function run() returns int", "    return " + closures, "init", "    run()"};
    }

    /**
     * The session cannot know which payload the map installed, so a closure told apart by its order keeps every body sent
     * for it: after 3, 4 and then 5, 6 (which the map may have rejected), a swap to 4, 3 is still a move.
     */
    @Test
    public void sessionRemembersEveryBodySentForCallbacksToldApartByOrder() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadShapes");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadShapeDelivery");
        LuaHotReloadSession.start(build, manifest(consumers("consume((int v) -> v + 1) + consume((int v) -> v + 2)")));
        LuaHotReloadSession.send(build, delivery, manifest(consumers("consume((int v) -> v + 3) + consume((int v) -> v + 4)")));
        LuaHotReloadSession.send(build, delivery, manifest(consumers("consume((int v) -> v + 5) + consume((int v) -> v + 6)")));
        LuaHotReload.Manifest swapped = manifest(consumers("consume((int v) -> v + 4) + consume((int v) -> v + 3)"));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, swapped));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("told apart by their order"));
    }

    /**
     * A body has moved only if the closure which had it lost it: a copy of a callback appended after the others, or one
     * edited into a copy of another, keeps every place and reloads. One inserted before identical callbacks takes the
     * name of the first; the copies keep their tables, under the next names.
     */
    @Test
    public void copiedCallbacksAreNoMove() throws Exception {
        String one = "consume((int v) -> v + 1)";
        String two = "consume((int v) -> v + 2)";
        LuaHotReload.Manifest base = manifest(consumers(one + " + " + two));
        LuaHotReload.payload(base, manifest(consumers(one + " + " + two + " + " + one)), Set.of(), 1);
        LuaHotReload.Manifest copies = manifest(consumers(one + " + " + one));
        LuaHotReload.payload(base, copies, Set.of(), 1);
        LuaHotReload.Payload inserted = LuaHotReload.payload(copies,
            manifest(consumers("consume((int v) -> v + 9) + " + one + " + " + one)), Set.of(), 1);
        assertTrue(inserted.source(), inserted.source().contains("__wurst_hotreload_lineages"));
    }

    @Test
    public void editingOneOfIdenticalCallbacksKeepsTheirCapturedValues() {
        String[] base = {"package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "interface Op", "    function apply() returns int", "function keep(Op op) returns Op", "    return op",
            "Op first", "Op second", "function makeAll(int initial)", "    var seed = initial", "    first = keep(() -> seed)",
            "    seed += 10", "    second = keep(() -> seed)", "init", "    makeAll(5)",
            "    if first.apply() != 5 or second.apply() != 15", "        testFail(\"original captures\")",
            "    hotReload()", "    if first.apply() != 105 or second.apply() != 15",
            "        testFail(\"identical callbacks exchanged their histories\")", "    testSuccess()"};
        String[] edited = base.clone();
        edited[12] = "    first = keep(() -> seed + 100)";
        test().testLua(true).executeProg().luaHotReload(edited).lines(base);
    }

    private static String[] orderedCallbacks(String second, String... more) {
        String[] lines = {
            "package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "interface Op", "    function apply() returns int",
            "function keep(Op op) returns Op", "    return op",
            "Op first", "Op second", "Op third",
            // Two callbacks of one interface in one function, told apart by their order alone.
            "function makeAll()", "    first = keep(() -> 1)", "    second = keep(() -> " + second + ")",
        };
        return concat(concat(lines, more), new String[] {
            "init",
            "    makeAll()",
            "    if first.apply() != 1 or second.apply() != 2",
            "        testFail(\"the original callbacks did not run\")",
            "    hotReload()",
            "    if first.apply() != 1",
            "        testFail(\"the callback before the edited one changed\")",
            "    if second.apply() != 20",
            "        testFail(\"the edited callback, made before the reload, runs its old body\")",
            "    makeAll()",
            "    if second.apply() != 20",
            "        testFail(\"a callback made after the reload runs the old body\")",
            "    testSuccess()",
        });
    }

    /** Editing one of several callbacks told apart by their order reloads it: objects made before run the new body. */
    @Test
    public void editedCallbackAmongSiblingsReloads() {
        test().testLua(true).executeProg().luaHotReload(orderedCallbacks("20")).lines(orderedCallbacks("2"));
    }

    /** A callback added after its siblings reloads too, and those made before keep their bodies. */
    @Test
    public void callbackAppendedAfterItsSiblingsReloads() {
        String[] baseline = java.util.Arrays.stream(orderedCallbacks("20"))
            .map(line -> line.replace("or second.apply() != 2", "or second.apply() != 20")).toArray(String[]::new);
        String[] appended = java.util.Arrays.stream(orderedCallbacks("20", "    third = keep(() -> 3)"))
            .map(line -> line.replace("or second.apply() != 2", "or second.apply() != 20")).toArray(String[]::new);
        test().testLua(true).executeProg()
            .luaHotReload(appended).lines(baseline);
    }

    private static String[] capturingCallbacks(String first) {
        return capturingCallbacks(first, 7);
    }

    private static String[] capturingCallbacks(String first, int firstAfter) {
        return new String[] {
            "package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "interface Op", "    function apply() returns int",
            "function keep(Op op) returns Op", "    return op",
            "Op first", "Op second",
            // Two callbacks told apart by their order; only the second captures s.
            "function makeAll(int s)", "    first = keep(() -> " + first + ")", "    second = keep(() -> s * 10)",
            "init",
            "    makeAll(5)",
            "    if second.apply() != 50",
            "        testFail(\"the original callback did not run\")",
            "    hotReload()",
            "    if second.apply() != 50",
            "        testFail(\"a callback made before the reload lost the value it captured\")",
            "    makeAll(6)",
            "    if first.apply() != " + firstAfter + " or second.apply() != 60",
            "        testFail(\"the callbacks made after the reload\")",
            "    testSuccess()",
        };
    }

    @Test
    public void callbackWhichStartsCapturingLeavesItsSiblingsCaptures() {
        test().testLua(true).executeProg().luaHotReload(capturingCallbacks("s + 1")).lines(capturingCallbacks("1"));
    }

    @Test
    public void callbackWhichStopsCapturingIsABodyEdit() {
        test().testLua(true).executeProg().luaHotReload(capturingCallbacks("1", 1)).lines(capturingCallbacks("s", 1));
    }

    private static String[] insertedCallbacks(boolean inserted) {
        return new String[] {
            "package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "interface Op", "    function apply() returns int",
            "function keep(Op op) returns Op", "    return op",
            "Op zero", "Op first", "Op second",
            // Callbacks of one interface in one function, told apart by their order alone, which capture a value.
            "function makeAll(int seed)",
            inserted ? "    zero = keep(() -> seed * 100)" : "    zero = null",
            "    first = keep(() -> seed + 1)",
            "    second = keep(() -> seed * 10 + 2)",
            "init",
            "    makeAll(5)",
            "    let a = first",
            "    let b = second",
            "    if a.apply() != 6 or b.apply() != 52",
            "        testFail(\"the original callbacks did not run\")",
            "    hotReload()",
            "    if a.apply() != 6",
            "        testFail(\"the first callback, made before the reload, runs another one's body or lost its value\")",
            "    if b.apply() != 52",
            "        testFail(\"the second callback, made before the reload, runs another one's body or lost its value\")",
            "    makeAll(7)",
            "    if zero.apply() != 700 or first.apply() != 8 or second.apply() != 72",
            "        testFail(\"callbacks made after the reload run other bodies\")",
            "    if a.apply() != 6 or b.apply() != 52",
            "        testFail(\"callbacks made before the reload changed with those made after\")",
            "    testSuccess()",
        };
    }

    /**
     * A callback inserted before others of its interface in one function takes the first one's name, and each of the
     * others the next one's: the objects made before keep the bodies and captured values of the callbacks they were made
     * from, and those made after run the callbacks the edit has.
     */
    @Test
    public void callbackInsertedBeforeItsSiblingsReloads() {
        test().testLua(true).executeProg().luaHotReload(insertedCallbacks(true)).lines(insertedCallbacks(false));
    }

    private static String[] shiftedCallbacks(String zero, String first) {
        List<String> lines = new java.util.ArrayList<>(List.of("package Test", "native I2S(int i) returns string",
            "interface Op", "    function apply() returns int",
            "Op zero", "Op first", "Op second", "Op a", "Op b",
            "function makeAll(int seed)"));
        if (zero != null) {
            lines.add("    zero = () -> " + zero);
        }
        lines.addAll(List.of(
            "    first = () -> " + first,
            "    second = () -> seed * 10 + 2",
            "function show(Op op) returns string",
            "    if op == null",
            "        return \"-\"",
            "    return I2S(op.apply())",
            "function old() returns string",
            "    return show(a) + \",\" + show(b)",
            "function made() returns string",
            "    return show(zero) + \",\" + show(first) + \",\" + show(second)",
            "function remake() returns string",
            "    makeAll(7)",
            "    return made()",
            // The driver calls the probes; a branch the map never takes keeps them in the script.
            "boolean probing = false",
            "init",
            "    makeAll(5)",
            "    a = first",
            "    b = second",
            "    if probing",
            "        probing = old() == made() and remake() == \"\""));
        return lines.toArray(new String[0]);
    }

    /**
     * A payload matches the closures of its edit with those the payload before it sent: after one which inserted a
     * callback, one which edits a callback that moved reaches the objects made from it before either payload, and those
     * made after the first. A map which missed the first payload gets the same from the second alone.
     */
    @Test
    public void editAfterAnInsertionReachesObjectsMadeBeforeEither() throws Exception {
        String[] base = shiftedCallbacks(null, "seed + 1");
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_editAfterAnInsertionReachesObjectsMadeBeforeEither.lua";
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReload.Manifest inserted = manifest(shiftedCallbacks("seed * 100", "seed + 1"));
        LuaHotReload.Manifest edited = manifest(shiftedCallbacks("seed * 100", "seed + 1000"));
        var build = java.nio.file.Files.createTempDirectory("hotReloadInsertion");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadInsertionDelivery");
        LuaHotReloadSession.start(build, baseline);
        String first = LuaHotReloadSession.send(build, delivery, inserted).payload().source();
        String second = LuaHotReloadSession.send(build, delivery, edited).payload().source();
        String[] probes = {luaName(edited, "old") + "()", luaName(edited, "made") + "()", luaName(edited, "remake") + "()"};
        assertEquals(List.of("true 6,52 -,6,52 700,8,72 nil", "true 1005,52 700,1007,72 700,1007,72 nil"),
            List.of(applyInLua(script, List.of(first, second), probes).split("\\R")));
        assertEquals("true 1005,52 -,1005,52 700,1007,72 nil", applyInLua(script, List.of(second), probes));
    }

    /**
     * Between the same unchanged callbacks, one inserted and another one edited cannot be told apart from two edited
     * and one appended: the payload is refused, and either edit alone applies.
     */
    @Test
    public void insertionAndEditBetweenTheSameCallbacksIsRefused() throws Exception {
        LuaHotReload.Manifest base = manifest(registered("1", "2"));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(base, manifest(registered("9", "10", "2")), Set.of(), 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("cannot be told apart"));
        LuaHotReload.payload(base, manifest(registered("9", "1", "2")), Set.of(), 1);
        LuaHotReload.payload(base, manifest(registered("10", "2")), Set.of(), 1);
    }

    /**
     * Callbacks registered one statement after another. Classes are ordered by their source position as text
     * ({@code ImTranslator.getSortKey}), so line 10 would come before line 9: the callbacks stay on the first nine lines, where that order is
     * the order of the statements.
     */
    private static String[] registered(String... bodies) {
        List<String> lines = new java.util.ArrayList<>(List.of("package Test", "function registerAll()"));
        for (String body : bodies) {
            lines.add("    keep(() -> " + body + ")");
        }
        lines.addAll(List.of("interface Op", "    function apply() returns int",
            "Op last", "function keep(Op op)", "    last = op", "init", "    registerAll()", "    last.apply()"));
        return lines.toArray(new String[0]);
    }

    /**
     * A callback removed while another one is inserted: in its place, the new one is an edit of it (its objects run the
     * new body), as an edit of a body is; elsewhere, the others' names change and the removed one's old body would read
     * another one's captured values, so the map refuses that, as it refuses two inserted in its place. Swapped callbacks
     * keep their bodies.
     */
    @Test
    public void callbackRemovedAndAnotherInserted() throws Exception {
        LuaHotReload.Manifest base = manifest(registered("1", "2", "3"));
        LuaHotReload.Payload replaced = LuaHotReload.payload(base, manifest(registered("9", "2", "3")), Set.of(), 1);
        assertFalse(replaced.source(), replaced.source().contains("__wurst_hotreload_lineages"));
        // Each continues the closure of its name: the new one the one it replaced.
        assertEquals(base.functions().keySet().stream().filter(replaced.groupLayout().stream()
            .map(LuaHotReload.GroupMember::name).toList()::contains).toList(),
            replaced.groupLayout().stream().map(LuaHotReload.GroupMember::lineage).toList());
        LuaHotReload.Rejected elsewhere = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(base, manifest(registered("9", "1", "3")), Set.of(), 1));
        assertTrue(elsewhere.getMessage(), elsewhere.getMessage().contains("was removed"));
        LuaHotReload.Rejected twoInserted = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(base, manifest(registered("9", "10", "2", "3")), Set.of(), 1));
        assertTrue(twoInserted.getMessage(), twoInserted.getMessage().contains("cannot be told apart"));
        LuaHotReload.Payload swapped = LuaHotReload.payload(base, manifest(registered("2", "1", "3")), Set.of(), 1);
        assertTrue(swapped.source(), swapped.source().contains("__wurst_hotreload_lineages"));
    }

    /**
     * Removing every closure of a group leaves nothing to confuse, and removing the last ones moves no other: what the
     * map made from the removed ones keeps its old body, with a warning. Removing one before others gives them its name,
     * and its old body would read their captured values: refused.
     */
    @Test
    public void removedCallbacksApplyUnlessOthersTakeTheirNames() throws Exception {
        LuaHotReload.Payload all = LuaHotReload.payload(manifest(consumers("consume((int v) -> v + 1) + consume((int v) -> v + 2)")),
            manifest(consumers("0")), Set.of(), 1);
        assertFalse(all.warnings().isEmpty());
        LuaHotReload.Manifest base = manifest(registered("1", "2", "3"));
        LuaHotReload.Payload last = LuaHotReload.payload(base, manifest(registered("1", "2")), Set.of(), 1);
        assertTrue(last.warnings().toString(), last.warnings().stream().anyMatch(w -> w.contains("was removed")));
        LuaHotReload.Rejected first = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(base, manifest(registered("2", "3")), Set.of(), 1));
        assertTrue(first.getMessage(), first.getMessage().contains("was removed while the others' names changed"));
    }

    /**
     * A callback added where an earlier payload removed one takes its names, which what the map made from the removed one
     * still reads its captured values by: refused, while one added where none was removed applies.
     */
    @Test
    public void callbackAddedInTheNamesOfARemovedOneIsRefused() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadReadded");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadReaddedDelivery");
        LuaHotReloadSession.start(build, manifest(registered("1", "2")));
        LuaHotReloadSession.send(build, delivery, manifest(registered("1")));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, manifest(registered("1", "5"))));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("an earlier edit removed it"));
        LuaHotReloadSession.send(build, delivery, manifest(registered("1", "2")));
    }

    @Test
    public void anEmptyRecordedCallbackLayoutDoesNotMeanTheBaselineLayout() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadEmptyLayout");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadEmptyLayoutDelivery");
        LuaHotReloadSession.start(build, manifest(registered("1", "2")));
        LuaHotReloadSession.send(build, delivery, manifest(registered()));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, manifest(registered("5", "2"))));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("an earlier edit removed it"));
        // Bringing the original closures back remains safe.
        LuaHotReloadSession.send(build, delivery, manifest(registered("1", "2")));
    }

    @Test
    public void aCallbackRemovedAfterItWasAddedKeepsItsLineageReserved() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadReservedLineage");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadReservedLineageDelivery");
        LuaHotReloadSession.start(build, manifest(registered("1")));
        LuaHotReloadSession.send(build, delivery, manifest(registered("1", "2")));
        LuaHotReloadSession.send(build, delivery, manifest(registered("1")));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, manifest(registered("1", "5"))));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("an earlier edit removed it"));
        LuaHotReloadSession.send(build, delivery, manifest(registered("1", "2")));
    }

    @Test
    public void callbackCaptureTypesSurvivePayloadHistory() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadCaptureTypes");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadCaptureTypesDelivery");
        LuaHotReloadSession.start(build, manifest(registered()));
        LuaHotReloadSession.send(build, delivery, manifest(typedCallbacks(false, false)));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, manifest(typedCallbacks(true, true))));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("captured as"));
    }

    @Test
    public void insertionAndBodyEditsAfterTheLastAnchorAreRefused() throws Exception {
        var baseline = manifest(registered("1", "2", "3"));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(baseline, manifest(registered("1", "9", "20", "30")), Set.of(), 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("cannot be told apart"));
    }

    @Test
    public void aSurvivingCallbackCannotTakeARemovedCallbacksCaptureNames() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadSurvivorNames");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadSurvivorNamesDelivery");
        LuaHotReloadSession.start(build, manifest(insertedCallbacks(false)));
        String[] removed = java.util.Arrays.stream(insertedCallbacks(false))
            .map(line -> line.contains("second = keep(") ? "    second = null" : line).toArray(String[]::new);
        String[] inserted = java.util.Arrays.stream(insertedCallbacks(true))
            .map(line -> line.contains("second = keep(") ? "    second = null" : line).toArray(String[]::new);
        LuaHotReloadSession.send(build, delivery, manifest(removed));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, manifest(inserted)));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("an earlier edit removed it"));
    }

    @Test
    public void restoringAnOriginalLayoutCannotOverwriteARemovedInsertedCallbacksNames() throws Exception {
        var build = java.nio.file.Files.createTempDirectory("hotReloadHistoricalNames");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadHistoricalNamesDelivery");
        LuaHotReloadSession.start(build, manifest(insertedCallbacks(false)));
        LuaHotReloadSession.send(build, delivery, manifest(insertedCallbacks(true)));
        String[] empty = java.util.Arrays.stream(insertedCallbacks(false))
            .map(line -> line.contains(" = keep(") ? "" : line).toArray(String[]::new);
        LuaHotReloadSession.send(build, delivery, manifest(empty));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, manifest(insertedCallbacks(false))));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("an earlier edit removed it"));
    }

    private static String[] typedCallbacks(boolean inserted, boolean real) {
        return new String[] {"package Test", "native R2I(real r) returns int", "interface Op", "    function apply() returns int",
            "Op last", "function keep(Op op)", "    last = op", "function registerAll()",
            real ? "    real s = 1." : "    int s = 1", inserted ? "    keep(() -> 9)" : "",
            "    keep(() -> R2I(s + 0.))", "    keep(() -> R2I(s + 0.) + 2)", "init", "    registerAll()", "    last.apply()"};
    }

    /**
     * The build of an edit is a function of its source alone: the payload, made for the running map, carries how its
     * closures continue the map's, and the same session gives the same payload.
     */
    @Test
    public void insertionLeavesTheBuildAlone() throws Exception {
        LuaHotReload.Manifest base = manifest(insertedCallbacks(false));
        LuaHotReload.Manifest inserted = manifest(insertedCallbacks(true));
        LuaHotReload.Manifest again = manifest(insertedCallbacks(true));
        assertEquals(inserted.abi(), again.abi());
        assertEquals(inserted.functions(), again.functions());
        assertTrue(inserted.functions().values().stream().noneMatch(f -> f.definition().contains("__wurst_hotreload_lineages")));
        assertTrue(inserted.statements().stream().noneMatch(s -> s.text().contains("__wurst_hotreload_lineages")));
        LuaHotReload.Payload payload = LuaHotReload.payload(base, inserted, LuaHotReload.History.NONE, 1);
        assertEquals(payload, LuaHotReload.payload(base, again, LuaHotReload.History.NONE, 1));
        assertTrue(payload.source(), payload.source().contains("__wurst_hotreload_lineages"));
    }

    @Test
    public void sameOwnerClosureBindingsSwapByTheirSource() throws Exception {
        String[] base = {"package Test", "interface Op", "    function apply(int v) returns int",
            "function make(bool first, int seed) returns Op", "    if first",
            "        Op action = (int v) -> seed + v", "        return action", "    else",
            "        Op action = (int v) -> seed * 10 + v", "        return action",
            "init", "    let a = make(true, 3)", "    let b = make(false, 4)",
            "    a.apply(1)", "    b.apply(1)", "    destroy a", "    destroy b"};
        base[0] += "\npublic int result\npublic function readResult() returns int\n    return result";
        base[14] = "    result = a.apply(1) + b.apply(1)";
        base[16] += "\n    readResult()";
        String[] edited = base.clone();
        edited[5] = base[8];
        edited[8] = base[5];
        var before = manifest(base);
        var after = manifest(edited);
        // Swapped, each keeps its source: the objects made from either keep its tables, under the other's names.
        LuaHotReload.Payload swapped = LuaHotReload.payload(before, after, LuaHotReload.History.NONE, 1);
        assertTrue(swapped.source(), swapped.source().contains("__wurst_hotreload_lineages"));

        // A group an earlier payload introduced is protected by what that payload sent; a session which recorded no
        // layout of its closures (an older one) matches them by name, as before.
        var empty = manifest(MAIN_PACKAGE);
        var first = LuaHotReload.payload(empty, before, LuaHotReload.History.NONE, 1);
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_sameOwnerClosureBindingsSwapByTheirSource.lua";
        assertEquals("true 45 nil", applyInLua(script, List.of(first.source()), globalName(before, "result")));
        var history = new LuaHotReload.History(Set.copyOf(first.functions()), first.captured(), first.slots(),
            first.initializers(), first.packageInits(), first.variables(),
            first.groupShapes().entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey,
                e -> List.of(e.getValue()))));
        LuaHotReload.Rejected later = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(empty, after, history, 2));
        assertTrue(later.getMessage(), later.getMessage().contains("told apart by their order"));
        LuaHotReload.payload(before, before, LuaHotReload.History.NONE, 1);
    }

    private static final String[] MAIN_PACKAGE = {
        "package Main",
        "public int made = 0",
        "public function bump() returns int",
        "    made++",
        "    return made",
        "init",
        "    bump()",
    };

    private static CU addedPackage(boolean failing) {
        return addedPackage(failing, 0);
    }

    private static CU addedPackage(boolean failing, int increment) {
        List<String> lines = new java.util.ArrayList<>(List.of("package Added", "import Main",
            "native I2S(int i) returns string", "int first = bump()", "init", "    made += first + " + increment));
        if (failing) {
            lines.addAll(List.of("    if made < 100", "        I2S(1 div 0)"));
        }
        lines.add("");
        return new CU("Added", String.join("\n", lines));
    }

    /**
     * A new package whose init fails fails the install, and the map records nothing of it: the next payload runs the
     * init again. Once it ran, neither it nor its globals' initial values run again, whatever the editor sent.
     */
    @Test
    public void newPackageInitRunsOnceAndAgainAfterAFailure() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_newPackageInitRunsOnceAndAgainAfterAFailure.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        CU main = new CU("Main", String.join("\n", MAIN_PACKAGE) + "\n");
        LuaHotReload.Manifest failing = compileLuaHotReloadManifest(false, HOT_RELOAD, main, addedPackage(true));
        LuaHotReload.Manifest fixed = compileLuaHotReloadManifest(false, HOT_RELOAD, main, addedPackage(false));
        List<String> payloads = List.of(
            LuaHotReload.payload(baseline, failing, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, fixed, LuaHotReload.History.NONE, 2).source(),
            LuaHotReload.payload(baseline, fixed, LuaHotReload.History.NONE, 3).source());
        String output = applyInLua(script, payloads, globalName(fixed, "made"), globalName(fixed, "first"));
        String[] lines = output.split("\\R");
        // made: 1 from Main's init; the failing init bumps to 2, adds 2 and fails; the fixed one runs again: 5, plus 5.
        assertTrue(output, lines[0].startsWith("false 4 2 ") && lines[0].contains("the init of a new package"));
        assertEquals(output, "true 10 5 nil", lines[1]);
        assertEquals("nothing runs twice: " + output, "true 10 5 nil", lines[2]);
    }

    /**
     * Runs {@code script}'s main, then applies each payload directly, printing for each whether it applied, the values
     * of {@code globals} and the reason it did not.
     */
    private String applyInLua(String script, List<String> payloads, String... globals) throws Exception {
        return applyInLua(script, payloads, false, globals);
    }

    private String applyInLua(String script, List<String> payloads, boolean messages, String... globals) throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("luaHotReloadApply");
        for (int i = 0; i < payloads.size(); i++) {
            java.nio.file.Files.writeString(dir.resolve("p" + (i + 1) + ".lua"), payloads.get(i));
        }
        StringBuilder values = new StringBuilder();
        for (String g : globals) {
            values.append(" .. ' ' .. tostring(").append(g).append(")");
        }
        String runtime = "src/test/resources/luaruntime/";
        String driver = String.join("\n",
            "dofile('" + runtime + "wc3shim.lua') dofile('" + runtime + "common.j.lua') dofile('" + runtime + "blizzard.j.lua')",
            "dofile('" + script + "')",
            "BJDebugMsg = " + (messages ? "print" : "function() end"),
            "main()",
            "for i = 1, " + payloads.size() + " do",
            "  local f = io.open('" + dir.toString().replace('\\', '/') + "/p' .. i .. '.lua', 'rb')",
            "  local source = f:read('a') f:close()",
            "  local ok, err = " + LuaHotReload.APPLY_FUNCTION + "(source)",
            "  print(tostring(ok)" + values + " .. ' ' .. tostring(err))",
            "end");
        java.io.File driverFile = new java.io.File("test-output/lua/LuaHotReloadTests_applyDriver.lua");
        java.nio.file.Files.writeString(driverFile.toPath(), driver);
        Process p = new ProcessBuilder(getLuaExecutable(), driverFile.getPath()).redirectErrorStream(true).start();
        String output = new String(p.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        assertEquals(output, 0, p.waitFor());
        return output;
    }

    /** The map warns about a changed init it already ran, even when the package was absent from the baseline. */
    @Test
    public void changedInitOfAnEarlierPayloadsPackageWarnsOfARestart() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_changedInitOfAnEarlierPayloadsPackageWarnsOfARestart.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        CU main = new CU("Main", String.join("\n", MAIN_PACKAGE) + "\n");
        LuaHotReload.Manifest first = compileLuaHotReloadManifest(false, HOT_RELOAD, main, addedPackage(false));
        LuaHotReload.Manifest edited = compileLuaHotReloadManifest(false, HOT_RELOAD, main, addedPackage(false, 10));
        String output = applyInLua(script, List.of(
            LuaHotReload.payload(baseline, first, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, first, LuaHotReload.History.NONE, 2).source(),
            LuaHotReload.payload(baseline, edited, LuaHotReload.History.NONE, 3).source()), true, globalName(first, "made"));
        assertEquals(output, 1, occurrences(output, "restart the map to apply changed package init code"));
        assertEquals("the init runs only once: " + output, 3, occurrences(output, "true 4 nil"));
    }

    private static String[] overrideProgram(boolean override) {
        String[] head = {
            "package Test", "@annotation function onHotReload()",
            "interface Op", "    function apply() returns int",
            "class A implements Op", "    override function apply() returns int", "        return 1",
            "class B extends A",
        };
        String[] tail = {
            "int result = 0", "Op made",
            "@onHotReload function reloaded()", "    result = made.apply()",
            "init", "    made = new B()", "    result = made.apply()",
        };
        return concat(override ? concat(head, new String[] {"    override function apply() returns int", "        return 2"})
            : head, tail);
    }

    /**
     * An override which one payload added and the next removes: the class's slot must be bound again to the inherited
     * body it had at the start, though that binding is the running script's own.
     */
    @Test
    public void removedOverrideOfAnEarlierPayloadRebindsTheInheritedSlot() throws Exception {
        String[] base = overrideProgram(false);
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_removedOverrideOfAnEarlierPayloadRebindsTheInheritedSlot.lua";
        var build = java.nio.file.Files.createTempDirectory("hotReloadOverride");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadOverrideDelivery");
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReloadSession.start(build, baseline);
        String added = LuaHotReloadSession.send(build, delivery, manifest(overrideProgram(true))).payload().source();
        String removed = LuaHotReloadSession.send(build, delivery, manifest(base)).payload().source();
        assertEquals("true 2 nil\ntrue 1 nil",
            applyInLua(script, List.of(added, removed), globalName(baseline, "result")).replace("\r\n", "\n"));
    }

    private static String[] mainWithExtra(String initialValue) {
        // MAIN_PACKAGE with a global, and a function which reads it and which its init calls, so neither is removed.
        return new String[] {
            "package Main",
            "public int made = 0",
            "public function bump() returns int",
            "    made++",
            "    return made",
            "int extra = " + initialValue,
            "public function readExtra() returns int",
            "    return extra",
            "init",
            "    bump()",
            "    readExtra()",
        };
    }

    /**
     * A global which a payload added keeps the value the map gave it when a later edit changes its initial value, as a
     * global of the started map does: the map records the global, not the text which initialised it.
     */
    @Test
    public void changedInitialValueOfAnAddedGlobalDoesNotRunAgain() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_changedInitialValueOfAnAddedGlobalDoesNotRunAgain.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        LuaHotReload.Manifest added = manifest(mainWithExtra("bump()"));
        LuaHotReload.Manifest changed = manifest(mainWithExtra("bump() + 100"));
        List<String> payloads = List.of(
            LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, changed, LuaHotReload.History.NONE, 2).source());
        String output = applyInLua(script, payloads, globalName(changed, "made"), globalName(changed, "extra"));
        String[] lines = output.split("\\R");
        // made: 1 from the init; the first payload bumps it to 2 for extra; the second runs nothing again.
        assertEquals(output, "true 2 2 nil", lines[0]);
        assertEquals(output, "true 2 2 nil", lines[1]);
    }

    /**
     * What a payload prints it cannot apply is kept for the status of that payload (__wurst_hotreload_status), so a map
     * can tell which of its payloads warned: here a later edit of an added global's initial value.
     */
    @Test
    public void warningsAreKeptForTheStatus() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_warningsAreKeptForTheStatus.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        LuaHotReload.Manifest added = manifest(mainWithExtra("bump()"));
        List<String> payloads = List.of(
            LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, manifest(mainWithExtra("bump() + 100")), LuaHotReload.History.NONE, 2).source());
        String output = applyInLua(script, payloads, "'[' .. table.concat(" + LuaHotReload.WARNINGS + ", '|') .. ']'");
        String[] lines = output.split("\\R");
        assertEquals(output, "true [] nil", lines[0]);
        assertTrue(output, lines[1].startsWith("true [") && lines[1].contains(globalName(added, "extra") + " keeps its value")
            && !lines[1].contains("Wurst hot reload: "));
    }

    /** A new global's initializer is one unit, including flattened locals, control flow, and side effects. */
    @Test
    public void addedGlobalKeepsItsCompleteInitializer() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_addedGlobalKeepsItsCompleteInitializer.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        LuaHotReload.Manifest added = manifest(mainWithExtra("begin\n    var value = bump()\n"
            + "    if value > 0\n        value += bump()\n    return value\nend"));
        String output = applyInLua(script, List.of(
            LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 2).source()),
            globalName(added, "made"), globalName(added, "extra"));
        assertEquals(output, "true 3 5 nil\ntrue 3 5 nil", output.replace("\r\n", "\n"));
    }

    /** Array and tuple initializers retain all stores and evaluate each element once. */
    @Test
    public void addedArrayAndTupleInitializersRunAsUnits() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_addedArrayAndTupleInitializersRunAsUnits.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        LuaHotReload.Manifest added = manifest(concat(MAIN_PACKAGE, new String[] {
            "tuple Pair(int a, int b)", "int array xs = [bump(), bump()]", "Pair pair = Pair(bump(), bump())",
            "function total() returns int", "    return xs[0] + xs[1] + pair.a + pair.b", "init", "    total()",
        }));
        String output = applyInLua(script, List.of(
            LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 2).source()),
            globalName(added, "made"), luaName(added, "total") + "()");
        assertEquals(output, "true 5 14 nil\ntrue 5 14 nil", output.replace("\r\n", "\n"));
    }

    private static String[] unreadGlobals(String bumpResult) {
        // A constant nothing reads, a variable only updated from itself and one never initialized, until an edit reads
        // them in bump.
        return new String[] {
            "package Main",
            "public int made = 0",
            "int reloads = 0",
            "int unset",
            "class BuildingIds",
            "    static constant templeOfStorm = 42",
            "public function bump() returns int",
            "    made++",
            "    reloads += 1",
            "    return " + bumpResult,
            "init",
            "    bump()",
        };
    }

    /**
     * A hot-reload build keeps the storage of every global of the source, read or not, so starting to read one changes
     * no initializer's storage: the payload applies, and the global has the value the map gave it at the start.
     */
    @Test
    public void startingToReadAGlobalKeepsItsStorage() throws Exception {
        String[] unread = unreadGlobals("made");
        test().testLua(true).luaHotReload(unread).lines(unread);
        String script = "test-output/lua/LuaHotReloadTests_startingToReadAGlobalKeepsItsStorage.lua";
        LuaHotReload.Manifest baseline = manifest(unread);
        LuaHotReload.Manifest reading = manifest(unreadGlobals("made + BuildingIds.templeOfStorm + reloads + unset"));
        String output = applyInLua(script, List.of(LuaHotReload.payload(baseline, reading, LuaHotReload.History.NONE, 1).source()),
            globalName(reading, "templeOfStorm"), globalName(reading, "reloads"), luaName(reading, "bump") + "()");
        // The init bumped once: reloads is 1; the call above bumps again, made 2 + 42 + reloads 2.
        assertEquals(output, "true 42 1 46 nil", output.replace("\r\n", "\n"));
    }

    /** Without the flag the globals nothing reads are removed, as before. */
    @Test
    public void unreadGlobalsAreRemovedWithoutTheFlag() throws Exception {
        test().testLua(true).lines(unreadGlobals("made"));
        String lua = java.nio.file.Files.readString(
            java.nio.file.Path.of("test-output/lua/LuaHotReloadTests_unreadGlobalsAreRemovedWithoutTheFlag.lua"));
        assertTrue(lua, lua.contains("made"));
        for (String global : List.of("templeOfStorm", "reloads", "unset")) {
            assertFalse(lua, lua.contains(global));
        }
    }

    /** The reverse edit: a global nothing reads any more keeps its storage too. */
    @Test
    public void stoppingToReadAGlobalKeepsItsStorage() throws Exception {
        LuaHotReload.Manifest reading = manifest(unreadGlobals("made + BuildingIds.templeOfStorm + reloads + unset"));
        LuaHotReload.Manifest unread = manifest(unreadGlobals("made"));
        LuaHotReload.payload(reading, unread, LuaHotReload.History.NONE, 1);
        for (String global : List.of("templeOfStorm", "reloads", "unset")) {
            assertEquals(globalName(unread, global), globalName(reading, global));
        }
    }

    @DataProvider
    public Object[][] referenceStorageCases() {
        return new Object[][] {{true}, {false}};
    }

    /** Equal Lua storage text does not make different source-level field or global types compatible. */
    @Test(dataProvider = "referenceStorageCases")
    public void earlierPayloadsReferenceFieldTypeChangeIsRefused(boolean field) throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_earlierPayloadsReferenceFieldTypeChangeIsRefused.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        String declaration = field ? "    A held" : "A held";
        String[] added = concat(MAIN_PACKAGE, new String[] {"class A", "class B", field ? "class Holder" : "", declaration,
            "function touch()", field ? "    let h = new Holder()" : "", "    made += " + (field ? "h.held" : "held") + " castTo int",
            "init", "    touch()"});
        String[] edited = added.clone();
        for (int i = 0; i < edited.length; i++) {
            if (edited[i].equals(declaration)) {
                edited[i] = declaration.replace("A held", "B held");
            }
        }
        LuaHotReload.Manifest first = manifest(added);
        LuaHotReload.Manifest second = manifest(edited);
        String output = applyInLua(script, List.of(
            LuaHotReload.payload(baseline, first, LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, second, LuaHotReload.History.NONE, 2).source()), globalName(first, "made"));
        assertTrue(output, output.split("\\R")[0].startsWith("true "));
        assertTrue(output, output.split("\\R")[1].startsWith("false ") && output.contains("restart the map"));
    }

    /** A function an earlier payload stored as a value keeps that payload's body, so a later change to it is rejected. */
    @Test
    public void changeOfAFunctionAnEarlierPayloadStoredIsRejected() throws Exception {
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        LuaHotReload.Manifest added = withCaptured(manifest(mainWithExtra("bump()")), "readExtra", "");
        LuaHotReload.Payload first = LuaHotReload.payload(baseline, added, LuaHotReload.History.NONE, 1);
        assertEquals(first.captured().toString(), 1, first.captured().size());
        LuaHotReload.History history = new LuaHotReload.History(Set.copyOf(first.functions()), first.captured());
        // Unchanged, it is sent again; changed, it is rejected.
        LuaHotReload.payload(baseline, added, history, 2);
        LuaHotReload.Manifest changed = withCaptured(manifest(mainWithExtra("bump()")), "readExtra", "\n-- edited");
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(baseline, changed, history, 3));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("by an earlier payload"));
    }

    private static String[] mainWithHolder(String fieldType) {
        return new String[] {
            "package Main",
            "public int made = 0",
            "public function bump() returns int",
            "    made++",
            "    return made",
            "class Holder",
            "    " + fieldType + " v",
            "function useHolder()",
            "    let h = new Holder()",
            "    h.v = 1",
            "    if h.v != 0",
            "        made++",
            "    destroy h",
            "init",
            "    bump()",
            "    useHolder()",
        };
    }

    /**
     * A declaration an earlier payload installed which a later edit changes (a field store of another type) cannot be
     * kept: the map refuses the payload before it changes anything.
     */
    @Test
    public void declarationChangedAfterAPayloadInstalledItIsRefused() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_declarationChangedAfterAPayloadInstalledItIsRefused.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        List<String> payloads = List.of(
            LuaHotReload.payload(baseline, manifest(mainWithHolder("int")), LuaHotReload.History.NONE, 1).source(),
            LuaHotReload.payload(baseline, manifest(mainWithHolder("real")), LuaHotReload.History.NONE, 2).source());
        String output = applyInLua(script, payloads, globalName(baseline, "made"));
        String[] lines = output.split("\\R");
        assertTrue(output, lines[0].startsWith("true "));
        assertTrue(output, lines[1].startsWith("false ") && lines[1].contains("restart the map"));
    }

    /** Packages one payload adds are initialised in the order the map would initialise them: a dependency first. */
    @Test
    public void newPackagesInitialiseInDependencyOrder() throws Exception {
        test().testLua(true).luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String script = "test-output/lua/LuaHotReloadTests_newPackagesInitialiseInDependencyOrder.lua";
        LuaHotReload.Manifest baseline = manifest(MAIN_PACKAGE);
        CU main = new CU("Main", String.join("\n", MAIN_PACKAGE) + "\n");
        CU alpha = new CU("Alpha", String.join("\n", "package Alpha", "import Main", "import Zulu",
            "int doubled = zulu * 2", "init", "    made += doubled", ""));
        CU zulu = new CU("Zulu", String.join("\n", "package Zulu", "public int zulu = 5", "init", "    zulu += 0", ""));
        LuaHotReload.Manifest edited = compileLuaHotReloadManifest(false, HOT_RELOAD, main, alpha, zulu);
        List<String> payloads = List.of(LuaHotReload.payload(baseline, edited, LuaHotReload.History.NONE, 1).source());
        String output = applyInLua(script, payloads, globalName(edited, "made"));
        assertEquals(output, "true 11 nil", output.split("\\R")[0]);
    }

    @DataProvider
    public Object[][] packageInitializationCases() {
        return new Object[][] {{false, false}, {false, true}, {true, false}, {true, true}};
    }

    /** Whole new packages and added globals share dependency order, including packages added by an earlier payload. */
    @Test(dataProvider = "packageInitializationCases")
    public void allPackageInitializationUsesDependencyOrder(boolean existingAlpha, boolean existingZulu) throws Exception {
        String[] main = concat(MAIN_PACKAGE, new String[] {"endpackage"});
        String[] alpha = {"package Alpha", "import Main", "init", "    made += 1", "endpackage"};
        String[] zulu = {"package Zulu", "import Main", "init", "    made += 1", "endpackage"};
        String[] base = concat(main, concat(existingAlpha ? alpha : new String[0], existingZulu ? zulu : new String[0]));
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_allPackageInitializationUsesDependencyOrder.lua";
        LuaHotReload.Manifest baseline = manifest(base);
        String[] edited = concat(main, concat(new String[] {
            "package Alpha", "import Main", "import Zulu", "public int doubled = extra * 2",
            "public function readDoubled() returns int", "    return doubled",
            "init", "    made += readDoubled()", "endpackage",
        }, new String[] {
            "package Zulu", "import Main", "public int extra = 5",
            "init", "    made += extra", "endpackage",
        }));
        LuaHotReload.Manifest first = manifest(edited);
        String[] later = edited.clone();
        for (int i = 0; i < later.length; i++) {
            if (later[i].equals("public int doubled = extra * 2")) {
                later[i] += "\npublic int tripled = extra * 3";
            } else if (later[i].equals("    return doubled")) {
                later[i] += " + tripled";
            } else if (later[i].equals("public int extra = 5")) {
                later[i] = "public int extra = 6";
            }
        }
        LuaHotReload.Manifest second = manifest(later);
        LuaHotReload.Payload p1 = LuaHotReload.payload(baseline, first, LuaHotReload.History.NONE, 1);
        LuaHotReload.Payload p2 = LuaHotReload.payload(baseline, second, LuaHotReload.History.NONE, 2);
        String output = applyInLua(script, List.of(p1.source(), p2.source(),
            LuaHotReload.payload(baseline, second, LuaHotReload.History.NONE, 3).source()),
            globalName(second, "doubled"), globalName(second, "extra"), globalName(second, "tripled"));
        String[] lines = output.split("\\R");
        assertEquals(output, "true 10 5 nil nil", lines[0]);
        assertEquals(output, "true 10 5 15 nil", lines[1]);
        assertEquals("repeated payload keeps every value: " + output, lines[1], lines[2]);
    }

    /** {@code m} with function {@code source} stored as a value, its definition extended by {@code edit}. */
    private static LuaHotReload.Manifest withCaptured(LuaHotReload.Manifest m, String source, String edit) {
        java.util.Map<String, LuaHotReload.Function> functions = new java.util.LinkedHashMap<>(m.functions());
        String name = luaName(m, source);
        LuaHotReload.Function f = functions.get(name);
        functions.put(name, new LuaHotReload.Function(name, f.semanticId(), f.definition() + edit, f.slots(),
            List.of("in a test"), f.kind()));
        return new LuaHotReload.Manifest(m.abi(), m.compilerVersion(), functions, m.statements(), m.bindings(), m.initOrder());
    }

    private static String globalName(LuaHotReload.Manifest m, String source) {
        return m.bindings().stream().map(b -> b.substring(0, b.indexOf('=')))
            .filter(n -> !m.functions().containsKey(n))
            .filter(n -> n.matches("(\\w+_)?" + Pattern.quote(source) + "_[0-9a-f]{12}")).findFirst()
            .orElseThrow(() -> new AssertionError(source + " in " + m.bindings()));
    }

    private static int occurrences(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            n++;
        }
        return n;
    }

    private static String[] mutableFieldProgram(boolean edited) {
        return new String[] {
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "native hotReload()",
            "class Box",
            "    int a",
            edited ? "    int array[2] xs" : "",
            "function poke(Box b) returns int",
            edited ? "    b.xs[1] = 5" : "",
            edited ? "    return b.xs[1]" : "    return b.a",
            "init",
            "    let b = new Box()",
            "    poke(b)",
            "    hotReload()",
            "    if poke(b) != 5",
            "        testFail(\"an array field added to an object made before does not keep its value\")",
            "    testSuccess()",
        };
    }

    /** An array field added while an object exists gets an array of its own, which keeps what is stored in it. */
    @Test
    public void addedArrayFieldOfAnOldObjectKeepsItsValues() {
        test().testLua(true).executeProg().luaHotReload(mutableFieldProgram(true)).lines(mutableFieldProgram(false));
    }

    /** {@code main} ran when the map started: a change to it ships with a warning that it needs a restart. */
    @Test
    public void changedEntryWarnsOfARestart() throws Exception {
        LuaHotReload.Manifest baseline = manifest(mutableFieldProgram(false));
        LuaHotReload.Manifest current = new LuaHotReload.Manifest(baseline.abi(), baseline.compilerVersion(),
            withChangedEntry(baseline.functions()), baseline.statements(), baseline.bindings(), baseline.initOrder());
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, current, LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().stream().anyMatch(w -> w.contains("restart")));
    }

    private static java.util.Map<String, LuaHotReload.Function> withChangedEntry(java.util.Map<String, LuaHotReload.Function> functions) {
        java.util.Map<String, LuaHotReload.Function> changed = new java.util.LinkedHashMap<>(functions);
        LuaHotReload.Function main = functions.values().stream().filter(f -> f.kind() == LuaHotReload.Kind.ENTRY).findFirst()
            .orElseThrow();
        changed.put(main.name(), new LuaHotReload.Function(main.name(), main.semanticId(), main.definition() + "\n-- edited",
            main.slots(), main.captures(), main.kind()));
        return changed;
    }

    /** A session saved by an older compiler has no statements: the payload is rejected with the reason, not a crash. */
    @Test
    public void manifestOfAnOlderCompilerIsRejected() {
        LuaHotReload.Manifest current = manifest(mutableFieldProgram(false));
        LuaHotReload.Manifest legacy = new LuaHotReload.Manifest(current.abi(), null, current.functions(), null, current.bindings(),
            null);
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(legacy, current, LuaHotReload.History.NONE, 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("another compiler"));
    }

    private static String[] structuralProgram(boolean edited) {
        List<String> lines = new java.util.ArrayList<>(List.of(
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "native hotReload()",
            "native I2S(int i) returns string",
            "interface Op",
            "    function apply(int v) returns int",
            edited ? "int counter = 6" : "int counter = 5"));
        if (edited) {
            // A new global, class and function above the existing declarations: they must move nothing.
            lines.addAll(List.of("int extra = 40", "class Fresh", "    function value() returns int",
                "        return 100", "function helper() returns int", "    return extra"));
        }
        lines.addAll(List.of("class Box", "    int a"));
        if (edited) {
            lines.add("    int bonus = 3");
        }
        lines.addAll(List.of(
            "    construct(int a)",
            "        this.a = a",
            "    function get() returns int",
            edited ? "        return a + bonus" : "        return a",
            edited ? "function compute(int x, int y) returns int" : "function compute(int x) returns int",
            edited ? "    return x * y + helper()" : "    return x + 1",
            "function run() returns int"));
        if (edited) {
            lines.addAll(List.of("    let f = new Fresh()", "    Op op = (int v) -> v + 2",
                "    return compute(2, 3) + f.value() + op.apply(1) + counter"));
        } else {
            lines.add("    return compute(1) + counter - 5");
        }
        lines.addAll(List.of(
            "init",
            "    let b = new Box(7)",
            "    if run() != 2 or b.get() != 7",
            "        testFail(\"the original program did not run\")",
            "    hotReload()",
            // compute(2, 3) 6 + extra 40 + Fresh 100 + the lambda 3 + counter 5: the running map keeps its counter.
            "    if run() != 154",
            "        testFail(\"after the reload run() is \" + I2S(run()))",
            "    if b.get() != 7",
            "        testFail(\"an object made before has the new field's default\")",
            "    if new Box(1).get() != 4",
            "        testFail(\"a new object runs the new constructor\")",
            "    testSuccess()"));
        return lines.toArray(new String[0]);
    }

    /**
     * One payload adds a function, changes a signature, adds a global, a class field, a class and a lambda, and changes a
     * global's initial value: the map runs the new program, keeps its counter and its object, and new objects get the
     * new field.
     */
    @Test
    public void structuralEditsReload() {
        test().testLua(true).executeProg()
            .luaHotReload(structuralProgram(true))
            .lines(structuralProgram(false));
    }

    @Test
    public void structuralPayloadRunsWhatIsNewAndWarnsOfKeptValues() throws Exception {
        LuaHotReload.Manifest baseline = manifest(structuralProgram(false));
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, manifest(structuralProgram(true)), LuaHotReload.History.NONE, 1);
        List<String> names = sourceNames(payload.functions());
        assertTrue(names.toString(), names.contains("helper") && names.contains("compute") && names.contains("run"));
        assertTrue(payload.source(), payload.source().contains("Fresh_"));
        assertEquals(payload.warnings().toString(), 1, payload.warnings().size());
        assertTrue(payload.warnings().get(0), payload.warnings().get(0).contains("counter"));
        assertTrue("a new global's initial value runs: " + payload.source(), payload.source().contains("= 40"));

        // The map records it once it ran, so a later payload does not run it again.
        assertTrue(payload.source(), Pattern.compile("if not " + LuaHotReload.RAN_TABLE + "\\[\"[0-9a-f]{12}\"\\] then\\ninit_global_\\w+\\(\\)\\n")
            .matcher(payload.source()).find());
    }

    private static String[] tunedProgram(boolean edited) {
        return new String[] {
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "native hotReload()",
            "native I2S(int i) returns string",
            "native R2S(real r) returns string",
            "tuple pair(int a, int b)",
            "class Box",
            "    int v",
            "    construct(int v)",
            "        this.v = v",
            "function triple(int x) returns int",
            "    return x * 3",
            "@configurable constant int DAMAGE = 1",
            // Unchanged text: computed from DAMAGE, so it follows it.
            "constant int DOUBLE = DAMAGE * 2",
            "constant real RANGE = " + (edited ? "250." : "100."),
            "let NAME = " + (edited ? "\"new\"" : "\"old\""),
            "constant pair SPAN = " + (edited ? "pair(3, 4)" : "pair(1, 2)"),
            "constant string LABEL = NAME + I2S(DAMAGE)",
            "constant int SCALED = triple(DAMAGE) div 2",
            "function rangeTwice() returns real",
            "    return RANGE * 2",
            "constant real FAR = rangeTwice()",
            // An array literal nothing else writes runs again, and so does a constant computed from its elements.
            "constant int array LEVELS = " + (edited ? "[3, 4]" : "[1, 2]"),
            "constant int FIRST_LEVEL = LEVELS[0] + " + (edited ? "2" : "1"),
            // Not re-run: a new object would leak the old one, and a variable holds the map's state.
            "constant Box BOX = new Box(" + (edited ? "2" : "1") + ")",
            "int counter = " + (edited ? "6" : "5"),
            "constant int FROM_COUNTER = counter + " + (edited ? "2" : "1"),
            "class Tower",
            "    static constant int REACH = " + (edited ? "7" : "5"),
            "function check(boolean ok, string what)",
            "    if not ok",
            "        testFail(what)",
            "init",
            "    check(DOUBLE == 10 and RANGE == 100. and NAME == \"old\" and SPAN.b == 2 and LABEL == \"old5\""
                + " and SCALED == 7 and FAR == 200. and BOX.v == 1 and LEVELS[1] == 2 and Tower.REACH == 5, \"the original values\")",
            "    hotReload()",
            "    check(DAMAGE == 7, \"DAMAGE \" + I2S(DAMAGE))",
            "    check(DOUBLE == 14, \"DOUBLE \" + I2S(DOUBLE))",
            "    check(RANGE == 250., \"RANGE \" + R2S(RANGE))",
            "    check(NAME == \"new\", \"NAME \" + NAME)",
            "    check(SPAN.a == 3 and SPAN.b == 4, \"SPAN \" + I2S(SPAN.a) + \" \" + I2S(SPAN.b))",
            "    check(LABEL == \"new7\", \"LABEL \" + LABEL)",
            "    check(SCALED == 10, \"SCALED \" + I2S(SCALED))",
            "    check(FAR == 500., \"FAR \" + R2S(FAR))",
            "    check(Tower.REACH == 7, \"REACH \" + I2S(Tower.REACH))",
            "    check(BOX.v == 1, \"a constant holding a new object was made again\")",
            "    check(LEVELS[0] == 3 and LEVELS[1] == 4, \"LEVELS \" + I2S(LEVELS[0]) + \" \" + I2S(LEVELS[1]))",
            "    check(FIRST_LEVEL == 5, \"FIRST_LEVEL \" + I2S(FIRST_LEVEL))",
            "    check(counter == 5 and FROM_COUNTER == 6, \"a variable, or a constant computed from one, lost its value\")",
            "    testSuccess()",
            "endpackage",
            "package Test_config",
            "@config constant int DAMAGE = " + (edited ? "7" : "5"),
            "endpackage",
        };
    }

    /**
     * Tuning a constant reaches the running map: the changed initial value of a constant runs again when nothing else
     * could have written it and it has no effect beyond its value, and so do the constants computed from it.
     */
    @Test(dataProvider = "stacktraceCases")
    public void tunedConstantsReachTheRunningMap(boolean stacktraces) {
        TestConfig config = test().testLua(true);
        if (stacktraces) {
            config = config.stacktraces();
        }
        config.executeProg().luaHotReload(tunedProgram(true)).lines(tunedProgram(false));
    }

    @DataProvider
    public Object[][] stacktraceCases() {
        return new Object[][] {{false}, {true}};
    }

    @Test
    public void tunedConstantPayloadRunsDependentsAndWarnsOfTheRest() throws Exception {
        LuaHotReload.Manifest baseline = manifest(tunedProgram(false));
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, manifest(tunedProgram(true)), LuaHotReload.History.NONE, 1);
        String warnings = payload.warnings().toString();
        String source = payload.source();
        for (String tuned : List.of("Test_config_DAMAGE", "Test_DOUBLE", "Test_RANGE", "Test_NAME", "Test_SPAN", "Test_LABEL",
            "Test_SCALED", "Test_FAR", "Tower_REACH", "Test_LEVELS", "Test_FIRST_LEVEL")) {
            assertFalse(tuned + " is run again, not kept: " + warnings, warnings.contains(tuned));
            assertTrue(tuned + " runs again:\n" + source, source.contains(initializerCall(baseline, tuned)));
        }
        for (String kept : List.of("Test_BOX", "Test_counter", "Test_FROM_COUNTER")) {
            assertTrue(kept + " keeps its value: " + warnings, warnings.contains(globalName(baseline, kept)));
            assertFalse(kept + " does not run again:\n" + source, source.contains(initializerCall(baseline, kept)));
        }
        // The unchanged initializers computed from DAMAGE run after its changed one.
        int damage = source.indexOf(initializerCall(baseline, "Test_config_DAMAGE"));
        for (String dependent : List.of("Test_DOUBLE", "Test_LABEL", "Test_SCALED")) {
            assertTrue(source, source.indexOf(initializerCall(baseline, dependent)) > damage);
        }
        assertTrue("an unchanged program runs nothing again",
            LuaHotReload.payload(baseline, baseline, LuaHotReload.History.NONE, 1).source().indexOf("init_global_") < 0);
    }

    /**
     * A configuration table written as an array literal: {@code NAMES} has the type {@code string[n]}, whose length an
     * edit which adds or removes an entry changes.
     */
    private static String[] arrayLiterals(String names, String levels) {
        return new String[] {
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "native hotReload()",
            "native I2S(int i) returns string",
            "constant NAMES = [" + names + "]",
            "constant int array LEVELS = [" + levels + "]",
            "constant FIRST = NAMES[0] + \"!\"",
            "function name(int i) returns string",
            "    return NAMES[i]",
            "function level(int i) returns int",
            "    return LEVELS[i]",
            "function check(boolean ok, string what)",
            "    if not ok",
            "        testFail(what)",
            "init",
            "    check(name(0) == \"a\" and name(1) == \"b\" and name(2) == \"\" and level(2) == 3 and FIRST == \"a!\","
                + " \"the original values\")",
            "    hotReload()",
            "    check(name(0) == \"z\" and name(1) == \"b\" and name(2) == \"c\" and name(3) == \"\","
                + " \"NAMES \" + name(0) + name(1) + name(2) + name(3))",
            "    check(level(0) == 1 and level(1) == 2 and level(2) == 0,"
                + " \"LEVELS \" + I2S(level(0)) + I2S(level(1)) + I2S(level(2)))",
            "    check(FIRST == \"z!\", \"FIRST \" + FIRST)",
            "    testSuccess()",
        };
    }

    /**
     * An edit of a constant array literal reaches the running map, whether it adds an entry (NAMES gains "c"), changes
     * one (NAMES[0] becomes "z") or drops one (LEVELS loses 3, which then reads the default, as in a restarted map), and
     * a constant computed from an element follows.
     */
    @Test(dataProvider = "stacktraceCases")
    public void editedArrayLiteralsReachTheRunningMap(boolean stacktraces) {
        TestConfig config = test().testLua(true);
        if (stacktraces) {
            config = config.stacktraces();
        }
        config.executeProg().luaHotReload(arrayLiterals("\"z\", \"b\", \"c\"", "1, 2"))
            .lines(arrayLiterals("\"a\", \"b\"", "1, 2, 3"));
    }

    private static String[] namesProgram(String names) {
        return new String[] {
            "package Main",
            "public constant NAMES = [" + names + "]",
            "public function name(int i) returns string",
            "    return NAMES[i]",
            "init",
            "    name(0)",
        };
    }

    /**
     * Each payload leaves exactly the entries of its version: the map records how many entries it wrote last and clears
     * the ones past the new length, through grown, shrunk and changed versions, and a map which missed payloads ends
     * at the same entries.
     */
    @Test
    public void arrayConstantHoldsTheEntriesOfEachPayload() throws Exception {
        String[] base = namesProgram("\"a\", \"b\"");
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_arrayConstantHoldsTheEntriesOfEachPayload.lua";
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadArrays");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReloadSession.start(buildDir, baseline);
        List<String> payloads = new java.util.ArrayList<>();
        for (String[] edit : List.of(namesProgram("\"a\", \"b\", \"c\", \"d\""), namesProgram("\"x\""),
            namesProgram("\"x\", \"y\""), namesProgram("\"x\", \"y\""))) {
            LuaHotReloadSession.Sent sent = LuaHotReloadSession.send(buildDir, customMapData, manifest(edit));
            assertTrue(sent.payload().warnings().toString(), sent.payload().warnings().isEmpty());
            payloads.add(sent.payload().source());
        }
        String names = globalName(baseline, "NAMES");
        String[] entries = {names + "[0]", names + "[1]", names + "[2]", names + "[3]"};
        String output = applyInLua(script, payloads, entries).replace("\r\n", "\n");
        assertEquals(output, "true a b c d nil\ntrue x    nil\ntrue x y   nil\ntrue x y   nil", output);
        // A map which read only the second and the last payload.
        String skipped = applyInLua(script, List.of(payloads.get(1), payloads.get(3)), entries).replace("\r\n", "\n");
        assertEquals(skipped, "true x    nil\ntrue x y   nil", skipped);
    }

    /** The length of an array literal is not part of its identity, but the type of its entries is. */
    @Test
    public void arrayLiteralEntryTypeChangeIsRefused() throws Exception {
        LuaHotReload.Manifest baseline = manifest(namesProgram("\"a\", \"b\""));
        LuaHotReload.payload(baseline, manifest(namesProgram("\"a\", \"b\", \"c\"")), LuaHotReload.History.NONE, 1);
        LuaHotReload.Manifest numbers = manifest("package Main", "public constant NAMES = [1, 2]",
            "public function name(int i) returns int", "    return NAMES[i]", "init", "    name(0)");
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(baseline, numbers, LuaHotReload.History.NONE, 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains(globalName(baseline, "NAMES") + " now means"));
    }

    private static String[] writtenArray(boolean edited) {
        return new String[] {
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "native hotReload()",
            "constant int array LEVELS = " + (edited ? "[3, 4]" : "[1, 2]"),
            "constant int TOTAL = LEVELS[0] + LEVELS[1] + " + (edited ? "1" : "0"),
            "function raise()",
            "    LEVELS[1] = 9",
            "init",
            "    raise()",
            "    hotReload()",
            "    if LEVELS[0] != 1 or LEVELS[1] != 9 or TOTAL != 3",
            "        testFail(\"an array constant code wrote was initialised again\")",
            "    testSuccess()",
        };
    }

    /**
     * Code may assign an element of a constant array: the map may hold entries its initializer never gave it, so it keeps
     * its value, with the warning, and so does a constant computed from it.
     */
    @Test
    public void arrayConstantCodeWritesKeepsItsValue() throws Exception {
        test().testLua(true).executeProg().luaHotReload(writtenArray(true)).lines(writtenArray(false));
        LuaHotReload.Manifest baseline = manifest(writtenArray(false));
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, manifest(writtenArray(true)), LuaHotReload.History.NONE, 1);
        for (String kept : List.of("Test_LEVELS", "Test_TOTAL")) {
            assertTrue(kept + " keeps its value: " + payload.warnings(), payload.warnings().toString().contains(globalName(baseline, kept)));
            assertFalse(kept + " does not run again:\n" + payload.source(), payload.source().contains(initializerCall(baseline, kept)));
        }
    }

    /**
     * The call of the initializer of the global {@code qualified} (with its package or class prefix) which a payload
     * makes when it runs it again.
     */
    private static String initializerCall(LuaHotReload.Manifest m, String qualified) {
        return "\n" + initializerName(m, qualified) + "()\n";
    }

    private static String initializerName(LuaHotReload.Manifest m, String qualified) {
        return m.functions().keySet().stream()
            .filter(n -> n.matches("init_global_" + Pattern.quote(qualified) + "_[0-9a-f]{12}"))
            .findFirst().orElseThrow(() -> new AssertionError("no initializer of " + qualified));
    }

    private static LuaHotReload.Manifest earlyAndLate(WurstScriptTest test, String early) {
        return test.compileLuaHotReloadManifest(false, HOT_RELOAD,
            new CU("Main", String.join("\n", "package Main", "import initlater Later", "public constant int EARLY = " + early,
                "public function total() returns int", "    return EARLY + LATE", "init", "    total()", "")),
            new CU("Later", String.join("\n", "package Later", "import Main", "public constant int LATE = 3", "")));
    }

    /**
     * A constant which reads one initialised after it (here from a package initialised later) read that one's default
     * when the map started, so running it again would give it another value than a restart: it keeps its value, with a
     * warning.
     */
    @Test
    public void constantReadingALaterConstantKeepsItsValue() throws Exception {
        LuaHotReload.Manifest baseline = earlyAndLate(this, "LATE + 1");
        assertFalse(baseline.constants().toString(), baseline.constants().get(initializerName(baseline, "Main_EARLY")).pure());
        assertTrue(baseline.constants().toString(), baseline.constants().get(initializerName(baseline, "Later_LATE")).pure());
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, earlyAndLate(this, "LATE + 2"), LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().toString().contains(globalName(baseline, "EARLY")));
        assertFalse(payload.source(), payload.source().contains(initializerCall(baseline, "Main_EARLY")));
    }

    private static String[] tunedMain(String damage, String doubled) {
        return new String[] {
            "package Main",
            "public constant int DAMAGE = " + damage,
            "public constant int DOUBLE = " + doubled,
            "public function total() returns int",
            "    return DAMAGE + DOUBLE",
            "init",
            "    total()",
        };
    }

    /**
     * Payloads are cumulative, so an edit which is undone puts the map's value back, the next edit applies on top, and a
     * map which missed a payload still ends at the current values.
     */
    @Test
    public void undoneConstantEditPutsTheValueBack() throws Exception {
        String[] base = tunedMain("5", "DAMAGE * 2");
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_undoneConstantEditPutsTheValueBack.lua";
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadConstants");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReloadSession.start(buildDir, baseline);
        List<String> payloads = new java.util.ArrayList<>();
        for (String[] edit : List.of(tunedMain("7", "DAMAGE * 2"), base, tunedMain("9", "DAMAGE * 3"),
            tunedMain("9", "DAMAGE * 3"))) {
            payloads.add(LuaHotReloadSession.send(buildDir, customMapData, manifest(edit)).payload().source());
        }
        String damage = globalName(baseline, "DAMAGE");
        String doubled = globalName(baseline, "DOUBLE");
        String output = applyInLua(script, payloads, damage, doubled).replace("\r\n", "\n");
        assertEquals(output, "true 7 14 nil\ntrue 5 10 nil\ntrue 9 27 nil\ntrue 9 27 nil", output);
        // A map which read only the last payload.
        String skipped = applyInLua(script, payloads.subList(3, 4), damage, doubled);
        assertEquals(skipped, "true 9 27 nil", skipped);
    }

    private static String[] tunedMainWithExtra(String extra) {
        // EXTRA is declared, and so initialised, before DOUBLE, which reads it.
        String[] lines = tunedMain("5", "DAMAGE * 2 + EXTRA");
        return concat(concat(java.util.Arrays.copyOf(lines, 2), new String[] {"public constant int EXTRA = " + extra}),
            java.util.Arrays.copyOfRange(lines, 2, lines.length));
    }

    /**
     * A constant which a payload added runs once as a new global, and again when a later edit changes it, and so does a
     * constant of a package a payload added.
     */
    @Test
    public void addedConstantsFollowLaterEdits() throws Exception {
        String[] base = tunedMain("5", "DAMAGE * 2");
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_addedConstantsFollowLaterEdits.lua";
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadConstants");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReloadSession.start(buildDir, baseline);
        CU main = new CU("addedConstantsFollowLaterEdits", String.join("\n", tunedMainWithExtra("3")) + "\n");
        List<LuaHotReload.Manifest> edits = List.of(
            compileLuaHotReloadManifest(false, HOT_RELOAD, main, bonusPackage("2")),
            compileLuaHotReloadManifest(false, HOT_RELOAD,
                new CU(main.name, String.join("\n", tunedMainWithExtra("4")) + "\n"), bonusPackage("7")));
        List<String> payloads = new java.util.ArrayList<>();
        for (LuaHotReload.Manifest edit : edits) {
            payloads.add(LuaHotReloadSession.send(buildDir, customMapData, edit).payload().source());
        }
        String output = applyInLua(script, payloads, globalName(baseline, "DOUBLE"), globalName(edits.get(0), "EXTRA"),
            globalName(edits.get(0), "BONUS"), globalName(edits.get(0), "TWICE")).replace("\r\n", "\n");
        assertEquals(output, "true 13 3 2 4 nil\ntrue 14 4 7 14 nil", output);
    }

    private static CU bonusPackage(String bonus) {
        return new CU("Bonus", String.join("\n", "package Bonus", "import Main", "public constant int BONUS = " + bonus,
            "constant int TWICE = BONUS * 2", "public function bonus() returns int", "    return TWICE", "init",
            "    bonus()", ""));
    }

    private static String[] uninitializedVariable(boolean constant) {
        return new String[] {
            "package Main",
            constant ? "public constant int DAMAGE = 5" : "public int DAMAGE",
            "public constant int DOUBLE = DAMAGE * " + (constant ? "3" : "2"),
            "public function total() returns int",
            "    return DAMAGE + DOUBLE",
            "init",
            constant ? "    total()" : "    DAMAGE = 50",
            constant ? "" : "    total()",
        };
    }

    /**
     * A variable need not have an initializer: one which the map's baseline declared without one, and which code wrote,
     * is still a former variable when an edit makes it a constant, so a constant computed from it keeps its value.
     */
    @Test
    public void constantComputedFromAFormerUninitializedVariableKeepsItsValue() throws Exception {
        String[] base = uninitializedVariable(false);
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_constantComputedFromAFormerUninitializedVariableKeepsItsValue.lua";
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, manifest(uninitializedVariable(true)),
            LuaHotReload.History.NONE, 1);
        String doubled = globalName(baseline, "DOUBLE");
        assertTrue(payload.warnings().toString(), payload.warnings().toString().contains(doubled));
        // DOUBLE was computed from DAMAGE's default before init gave it 50; neither changes.
        String output = applyInLua(script, List.of(payload.source()), globalName(baseline, "DAMAGE"), doubled);
        assertEquals(output, "true 50 0 nil", output);
    }

    private static String[] variableBecomingConstant(boolean constant) {
        return new String[] {
            "package Main",
            constant ? "public constant int DAMAGE = 7" : "public int DAMAGE = 5",
            "public constant int DOUBLE = DAMAGE * 2",
            "public function total() returns int",
            "    return DAMAGE + DOUBLE",
            "init",
            constant ? "    total()" : "    DAMAGE = 50",
            constant ? "" : "    total()",
        };
    }

    /**
     * A variable which an edit makes a constant may hold a value code wrote: it keeps that value, with the warning, and
     * so does what is computed from it, rather than the map mixing the new value with values computed from the old.
     */
    @Test
    public void variableBecomingAConstantKeepsItsValue() throws Exception {
        String[] base = variableBecomingConstant(false);
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_variableBecomingAConstantKeepsItsValue.lua";
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, manifest(variableBecomingConstant(true)),
            LuaHotReload.History.NONE, 1);
        String damage = globalName(baseline, "DAMAGE");
        assertTrue(payload.warnings().toString(), payload.warnings().toString().contains(damage));
        String output = applyInLua(script, List.of(payload.source()), damage, globalName(baseline, "DOUBLE"));
        assertEquals(output, "true 50 10 nil", output);
    }

    private static String[] constantFromAFunction(String amount) {
        return new String[] {
            "package Main",
            "function amount() returns int",
            "    return " + amount,
            "public constant int DAMAGE = amount()",
            "public constant int DOUBLE = DAMAGE * 2",
            "public function total() returns int",
            "    return DAMAGE + DOUBLE",
            "init",
            "    total()",
        };
    }

    /**
     * A constant computed by a pure function follows an edit of that function, and so do the constants computed from it;
     * undone, the edit puts the values back.
     */
    @Test
    public void constantFollowsAnEditOfThePureFunctionItCalls() throws Exception {
        String[] base = constantFromAFunction("5");
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_constantFollowsAnEditOfThePureFunctionItCalls.lua";
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadConstants");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReloadSession.start(buildDir, baseline);
        List<String> payloads = new java.util.ArrayList<>();
        for (String[] edit : List.of(constantFromAFunction("7"), base)) {
            payloads.add(LuaHotReloadSession.send(buildDir, customMapData, manifest(edit)).payload().source());
        }
        String output = applyInLua(script, payloads, globalName(baseline, "DAMAGE"), globalName(baseline, "DOUBLE"))
            .replace("\r\n", "\n");
        assertEquals(output, "true 7 14 nil\ntrue 5 10 nil", output);
    }

    private static String[] constantsFromNatives(String added) {
        return new String[] {
            "type player extends handle",
            "package Main",
            "@extern native Player(int i) returns player",
            "@extern native GetPlayerId(player p) returns int",
            "@extern native GetPlayerTeam(player p) returns int",
            // A getter of game state: a payload could see a team code changed since the start.
            "public constant int TEAM = GetPlayerTeam(Player(0)) + " + added,
            // A function of its arguments alone.
            "public constant int SLOT = GetPlayerId(Player(3)) + " + added,
            "public function total() returns int",
            "    return TEAM + SLOT",
            "init",
            "    total()",
        };
    }

    /** Only natives whose result depends on their arguments alone let a constant run again, not getters of game state. */
    @Test
    public void constantFromAGetterOfGameStateKeepsItsValue() throws Exception {
        LuaHotReload.Manifest baseline = manifest(constantsFromNatives("1"));
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, manifest(constantsFromNatives("2")),
            LuaHotReload.History.NONE, 1);
        assertTrue(payload.warnings().toString(), payload.warnings().toString().contains(globalName(baseline, "TEAM")));
        assertFalse(payload.source(), payload.source().contains(initializerCall(baseline, "Main_TEAM")));
        assertTrue(payload.source(), payload.source().contains(initializerCall(baseline, "Main_SLOT")));
    }

    private static CU writerPackage() {
        return new CU("Writer", "package Writer\nimport Main\ninit\n    DAMAGE = 50\n");
    }

    /**
     * A constant which an earlier payload made a variable may hold a value its initializer never gave it: a constant
     * computed from it keeps its value, with a warning, rather than being computed from that value.
     */
    @Test
    public void constantComputedFromAFormerVariableKeepsItsValue() throws Exception {
        String[] base = tunedMain("5", "DAMAGE * 2");
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_constantComputedFromAFormerVariableKeepsItsValue.lua";
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadConstants");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReloadSession.start(buildDir, baseline);
        String[] variable = base.clone();
        variable[1] = "public int DAMAGE = 5";
        CU main = new CU("constantComputedFromAFormerVariableKeepsItsValue", String.join("\n", variable) + "\n");
        LuaHotReloadSession.Sent written = LuaHotReloadSession.send(buildDir, customMapData,
            compileLuaHotReloadManifest(false, HOT_RELOAD, main, writerPackage()));
        LuaHotReloadSession.Sent tuned = LuaHotReloadSession.send(buildDir, customMapData, manifest(tunedMain("5", "DAMAGE * 3")));
        String doubled = globalName(baseline, "DOUBLE");
        assertTrue(tuned.payload().warnings().toString(), tuned.payload().warnings().toString().contains(doubled));
        String output = applyInLua(script, List.of(written.payload().source(), tuned.payload().source()),
            globalName(baseline, "DAMAGE"), doubled).replace("\r\n", "\n");
        assertEquals(output, "true 50 10 nil\ntrue 50 10 nil", output);
    }

    private static String[] initClosures(String bound) {
        return new String[] {
            "package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "interface Op", "    function apply() returns int",
            "function run(Op op) returns int", "    return op.apply()",
            "Op kept",
            "init",
            // Bound to a variable, and passed as an argument as the standard library's Preloader does.
            "    kept = () -> " + bound,
            "    Op local = () -> 4",
            "    if run(() -> 5) + local.apply() != 9 or kept.apply() != 3",
            "        testFail(\"the closures of init did not run\")",
            "    hotReload()",
            "    if kept.apply() != 30",
            "        testFail(\"a closure made in init does not run its new body\")",
            "    testSuccess()",
        };
    }

    /**
     * A closure in a package's init block, whose owner is the package's init function rather than a function of its
     * own: a hot-reload build names it like any other, and its body is replaced.
     */
    @Test
    public void closuresInAnInitBlockAreNamedAndReloaded() {
        test().testLua(true).executeProg().luaHotReload(initClosures("30")).lines(initClosures("3"));
    }

    private static String[] constructorClosures(String body) {
        return new String[] {
            "package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "interface Op", "    function apply() returns int",
            "class Counter",
            "    int n = 2",
            "    Op op",
            "    construct(int start)",
            "        n = start",
            "        op = () -> " + body,
            "    construct()",
            "        op = () -> n * 100",
            "init",
            "    let c = new Counter(5)",
            "    if c.op.apply() != 6 or new Counter().op.apply() != 200",
            "        testFail(\"the closures of the constructors did not run\")",
            "    hotReload()",
            "    if c.op.apply() != 15",
            "        testFail(\"a closure made in a constructor does not run its new body\")",
            "    testSuccess()",
        };
    }

    /**
     * A closure in a constructor, which captures the object: its owner is the constructor's function, which a
     * hot-reload build names it after, and its body is replaced.
     */
    @Test
    public void closuresInAConstructorAreNamedAndReloaded() {
        test().testLua(true).executeProg().luaHotReload(constructorClosures("n + 10")).lines(constructorClosures("n + 1"));
    }

    private LuaHotReload.Manifest siblingCallbacks(boolean lineAbove, String secondBody) {
        List<String> lines = new java.util.ArrayList<>(List.of("package Test", "interface Op", "    function apply() returns int",
            "function run(Op op) returns int", "    return op.apply()"));
        if (lineAbove) {
            lines.add("// a line which moves everything below it");
        }
        // Closures of one interface method in one function, told apart by their order alone.
        lines.addAll(List.of("function both() returns int", "    return run(() -> run(() -> 1) + 1) + run(() -> " + secondBody + ")",
            "init", "    both()"));
        return compileLuaHotReloadManifest(false, HOT_RELOAD.with("-stacktraces"),
            new CU("siblingCallbacks", String.join("\n", lines) + "\n"));
    }

    /**
     * With -stacktraces a call passes its source position, so an edit which only moves lines changes the bodies below
     * it. A moved line is no change of a body of closures told apart by their order: the payload ships the new
     * positions, and an edited body as well.
     */
    @Test
    public void movedLinesDoNotFreezeClosuresToldApartByOrder() throws Exception {
        LuaHotReload.Manifest baseline = siblingCallbacks(false, "2");
        LuaHotReload.Payload moved = LuaHotReload.payload(baseline, siblingCallbacks(true, "2"), LuaHotReload.History.NONE, 1);
        assertEquals("the closure which makes a call ships with its new position: " + moved.functions(), 1,
            moved.functions().stream().filter(f -> f.startsWith("Op_")).count());
        LuaHotReload.Payload again = LuaHotReload.payload(baseline, siblingCallbacks(true, "2"),
            new LuaHotReload.History(Set.copyOf(moved.functions()), moved.captured()), 2);
        assertTrue(again.source(), again.warnings().isEmpty());
        LuaHotReload.Payload edited = LuaHotReload.payload(baseline, siblingCallbacks(true, "3"), LuaHotReload.History.NONE, 1);
        assertTrue("an edited body ships too: " + edited.functions(),
            edited.functions().stream().filter(f -> f.startsWith("Op_")).count() == 2);
    }

    private static String[] reloadHook(String value, String... hookLines) {
        String[] head = {
            "package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "@annotation function onHotReload()",
            "int reloads = 0", "int setup = 0",
            "function setupValue() returns int", "    return " + value,
        };
        return concat(concat(head, hookLines), new String[] {
            "init",
            "    setup = setupValue()",
            "    if setup != 1 or reloads != 0",
            "        testFail(\"a reload hook ran when the map started\")",
            "    hotReload()",
            "    if reloads != 1",
            "        testFail(\"the reload hook did not run once\")",
            "    if setup != 2",
            "        testFail(\"the reload hook did not run the setup code the payload has\")",
            "    testSuccess()",
        });
    }

    private static final String[] HOOK = {"@onHotReload function reloaded()", "    reloads += 1", "    setup = setupValue()"};

    /**
     * A function annotated @onHotReload runs once each payload installed everything else, with the code the payload has,
     * so a map can run its setup code again; nothing calls it when the map starts.
     */
    @Test
    public void reloadHookRunsTheSetupCodeThePayloadHas() {
        test().testLua(true).executeProg().luaHotReload(reloadHook("2", HOOK)).lines(reloadHook("1", HOOK));
    }

    /** A hook which an edit adds runs with the payload which adds it. */
    @Test
    public void reloadHookAddedByAnEditRuns() {
        test().testLua(true).executeProg().luaHotReload(reloadHook("2", HOOK)).lines(reloadHook("1"));
    }

    /** A hook which fails does not stop the others or the payload; what it raised is in the payload's status. */
    @Test
    public void failingReloadHookIsReportedAndTheOthersRun() throws Exception {
        String[] base = MAIN_PACKAGE;
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_failingReloadHookIsReportedAndTheOthersRun.lua";
        String[] hooked = concat(MAIN_PACKAGE, new String[] {
            "@annotation function onHotReload()",
            "native I2S(int i) returns string",
            "@onHotReload function first()", "    made += 10",
            "@onHotReload function broken()", "    I2S(1 div 0)",
            "@onHotReload function last()", "    made += 100"});
        LuaHotReload.Manifest added = manifest(hooked);
        String output = applyInLua(script, List.of(LuaHotReload.payload(manifest(base), added, LuaHotReload.History.NONE, 1).source()),
            globalName(added, "made"), "table.concat(" + LuaHotReload.WARNINGS + ", '|')");
        assertTrue(output, output.startsWith("true 111 "));
        assertTrue(output, output.contains("the reload hook " + luaName(added, "broken") + " failed"));
    }

    /**
     * With -stacktraces every function takes the position of its call, a hook too: the payload passes one, so a stack
     * trace taken in a hook names the reload instead of an empty frame.
     */
    @Test
    public void reloadHookFrameNamesTheReloadInStackTraces() throws Exception {
        RunArgs traced = HOT_RELOAD.with("-stacktraces");
        String name = "reloadHookFrameNamesTheReloadInStackTraces";
        String[] base = MAIN_PACKAGE;
        test().testLua(true).stacktraces().luaHotReload(base).lines(base);
        String[] hooked = concat(MAIN_PACKAGE, new String[] {
            "@annotation function onHotReload()", "public string trace = \"\"",
            "function getStackTraceString() returns string", "    return \"\"",
            "@onHotReload function traced()", "    trace += getStackTraceString()"});
        LuaHotReload.Manifest before = compileLuaHotReloadManifest(false, traced, new CU(name, String.join("\n", base) + "\n"));
        LuaHotReload.Manifest added = compileLuaHotReloadManifest(false, traced, new CU(name, String.join("\n", hooked) + "\n"));
        String output = applyInLua("test-output/lua/LuaHotReloadTests_" + name + ".lua",
            List.of(LuaHotReload.payload(before, added, LuaHotReload.History.NONE, 1).source()), globalName(added, "trace"))
            .replace("\r\n", "\n");
        assertTrue(output, output.startsWith("true "));
        assertTrue(output, output.contains("Stacktrace:\n   when a hot reload ran its reload hooks"));
    }

    /** Applies a payload adding {@code hookLines} to MAIN_PACKAGE in a map built with -stacktraces; prints the globals. */
    private String tracedHookPayload(String name, String[] hookLines, String... globals) throws Exception {
        RunArgs traced = HOT_RELOAD.with("-stacktraces");
        String[] base = MAIN_PACKAGE;
        String[] hooked = concat(MAIN_PACKAGE, hookLines);
        LuaHotReload.Manifest before = compileLuaHotReloadManifest(false, traced, new CU(name, String.join("\n", base) + "\n"));
        LuaHotReload.Manifest added = compileLuaHotReloadManifest(false, traced, new CU(name, String.join("\n", hooked) + "\n"));
        String[] names = java.util.Arrays.stream(globals).map(g -> globalName(added, g)).toArray(String[]::new);
        return applyInLua("test-output/lua/LuaHotReloadTests_" + name + ".lua",
            List.of(LuaHotReload.payload(before, added, LuaHotReload.History.NONE, 1).source()), names).replace("\r\n", "\n");
    }

    /**
     * With -stacktraces a function reference gets a bridge with the trace of the function it calls; a hook which is
     * also a function reference still runs once per payload.
     */
    @Test
    public void reloadHookUsedAsAFunctionReferenceRunsOnce() throws Exception {
        String name = "reloadHookUsedAsAFunctionReferenceRunsOnce";
        test().testLua(true).stacktraces().luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String output = tracedHookPayload(name, new String[] {
            "@annotation function onHotReload()", "public int reloads = 0", "code saved = function counted",
            "public function savedCode() returns code", "    return saved",
            "public function readReloads() returns int", "    return reloads",
            "@onHotReload function counted()", "    reloads += 1", "init", "    savedCode()", "    readReloads()"}, "reloads");
        assertTrue(output, output.startsWith("true 1 "));
    }

    /**
     * A hook which fails left its frames on the stack the -stacktraces build keeps, so the next hook's traces showed
     * them: the depth is put back after each hook.
     */
    @Test
    public void failingReloadHookLeavesNoFramesBehind() throws Exception {
        String name = "failingReloadHookLeavesNoFramesBehind";
        test().testLua(true).stacktraces().luaHotReload(MAIN_PACKAGE).lines(MAIN_PACKAGE);
        String output = tracedHookPayload(name, new String[] {
            "@annotation function onHotReload()", "native I2S(int i) returns string", "public string trace = \"\"",
            "function getStackTraceString() returns string", "    return \"\"",
            "function explode()", "    I2S(1 div 0)",
            "@onHotReload function broken()", "    explode()",
            "@onHotReload function traced()", "    trace += getStackTraceString()"}, "trace");
        assertTrue(output, output.startsWith("true "));
        assertTrue(output, output.contains("Stacktrace:\n   when a hot reload ran its reload hooks"));
        assertFalse(output, output.contains("explode"));
    }

    /**
     * A payload whose install fails (here the init of a package it adds raises) leaves the -stacktraces stack as it
     * found it: the frames the init pushed are not in the traces the map takes after.
     */
    @Test
    public void failedInstallLeavesNoFramesBehind() throws Exception {
        RunArgs traced = HOT_RELOAD.with("-stacktraces");
        String name = "failedInstallLeavesNoFramesBehind";
        String[] base = concat(MAIN_PACKAGE, new String[] {
            "function getStackTraceString() returns string", "    return \"\"",
            "public function traceNow() returns string", "    return getStackTraceString()",
            "init", "    traceNow()"});
        String[] added = concat(base, new String[] {"endpackage",
            "package Broken", "import Main", "native I2S(int i) returns string",
            "function explode()", "    I2S(1 div 0)", "init", "    explode()"});
        test().testLua(true).stacktraces().luaHotReload(base).lines(base);
        LuaHotReload.Manifest before = compileLuaHotReloadManifest(false, traced, new CU(name, String.join("\n", base) + "\n"));
        LuaHotReload.Manifest after = compileLuaHotReloadManifest(false, traced, new CU(name, String.join("\n", added) + "\n"));
        String output = applyInLua("test-output/lua/LuaHotReloadTests_" + name + ".lua",
            List.of(LuaHotReload.payload(before, after, LuaHotReload.History.NONE, 1).source()),
            luaName(before, "traceNow") + "('after the payload')").replace("\r\n", "\n");
        assertTrue(output, output.startsWith("false "));
        assertTrue(output, output.contains("after the payload"));
        assertFalse(output, output.contains("explode"));
    }

    /** A hook is called with nothing, so it has no type arguments either. */
    @Test
    public void reloadHookIsNotGeneric() {
        testAssertErrorsLines(false, "Functions annotated '@onHotReload' may not be generic",
            "package Test", "@annotation function onHotReload()", "class A<T:>", "    @onHotReload static function reloaded()",
            "        skip");
    }

    /** Outside a -luaHotReload build the annotation does nothing: a hook nothing calls is not even emitted. */
    @Test
    public void reloadHookIsNotEmittedOutsideHotReloadBuilds() throws IOException {
        String[] program = concat(MAIN_PACKAGE, new String[] {
            "@annotation function onHotReload()", "@onHotReload function reloadedHookOnly()", "    made += 1"});
        test().testLua(true).lines(program);
        String compiled = compiledLua("reloadHookIsNotEmittedOutsideHotReloadBuilds");
        assertFalse(compiled, compiled.contains("reloadedHookOnly"));
    }

    @Test
    public void reloadHookTakesNoParameters() {
        testAssertErrorsLines(false, "Functions annotated '@onHotReload' may not take parameters",
            "package Test", "@annotation function onHotReload()", "@onHotReload function reloaded(int version)",
            "    skip");
    }

    @Test
    public void reloadHookIsStatic() {
        testAssertErrorsLines(false, "Functions annotated '@onHotReload' must be static",
            "package Test", "@annotation function onHotReload()", "class A", "    @onHotReload function reloaded()",
            "        skip");
    }

    @Test
    public void hotReloadBuildKeepsItsStateInGlobals() throws IOException {
        test().testLua(true).luaHotReload(program("x + 1", "side * side", INIT))
            .lines(program("x + 1", "side * side", INIT));
        String compiled = compiledLua("hotReloadBuildKeepsItsStateInGlobals");
        assertFalse("a payload cannot see a local of the map's chunk:\n" + compiled,
            Pattern.compile("(?m)^local ").matcher(compiled).find());
        assertTrue(compiled, compiled.contains("__wurst_objectMax = 0"));
        assertTrue(compiled, compiled.contains("TimerStart(CreateTimer(), 1.0, true, " + LuaHotReload.POLL_FUNCTION + ")"));
    }

    @Test
    public void ordinaryBuildHasNoHotReloadRuntime() throws IOException {
        test().testLua(true).lines(program("x + 1", "side * side", INIT));
        String compiled = compiledLua("ordinaryBuildHasNoHotReloadRuntime");
        assertFalse(compiled, compiled.contains("__wurst_hotreload"));
        assertTrue(compiled, compiled.contains("local __wurst_objectMax = 0"));
    }

    @Test
    public void inliningIsRejected() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class,
            () -> compileLuaHotReloadManifest(false, HOT_RELOAD.with("-inline"),
                new CU("inliningIsRejected", String.join("\n", program("x + 1", "side * side", INIT)) + "\n")));
        assertTrue(e.getMessage(), e.getMessage().contains("remove -inline and -opt"));
    }

    private static String[] libraryProgram(String scoreBody, String lambdaBody) {
        return new String[] {
            "package Test",
            "import Wurstunit",
            "import LinkedList",
            "native hotReload()",
            "int total = 0",
            "function score(int x) returns int",
            "    return " + scoreBody,
            "function record(int value)",
            "    total += value",
            "function addAll(LinkedList<int> list)",
            "    list.forEach(i -> record(" + lambdaBody + "))",
            "init",
            "    let list = new LinkedList<int>..add(1)..add(2)",
            "    addAll(list)",
            "    if total != 5",
            "        testFail(\"the original bodies did not run\")",
            "    hotReload()",
            "    total = 0",
            "    addAll(list)",
            "    if total != 2300",
            "        testFail(\"the closure or the function still runs the old body: \" + total.toString())",
            "    if list.size() != 2",
            "        testFail(\"the list lost its elements\")",
            "    testSuccess()",
        };
    }

    /** With the standard library: a closure's body is a function its class table binds, and is replaced like one. */
    @Test
    public void closureBodyIsReplacedWithTheStandardLibrary() {
        test().withStdLib().testLua(true).executeProg()
            .luaHotReload(libraryProgram("x + 10", "score(i) * 100"))
            .lines(libraryProgram("x + 1", "score(i)"));
    }

    /**
     * Stack traces print source positions into bodies, so a line moved by an edit changes the bodies below it, which a
     * payload then carries; nothing outside the bodies holds a position.
     */
    @Test
    public void movedLinesShipTheBodiesWhosePositionsChanged() throws Exception {
        String[] base = libraryProgram("x + 1", "score(i)");
        String[] moved = base.clone();
        moved[5] = "// a new line above score\n" + base[5];
        RunArgs library = HOT_RELOAD.with("-lib", StdLib.getLib(), "-runcompiletimefunctions");
        LuaHotReload.Manifest plainBase = libraryManifest(library, base);
        assertTrue("no function the standard library stores as a value",
            plainBase.functions().values().stream().allMatch(f -> f.kind() != LuaHotReload.Kind.PROGRAM || f.captures().isEmpty()));
        assertTrue(LuaHotReload.payload(plainBase, libraryManifest(library, moved), Set.of(), 1).functions().isEmpty());

        RunArgs traced = library.with("-stacktraces");
        LuaHotReload.Manifest tracedBase = libraryManifest(traced, base);
        LuaHotReload.Payload payload = LuaHotReload.payload(tracedBase, libraryManifest(traced, moved), Set.of(), 1);
        assertFalse(payload.functions().isEmpty());
        assertTrue(payload.functions().toString(), payload.functions().stream().allMatch(tracedBase.functions()::containsKey));
    }

    @Test
    public void sessionPayloadsAreCumulative() throws Exception {
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadSession");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        LuaHotReload.Manifest baseline = manifest(program("x + 1", "side * side", INIT));
        LuaHotReloadSession.start(buildDir, baseline);

        // A payload which cannot be written leaves the session as it was: the map waits for file 1.
        java.nio.file.Files.writeString(customMapData, "a file where the folder should be");
        LuaHotReload.Manifest firstEdit = manifest(program("x + 10", "side * side", INIT));
        expectThrows(IOException.class, () -> LuaHotReloadSession.send(buildDir, customMapData, firstEdit));
        java.nio.file.Files.delete(customMapData);

        LuaHotReloadSession.Sent first = LuaHotReloadSession.send(buildDir, customMapData, firstEdit);
        assertEquals(1, first.payload().version());
        assertEquals(List.of("score"), sourceNames(first.payload().functions()));
        assertEquals("each payload has a file of its own", customMapData.resolve("Wurst").resolve("hotreload-1.pld"), first.file());

        LuaHotReloadSession.Sent second = LuaHotReloadSession.send(buildDir, customMapData,
            manifest(program("x + 1", "side * side * 10", INIT)));
        assertEquals(2, second.payload().version());
        assertEquals("score, back at its original body, is sent again with the new edit", 2, second.payload().functions().size());
        assertTrue(sourceNames(second.payload().functions()).contains("score"));
        assertFalse("the file is written whole, then renamed", java.nio.file.Files.exists(
            second.file().resolveSibling("hotreload-2.pld.tmp")));

        // The state cannot be saved after payload 3 is out: the next payload must not reuse its file, and carries its
        // functions again.
        java.nio.file.Path stateTemp = buildDir.resolve("luaHotReload").resolve("session.json.tmp");
        java.nio.file.Files.createDirectories(stateTemp);
        LuaHotReload.Manifest areaOnly = manifest(program("x + 1", "side * side * 100", INIT));
        expectThrows(IOException.class, () -> LuaHotReloadSession.send(buildDir, customMapData, areaOnly));
        java.nio.file.Files.delete(stateTemp);
        assertTrue(java.nio.file.Files.exists(customMapData.resolve("Wurst").resolve("hotreload-3.pld")));
        LuaHotReloadSession.Sent fourth = LuaHotReloadSession.send(buildDir, customMapData,
            manifest(program("x + 1", "side * side", INIT)));
        assertEquals("the unrecorded payload 3 counts as sent", 4, fourth.payload().version());
        assertTrue("the area override, back at its original body, is sent again", fourth.payload().functions().size() == 2);

        java.nio.file.Path other = customMapData.resolve("Wurst").resolve("other.pld");
        java.nio.file.Files.writeString(other, "kept");
        // A launch which fails puts the files of the map still running back; one which succeeds deletes them.
        LuaHotReloadSession.setAsideDeliveries(customMapData);
        assertFalse("the new map does not see the payloads of the last", java.nio.file.Files.exists(second.file()));
        LuaHotReloadSession.restoreDeliveries(customMapData);
        assertTrue("a failed launch keeps them for the map still running", java.nio.file.Files.exists(second.file()));
        // A restore which was interrupted leaves the set-aside folder: the next launch puts its files back first.
        java.nio.file.Path aside = customMapData.resolve("Wurst").resolve("hotreload-set-aside");
        LuaHotReloadSession.setAsideDeliveries(customMapData);
        LuaHotReloadSession.setAsideDeliveries(customMapData);
        assertTrue("files left aside are not lost", java.nio.file.Files.exists(aside.resolve("hotreload-2.pld")));
        LuaHotReloadSession.discardDeliveries(customMapData);
        assertFalse(java.nio.file.Files.exists(second.file()));
        // One whose deletion was interrupted is marked: the next launch deletes it instead.
        java.nio.file.Files.createDirectories(aside);
        java.nio.file.Files.writeString(aside.resolve("hotreload-9.pld"), "old session");
        java.nio.file.Files.writeString(aside.resolve("superseded"), "");
        LuaHotReloadSession.setAsideDeliveries(customMapData);
        assertFalse("a superseded session's file does not come back",
            java.nio.file.Files.exists(customMapData.resolve("Wurst").resolve("hotreload-9.pld")));
        assertFalse(java.nio.file.Files.exists(aside.resolve("hotreload-9.pld")));
        LuaHotReloadSession.discardDeliveries(customMapData);
        assertFalse(java.nio.file.Files.exists(customMapData.resolve("Wurst").resolve("hotreload-set-aside")));
        assertTrue(java.nio.file.Files.exists(other));
    }

    /**
     * The baseline is the manifest of the map which was started, which no reload changes; a large map's is tens of
     * megabytes. A session writes it once, in a file named by its content, and a reload saves only what it sent. Starting
     * another map replaces it, and a launch which is undone leaves the running map's.
     */
    @Test
    public void aReloadDoesNotRewriteTheBaseline() throws Exception {
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadBaseline");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        java.nio.file.Path folder = buildDir.resolve("luaHotReload");
        java.nio.file.Path state = folder.resolve("session.json");
        LuaHotReload.Manifest baseline = manifest(program("x + 1", "side * side", INIT));
        LuaHotReloadSession.start(buildDir, baseline);
        java.nio.file.Path baselineFile = onlyBaselineFile(folder);
        assertTrue(java.nio.file.Files.readString(baselineFile).contains(baseline.abi()));
        byte[] written = java.nio.file.Files.readAllBytes(baselineFile);
        java.nio.file.attribute.FileTime writtenAt = java.nio.file.Files.getLastModifiedTime(baselineFile);

        LuaHotReload.Manifest firstEdit = manifest(program("x + 10", "side * side", INIT));
        LuaHotReloadSession.Sent first = LuaHotReloadSession.send(buildDir, customMapData, firstEdit);
        assertEquals(LuaHotReload.payload(baseline, firstEdit, Set.of(), 1).source(), first.payload().source());
        LuaHotReloadSession.send(buildDir, customMapData, manifest(program("x + 1", "side * side * 10", INIT)));
        assertFalse("the session saves what was sent, not the baseline",
            java.nio.file.Files.readString(state).contains(baseline.abi()));
        assertEquals(writtenAt, java.nio.file.Files.getLastModifiedTime(baselineFile));
        assertTrue(java.util.Arrays.equals(written, java.nio.file.Files.readAllBytes(baselineFile)));

        // A launch which fails keeps the running map's baseline and drops the other.
        LuaHotReloadSession.stage(buildDir, manifest(program("x + 2", "side * side", INIT)));
        LuaHotReloadSession.unstage(buildDir);
        assertEquals(baselineFile, onlyBaselineFile(folder));
        assertEquals(3, LuaHotReloadSession.send(buildDir, customMapData, firstEdit).payload().version());

        // Another map replaces it: its payloads are made against its own manifest.
        LuaHotReload.Manifest other = manifest(program("x + 2", "side * side", INIT));
        LuaHotReloadSession.start(buildDir, other);
        assertFalse(baselineFile.equals(onlyBaselineFile(folder)));
        LuaHotReloadSession.Sent afterRestart = LuaHotReloadSession.send(buildDir, customMapData, firstEdit);
        assertEquals(LuaHotReload.payload(other, firstEdit, Set.of(), 1).source(), afterRestart.payload().source());
    }

    /** The file of the session's baseline: the only one in the session's folder besides its state. */
    private static java.nio.file.Path onlyBaselineFile(java.nio.file.Path folder) throws IOException {
        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(folder)) {
            List<java.nio.file.Path> others = files
                .filter(f -> !f.getFileName().toString().equals("session.json"))
                .toList();
            assertEquals(others.toString(), 1, others.size());
            return others.get(0);
        }
    }

    /**
     * A session without the editor: starting it with the map of a build deletes an earlier session's payloads, and each
     * later build sends the next cumulative payload.
     */
    @Test
    public void sessionIsDrivenFromTheCommandLine() throws Exception {
        java.nio.file.Path buildDir = java.nio.file.Files.createTempDirectory("luaHotReloadCli");
        java.nio.file.Path customMapData = buildDir.resolve("CustomMapData");
        java.nio.file.Path first = customMapData.resolve("Wurst").resolve("hotreload-1.pld");
        java.nio.file.Files.createDirectories(first.getParent());
        java.nio.file.Files.writeString(first, "a payload of an earlier session");
        String[] start = {"start", buildDir.toString(), customMapData.toString()};
        String[] send = {"send", buildDir.toString(), customMapData.toString()};

        LuaHotReloadSession.writeManifest(buildDir, manifest(program("x + 1", "side * side", INIT)));
        LuaHotReloadSession.main(start);
        assertFalse("an earlier session's payload is gone", java.nio.file.Files.exists(first));
        LuaHotReloadSession.writeManifest(buildDir, manifest(program("x + 10", "side * side", INIT)));
        LuaHotReloadSession.main(send);
        assertTrue(java.nio.file.Files.readString(first).contains("(x + 10)"));
        LuaHotReloadSession.writeManifest(buildDir, manifest(program("x + 1", "side * side * 10", INIT)));
        LuaHotReloadSession.main(send);
        String second = java.nio.file.Files.readString(first.resolveSibling("hotreload-2.pld"));
        assertTrue("cumulative: score, back at its original body, comes again", second.contains("(x + 1)") && second.contains(", 2, function()"));
    }

    /**
     * Hot reload is for a single-player game: with more users playing the poll reads no payload file, says so once, and
     * the status tells why. It counts the users itself, as blizzard.j does for bj_isSinglePlayer, which a map whose main
     * skips InitBlizzard never sets: such a map read no payload, and said nothing.
     */
    @Test
    public void pollIsOffInAGameOfMoreThanOneUser() throws Exception {
        String[] base = program("x + 1", "side * side", INIT);
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_pollIsOffInAGameOfMoreThanOneUser.lua";
        String runtime = "src/test/resources/luaruntime/";
        String driver = String.join("\n",
            "dofile('" + runtime + "wc3shim.lua') dofile('" + runtime + "common.j.lua') dofile('" + runtime + "blizzard.j.lua')",
            "dofile('" + script + "')",
            "__wurst_init_bootstrap()",
            LuaHotReloadTestPlayers.users(2),
            "local requested = 0",
            "BJDebugMsg = function(text) print(text) end",
            "Preloader = function(path) requested = requested + 1 end",
            LuaHotReload.POLL_FUNCTION + "() " + LuaHotReload.POLL_FUNCTION + "()",
            "print(requested)",
            "print(" + LuaHotReload.STATUS_FUNCTION + "())");
        java.io.File driverFile = new java.io.File("test-output/lua/LuaHotReloadTests_multiplayerPollDriver.lua");
        java.nio.file.Files.writeString(driverFile.toPath(), driver);
        Process p = new ProcessBuilder(getLuaExecutable(), driverFile.getPath()).redirectErrorStream(true).start();
        String output = new String(p.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        assertEquals(output, 0, p.waitFor());
        String[] lines = output.split("\\R");
        assertEquals(output, 3, lines.length);
        assertTrue(output, lines[0].contains("hot reload is off") && lines[0].contains("2 users"));
        assertEquals(output, "0", lines[1]);
        assertTrue(output, lines[2].contains("hot reload is off") && lines[2].contains("2 users"));
    }

    /**
     * The game reads a preload file once, so payload n comes in file n and the poll reads each file once, in order. A
     * file whose payload is rejected is not read again, and the next payload still applies.
     */
    @Test
    public void pollReadsEachPayloadFileOnceInOrder() throws Exception {
        String[] base = program("x + 1", "side * side", INIT);
        test().testLua(true).luaHotReload(base).lines(base);
        String script = "test-output/lua/LuaHotReloadTests_pollReadsEachPayloadFileOnceInOrder.lua";
        LuaHotReload.Manifest baseline = manifest(base);
        LuaHotReload.Payload first = LuaHotReload.payload(baseline, manifest(program("x + 10", "side * side", INIT)), Set.of(), 1);
        LuaHotReload.Payload second = LuaHotReload.payload(baseline, manifest(program("x + 100", "side * side", INIT)), Set.of(luaName(baseline, "score")), 2);
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("luaHotReloadPoll");
        java.nio.file.Files.writeString(dir.resolve("hotreload-1.pld"),
            de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.preloadFile(first.source()));
        java.nio.file.Files.writeString(dir.resolve("hotreload-2.pld"),
            de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.preloadFile("return ("));
        java.nio.file.Files.writeString(dir.resolve("hotreload-3.pld"),
            de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.preloadFile(second.source()));
        String runtime = "src/test/resources/luaruntime/";
        String driver = String.join("\n",
            "dofile('" + runtime + "wc3shim.lua') dofile('" + runtime + "common.j.lua') dofile('" + runtime + "blizzard.j.lua')",
            "dofile('" + script + "')",
            "__wurst_init_bootstrap()",
            LuaHotReloadTestPlayers.SINGLE_PLAYER,
            "local tooltips, requested = {}, {}",
            // As measured in the game: a tooltip starts as the ability's own text, and setting "" leaves it.
            "BlzSetAbilityTooltip = function(id, s, level) if s ~= '' then tooltips[level] = s end end",
            "BlzGetAbilityTooltip = function(id, level) return tooltips[level] or 'Attribute Bonus' end",
            "BJDebugMsg = function() end",
            "Preloader = function(path)",
            "  requested[#requested + 1] = path",
            "  local f = io.open('" + dir.toString().replace('\\', '/') + "/' .. path:match('[^\\\\]+$'), 'rb')",
            "  if not f then return end",
            "  local text = f:read('a') f:close()",
            "  assert(load(text:match('//!beginusercode%s(.-)%s//!endusercode')))()",
            "end",
            "local scores = {}",
            "for i = 1, 4 do " + LuaHotReload.POLL_FUNCTION + "() scores[i] = " + luaName(baseline, "score") + "(1) end",
            "print(table.concat(requested, ' '))",
            "print(table.concat(scores, ' '))",
            "print(" + LuaHotReload.STATUS_FUNCTION + "())",
            "print(BlzGetAbilityTooltip(0, 0))");
        java.io.File driverFile = new java.io.File("test-output/lua/LuaHotReloadTests_pollDriver.lua");
        java.nio.file.Files.writeString(driverFile.toPath(), driver);
        Process p = new ProcessBuilder(getLuaExecutable(), driverFile.getPath()).redirectErrorStream(true).start();
        String output = new String(p.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        assertEquals(output, 0, p.waitFor());
        String[] lines = output.split("\\R");
        assertEquals("Wurst\\hotreload-1.pld Wurst\\hotreload-2.pld Wurst\\hotreload-3.pld Wurst\\hotreload-4.pld", lines[0]);
        assertEquals("the broken file changes nothing, the next one applies", "11 11 101 101", lines[1]);
        assertEquals("applied payload 2", lines[2]);
        assertEquals("the poll puts the ability's tooltip back", "Attribute Bonus", lines[3]);
    }

    /** The preload file hands the payload over unchanged, whatever characters and length it has. */
    @Test
    public void preloadFileCarriesThePayloadText() throws Exception {
        StringBuilder payload = new StringBuilder("-- \"quoted\" \\ back\\slash\r\n\ttab \u0001 ctrl1 2\n//!endusercode\n");
        // A character outside the BMP is two Java chars; this one straddles the first chunk boundary.
        while (payload.length() < LuaHotReloadSession.PRELOAD_CHUNK - 1) {
            payload.append('x');
        }
        payload.append("ðŸ˜€");
        while (payload.length() < 2500) {
            payload.append("x = \"").append(payload.length()).append("\"\n");
        }
        java.io.File dir = new java.io.File("test-output/lua");
        dir.mkdirs();
        java.io.File preload = new java.io.File(dir, "LuaHotReloadTests_preload.pld");
        java.io.File received = new java.io.File(dir, "LuaHotReloadTests_preload_received.txt");
        java.nio.file.Files.writeString(preload.toPath(),
            de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.preloadFile(payload.toString()));
        String chunk = "FourCC = function(s) return s end;"
            + " BlzSetAbilityTooltip = function(id, s, level) local f = assert(io.open('" + received.getPath().replace('\\', '/')
            + "', 'wb')) f:write(s) f:close() end;"
            + " local f = assert(io.open('" + preload.getPath().replace('\\', '/') + "', 'rb')) local text = f:read('a') f:close();"
            + " assert(load(text:match('//!beginusercode%s(.-)//!endusercode')))()";
        Process p = new ProcessBuilder(getLuaExecutable(), "-e", chunk).redirectErrorStream(true).start();
        String output = new String(p.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(output, 0, p.waitFor());
        assertEquals(LuaHotReload.PAYLOAD_MARKER + payload, java.nio.file.Files.readString(received.toPath()));

        assertEquals("the in-game probe reads a file in the layout the editor writes",
            de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.preloadFile(
                "wurst hot reload probe: \"quoted\" back\\slash //! marker\nsecond line"),
            java.nio.file.Files.readString(java.nio.file.Paths.get("../docs/profiling/lua-opportunities/hot-reload-probe.pld"))
                .replace("\r\n", "\n"));
    }

    /**
     * Closures on lines 9 and 12 move to 10 and 13: nothing moved. Ordered by their positions as text ("L12" before "L9"),
     * the two swapped names and the map refused the edit.
     */
    @Test
    public void closuresKeepTheirPlacesWhenTheLinesAboveShift() throws Exception {
        LuaHotReload.Manifest baseline = manifest(shiftedCallbacks(false));
        LuaHotReload.Manifest shifted = manifest(shiftedCallbacks(true));
        assertEquals(baseline.functions().keySet(), shifted.functions().keySet());
        LuaHotReload.payload(baseline, shifted, Set.of(), 1);
    }

    /** Declarations of one name are ordered by their positions as numbers, line before column. */
    @Test
    public void sortKeysComparePositionsAsNumbers() {
        List<de.peeeq.wurstscript.translation.imtranslation.ImTranslator.SortKey> keys = new java.util.ArrayList<>(List.of(
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator.SortKey("f", 101, 5),
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator.SortKey("f", 12, 10),
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator.SortKey("f", 12, 9),
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator.SortKey("f", 98, 1),
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator.SortKey("f", -1, -1),
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator.SortKey("e", 1000, 1)));
        java.util.Collections.sort(keys);
        assertEquals(List.of("e@1000:1", "f@-1:-1", "f@12:9", "f@12:10", "f@98:1", "f@101:5"),
            keys.stream().map(k -> k.name() + "@" + k.line() + ":" + k.column()).toList());
    }

    private static String[] shiftedCallbacks(boolean lineAbove) {
        List<String> lines = new java.util.ArrayList<>(List.of("package Test", "interface Op", "    function apply() returns int",
            "Op first = null", "Op second = null", "function keep(Op op) returns Op", "    return op",
            "function makeAll()", "    first = keep(() -> 1)", "", "", "    second = keep(() -> 2)",
            "init", "    makeAll()", "    first.apply()", "    second.apply()"));
        if (lineAbove) {
            lines.add(1, "// one line more");
        }
        return lines.toArray(new String[0]);
    }

    /** A class which implements another interface reloads: its table of supertypes is assigned again. */
    @Test
    public void aClassWhichImplementsAnotherInterfaceReloads() throws Exception {
        LuaHotReload.Payload payload = LuaHotReload.payload(manifest(implementing("Visitor")),
            manifest(implementing("Predicate")), Set.of(), 1);
        assertTrue(payload.source(), Pattern.compile("Doubler\\w*\\.__wurst_supertypes = \\(\\{[^\\n]*Predicate")
            .matcher(payload.source()).find());
    }

    private static String[] implementing(String iface) {
        return new String[] {"package Test", "interface Visitor", "    function visit(int x) returns int",
            "interface Predicate", "    function visit(int x) returns int", "abstract class Base",
            "class Doubler extends Base implements " + iface, "    function visit(int x) returns int", "        return 2 * x",
            "function useVisitor(Visitor v) returns int", "    return v.visit(1)",
            "function usePredicate(Predicate p) returns int", "    return p.visit(1)",
            "function check(Base b) returns boolean", "    return b instanceof Doubler",
            "init", "    let d = new Doubler()", "    check(d)", "    useVisitor(null)", "    usePredicate(null)"};
    }

    /** An object made before the reload is an instance of the interface its class implements now, and no longer of the old. */
    @Test
    public void instanceofFollowsTheInterfacesAClassImplementsAfterTheReload() {
        test().testLua(true).executeProg().luaHotReload(implementingAndChecking("Predicate"))
            .lines(implementingAndChecking("Visitor"));
    }

    @Test
    public void undoingAnInterfaceChangeRestoresTheRunningObjectsSupertypes() throws Exception {
        String[] original = interfaceUndoProgram("Visitor");
        String[] changed = interfaceUndoProgram("Predicate");
        var baseline = manifest(original);
        var build = java.nio.file.Files.createTempDirectory("hotReloadInterfaceUndo");
        var delivery = java.nio.file.Files.createTempDirectory("hotReloadInterfaceUndoDelivery");
        LuaHotReloadSession.start(build, baseline);
        var first = LuaHotReloadSession.send(build, delivery, manifest(changed)).payload();
        var second = LuaHotReloadSession.send(build, delivery, baseline).payload();
        test().testLua(true).luaHotReload(original).lines(original);
        String script = "test-output/lua/LuaHotReloadTests_undoingAnInterfaceChangeRestoresTheRunningObjectsSupertypes.lua";
        String probe = luaName(baseline, "isVisitor") + "()";
        assertEquals("true false nil\ntrue true nil", applyInLua(script,
            List.of(first.source(), second.source()), probe).replace("\r\n", "\n"));
        assertEquals("true true nil", applyInLua(script, List.of(second.source()), probe));
    }

    private static String[] interfaceUndoProgram(String iface) {
        return new String[] {"package Test", "interface Thing", "interface Visitor extends Thing",
            "    function visit() returns int", "interface Predicate extends Thing", "    function visit() returns int",
            "class Doubler implements " + iface, "    function visit() returns int", "        return 2",
            "Thing saved", "function isVisitor() returns boolean", "    return saved instanceof Visitor",
            "function probePredicate(Predicate p) returns int", "    return p.visit()", "init", "    saved = new Doubler()",
            "    isVisitor()", "    probePredicate(null)"};
    }

    private static String[] implementingAndChecking(String iface) {
        return new String[] {"package Test", "native testSuccess()", "native testFail(string msg)", "native hotReload()",
            "interface Thing", "interface Visitor extends Thing", "    function visit(int x) returns int",
            "interface Predicate extends Thing", "    function visit(int x) returns int", "abstract class Base implements Thing",
            "class Doubler extends Base implements " + iface, "    function visit(int x) returns int", "        return 2 * x",
            "function isVisitor(Thing t) returns boolean", "    return t instanceof Visitor",
            "function isPredicate(Thing t) returns boolean", "    return t instanceof Predicate",
            "init", "    Thing d = new Doubler()",
            "    if not isVisitor(d) or isPredicate(d)", "        testFail(\"the original supertypes\")",
            "    hotReload()",
            "    if isVisitor(d) or not isPredicate(d)", "        testFail(\"the supertypes after the reload\")",
            "    testSuccess()"};
    }

    private LuaHotReload.Manifest libraryManifest(RunArgs args, String[] lines) {
        return compileLuaHotReloadManifest(true, args,
            new CU("movedLinesShipTheBodiesWhosePositionsChanged", String.join("\n", lines) + "\n"));
    }

    /** The source names of Lua names: a hot-reload build adds a hash of its identity to each declaration's name. */
    private static List<String> sourceNames(List<String> luaNames) {
        return luaNames.stream().map(n -> n.replaceAll("_[0-9a-f]{12}$", "")).toList();
    }

    /** The Lua name of the function called {@code source} in a hot-reload build. */
    private static String luaName(LuaHotReload.Manifest m, String source) {
        return m.functions().keySet().stream().filter(n -> n.matches(Pattern.quote(source) + "_[0-9a-f]{12}")).findFirst()
            .orElseThrow();
    }

    /** INIT with the first square made with another side: a change to the package init body. */
    private static String[] initWithSquare(int side) {
        String[] init = INIT.clone();
        init[1] = "    Shape square = new Square(" + side + ")";
        return init;
    }

    private LuaHotReload.Manifest manifest(String... lines) {
        String name = new Throwable().getStackTrace()[1].getMethodName();
        return compileLuaHotReloadManifest(false, HOT_RELOAD, new CU(name, String.join("\n", lines) + "\n"));
    }

    /**
     * A closure passed to a call in a builder chain keeps its identity when a later call of the chain, whose receiver
     * holds the closure, is removed: an object made before the reload gets the edited body.
     */
    @Test
    public void aClosureKeepsItsIdentityWhenALaterCallInItsChainIsRemoved() {
        test().testLua(true).executeProg()
            .luaHotReload(chained(false, 2, 3))
            .lines(chained(true, 1, 3));
    }

    @Test
    public void aClosureKeepsItsIdentityWhenALaterCallIsAddedToItsChain() {
        test().testLua(true).executeProg()
            .luaHotReload(chained(true, 2, 3))
            .lines(chained(false, 1, 3));
    }

    /** Two closures of one chain stay apart: the edit of one reaches only that one. */
    @Test
    public void twoClosuresOfOneChainStayDistinct() {
        test().testLua(true).executeProg()
            .luaHotReload(chained(false, 2, 3))
            .lines(chained(true, 2, 2));
        test().testLua(true).executeProg()
            .luaHotReload(chained(false, 2, 3))
            .lines(chained(true, 1, 3));
    }

    @Test
    public void twoClosuresPassedToTheSameChainMethodKeepTheirBodies() {
        test().testLua(true).executeProg().luaHotReload(repeatedChain(false, 2, 3))
            .lines(repeatedChain(true, 1, 2));
    }

    private static String[] repeatedChain(boolean armor, int add, int factor) {
        return java.util.Arrays.stream(chained(armor, add, factor))
            .map(line -> line.replace("        modi = m", "        if modi == null\n            modi = m\n        else\n            bonus = m")
                .replace(".bonus(x", ".modifier(x"))
            .toArray(String[]::new);
    }

    @Test
    public void removingALaterCallOfAChainWarnsOfNoClosure() throws Exception {
        LuaHotReload.Payload removed = LuaHotReload.payload(manifest(chained(true, 1, 3)), manifest(chained(false, 1, 3)),
            Set.of(), 1);
        assertEquals("no closure was renamed: " + removed.warnings(), List.of(), removed.warnings());
        LuaHotReload.Payload edited = LuaHotReload.payload(manifest(chained(true, 1, 3)), manifest(chained(false, 2, 3)),
            Set.of(), 1);
        assertEquals(List.of(), edited.warnings());
        assertTrue("the edited closure ships:\n" + edited.source(), edited.source().contains("(x + 2)"));
    }

    /**
     * A wave built by a chain with two closures; the init checks the bodies after the reload against those of the
     * edited program, {@code x + 2} and {@code x * 3}, so it is the same text in both programs.
     */
    private static String[] chained(boolean armor, int add, int factor) {
        List<String> lines = new java.util.ArrayList<>(List.of("package Test", "native testSuccess()",
            "native testFail(string msg)", "native hotReload()", "interface Modifier",
            "    function modify(int x) returns int",
            "class Wave", "    int hp = 0", "    int armor = 0", "    Modifier modi = null", "    Modifier bonus = null",
            "    function hp(int v) returns Wave", "        hp = v", "        return this",
            "    function armor(int v) returns Wave", "        armor = v", "        return this",
            "    function modifier(Modifier m) returns Wave", "        modi = m", "        return this",
            "    function bonus(Modifier m) returns Wave", "        bonus = m", "        return this",
            "function build() returns Wave", "    return new Wave()", "        .modifier(x -> x + " + add + ")",
            "        .hp(100)", "        .bonus(x -> x * " + factor + ")", "        .armor(5)",
            "init", "    let w = build()", "    hotReload()",
            "    if w.modi.modify(2) != 4",
            "        testFail(\"the modifier made before the reload runs another body\")",
            "    if w.bonus.modify(2) != 6",
            "        testFail(\"the bonus made before the reload runs another body\")",
            "    testSuccess()"));
        if (!armor) {
            lines.remove("        .armor(5)");
        }
        return lines.toArray(new String[0]);
    }

    private static String[] concat(String[] a, String[] b) {
        String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
