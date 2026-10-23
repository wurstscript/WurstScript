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

    /** Removing a callback cannot leave an already-registered adapter silently running its old body. */
    @Test
    public void removedStoredFunctionIsRejectedFromBaselineAndHistory() {
        LuaHotReload.Function stored = new LuaHotReload.Function("f", "Test:f()void", "function f() end",
            List.of(), List.of("in register"), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Manifest empty = new LuaHotReload.Manifest("abi", "v", java.util.Map.of(), List.of(), List.of(), List.of());
        LuaHotReload.Manifest baseline = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", stored),
            List.of(), List.of(), List.of());
        LuaHotReload.Rejected original = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(baseline, empty, LuaHotReload.History.NONE, 1));
        assertTrue(original.getMessage(), original.getMessage().contains("removed") && original.getMessage().contains("register"));
        LuaHotReload.Rejected added = expectThrows(LuaHotReload.Rejected.class, () -> LuaHotReload.payload(empty, empty,
            new LuaHotReload.History(Set.of("f"), java.util.Map.of("f", LuaHotReload.definitionHash(stored))), 2));
        assertTrue(added.getMessage(), added.getMessage().contains("earlier payload"));
    }

    @Test
    public void removedCallbackAdapterIsRejected() {
        String[] callback = concat(MAIN_PACKAGE, new String[] {"native register(code callback)",
            "function callback()", "    bump()", "function setup()", "    register(function callback)",
            "init", "    setup()"});
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(manifest(callback), manifest(MAIN_PACKAGE), LuaHotReload.History.NONE, 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("removed") && rejected.getMessage().contains("stored"));
    }

    @Test
    public void removedMethodIsRejectedUnlessItsSlotIsRebound() throws Exception {
        LuaHotReload.Function method = new LuaHotReload.Function("f", "Test:f()void", "function f() end",
            List.of("C.slot"), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Manifest baseline = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("f", method),
            List.of(), List.of(), List.of());
        LuaHotReload.Manifest empty = new LuaHotReload.Manifest("abi", "v", java.util.Map.of(), List.of(), List.of(), List.of());
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(baseline, empty, LuaHotReload.History.NONE, 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("C.slot"));
        LuaHotReload.Function replacement = new LuaHotReload.Function("g", "Test:g()void", "function g() end",
            List.of("C.slot"), List.of(), LuaHotReload.Kind.PROGRAM);
        LuaHotReload.Manifest rebound = new LuaHotReload.Manifest("abi", "v", java.util.Map.of("g", replacement),
            List.of(), List.of(), List.of());
        LuaHotReload.payload(baseline, rebound, LuaHotReload.History.NONE, 2);
    }

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
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReloadSession.send(build, delivery, baseline));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("slot"));
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

    /**
     * The closures of one interface method in one function share an identity and are told apart by their order. A
     * closure added before another would give the old one's name to the new one, so the edit is rejected.
     */
    @Test
    public void closureAddedBesideAnotherOfTheSameMethodIsRejected() {
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
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(manifest(base), manifest(edited), Set.of(), 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("told apart by their order"));
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

    private static String[] concat(String[] a, String[] b) {
        String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
