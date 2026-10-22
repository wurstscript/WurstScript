package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import org.testng.annotations.Test;

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
            Pattern.compile("(?m)^Square_[0-9a-f]{6}\\.\\w+ = " + areaFunction + "$").matcher(area.source()).find());
        assertFalse("a sibling's slot is left alone:\n" + area.source(),
            Pattern.compile("(?m)^Circle_[0-9a-f]{6}\\.").matcher(area.source()).find());

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
        LuaHotReload.Manifest current = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", edited), List.of(), List.of());
        LuaHotReload.Rejected byBaseline = expectThrows(LuaHotReload.Rejected.class, () -> LuaHotReload.payload(
            new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", stored), List.of(), List.of()), current, Set.of(), 1));
        assertTrue(byBaseline.getMessage(), byBaseline.getMessage().contains("in g"));
        LuaHotReload.Manifest plainBaseline = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", plain), List.of(), List.of());
        expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(plainBaseline, current, Set.of(), Set.of("f"), 2));
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
            assertTrue("named from its identity, not numbered: " + names, name.matches(".*_[0-9a-f]{6}"));
        }
    }

    /**
     * Two declarations whose short hashes collide ({@code P4769:C.C_x} and {@code P8706:C.C_x}) both get the long hash, so
     * their names do not depend on the order of the compilation units.
     */
    @Test
    public void collidingShortHashesDoNotDependOnOrder() {
        CU first = new CU("P4769", String.join("\n", "package P4769", "public class C", "    int x = 1", "init", "    let c = new C()", "    c.x = c.x + 1", ""));
        CU second = new CU("P8706", String.join("\n", "package P8706", "public class C", "    int x = 2", "init", "    let c = new C()", "    c.x = c.x + 1", ""));
        LuaHotReload.Manifest forward = compileLuaHotReloadManifest(false, HOT_RELOAD, first, second);
        LuaHotReload.Manifest backward = compileLuaHotReloadManifest(false, HOT_RELOAD, second, first);
        assertEquals(new java.util.TreeSet<>(forward.bindings()), new java.util.TreeSet<>(backward.bindings()));
        assertTrue(forward.bindings().toString(), forward.bindings().stream()
            .anyMatch(b -> b.matches("C_x_storage_[0-9a-f]{16}=P4769:C\\.C_x.*")));
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

        // A later payload does not run again what an earlier one ran: the new global keeps the value the map gave it.
        LuaHotReload.Payload again = LuaHotReload.payload(baseline, manifest(structuralProgram(true)), new LuaHotReload.History(
            new java.util.HashSet<>(payload.functions()), Set.of(), new java.util.HashSet<>(payload.ran())), 2);
        for (String statement : payload.ran()) {
            // A slot binding comes again with the function it binds, which a cumulative payload defines again.
            if (!statement.matches("\\w+\\.\\w+ = \\w+")) {
                // Unindented: a statement the payload runs, not one in the body of a function it defines.
                assertFalse(statement, again.source().contains("\n" + statement + "\n"));
            }
        }
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
            "bj_isSinglePlayer = true",
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
        payload.append("😀");
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

    private LuaHotReload.Manifest libraryManifest(RunArgs args, String[] lines) {
        return compileLuaHotReloadManifest(true, args,
            new CU("movedLinesShipTheBodiesWhosePositionsChanged", String.join("\n", lines) + "\n"));
    }

    /** The source names of Lua names: a hot-reload build adds a hash of its identity to each declaration's name. */
    private static List<String> sourceNames(List<String> luaNames) {
        return luaNames.stream().map(n -> n.replaceAll("_[0-9a-f]{6}$", "")).toList();
    }

    /** The Lua name of the function called {@code source} in a hot-reload build. */
    private static String luaName(LuaHotReload.Manifest m, String source) {
        return m.functions().keySet().stream().filter(n -> n.matches(Pattern.quote(source) + "_[0-9a-f]{6}")).findFirst()
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

    private static String[] concat(String[] a, String[] b) {
        String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
