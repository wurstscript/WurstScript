package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.translation.imtranslation.IncrementalImTranslation;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.*;

public class IncrementalHotReloadCompilationTests {
    private record Project(Path root, Path file, ModelManagerImpl manager) { }
    private record Output(String script, LuaHotReload.Manifest manifest) { }

    private static Project project(String source) throws Exception {
        return project(source, null);
    }

    private static Project project(String source, String library) throws Exception {
        Path root = Files.createTempDirectory("incremental-hot-reload");
        Files.createDirectories(root.resolve("wurst"));
        Files.writeString(root.resolve("wurst.build"), "projectName: Test\nwc3Patch: v2.0\n");
        Files.writeString(root.resolve("wurst/Wurst.wurst"), "package Wurst\n");
        Path file = root.resolve("wurst/Main.wurst");
        Files.writeString(file, source);
        if (library != null) Files.writeString(root.resolve("wurst/Library.wurst"), library);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        manager.buildProject();
        assertFalse(manager.hasErrors(), manager.getFirstErrorDescription());
        return new Project(root, file, manager);
    }

    private static Output compile(Project project, IncrementalImTranslation cache) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        RunArgs args = new RunArgs("-lua", "-luaHotReload", "-stacktraces", "-runcompiletimefunctions");
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(project.root.toFile(), gui, null, args);
        if (cache != null) compiler.setIncrementalImTranslation(cache);
        MapRequest.checkModel(project.manager, compiler, project.manager.getModel(), args);
        compiler.translateProgToIm(project.manager.getModel());
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        StringBuilder script = new StringBuilder();
        compiler.transformProgToLua().print(script, 0);
        assertEquals(gui.getErrorCount(), 0, gui.getErrors());
        return new Output(script.toString(), compiler.getLuaTranslator().hotReloadManifest());
    }

    private static final String SOURCE = """
        package Main
        import NoWurst
        native observe(int value)
        int stored = 7
        function stable(int value) returns int
            int result = value + stored
            return result * 2
        function changed() returns int
            return stable(3)
        init
            observe(changed())
        """;

    @Test
    public void unchangedFunctionsAreReusedWithoutSharingLoweredTrees() throws Exception {
        Project project = project(SOURCE);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        Output first = compile(project, cache);
        Output second = compile(project, cache);
        assertEquals(second.script, first.script);
        assertEquals(second.manifest, first.manifest);
        assertTrue(cache.statistics().reusedFunctions() >= 2, cache.statistics().toString());
        assertEquals(cache.statistics().translatedFunctions(), 0);
        assertEquals(cache.statistics().frontendBuilds(), 1);
        assertEquals(second, fresh(project));
    }

    private static Output fresh(Project project) {
        ModelManagerImpl manager = new ModelManagerImpl(project.root.toFile(), new BufferManager());
        manager.buildProject();
        return compile(new Project(project.root, project.file, manager), null);
    }

    private static void edit(Project project, String source) throws Exception {
        Files.writeString(project.file, source);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.file), source));
        assertFalse(project.manager.hasErrors(), project.manager.getFirstErrorDescription());
    }

    @Test
    public void bodyEditsAndMovedLinesKeepFreshBuildOutput() throws Exception {
        Project project = project(SOURCE);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        for (String source : new String[]{SOURCE.replace("stable(3)", "stable(8)"), "\n\n" + SOURCE, SOURCE}) {
            edit(project, source);
            assertEquals(compile(project, cache), fresh(project));
            assertTrue(cache.statistics().reusedFunctions() >= 1, cache.statistics().toString());
            assertEquals(cache.statistics().frontendBuilds(), 1);
        }
    }

    @Test
    public void declarationsInvalidatePreviouslyResolvedCalls() throws Exception {
        Project project = project(SOURCE);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        edit(project, SOURCE.replace("int stored = 7", "int stored = 11"));
        assertEquals(compile(project, cache), fresh(project));
        assertEquals(cache.statistics().reusedFunctions(), 0);
        assertEquals(compile(project, cache), fresh(project));
        assertTrue(cache.statistics().reusedFunctions() >= 2, cache.statistics().toString());
    }

    @Test
    public void recursionAndCallbackReferencesAreRebound() throws Exception {
        String source = SOURCE.replace("native observe(int value)", "native observe(int value)\nnative register(code callback)")
            + "function recursive(int n) returns int\n    if n == 0\n        return 1\n    return recursive(n - 1)\n"
            + "function callback()\n    observe(recursive(3))\ninit\n    register(function callback)\n";
        Project project = project(source);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        assertEquals(compile(project, cache), fresh(project));
        assertTrue(cache.statistics().reusedFunctions() >= 4, cache.statistics().toString());
    }

    @Test
    public void aSignatureChangeInAnotherUnitInvalidatesResolution() throws Exception {
        String library = "package Library\npublic function choose(real value) returns int\n    return 7\n";
        Project project = project(SOURCE.replace("import NoWurst", "import NoWurst\nimport Library")
            .replace("return stable(3)", "return choose(3)"), library);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        Path file = project.root.resolve("wurst/Library.wurst");
        edit(new Project(project.root, file, project.manager), library.replace("real value", "int value").replace("return 7", "return 11"));
        assertEquals(compile(project, cache), fresh(project));
        assertEquals(cache.statistics().reusedFunctions(), 0);
    }

    @Test
    public void invalidBodyEditsCanBeRepairedWithoutPoisoningTheCache() throws Exception {
        Project project = project(SOURCE);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        String invalid = SOURCE.replace("return stable(3)", "return missingName");
        Files.writeString(project.file, invalid);
        project.manager.reconcile(project.manager.syncCompilationUnitContent(WFile.create(project.file), invalid));
        assertTrue(project.manager.hasErrors());
        edit(project, SOURCE);
        assertEquals(compile(project, cache), fresh(project));
        assertTrue(cache.statistics().reusedFunctions() >= 2, cache.statistics().toString());
    }

    @Test
    public void compiletimeEvaluationUsesReboundAndEditedFunctions() throws Exception {
        String source = SOURCE + "function compiletime(int value) returns int\n    return value\n"
            + "constant evaluated = compiletime(stable(3))\ninit\n    observe(evaluated)\n";
        Project project = project(source);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        assertEquals(compile(project, cache), fresh(project));
        edit(project, source.replace("return result * 2", "return result * 3"));
        assertEquals(compile(project, cache), fresh(project));
    }

    @Test
    public void cleaningTheModelReleasesCachedTranslations() throws Exception {
        Project project = project(SOURCE);
        IncrementalImTranslation cache = project.manager.incrementalImTranslation();
        compile(project, cache);
        assertTrue(cache.statistics().cachedFunctions() >= 2);
        project.manager.clean();
        assertEquals(cache.statistics().cachedFunctions(), 0);
    }

    @Test
    public void failedCompiletimeEvaluationDoesNotPoisonRetainedInput() throws Exception {
        String source = SOURCE + "function compiletime(int value) returns int\n    return value\n"
            + "constant evaluated = compiletime(stable(3))\ninit\n    observe(evaluated)\n";
        Project project = project(source);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        edit(project, source.replace("return result * 2", "return result div 0"));
        expectThrows(RuntimeException.class, () -> compile(project, cache));
        edit(project, source.replace("return result * 2", "return result * 3"));
        assertEquals(compile(project, cache), fresh(project));
        assertEquals(cache.statistics().frontendBuilds(), 1);
    }

    @Test
    public void memberStorageAndVirtualCallsBindToTheCurrentClasses() throws Exception {
        String source = SOURCE + "class Reader\n    int value\n"
            + "    function read(int delta) returns int\n        return value + delta\n"
            + "    function through(Reader other) returns int\n        return other.read(value)\n"
            + "init\n    let reader = new Reader\n    reader.value = 5\n    observe(reader.through(reader))\n";
        Project project = project(source);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        compile(project, cache);
        assertEquals(compile(project, cache), fresh(project));
        assertTrue(cache.statistics().reusedFunctions() >= 4, cache.statistics().toString());
        edit(project, source.replace("return value + delta", "return value + delta + 1"));
        assertEquals(compile(project, cache), fresh(project));
        assertTrue(cache.statistics().reusedFunctions() >= 3, cache.statistics().toString());
    }

    @Test
    public void loweringForkPreservesGenericClassesAndCompiletimeState() throws Exception {
        String source = SOURCE + "class Box<T:>\n    T value\n    construct(T value)\n        this.value = value\n"
            + "function compiletime(int value) returns int\n    return value\n"
            + "constant evaluated = compiletime(stable(3))\ninit\n    let box = new Box<int>(evaluated)\n"
            + "    observe(box.value)\n    destroy box\n";
        Project project = project(source);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        assertEquals(compile(project, cache), fresh(project));
        assertEquals(compile(project, cache), fresh(project));
        edit(project, source.replace("return result * 2", "return result * 5"));
        assertEquals(compile(project, cache), fresh(project));
        assertEquals(cache.statistics().frontendBuilds(), 1);
    }

    @Test
    public void unchangedClosuresKeepFreshPositionsAndRemovedClosuresRebuild() throws Exception {
        String source = SOURCE + "interface Value\n    function get() returns int\n"
            + "function make(int captured) returns Value\n    return () -> captured + 1\n"
            + "init\n    let closure = make(5)\n    observe(closure.get())\n    destroy closure\n";
        Project project = project(source);
        IncrementalImTranslation cache = new IncrementalImTranslation();
        assertEquals(compile(project, cache), fresh(project));
        edit(project, "\n\n" + source.replace("return result * 2", "return result * 4"));
        assertEquals(compile(project, cache), fresh(project));
        assertEquals(cache.statistics().frontendBuilds(), 1);
        edit(project, source.replace("return () -> captured + 1", "return null"));
        assertEquals(compile(project, cache), fresh(project));
        assertEquals(cache.statistics().frontendBuilds(), 2);
    }
}
