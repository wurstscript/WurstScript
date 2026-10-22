package tests.wurstscript.tests;

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

        assertTrue("an unchanged program needs an empty payload",
            LuaHotReload.payload(baseline, baseline, Set.of(), 1).functions().isEmpty());
        assertEquals("a function an earlier payload replaced is replaced again, here by its original body",
            List.of("score"), LuaHotReload.payload(baseline, baseline, Set.of("score"), 2).functions());
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
        LuaHotReload.Manifest baseline = manifest(program("x + 1", "side * side", INIT));
        de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.start(buildDir, baseline);

        LuaHotReload.Payload first = de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.nextPayload(buildDir,
            manifest(program("x + 10", "side * side", INIT)));
        assertEquals(1, first.version());
        assertEquals(List.of("score"), first.functions());

        LuaHotReload.Payload second = de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.nextPayload(buildDir,
            manifest(program("x + 1", "side * side * 10", INIT)));
        assertEquals(2, second.version());
        assertEquals("score, back at its original body, is sent again with the new edit", 2, second.functions().size());
        assertTrue(second.functions().contains("score"));

        java.nio.file.Path delivered = de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession.deliver(buildDir, second);
        assertEquals(buildDir.resolve("Wurst").resolve("hotreload.pld"), delivered);
    }

    /** The preload file hands the payload over unchanged, whatever characters and length it has. */
    @Test
    public void preloadFileCarriesThePayloadText() throws Exception {
        StringBuilder payload = new StringBuilder("-- \"quoted\" \\ back\\slash\r\n\ttab \u0001 ctrl1 2\n//!endusercode\n");
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
        assertEquals(payload.toString(), java.nio.file.Files.readString(received.toPath()));

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
