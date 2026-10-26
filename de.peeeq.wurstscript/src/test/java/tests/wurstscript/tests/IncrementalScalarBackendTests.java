package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.RunMap;
import de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.translation.BackendWork;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import de.peeeq.wurstscript.translation.imtranslation.EliminateLocalTypes;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.CompilationCancellation;
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

import static org.testng.Assert.*;

public class IncrementalScalarBackendTests extends WurstScriptTest {
    private static final List<String> ARGS = List.of("-lua", "-luaHotReload", "-hotreload");
    private record Output(String script, LuaHotReload.Manifest manifest, BackendWork.Snapshot work) { }
    private record Project(Path root, Path file, Path script, String source, ModelManagerImpl manager) { }

    private static class Probe extends RunMap {
        Probe(Path root, List<String> args) {
            super(null, WFile.create(root), Optional.empty(), Optional.empty(), args,
                Optional.of(root.resolve("NO_GAME.exe").toString()));
        }
        Output compile(ModelManager manager, Path script) throws Exception {
            return compile(manager, script, new WurstGuiCliImpl(), () -> false);
        }
        Output compile(ModelManager manager, Path script, WurstGui gui, BooleanSupplier cancelled) throws Exception {
            File[] output = new File[1];
            try (BackendWork work = BackendWork.measure()) {
                CompilationCancellation.run(cancelled, () -> output[0] = compileScript(gui, manager, compileArgs,
                    Optional.empty(), WurstProjectConfigData.empty(), false, script.toFile()));
                assertEquals(gui.getErrorCount(), 0, gui.getErrors());
                return new Output(Files.readString(output[0].toPath()), luaHotReloadManifest, work.snapshot());
            }
        }
    }

    @DataProvider(name = "sizes")
    public Object[][] sizes() { return new Object[][]{{0}, {1000}}; }

    @Test(dataProvider = "sizes")
    public void aLiteralEditDoesNotCopyLowerOrEmitUnrelatedFunctions(int extraFunctions) throws Exception {
        Project project = project(extraFunctions);
        Path root = project.root, file = project.file, script = project.script;
        String source = project.source;
        ModelManagerImpl manager = project.manager;
        Output baseline = new Probe(root, ARGS).compile(manager, script);
        assertTrue(baseline.work.copiedFunctions() > 1);
        Path session = root.resolve("_build/scalar-test-session"), delivery = root.resolve("_build/scalar-test-delivery");
        LuaHotReloadSession.start(session, baseline.manifest);
        String edited = source.replace("x + 1", "x + 2");
        long start = System.nanoTime();
        edit(project, edited);
        Output changed = new Probe(root, ARGS).compile(manager, script);
        LuaHotReloadSession.Sent sent = LuaHotReloadSession.send(session, delivery, changed.manifest);
        System.out.println("[scalar slice] unrelated=" + (extraFunctions + 2) + " edit-to-file-ms="
            + (System.nanoTime() - start) / 1e6 + " " + changed.work);
        assertTrue(Files.isRegularFile(sent.file()));
        assertScalarWork(changed, manager);
        Output expected = fresh(project, ARGS);
        assertSameOutput(changed, expected);
        assertEquals(LuaHotReload.payload(baseline.manifest, changed.manifest, Set.of(), 1),
            LuaHotReload.payload(baseline.manifest, expected.manifest, Set.of(), 1));
    }

