package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.RunMap;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import de.peeeq.wurstscript.translation.BackendWork;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.*;

public class IncrementalSymbolBackendTests extends WurstScriptTest {
    private static final List<String> ARGS = List.of("-lua", "-luaHotReload", "-hotreload", "-runcompiletimefunctions");
    private record Output(String script, LuaHotReload.Manifest manifest, BackendWork.Snapshot work) { }
    private record Project(Path root, Path file, Path script, String source, ModelManagerImpl manager) { }
    private static final String SOURCE = "package Main\nimport NoWurst\nnative observe(int value)\n"
        + "int counter = 0\n"
        + "function helper(int x) returns int\n    return x * 3\n"
        + "function changed(int x) returns int\n    counter = counter + 1\n    return helper(x) + 7\n"
        + "function unrelated(int x) returns int\n    return helper(x) + 9\n"
        + "init\n    observe(changed(4))\n    observe(unrelated(5))\n";
    private static class Probe extends RunMap {
        Probe(Path root) {
            super(null, WFile.create(root), Optional.empty(), Optional.empty(), ARGS,
                Optional.of(root.resolve("NO_GAME.exe").toString()));
        }
        Output compile(ModelManager manager, Path script) throws Exception {
            return compile(manager, script, false);
        }
        Output compile(ModelManager manager, Path script, boolean production) throws Exception {
            return compile(manager, script, production, new WurstGuiCliImpl(), () -> false);
        }
        Output compile(ModelManager manager, Path script, boolean production, WurstGui gui, BooleanSupplier cancelled) throws Exception {
            try (BackendWork work = BackendWork.measure()) {
                File[] output = new File[1];
                CompilationCancellation.run(cancelled, () -> output[0] = compileScript(gui, manager, compileArgs, Optional.empty(),
                    WurstProjectConfigData.empty(), production, script.toFile()));
                assertEquals(gui.getErrorCount(), 0, gui.getErrors());
                return new Output(Files.readString(output[0].toPath()), luaHotReloadManifest, work.snapshot());
            }
        }
    }

