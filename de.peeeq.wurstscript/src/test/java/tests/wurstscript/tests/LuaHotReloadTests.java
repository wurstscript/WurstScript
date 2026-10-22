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
        assertEquals(baseline.abi(), edited.abi());
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline, edited, Set.of(), 1);
        assertEquals(List.of("score"), payload.functions());
        assertTrue(payload.source(), payload.source().contains("(x + 10)"));

        LuaHotReload.Payload area = LuaHotReload.payload(baseline,
            manifest(program("x + 1", "side * side * 10", INIT)), Set.of(), 1);
        assertEquals(1, area.functions().size());
        String areaFunction = area.functions().get(0);
        assertTrue("the override's class-table slot is rebound:\n" + area.source(),
            Pattern.compile("(?m)^Square\\.\\w+ = " + areaFunction + "$").matcher(area.source()).find());
        assertFalse("a sibling's slot is left alone:\n" + area.source(),
            Pattern.compile("(?m)^Circle\\.").matcher(area.source()).find());

        assertTrue("an ordinary body edit warns of nothing", payload.warnings().isEmpty());
        LuaHotReload.Payload initEdit = LuaHotReload.payload(baseline,
            manifest(program("x + 1", "side * side", initWithSquare(4))), Set.of(), 1);
        assertEquals("a changed init body ships, with a warning that it does not run again", 1, initEdit.warnings().size());
        assertTrue(initEdit.warnings().get(0), initEdit.warnings().get(0).contains("restart the map"));
        assertTrue("an unchanged program needs an empty payload",
            LuaHotReload.payload(baseline, baseline, Set.of(), 1).functions().isEmpty());
        assertEquals("a function an earlier payload replaced is replaced again, here by its original body",
            List.of("score"), LuaHotReload.payload(baseline, baseline, Set.of("score"), 2).functions());
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
        assertEquals(List.of("first"), payload.functions());
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

    /** An edit outside the bodies changes the script the map is running, which a payload cannot reach. */
    @Test
    public void structuralEditIsRejected() {
        LuaHotReload.Manifest baseline = manifest(program("x + 1", "side * side", INIT));
        String[] withGlobalLines = program("x + 1 + extra", "side * side", INIT);
        withGlobalLines[4] = "int calls = 0\nint extra = 4";
        LuaHotReload.Manifest withGlobal = manifest(withGlobalLines);
        assertFalse(baseline.abi().equals(withGlobal.abi()));
        LuaHotReload.Rejected rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(baseline, withGlobal, Set.of(), 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("outside the function bodies"));

        LuaHotReload.Manifest withFunction = manifest(program("helper(x)", "side * side",
            concat(new String[] {"function helper(int x) returns int", "    return x"}, INIT)));
        rejected = expectThrows(LuaHotReload.Rejected.class,
            () -> LuaHotReload.payload(baseline, withFunction, Set.of(), 1));
        assertTrue(rejected.getMessage(), rejected.getMessage().contains("functions the running script does not have: helper"));
        assertFalse("a new function shifts the names after it, which is not reported apart:\n" + rejected.getMessage(),
            rejected.getMessage().contains("a name now means something else"));
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
            plainBase.functions().values().stream().allMatch(f -> f.captures().isEmpty()));
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
        assertEquals(List.of("score"), first.payload().functions());
        assertEquals("each payload has a file of its own", customMapData.resolve("Wurst").resolve("hotreload-1.pld"), first.file());

        LuaHotReloadSession.Sent second = LuaHotReloadSession.send(buildDir, customMapData,
            manifest(program("x + 1", "side * side * 10", INIT)));
        assertEquals(2, second.payload().version());
        assertEquals("score, back at its original body, is sent again with the new edit", 2, second.payload().functions().size());
        assertTrue(second.payload().functions().contains("score"));
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
        LuaHotReload.Payload second = LuaHotReload.payload(baseline, manifest(program("x + 100", "side * side", INIT)), Set.of("score"), 2);
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
            "for i = 1, 4 do " + LuaHotReload.POLL_FUNCTION + "() scores[i] = score(1) end",
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