    private static Project project(int extraFunctions) throws Exception {
        Path root = Files.createTempDirectory("scalar-backend");
        Files.createDirectories(root.resolve("wurst"));
        Files.writeString(root.resolve("wurst.build"), "projectName: Test\nwc3Patch: v2.0\n");
        Files.writeString(root.resolve("wurst/Wurst.wurst"), "package Wurst\n");
        Path file = root.resolve("wurst/Main.wurst");
        String source = "package Main\nimport NoWurst\nnative observe(int value)\n"
            + "function scalar(int x) returns int\n    return x + 1\n"
            + "function caller(int x) returns int\n    return scalar(x)\n"
            + "function unrelated(int x) returns int\n    return x * 3\n"
            + "init\n    observe(caller(4))\n    observe(unrelated(5))\n";
        for (int i = 0; i < extraFunctions; i++) source += "function other" + i + "(int x) returns int\n    return x * 3\n"
            + "init\n    observe(other" + i + "(5))\n";
        Files.writeString(file, source);
        Path script = root.resolve("wurst/war3map.j");
        Files.writeString(script, "function main takes nothing returns nothing\nendfunction\n");
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

    private static Output fresh(Project project, List<String> args) throws Exception {
        ModelManagerImpl manager = new ModelManagerImpl(project.root.toFile(), new BufferManager());
        manager.buildProject();
        return new Probe(project.root, args).compile(manager, project.script);
    }

    private static Project withBody(Project project, String body) throws Exception {
        String source = project.source.replace("    return x + 1\n", body);
        edit(project, source);
        return new Project(project.root, project.file, project.script, source, project.manager);
    }

    @Test(dataProvider = "sizes")
    public void localAssignmentsBranchesAndLoopsOnlyRecompileTheirOwner(int extraFunctions) throws Exception {
        Project project = withBody(project(extraFunctions),
            "    int result = x + 1\n"
            + "    bool positive = result > 0\n"
            + "    if positive and x != 0\n"
            + "        result = result * 3\n"
            + "    else\n"
            + "        result = result - 2\n"
            + "    while result < 9\n"
            + "        result = result + 1\n"
            + "    return result\n");
        String changed = project.source.replace("result * 3", "result * 4");
        Files.writeString(project.file, changed);
        Output expected = fresh(project, ARGS);
        Files.writeString(project.file, project.source);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, changed);
        Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(actual, project.manager);
        assertSameOutput(actual, expected);
        assertEquals(LuaHotReload.payload(baseline.manifest, actual.manifest, Set.of(), 1),
            LuaHotReload.payload(baseline.manifest, expected.manifest, Set.of(), 1));
    }

    @DataProvider(name = "controlBodies")
    public Object[][] controlBodies() {
        return new Object[][] {
            {"    int result = x + 1\n    for i = 0 to 4\n        result = result + i\n    return result\n"},
            {"    int result = x + 1\n    for i = 3 downto 0\n        for j = 0 to 2\n            result = result + i * j\n    return result\n"},
            {"    int result = x + 1\n    if result < 0\n        return -result\n    else\n        if result > 5\n            return result * 2\n    return result\n"},
            {"    int local = x + 1\n    int local_ = local * 2\n    bool flag = not (local_ <= 0) or x >= 3\n    if flag\n        local_ = local_ - 1\n    return local_\n"},
            {"    int result = x + 1\n    int count = 4\n    while count > 0\n        if count == 2\n            break\n        result = result + count\n        count = count - 1\n    return result\n"},
            {"    bool flag = true\n    int result = x + 1\n    if flag\n        result = result + 2\n    return result\n"}
        };
    }

    @Test(dataProvider = "controlBodies")
    public void controlFlowEditChainsAndRevertsMatchBatch(String body) throws Exception {
        Project project = withBody(project(0), body);
        List<String> states = List.of(project.source, project.source.replace("x + 1", "x + 2"),
            project.source.replace("x + 1", "x + 0"), project.source);
        List<Output> expected = new ArrayList<>();
        for (String state : states) { Files.writeString(project.file, state); expected.add(fresh(project, ARGS)); }
        Files.writeString(project.file, project.source);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        for (int i = 1; i < states.size(); i++) {
            edit(project, states.get(i));
            Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
            assertScalarWork(actual, project.manager);
            assertSameOutput(actual, expected.get(i));
            assertEquals(LuaHotReload.payload(baseline.manifest, actual.manifest, Set.of(), i),
                LuaHotReload.payload(baseline.manifest, expected.get(i).manifest, Set.of(), i));
        }
    }

    @DataProvider(name = "localCounts")
    public Object[][] localCounts() { return new Object[][] {{198, false}, {199, false}, {193, true}, {194, true}, {220, true}}; }

