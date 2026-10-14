package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.*;

public class MapModelReuseTests {
    @Test(dataProvider = "commands")
    public void excludedVariableEventsDoNotPreserveSelectedGlobals(boolean build, boolean lua) throws Exception {
        Project project = project();
        String script = "globals\nreal selectedGlobal = 1.0\nendglobals\n"
            + "function main takes nothing returns nothing\nendfunction\n";
        File base = new File(project.root, "wurst/war3map.j");
        Files.writeString(base.toPath(), script);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(base), script));
        File extra = new File(project.root, "wurst/editor-only.j");
        String excluded = "function unusedEventRegistration takes nothing returns nothing\n"
            + "call TriggerRegisterVariableEvent(null, \"selectedGlobal\", null, 0.0)\nendfunction\n";
        Files.writeString(extra.toPath(), excluded);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(extra), excluded));
        assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
        Files.writeString(project.script.toPath(), script);
        de.peeeq.wurstscript.ast.GlobalVarDef[] selected = new de.peeeq.wurstscript.ast.GlobalVarDef[1];
        project.manager.getModel().accept(new WurstModel.DefaultVisitor() {
            @Override public void visit(de.peeeq.wurstscript.ast.GlobalVarDef variable) {
                if (variable.getName().equals("selectedGlobal")) selected[0] = variable;
            }
        });
        assertTrue(selected[0].hasAnnotation("@preserveName"), "the editor-wide check sees the excluded registration");
        List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
        Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        String output = compile(request, project, new WurstGuiCliImpl());
        assertFalse(output.contains("selectedGlobal"), "an excluded registration must not keep a dead global alive");
        assertTrue(selected[0].hasAnnotation("@preserveName"), "map compilation must restore editor-wide metadata");
        assertTrue(project.manager.isFullyChecked(project.manager.getModel()));
        var full = new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(
            project.manager.getModel(), false, RunArgs.defaults()).translateProg();
        assertTrue(de.peeeq.wurstscript.validation.NamePreservation.isPreserved(full.getGlobals().stream()
            .filter(v -> v.getName().equals("selectedGlobal")).findFirst().orElseThrow()),
            "a later full-model translation must still see the editor-wide preservation state");
        assertTrue(de.peeeq.wurstscript.validation.NamePreservation.isPreserved(full.copyWithRefs().getGlobals().stream()
            .filter(v -> v.getName().equals("selectedGlobal")).findFirst().orElseThrow()),
            "private preservation metadata must survive IM copying");
        assertEquals(compile(request, project, new WurstGuiCliImpl()), output);
        Files.writeString(project.script.toPath(), script
            + "function configuredHelper takes nothing returns integer\nreturn 2\nendfunction\n");
        assertFalse(compile(request, project, new WurstGuiCliImpl()).contains("selectedGlobal"),
            "validation of a changed configured script must also use the selected view");
    }

    @Test(dataProvider = "commands")
    public void excludedNativeTypesFailBeforeEmission(boolean build, boolean lua) throws Exception {
        for (String script : List.of(
            "globals\neditorhandle selectedGlobal = null\nendglobals\n"
                + "function main takes nothing returns nothing\ncall DestroyTrigger(selectedGlobal)\nendfunction\n",
            "type selectedhandle extends editorhandle\nglobals\nselectedhandle selectedGlobal = null\nendglobals\n"
                + "function main takes nothing returns nothing\ncall DestroyTrigger(selectedGlobal)\nendfunction\n",
            "function selectedHelper takes editorhandle value returns nothing\n"
                + "call DestroyTrigger(value)\nendfunction\n"
                + "function main takes nothing returns nothing\ncall selectedHelper(null)\nendfunction\n",
            "function selectedHelper takes nothing returns editorhandle\nreturn null\nendfunction\n"
                + "function main takes nothing returns nothing\ncall DestroyTrigger(selectedHelper())\nendfunction\n")) {
            Project project = project();
            File extra = new File(project.root, "wurst/editor-only.j");
            String declaration = "type editorhandle extends trigger\n";
            Files.writeString(extra.toPath(), declaration);
            project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(extra), declaration));
            File base = new File(project.root, "wurst/war3map.j");
            Files.writeString(base.toPath(), script);
            project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(base), script));
            assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
            Files.writeString(project.script.toPath(), script);
            List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
            Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
            CompileError error = expectThrows(CompileError.class,
                () -> compile(request, project, new WurstGuiCliImpl()));
            assertTrue(error.getMessage().contains("not included in this map compilation"), error.getMessage());
            assertTrue(error.getMessage().contains("editor-only.j"), error.getMessage());
        }
    }

    @Test(dataProvider = "commands")
    public void excludedEditorUnitsDoNotBlockConfiguredScriptCompilation(boolean build, boolean lua) throws Exception {
        Project project = project();
        File base = new File(project.root, "wurst/war3map.j");
        String script = configuredScript(1);
        Files.writeString(base.toPath(), script);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(base), script));
        File extra = new File(project.root, "wurst/editor-only.j");
        String editorOnly = "function unusedEditorHelper takes nothing returns integer\n"
            + "return getConfigured()\nendfunction\n";
        Files.writeString(extra.toPath(), editorOnly);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(extra), editorOnly));
        assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
        Files.writeString(project.script.toPath(), "function main takes nothing returns nothing\nendfunction\n");
        List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
        Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        int units = project.manager.getModel().size();
        String output = compile(request, project, new WurstGuiCliImpl());
        assertFalse(output.contains("unusedEditorHelper"));
        assertEquals(project.manager.getModel().size(), units);
        assertFalse(project.manager.isFullyChecked(project.manager.getModel()), "excluded units remain unchecked");
        assertEquals(compile(request, project, new WurstGuiCliImpl()), output);
        assertEquals(project.manager.cleanCalls, 0);
    }

    @Test(dataProvider = "commands")
    public void referencesToExcludedJassDefinitionsFailBeforeEmission(boolean build, boolean lua) throws Exception {
        for (String expression : List.of("editorOnly", "editorHelper()")) {
            Project project = project();
            File extra = new File(project.root, "wurst/editor-only.j");
            String declarations = "globals\ninteger editorOnly = 1\nendglobals\n"
                + "function editorHelper takes nothing returns integer\nreturn 2\nendfunction\n";
            Files.writeString(extra.toPath(), declarations);
            project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(extra), declarations));
            String script = "function main takes nothing returns nothing\n"
                + "call BJDebugMsg(I2S(" + expression + "))\nendfunction\n";
            File base = new File(project.root, "wurst/war3map.j");
            Files.writeString(base.toPath(), script);
            project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(base), script));
            assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
            Files.writeString(project.script.toPath(), script);
            int units = project.manager.getModel().size();
            List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
            Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
            CompileError error = expectThrows(CompileError.class,
                () -> compile(request, project, new WurstGuiCliImpl()));
            assertTrue(error.getMessage().contains("not included in this map compilation"), error.getMessage());
            assertTrue(error.getMessage().contains("editor-only.j"), error.getMessage());
            assertEquals(project.manager.getModel().size(), units);
            assertTrue(project.manager.isFullyChecked(project.manager.getModel()));
            Files.writeString(project.script.toPath(), configuredScript(1));
            compile(request, project, new WurstGuiCliImpl());
            assertEquals(project.manager.cleanCalls, 0);
        }
    }

    @Test(dataProvider = "commands")
    public void replacingMapGlobalsInvalidatesLookupsInTheirUsers(boolean build, boolean lua) throws Exception {
        Project project = project();
        File base = new File(project.root, "wurst/war3map.j");
        String script = "globals\ninteger gg_test = 1\nendglobals\n" + configuredScript(1);
        Files.writeString(base.toPath(), script);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(base), script));
        String source = source(7) + "init\n    BJDebugMsg(I2S(gg_test))\n";
        Files.writeString(project.source.toPath(), source);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.source), source));
        File extra = new File(project.root, "wurst/editor-only.j");
        Files.writeString(extra.toPath(), "globals\ninteger editorOnly = 1\nendglobals\n");
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(extra), Files.readString(extra.toPath())));
        int units = project.manager.getModel().size();
        Files.writeString(project.script.toPath(), script.replace("gg_test = 1", "gg_test = 2"));
        List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
        Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        String output = compile(request, project, new WurstGuiCliImpl());
        assertTrue(output.contains(lua ? "gg_test = 2" : "integer gg_test"), output);
        assertEquals(project.manager.getModel().size(), units, "building must retain editor compilation units");
        Project fresh = new Project(project.root, project.source, project.script, new CountingManager(project.root));
        fresh.manager.buildProject();
        Probe freshRequest = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        assertEquals(output, compile(freshRequest, fresh, new WurstGuiCliImpl()));
    }
    @DataProvider(name = "commands")
    public Object[][] commands() {
        return new Object[][]{{false, false}, {false, true}, {true, false}, {true, true}};
    }

    @Test(dataProvider = "commands")
    public void scopedRunAndBuildReuseTheManagedModelAfterEdits(boolean build, boolean lua) throws Exception {
        Project project = project();
        List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
        Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        WurstModel original = project.manager.getModel();
        String first = compile(request, project, new WurstGuiCliImpl());
        assertSame(request.model(), original, "cancellation must not force an unchecked AST copy");
        assertTrue(project.manager.isFullyChecked(original));
        assertEquals(compile(request, project, new WurstGuiCliImpl()), first);
        assertEquals(project.manager.cleanCalls, 0, "map compilation must not clean/reparse the editor model");

        String edited = source(17);
        Files.writeString(project.source.toPath(), edited);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.source), edited));
        assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
        String afterEdit = compile(request, project, new WurstGuiCliImpl());
        assertNotEquals(afterEdit, first, "the edited method must reach the emitted script");

        Project freshProject = new Project(project.root, project.source, project.script, new CountingManager(project.root));
        freshProject.manager.buildProject();
        Probe freshRequest = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        assertEquals(compile(freshRequest, freshProject, new WurstGuiCliImpl()), afterEdit,
            "reusing the edited model must emit the same script as a fresh parse/check");
        assertSame(request.model(), original);
        assertEquals(project.manager.cleanCalls, 0);
    }

    @Test
    public void cancellationDuringSharedPreparationLeavesAUsableCheckedModel() throws Exception {
        Project project = project();
        String edited = source(23);
        Files.writeString(project.source.toPath(), edited);
        project.manager.syncCompilationUnitContent(WFile.create(project.source), edited);
        assertFalse(project.manager.isFullyChecked(project.manager.getModel()));
        AtomicBoolean canceled = new AtomicBoolean();
        AtomicBoolean checkedStarted = new AtomicBoolean();
        WurstGui gui = new WurstGuiCliImpl() {
            @Override public void sendProgress(String message) {
                if (message.equals("Checking Files")) {
                    checkedStarted.set(true);
                    canceled.set(true);
                }
                CompilationCancellation.check();
            }
        };
        Probe request = new RunProbe(project.root, List.of("-lua"));
        expectThrows(CancellationException.class, () -> CompilationCancellation.run(canceled::get,
            () -> request.compile(gui, project.manager, project.script)));
        assertTrue(checkedStarted.get());
        assertTrue(project.manager.isFullyChecked(project.manager.getModel()),
            "shared preparation must finish before honoring cancellation");
        assertEquals(gui.getErrorCount(), 0);
        CompilationCancellation.check();
        compile(new RunProbe(project.root, List.of("-lua")), project, new WurstGuiCliImpl());
        assertFalse(project.manager.hasErrors());
        assertEquals(project.manager.cleanCalls, 0);
    }

    @Test
    public void cancellationDuringPrivateCompiletimeWorkKeepsTheModelReusable() throws Exception {
        Project project = project();
        AtomicBoolean canceled = new AtomicBoolean();
        WurstGui gui = new WurstGuiCliImpl() {
            @Override public void sendProgress(String message) {
                if (message.equals("Running compiletime functions")) canceled.set(true);
                CompilationCancellation.check();
            }
        };
        Probe request = new RunProbe(project.root, List.of("-lua", "-runcompiletimefunctions"));
        expectThrows(CancellationException.class, () -> CompilationCancellation.run(canceled::get,
            () -> request.compile(gui, project.manager, project.script)));
        assertTrue(canceled.get(), "private compilation must remain cancellable");
        assertTrue(project.manager.isFullyChecked(project.manager.getModel()));
        assertEquals(gui.getErrorCount(), 0);
        String afterCancel = compile(new RunProbe(project.root, List.of("-lua")), project, new WurstGuiCliImpl());
        Project fresh = new Project(project.root, project.source, project.script, new CountingManager(project.root));
        fresh.manager.buildProject();
        assertEquals(compile(new RunProbe(project.root, List.of("-lua")), fresh, new WurstGuiCliImpl()), afterCancel);
        assertEquals(project.manager.cleanCalls, 0);
    }

    @Test(dataProvider = "commands")
    public void invalidEditsFailAndARepairNeedsNoClean(boolean build, boolean lua) throws Exception {
        Project project = project();
        List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
        Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        String invalid = source(7).replace("return result", "return missingValue");
        Files.writeString(project.source.toPath(), invalid);
        project.manager.syncCompilationUnitContent(WFile.create(project.source), invalid);
        WurstGui gui = new WurstGuiCliImpl();
        expectThrows(RuntimeException.class, () -> CompilationCancellation.run(() -> false,
            () -> request.compile(gui, project.manager, project.script)));
        assertTrue(gui.getErrors().contains("missingValue"), gui.getErrors());
        assertFalse(project.manager.isFullyChecked(project.manager.getModel()));

        String repaired = source(31);
        Files.writeString(project.source.toPath(), repaired);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.source), repaired));
        assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
        compile(request, project, new WurstGuiCliImpl());
        assertTrue(project.manager.isFullyChecked(project.manager.getModel()));
        assertEquals(project.manager.cleanCalls, 0);
    }

    @Test(dataProvider = "commands")
    public void changedConfiguredScriptIsCheckedBeforeReusingTheModel(boolean build, boolean lua) throws Exception {
        Project project = project();
        File baseScript = new File(project.root, "wurst/war3map.j");
        String base = configuredScript(1);
        Files.writeString(baseScript.toPath(), base);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(baseScript), base));
        String main = source(7) + "public int fromMap = getConfigured()\n"
            + "init\n    BJDebugMsg(I2S(fromMap))\n";
        Files.writeString(project.source.toPath(), main);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.source), main));
        List<String> args = lua ? List.of("-lua") : List.of("-noPJass");
        Probe request = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);

        Files.writeString(project.script.toPath(), configuredScript(2));
        String first = compile(request, project, new WurstGuiCliImpl());
        assertTrue(project.manager.isFullyChecked(project.manager.getModel()));
        Files.writeString(project.script.toPath(), configuredScript(3));
        String changed = compile(request, project, new WurstGuiCliImpl());
        assertNotEquals(changed, first);
        assertEquals(compile(request, project, new WurstGuiCliImpl()), changed);

        Project fresh = new Project(project.root, project.source, project.script, new CountingManager(project.root));
        fresh.manager.buildProject();
        Probe freshRequest = build ? new BuildProbe(project.root, args) : new RunProbe(project.root, args);
        assertEquals(compile(freshRequest, fresh, new WurstGuiCliImpl()), changed);
        assertEquals(project.manager.cleanCalls, 0);
    }

    private static String configuredScript(int value) {
        return "function main takes nothing returns nothing\nendfunction\n"
            + "function getConfigured takes nothing returns integer\n    return " + value + "\nendfunction\n";
    }

    private static String compile(Probe request, Project project, WurstGui gui) throws Exception {
        File[] output = new File[1];
        CompilationCancellation.run(() -> false, () -> output[0] = request.compile(gui, project.manager, project.script));
        assertEquals(gui.getErrorCount(), 0, gui.getErrors());
        return Files.readString(output[0].toPath());
    }

    private static Project project() throws Exception {
        File root = Files.createTempDirectory("wurst-map-model-reuse").toFile();
        File wurst = new File(root, "wurst");
        assertTrue(wurst.mkdirs());
        Files.writeString(new File(root, "wurst.build").toPath(), "projectName: Test\nwc3Patch: v2.0\n");
        Files.writeString(new File(wurst, "Wurst.wurst").toPath(), "package Wurst\n");
        File source = new File(wurst, "Main.wurst");
        Files.writeString(source.toPath(), source(7));
        String jass = "function main takes nothing returns nothing\nendfunction\n";
        Files.writeString(new File(wurst, "war3map.j").toPath(), jass);
        File script = new File(root, "configured.j");
        Files.writeString(script.toPath(), jass);
        CountingManager manager = new CountingManager(root);
        manager.buildProject();
        assertFalse(manager.hasErrors(), manager.getFirstErrorDescription());
        return new Project(root, source, script, manager);
    }

    private static String source(int value) {
        return "package Main\nimport NoWurst\n"
            + "module Value<T>\n    T stored\n    function get() returns T\n        return stored\n"
            + "class Box<T>\n    use Value<T>\n    construct(T value)\n        stored = value\n"
            + "class Modern<T:>\n    T stored\n    construct(T value)\n        stored = value\n"
            + "function answer() returns int\n    let old = new Box<int>(" + value + ")\n"
            + "    let newer = new Modern<int>(5)\n    let result = old.get() + newer.stored\n"
            + "    destroy old\n    destroy newer\n    return result\n"
            + "public int result = answer()\n";
    }

    private record Project(File root, File source, File script, CountingManager manager) {}

    private static class CountingManager extends ModelManagerImpl {
        int cleanCalls;
        CountingManager(File root) { super(root, new BufferManager()); }
        @Override public void clean() { cleanCalls++; super.clean(); }
    }

    private interface Probe {
        File compile(WurstGui gui, ModelManager manager, File script) throws Exception;
        WurstModel model();
    }

    private static class RunProbe extends RunMap implements Probe {
        private WurstModel model;
        RunProbe(File root, List<String> args) {
            super(null, WFile.create(root), Optional.empty(), Optional.empty(), args, Optional.empty());
        }
        @Override public File compile(WurstGui gui, ModelManager manager, File script) throws Exception {
            return compileScript(gui, manager, compileArgs, Optional.empty(), WurstProjectConfigData.empty(), false, script);
        }
        @Override public WurstModel model() { return model; }
        @Override protected File compileMap(ModelManager manager, File root, WurstGui gui, Optional<File> map,
            RunArgs args, WurstModel model, WurstProjectConfigData config, boolean prod) {
            this.model = model;
            return super.compileMap(manager, root, gui, map, args, model, config, prod);
        }
    }

    private static class BuildProbe extends BuildMap implements Probe {
        private WurstModel model;
        BuildProbe(File root, List<String> args) {
            super(null, WFile.create(root), Optional.empty(), Optional.empty(), args);
        }
        @Override public File compile(WurstGui gui, ModelManager manager, File script) throws Exception {
            return compileScript(gui, manager, compileArgs, Optional.empty(), WurstProjectConfigData.empty(), true, script);
        }
        @Override public WurstModel model() { return model; }
        @Override protected File compileMap(ModelManager manager, File root, WurstGui gui, Optional<File> map,
            RunArgs args, WurstModel model, WurstProjectConfigData config, boolean prod) {
            this.model = model;
            return super.compileMap(manager, root, gui, map, args, model, config, prod);
        }
    }
}