    private static Project project(String source) throws Exception {
        Path root = Files.createTempDirectory("symbol-backend");
        Files.createDirectories(root.resolve("wurst"));
        Files.writeString(root.resolve("wurst.build"), "projectName: Test\nwc3Patch: v2.0\n");
        Files.writeString(root.resolve("wurst/Wurst.wurst"), "package Wurst\n");
        Path file = root.resolve("wurst/Main.wurst"), script = root.resolve("wurst/war3map.j");
        Files.writeString(script, "function main takes nothing returns nothing\nendfunction\n");
        Files.writeString(file, source);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        manager.buildProject();
        assertFalse(manager.hasErrors(), manager.getFirstErrorDescription());
        return new Project(root, file, script, source, manager);
    }
    private static void edit(Project project, String text) throws Exception {
        Files.writeString(project.file, text);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.file), text));
        assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
    }
    private static Output compile(Project project) throws Exception { return new Probe(project.root).compile(project.manager, project.script); }
    private static Output fresh(Project project, boolean production) throws Exception {
        ModelManagerImpl fresh = new ModelManagerImpl(project.root.toFile(), new BufferManager());
        fresh.buildProject();
        try { return new Probe(project.root).compile(fresh, project.script, production); }
        finally { fresh.clean(); }
    }
    private static void sameOutput(Output actual, Output expected) {
        assertEquals(actual.script, expected.script);
        assertEquals(actual.manifest, expected.manifest);
    }
    private static void work(Output output, int functions, Project project) {
        assertEquals(output.work.runtimeLoweredFunctions(), functions, output.work + " " + project.manager.incrementalImTranslation().scalarBackendReason());
        assertEquals(output.work.emittedFunctions(), functions, output.work.toString());
    }
    @DataProvider public Object[][] sizes() { return new Object[][]{{0}, {1000}}; }
    @Test(dataProvider = "sizes") public void freshCompiletimeOrdinaryCallEditLowersAndEmitsOnlyItsOwner(int extra) throws Exception {
        StringBuilder source = new StringBuilder(SOURCE);
        for (int i = 0; i < extra; i++) source.append("function extra").append(i)
            .append("(int x) returns int\n    return helper(x) + 9\ninit\n    observe(extra").append(i).append("(4))\n");
        Project project = project(source.toString()); Output before = compile(project);
        edit(project, project.source.replace("helper(x) + 7", "helper(x) + 8"));
        Output actual = compile(project); work(actual, 1, project);
        Output expected = fresh(project, false); sameOutput(actual, expected);
        assertEquals(LuaHotReload.payload(before.manifest, actual.manifest, Set.of(), 1),
            LuaHotReload.payload(before.manifest, expected.manifest, Set.of(), 1));
    }

    @Test public void realComparisonsAndArrayReadsKeepUnrelatedOwnersFrozen() throws Exception {
        String source = SOURCE + "int array weights\n"
            + "native observeBool(boolean value)\n"
            + "function chanceAt(int tile) returns real\n    return weights[tile] * 0.5\n"
            + "function rock(int tile, real chance) returns boolean\n    return chanceAt(tile) < chance and tile > 1\n"
            + "init\n    weights[4] = 3\n    observeBool(rock(4, 2.))\n";
        Project project = project(source); compile(project);
        source = source.replace("weights[tile] * 0.5", "weights[tile] * 0.6");
        edit(project, source); Output after = compile(project); work(after, 1, project); sameOutput(after, fresh(project, false));
        edit(project, source.replace("tile > 1", "tile > 2"));
        after = compile(project); work(after, 1, project); sameOutput(after, fresh(project, false));
    }

    @Test public void unchangedCallersKeepTheirBodiesAndObserveTheNewCalleeInLua() throws Exception {
        Project project = project(SOURCE); Output before = compile(project);
        edit(project, SOURCE.replace("x * 3", "x * 4")); Output after = compile(project); work(after, 1, project);
        sameOutput(after, fresh(project, false));
        String changed = name(before, "changed"), helper = name(before, "helper"), unrelated = name(before, "unrelated");
        String counter = before.manifest.variables().stream().filter(v -> v.contains("counter")).findFirst().orElseThrow();
        Path script = project.root.resolve("before.lua"), payload = project.root.resolve("payload.lua"), driver = project.root.resolve("driver.lua");
        Files.writeString(script, before.script);
        Files.writeString(payload, LuaHotReload.payload(before.manifest, after.manifest, Set.of(), 1).source());
        Files.writeString(driver, "dofile(arg[1])\n" + counter + " = 0\nlocal caller = " + changed + "\nlocal helper = " + helper
            + "\nprint(" + changed + "(4))\nprint(" + counter + ")\nlocal f = assert(io.open(arg[2], 'rb')) local source = f:read('a') f:close()\n"
            + "local ok, err = " + LuaHotReload.APPLY_FUNCTION + "(source) if not ok then error(err) end\n"
            + "assert(caller == " + changed + ") assert(helper ~= " + helper + ")\nprint(" + changed + "(4))\nprint("
            + counter + ")\nprint(" + unrelated + "(5))\n");
        Process process = new ProcessBuilder(getLuaExecutable(), driver.toString(), script.toString(), payload.toString()).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Lua payload timed out");
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(process.exitValue(), 0, output); assertEquals(output.replace("\r\n", "\n"), "19\n1\n23\n2\n29\n");
        } finally { if (process.isAlive()) { process.destroyForcibly(); assertTrue(process.waitFor(5, TimeUnit.SECONDS)); } }
    }
    private static String name(Output output, String name) {
        return output.manifest.functions().keySet().stream().filter(n -> n.startsWith(name + "_")).findFirst().orElseThrow();
    }

    @Test public void multipleChangedOwnersAndNoOpRequestsMatchFreshBuilds() throws Exception {
        Project project = project(SOURCE); compile(project);
        work(compile(project), 0, project);
        edit(project, SOURCE.replace("x * 3", "x * 4").replace("helper(x) + 7", "helper(x) + 8"));
        Output changed = compile(project); work(changed, 2, project); sameOutput(changed, fresh(project, false));
        work(compile(project), 0, project);
    }

    @DataProvider public Object[][] annotations() { return new Object[][]{{"@noinline"}, {"@w3r_keepName"}}; }
    @Test(dataProvider = "annotations") public void ordinaryFunctionAnnotationsKeepFrozenBindings(String annotation) throws Exception {
        String source = SOURCE.replace("function changed", annotation + "\nfunction changed");
        Project project = project(source); compile(project);
        edit(project, source.replace("helper(x) + 7", "helper(x) + 8"));
        Output after = compile(project); work(after, 1, project); sameOutput(after, fresh(project, false));
    }

    @Test public void freshCompiletimeProductsObserveExternalProductionModeWithoutSourceEdits() throws Exception {
        String source = SOURCE + "native isProductionBuild() returns boolean\n"
            + "function compiletime(boolean b) returns boolean\n    return b\n"
            + "constant build = compiletime(isProductionBuild())\ninit\n    if build\n        observe(42)\n";
        Project project = project(source); Output before = compile(project);
        Output after = new Probe(project.root).compile(project.manager, project.script, true);
        assertNotEquals(after.script, before.script);
        sameOutput(after, fresh(project, true));
    }

    @Test public void compiletimeExecutionRunsAgainWhileRuntimeOwnersAreReused() throws Exception {
        String source = SOURCE + "native println(string message)\n"
            + "function compiletime(int value) returns int\n    return value\n"
            + "function evaluated() returns int\n    println(\"CT_FRESH_RECEIPT\")\n    return 6\n"
            + "constant value = compiletime(evaluated())\ninit\n    observe(value)\n";
        Project project = project(source);
        var bytes = new java.io.ByteArrayOutputStream(); var previous = System.err;
        try (var stream = new java.io.PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setErr(stream); compile(project);
            assertEquals(bytes.toString(StandardCharsets.UTF_8).split("CT_FRESH_RECEIPT", -1).length - 1, 1);
            edit(project, source.replace("helper(x) + 7", "helper(x) + 8"));
            Output after = compile(project); work(after, 1, project);
            assertEquals(bytes.toString(StandardCharsets.UTF_8).split("CT_FRESH_RECEIPT", -1).length - 1, 2);
            sameOutput(after, fresh(project, false));
        } finally { System.setErr(previous); }
    }

    @Test public void declarationAndDependencyChangesTakeTheBatchPath() throws Exception {
        Project project = project(SOURCE); compile(project);
        String source = SOURCE.replace("return helper(x) + 7", "return unrelated(x) + 7");
        edit(project, source); Output changed = compile(project);
        assertTrue(changed.work.runtimeLoweredFunctions() > 1); sameOutput(changed, fresh(project, false));
        edit(project, source.replace("counter = 0", "counter = 2"));
        changed = compile(project); assertTrue(changed.work.runtimeLoweredFunctions() > 1); sameOutput(changed, fresh(project, false));
    }

    @Test public void cancellationKeepsTheEditPendingAndLeavesThePreviousFileIntact() throws Exception {
        Project project = project(SOURCE); Output before = compile(project);
        edit(project, SOURCE.replace("helper(x) + 7", "helper(x) + 8"));
        AtomicBoolean cancelled = new AtomicBoolean();
        WurstGui gui = new WurstGuiCliImpl() {
            @Override public void sendProgress(String message) {
                if (message.equals("Preparing Lua backend")) cancelled.set(true);
                CompilationCancellation.check();
            }
        };
        expectThrows(CancellationException.class,
            () -> new Probe(project.root).compile(project.manager, project.script, false, gui, cancelled::get));
        assertTrue(cancelled.get());
        Path written = project.root.resolve("_build").resolve(de.peeeq.wurstio.languageserver.requests.MapRequest.BUILD_COMPILED_LUA_NAME);
        assertEquals(Files.readString(written), before.script);
        Output retry = compile(project); work(retry, 1, project); sameOutput(retry, fresh(project, false));
    }
}