    private static String largeBody(int count, boolean counted) {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < count; i++) body.append("    int v").append(i).append(" = x + 1\n");
        if (counted) body.append("    for i = 0 to 3\n        v0 = v0 + i\n");
        body.append("    return ").append(String.join(" + ", java.util.stream.IntStream.range(0, count).mapToObj(i -> "v" + i).toList())).append('\n');
        return body.toString();
    }

    @Test(dataProvider = "localCounts")
    public void theLocalRegisterBoundaryOnlyRecompilesItsOwner(int count, boolean counted) throws Exception {
        Project project = withBody(project(0), largeBody(count, counted));
        new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, project.source.replace("v0 = x + 1", "v0 = x + 2"));
        Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(actual, project.manager);
        String definition = actual.manifest.functions().get(functionName(actual.manifest, "scalar")).definition();
        if (count >= 199) assertTrue(definition.contains("local __wurst_locals = ({})"), definition);
        if (counted && count >= 194) assertTrue(definition.contains("while true do"), definition);
        assertSameOutput(actual, fresh(project, ARGS));
    }

    @Test(dataProvider = "sizes")
    public void spillingAndNestedLoopDemotionSkipUnrelatedFunctions(int extraFunctions) throws Exception {
        String body = largeBody(220, false).replace("    return ",
            "    for i = 0 to 3\n        for j = 0 to 2\n            v0 = v0 + i * j\n    return ");
        Project project = withBody(project(extraFunctions), body);
        String changed = project.source.replace("v0 = x + 1", "v0 = x + 2");
        Files.writeString(project.file, changed);
        Output expected = fresh(project, ARGS);
        Files.writeString(project.file, project.source);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, changed);
        Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(actual, project.manager);
        assertSameOutput(actual, expected);
        assertEquals(LuaHotReload.payload(baseline.manifest, actual.manifest, Set.of(), 1),
            LuaHotReload.payload(baseline.manifest, expected.manifest, Set.of(), 1));
    }

    @DataProvider(name = "spillCollisions")
    public Object[][] spillCollisions() { return new Object[][] {{false, false}, {true, false}, {false, true}, {true, true}}; }

    @Test(dataProvider = "spillCollisions")
    public void spillBindingsSurviveLocalAndGlobalNameCollisions(boolean local, boolean global) throws Exception {
        String body = largeBody(220, true);
        if (local) body = body.replace("v0", "__wurst_locals");
        Project project = withBody(project(0), body);
        String source = project.source;
        if (global) source += "@extern native __wurst_locals() returns int\ninit\n    observe(__wurst_locals())\n";
        edit(project, source);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        String scalar = functionName(baseline.manifest, "scalar");
        int suffix = (local ? 1 : 0) + (global ? 1 : 0);
        String table = "__wurst_locals" + (suffix == 0 ? "" : suffix);
        assertTrue(baseline.manifest.functions().get(scalar).definition().contains("local " + table + " = ({})"),
            baseline.manifest.functions().get(scalar).definition());
        for (String value : List.of("2", "0", "1")) {
            edit(project, source.replace((local ? "__wurst_locals" : "v0") + " = x + 1",
                (local ? "__wurst_locals" : "v0") + " = x + " + value));
            Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
            assertScalarWork(actual, project.manager);
            assertSameOutput(actual, fresh(project, ARGS));
            assertTrue(actual.manifest.functions().get(scalar).definition().contains("local " + table + " = ({})"));
        }
    }

    @Test
    public void loopRecognitionChangesKeepTheSpillFrameAndFreshOutput() throws Exception {
        Project project = withBody(project(0), largeBody(220, true).replace("to 3", "to 3 step 2"));
        List<String> states = List.of(project.source, project.source.replace("step 2", "step 0"),
            project.source.replace("step 2", "step 1"), project.source);
        List<Output> expected = new ArrayList<>();
        for (String state : states) { Files.writeString(project.file, state); expected.add(fresh(project, ARGS)); }
        Files.writeString(project.file, project.source);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        for (int i = 1; i < states.size(); i++) {
            edit(project, states.get(i));
            Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
            assertScalarWork(actual, project.manager);
            assertSameOutput(actual, expected.get(i));
            assertEquals(LuaHotReload.payload(baseline.manifest, actual.manifest, Set.of(), i),
                LuaHotReload.payload(baseline.manifest, expected.get(i).manifest, Set.of(), i));
        }
    }

    @Test
    public void booleanParametersKeepTheirColdDeclarationBindings() throws Exception {
        Project project = project(0);
        String source = project.source.replace("    return x + 1\n", "    if flag\n        return x + 1\n    return -x\n")
            .replace("scalar(int x)", "scalar(int x, bool flag)").replace("scalar(x)", "scalar(x, x > 0)");
        // Install the complete declaration before checking the body that refers to the new parameter.
        Files.writeString(project.file, source);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.file), source));
        assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
        new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, source.replace("x + 1", "x + 2"));
        Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(actual, project.manager);
        assertSameOutput(actual, fresh(project, ARGS));
    }

    @Test
    public void unreadLocalsUseBatchBeforeReuseRecovers() throws Exception {
        Project project = withBody(project(0), "    int unused = x + 1\n    return x * 3\n");
        new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, project.source.replace("x + 1", "x + 2"));
        Output unused = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertTrue(unused.work.copiedFunctions() > 1);
        assertSameOutput(unused, fresh(project, ARGS));
        String used = project.source.replace("    int unused = x + 1\n    return x * 3\n", "    int result = x + 1\n    return result\n");
        edit(project, used);
        Output rebound = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertTrue(rebound.work.copiedFunctions() > 1);
        assertSameOutput(rebound, fresh(project, ARGS));
        edit(project, used.replace("x + 1", "x + 2"));
        Output warm = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(warm, project.manager);
        assertSameOutput(warm, fresh(project, ARGS));
    }

    @Test
    public void oneFunctionTypeLoweringKeepsMutableTypesOwned() {
        var trace = Ast.NoExpr();
        var first = JassIm.ImFunction(trace, "first", JassIm.ImTypeVars(), JassIm.ImVars(), TypesHelper.imInt().copy(),
            JassIm.ImVars(JassIm.ImVar(trace, TypesHelper.imInt().copy(), "a", false)), JassIm.ImStmts(), new ArrayList<>());
        var second = first.copy();
        EliminateLocalTypes.eliminateLocalTypesFunc(first);
        EliminateLocalTypes.eliminateLocalTypesFunc(second);
        ImSimpleType type = (ImSimpleType) first.getLocals().get(0).getType();
        assertNotSame(type, second.getLocals().get(0).getType());
        type.setTypename("changed scratch type");
        assertTrue(EliminateLocalTypes.isIntegerOrLocalInteger(second.getLocals().get(0).getType()));
        assertTrue(EliminateLocalTypes.isIntegerOrLocalInteger(EliminateLocalTypes.localTypeFor(TypesHelper.imInt())));
    }

    private static void assertSameOutput(Output actual, Output expected) {
        assertEquals(actual.script, expected.script);
        assertEquals(actual.manifest, expected.manifest);
    }

    private static void assertScalarWork(Output changed, ModelManagerImpl manager) {
        assertEquals(changed.work.copiedFunctions(), 0, changed.work + " " + manager.incrementalImTranslation().scalarBackendReason());
        assertEquals(changed.work.copiedNodes(), 0L);
        assertEquals(changed.work.loweredFunctions(), 1, changed.work.toString());
        assertEquals(changed.work.emittedFunctions(), 1, changed.work.toString());
        assertEquals(manager.incrementalImTranslation().statistics().translatedFunctions(), 1);
    }

    @Test
    public void repeatedEditsAndRevertsKeepTheSameBaselineAndArtifacts() throws Exception {
        Project project = project(0);
        List<String> states = List.of(project.source, project.source.replace("x + 1", "x + 0"),
            project.source.replace("x + 1", "x + 9"), project.source);
        List<Output> expected = new ArrayList<>();
        // Build all expectations first: no fresh build clears shared lookup caches during replay.
        for (String state : states) {
            Files.writeString(project.file, state);
            expected.add(fresh(project, ARGS));
        }
        Files.writeString(project.file, states.get(0));
        var manager = new ModelManagerImpl(project.root.toFile(), new BufferManager());
        manager.buildProject();
        project = new Project(project.root, project.file, project.script, project.source, manager);
        Output baseline = new Probe(project.root, ARGS).compile(manager, project.script);
        for (int i = 1; i < states.size(); i++) {
            edit(project, states.get(i));
            Output actual = new Probe(project.root, ARGS).compile(manager, project.script);
            assertScalarWork(actual, manager);
            assertSameOutput(actual, expected.get(i));
            assertEquals(LuaHotReload.payload(baseline.manifest, actual.manifest, Set.of(), i),
                LuaHotReload.payload(baseline.manifest, expected.get(i).manifest, Set.of(), i));
        }
        Output unchanged = new Probe(project.root, ARGS).compile(manager, project.script);
        assertEquals(unchanged.work, new BackendWork.Snapshot(0, 0, 0, 0));
        assertSameOutput(unchanged, expected.get(0));
    }

    @Test
    public void sourceMovesAndCallsUseTheBatchPathThenScalarReuseRecovers() throws Exception {
        Project project = project(0);
        new Probe(project.root, ARGS).compile(project.manager, project.script);
        for (String state : List.of("\n" + project.source, project.source.replace("return x + 1", "return unrelated(x)"), project.source)) {
            edit(project, state);
            Output output = new Probe(project.root, ARGS).compile(project.manager, project.script);
            assertTrue(output.work.copiedFunctions() > 1, output.work.toString());
            assertSameOutput(output, fresh(project, ARGS));
        }
        edit(project, project.source.replace("x + 1", "x + 3"));
        Output output = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(output, project.manager);
        assertSameOutput(output, fresh(project, ARGS));
    }

    @Test
    public void compiletimeAndStackTraceOptionsNeverReuseScalarArtifacts() throws Exception {
        Project project = project(0);
        new Probe(project.root, ARGS).compile(project.manager, project.script);
        for (String option : List.of("-runcompiletimefunctions", "-stacktraces", "-localOptimizations")) {
            List<String> args = new ArrayList<>(ARGS);
            args.add(option);
            Output output = new Probe(project.root, args).compile(project.manager, project.script);
            assertTrue(output.work.copiedFunctions() > 1, output.work.toString());
            assertSameOutput(output, fresh(project, args));
        }
    }

    @DataProvider(name = "cancelBodies")
    public Object[][] cancelBodies() { return new Object[][] {{"    return x + 1\n"}, {largeBody(220, true)}}; }

    @Test(dataProvider = "cancelBodies")
    public void aCancelledPrivateTaskKeepsTheEditedFunctionPending(String body) throws Exception {
        Project project = withBody(project(0), body);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, project.source.replace("x + 1", "x + 2"));
        AtomicBoolean cancelled = new AtomicBoolean();
        WurstGui gui = new WurstGuiCliImpl() {
            @Override public void sendProgress(String message) {
                if (message.equals("Compiling changed scalar functions")) cancelled.set(true);
                CompilationCancellation.check();
            }
        };
        expectThrows(CancellationException.class,
            () -> new Probe(project.root, ARGS).compile(project.manager, project.script, gui, cancelled::get));
        assertTrue(cancelled.get());
        Path written = project.root.resolve("_build").resolve(de.peeeq.wurstio.languageserver.requests.MapRequest.BUILD_COMPILED_LUA_NAME);
        assertEquals(Files.readString(written), baseline.script);
        // No new source edit: the frontend has already observed the change, but the backend must still do it.
        Output retry = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertEquals(retry.work, new BackendWork.Snapshot(0, 0, 1, 1));
        assertSameOutput(retry, fresh(project, ARGS));
    }

    @Test
    public void cleaningTheModelDropsTheScalarSnapshot() throws Exception {
        Project project = project(0);
        new Probe(project.root, ARGS).compile(project.manager, project.script);
        project.manager.clean();
        project.manager.buildProject();
        Output rebuilt = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertTrue(rebuilt.work.copiedFunctions() > 1);
        edit(project, project.source.replace("x + 1", "x + 2"));
        Output edited = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(edited, project.manager);
        assertSameOutput(edited, fresh(project, ARGS));
    }

    @Test
    public void invalidSourceCanBeRepairedWithoutPublishingOrLosingTheEdit() throws Exception {
        Project project = project(0);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        String invalid = project.source.replace("return x + 1", "return missingValue");
        Files.writeString(project.file, invalid);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.file), invalid));
        assertTrue(project.manager.hasErrors());
        expectThrows(RuntimeException.class, () -> new Probe(project.root, ARGS).compile(project.manager, project.script));
        edit(project, project.source.replace("x + 1", "x + 2"));
        Output repaired = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(repaired, project.manager);
        assertSameOutput(repaired, fresh(project, ARGS));
        assertNotEquals(repaired.script, baseline.script);
    }

    @Test
    public void aSignatureChangeRebindsFramesBeforeScalarReuse() throws Exception {
        Project project = project(0);
        new Probe(project.root, ARGS).compile(project.manager, project.script);
        String signature = project.source.replace("scalar(int x)", "scalar(int x, int extra)")
            .replace("scalar(x)", "scalar(x, 0)");
        edit(project, signature);
        Output batch = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertTrue(batch.work.copiedFunctions() > 1);
        assertSameOutput(batch, fresh(project, ARGS));
        edit(project, signature.replace("x + 1", "x + 2"));
        Output changed = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(changed, project.manager);
        assertSameOutput(changed, fresh(project, ARGS));
    }

    @DataProvider(name = "runtimeBodies")
    public Object[][] runtimeBodies() {
        return new Object[][] {
            {"    return x + 1\n", "x + 1", "x + 2", "5\n6\n6\n15\n"},
            {"    int result = x + 1\n    if result > 3\n        return result * 3\n    return result - 1\n", "result * 3", "result * 4", "15\n20\n20\n15\n"},
            {"    int result = x + 1\n    for i = 0 to 3\n        result = result + i\n    return result\n", "x + 1", "x + 2", "11\n12\n12\n15\n"},
            {largeBody(220, true), "v0 = x + 1", "v0 = x + 2", "1106\n1107\n1107\n15\n"},
            {largeBody(220, true).replace("    return ", "    if x > 0\n        int nested = 7\n        v0 = v0 + nested\n    return "),
                "v0 = x + 1", "v0 = x + 2", "1113\n1114\n1114\n15\n"},
            {largeBody(220, true).replace("v0", "__wurst_locals"), "__wurst_locals = x + 1", "__wurst_locals = x + 2", "1106\n1107\n1107\n15\n"}
        };
    }

    @Test(dataProvider = "runtimeBodies")
    public void anIncrementalPayloadUpdatesTheFunctionAndItsUnchangedCallerInLua(String body, String before, String after, String expected) throws Exception {
        Project project = withBody(project(0), body);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, project.source.replace(before, after));
        Output edited = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(edited, project.manager);
        String scalar = functionName(baseline.manifest, "scalar");
        String caller = functionName(baseline.manifest, "caller");
        String unrelated = functionName(baseline.manifest, "unrelated");
        Path script = project.root.resolve("baseline.lua"), payload = project.root.resolve("payload.lua"), driver = project.root.resolve("driver.lua");
        Files.writeString(script, baseline.script);
        Files.writeString(payload, LuaHotReload.payload(baseline.manifest, edited.manifest, Set.of(), 1).source());
        Files.writeString(driver, "dofile(arg[1])\nprint(" + scalar + "(4))\n"
            + "local f = assert(io.open(arg[2], 'rb')) local source = f:read('a') f:close()\n"
            + "local ok, err = " + LuaHotReload.APPLY_FUNCTION + "(source) if not ok then error(err) end\n"
            + "print(" + scalar + "(4))\nprint(" + caller + "(4))\nprint(" + unrelated + "(5))\n");
        Process process = new ProcessBuilder(getLuaExecutable(), driver.toString(), script.toString(), payload.toString())
            .redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Lua payload execution timed out");
            String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertEquals(process.exitValue(), 0, output);
            assertEquals(output.replace("\r\n", "\n"), expected);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(5, TimeUnit.SECONDS), "Lua process did not terminate");
            }
        }
    }

    private static String functionName(LuaHotReload.Manifest manifest, String sourceName) {
        return manifest.functions().values().stream().filter(f -> f.name().startsWith(sourceName + "_"))
            .findFirst().orElseThrow().name();
    }

    @Test
    public void aConstantDependingOnTheChangedFunctionKeepsFreshPayloadRevisions() throws Exception {
        Project project = project(0);
        String source = project.source + "constant int tuned = scalar(4)\ninit\n    observe(tuned)\n";
        edit(project, source);
        Output baseline = new Probe(project.root, ARGS).compile(project.manager, project.script);
        edit(project, source.replace("x + 1", "x + 2"));
        Output actual = new Probe(project.root, ARGS).compile(project.manager, project.script);
        assertScalarWork(actual, project.manager);
        Output expected = fresh(project, ARGS);
        assertSameOutput(actual, expected);
        assertEquals(LuaHotReload.payload(baseline.manifest, actual.manifest, Set.of(), 1),
            LuaHotReload.payload(baseline.manifest, expected.manifest, Set.of(), 1));
    }

    @Test
    public void randomScalarEditChainsMatchIndependentBatchBuilds() throws Exception {
        int programs = Integer.parseInt(System.getenv().getOrDefault("WURST_FUZZ_PROGRAMS", "10"));
        long seed = Long.parseLong(System.getenv().getOrDefault("WURST_FUZZ_SEED", "20261026129"));
        Random random = new Random(seed);
        for (int program = 0; program < programs; program++) {
            Project project = project(0);
            String baseSource = project.source.replace("scalar(int x)", "scalar(int x, int y)")
                .replace("scalar(x)", "scalar(x, 3)");
            String expression = "(x + #)";
            int depth = random.nextInt(7);
            for (int level = 0; level < depth; level++) {
                String other = switch (random.nextInt(3)) { case 0 -> "x"; case 1 -> "y"; default -> Integer.toString(random.nextInt(10)); };
                String operator = List.of(" + ", " - ", " * ").get(random.nextInt(3));
                expression = random.nextBoolean() ? "(" + expression + operator + other + ")" : "(" + other + operator + expression + ")";
            }
            String condition = "x " + List.of("<", ">", "==", "!=", "<=", ">=").get(random.nextInt(6)) + " y";
            String body = "    int result = " + expression + "\n    bool choose = " + condition + "\n"
                + "    if choose and not (x == 0)\n        result = result * 2\n    else\n        result = result - 3\n";
            body += switch (random.nextInt(3)) {
                case 0 -> "    int count = 3\n    while count > 0\n        result = result + count\n        count = count - 1\n";
                case 1 -> "    for i = 0 to 3\n        result = result + i\n";
                default -> "    if result < 0\n        return -result\n";
            };
            int locals = 190 + random.nextInt(35);
            StringBuilder prologue = new StringBuilder();
            for (int i = 0; i < locals; i++) prologue.append("    int v").append(i).append(" = x + ").append(i % 10).append('\n');
            body = prologue + body + "    result = result + "
                + String.join(" + ", java.util.stream.IntStream.range(0, locals).mapToObj(i -> "v" + i).toList()) + "\n    return result\n";
            if (random.nextBoolean()) body = body.replace("v0", "__wurst_locals");
            String template = baseSource.replace("    return x + 1\n", body);
            if (random.nextBoolean()) template += "@extern native __wurst_locals() returns int\ninit\n    observe(__wurst_locals())\n";
            List<String> states = new ArrayList<>();
            List<Output> expected = new ArrayList<>();
            for (int edit = 0; edit < 4; edit++) {
                String source = template.replace("#", Integer.toString((edit + program) % 10));
                states.add(source);
                Files.writeString(project.file, source);
                expected.add(fresh(project, ARGS));
            }
            Files.writeString(project.file, states.get(0));
            var manager = new ModelManagerImpl(project.root.toFile(), new BufferManager());
            manager.buildProject();
            project = new Project(project.root, project.file, project.script, project.source, manager);
            Output baseline = new Probe(project.root, ARGS).compile(manager, project.script);
            assertSameOutput(baseline, expected.get(0));
            for (int edit = 1; edit < states.size(); edit++) {
                edit(project, states.get(edit));
                Output actual = new Probe(project.root, ARGS).compile(manager, project.script);
                assertScalarWork(actual, manager);
                assertSameOutput(actual, expected.get(edit));
                assertEquals(LuaHotReload.payload(baseline.manifest, actual.manifest, Set.of(), edit),
                    LuaHotReload.payload(baseline.manifest, expected.get(edit).manifest, Set.of(), edit));
            }
        }
        System.out.println("[scalar backend fuzz] programs=" + programs + " seed=" + seed + " edits=" + programs * 3);
    }
}
