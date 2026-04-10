package tests.wurstscript.tests;

import config.WurstProjectConfigData;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.testng.SkipException;
import org.testng.annotations.Test;
import systems.crigges.jmpq3.JMpqEditor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class HotReloadPipelineTests {

    @Test
    public void hotReloadExtractionUsesSourceMap() throws Exception {
        File projectFolder = new File("./temp/testProject_hotrun_extract/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);

        File sourceMap = new File(projectFolder, "source_map.w3x");
        File cachedMap = new File(projectFolder, "cached_map.w3x");
        Files.write(sourceMap.toPath(), new byte[] {0x01});
        Files.write(cachedMap.toPath(), new byte[] {0x02});

        WurstLanguageServer langServer = new WurstLanguageServer();
        TestMapRequest request = new TestMapRequest(
            langServer,
            Optional.of(sourceMap),
            List.of(),
            WFile.create(projectFolder),
            Map.of(
                sourceMap, "source script".getBytes(StandardCharsets.UTF_8),
                cachedMap, "cached script".getBytes(StandardCharsets.UTF_8)
            )
        );

        MapRequest.mapLastModified = System.currentTimeMillis();
        MapRequest.mapPath = sourceMap.getAbsolutePath();

        Optional<File> extractionMap = request.getMapForScriptExtractionForTest(Optional.of(cachedMap));
        File scriptFile = request.loadMapScriptForTest(extractionMap, new ModelManagerImpl(projectFolder, new BufferManager()), new WurstGuiLogger());

        assertEquals(extractionMap.orElse(null), sourceMap);
        assertEquals(request.getLastExtractedMap(), sourceMap);
        assertEquals(Files.readString(scriptFile.toPath()), "source script");
    }

    @Test
    public void cachedMapFileNameIsModeSpecific() throws Exception {
        File projectFolder = new File("./temp/testProject_cache_mode_specific/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);

        File sourceMap = new File(projectFolder, "source_map.w3x");
        Files.write(sourceMap.toPath(), new byte[] {0x01});

        WurstLanguageServer langServer = new WurstLanguageServer();
        TestMapRequest luaRequest = new TestMapRequest(
            langServer,
            Optional.of(sourceMap),
            List.of("-lua"),
            WFile.create(projectFolder),
            Map.of()
        );
        TestMapRequest jassRequest = new TestMapRequest(
            langServer,
            Optional.of(sourceMap),
            List.of(),
            WFile.create(projectFolder),
            Map.of()
        );

        File luaCache = luaRequest.getCachedMapFileForTest();
        File jassCache = jassRequest.getCachedMapFileForTest();
        assertNotNull(luaCache);
        assertNotNull(jassCache);
        assertEquals(luaCache.equals(jassCache), false, "Lua/Jass modes must not share cached map filename");
        assertEquals(luaCache.getName().contains("_lua_cached.w3x"), true);
        assertEquals(jassCache.getName().contains("_jass_cached.w3x"), true);
    }

    @Test
    public void folderMapInputIsMaterializedAsCachedArchive() throws Exception {
        if (!jmpqCreateEmptyArchiveAvailable()) {
            throw new SkipException("Requires JMPQ3 createEmptyArchive(File).");
        }

        File projectFolder = new File("./temp/testProject_folder_map_cache/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);

        File sourceMap = new File(projectFolder, "folder_map.w3x");
        Files.createDirectories(sourceMap.toPath());
        Files.writeString(new File(sourceMap, "war3map.j").toPath(), "folder script");
        File nestedFile = new File(sourceMap, "war3mapImported\\asset.txt");
        Files.createDirectories(nestedFile.getParentFile().toPath());
        Files.writeString(nestedFile.toPath(), "asset data");

        WurstLanguageServer langServer = new WurstLanguageServer();
        TestMapRequest request = new TestMapRequest(
            langServer,
            Optional.of(sourceMap),
            List.of(),
            WFile.create(projectFolder),
            Map.of()
        );

        File cachedMap = request.ensureCachedMapForTest(new WurstGuiLogger());

        try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(Optional.of(cachedMap), true)) {
            assertEquals(mpqEditor.hasFile("war3map.j"), true);
            assertEquals(mpqEditor.hasFile("war3mapImported\\asset.txt"), true);
            assertEquals(new String(mpqEditor.extractFile("war3map.j"), StandardCharsets.UTF_8), "folder script");
            assertEquals(new String(mpqEditor.extractFile("war3mapImported\\asset.txt"), StandardCharsets.UTF_8), "asset data");
        }
    }

    private boolean jmpqCreateEmptyArchiveAvailable() {
        try {
            JMpqEditor.class.getMethod("createEmptyArchive", File.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Test
    public void jhcrPipelineRenamesOutputScript() throws Exception {
        File projectFolder = new File("./temp/testProject_jhcr_output/");
        File wurstFolder = new File(projectFolder, "wurst");
        File buildDir = new File(projectFolder, "_build");
        newCleanFolder(wurstFolder);
        buildDir.mkdirs();

        File sourceMap = new File(projectFolder, "source_map.w3x");
        WurstLanguageServer langServer = new WurstLanguageServer();
        TestMapRequest request = new TestMapRequest(
            langServer,
            Optional.of(sourceMap),
            List.of("-hotstart"),
            WFile.create(projectFolder),
            new HashMap<>()
        );

        File rawJhcrScript = new File(buildDir, "jhcr_war3map.j");
        Files.writeString(rawJhcrScript.toPath(), "jhcr output");

        File renamed = request.renameJhcrOutputForTest(buildDir);

        assertEquals(request.isHotStartmapForTest(), true);
        assertNotNull(renamed);
        assertEquals(renamed.getName(), MapRequest.BUILD_JHCR_SCRIPT_NAME);
    }

    @Test
    public void hotReloadDoesNotRequireSourceMap() throws Exception {
        File projectFolder = new File("./temp/testProject_hotreload_nomap/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);

        File war3mapJ = new File(wurstFolder, "war3map.j");
        Files.writeString(war3mapJ.toPath(), "function main takes nothing returns nothing\nendfunction");

        WurstLanguageServer langServer = new WurstLanguageServer();
        HotReloadNoMapRequest request = new HotReloadNoMapRequest(
            langServer,
            Optional.empty(),
            List.of("-hotreload"),
            WFile.create(projectFolder)
        );

        MapRequest.CompilationResult result = request.compileScriptForTest(
            new ModelManagerImpl(projectFolder, new BufferManager()),
            new WurstGuiLogger(),
            Optional.empty(),
            new WurstProjectConfigData()
        );

        assertEquals(result.script.getCanonicalFile(), war3mapJ.getCanonicalFile());
    }

    private void newCleanFolder(File f) throws Exception {
        FileUtils.deleteRecursively(f);
        Files.createDirectories(f.toPath());
    }

    private static final class HotReloadNoMapRequest extends MapRequest {
        private HotReloadNoMapRequest(WurstLanguageServer langServer, Optional<File> map, List<String> compileArgs,
                                      WFile workspaceRoot) {
            super(langServer, map, compileArgs, workspaceRoot, Optional.empty(), Optional.empty());
        }

        @Override
        public Object execute(de.peeeq.wurstio.languageserver.ModelManager modelManager) {
            return null;
        }

        @Override
        protected File ensureCachedMap(WurstGui gui) {
            throw new AssertionError("ensureCachedMap should not be called for hotreload without a map.");
        }

        @Override
        protected File compileScript(WurstGui gui, de.peeeq.wurstio.languageserver.ModelManager modelManager,
                                     List<String> compileArgs, Optional<File> mapCopy,
                                     WurstProjectConfigData projectConfigData, boolean isProd, File scriptFile) {
            return scriptFile;
        }

        private CompilationResult compileScriptForTest(de.peeeq.wurstio.languageserver.ModelManager modelManager,
                                                       WurstGui gui,
                                                       Optional<File> testMap,
                                                       WurstProjectConfigData projectConfigData) throws Exception {
            return compileScript(modelManager, gui, testMap, projectConfigData, getBuildDir(), false);
        }
    }

    private static final class TestMapRequest extends MapRequest {
        private final Map<File, byte[]> scriptByMap;
        private File lastExtractedMap;

        private TestMapRequest(WurstLanguageServer langServer, Optional<File> map, List<String> compileArgs, WFile workspaceRoot,
                               Map<File, byte[]> scriptByMap) {
            super(langServer, map, compileArgs, workspaceRoot, Optional.empty(), Optional.empty());
            this.scriptByMap = scriptByMap;
        }

        @Override
        public Object execute(de.peeeq.wurstio.languageserver.ModelManager modelManager) {
            return null;
        }

        @Override
        protected byte[] extractMapScript(Optional<File> mapCopy) {
            if (mapCopy.isEmpty()) {
                return null;
            }
            lastExtractedMap = mapCopy.get();
            return scriptByMap.get(mapCopy.get());
        }

        private File getLastExtractedMap() {
            return lastExtractedMap;
        }

        private boolean isHotStartmapForTest() {
            return runArgs.isHotStartmap();
        }

        private Optional<File> getMapForScriptExtractionForTest(Optional<File> mapCopy) {
            return getMapForScriptExtraction(mapCopy);
        }

        private File loadMapScriptForTest(Optional<File> mapCopy, ModelManagerImpl modelManager, WurstGui gui) throws Exception {
            return loadMapScript(mapCopy, modelManager, gui);
        }

        private File renameJhcrOutputForTest(File buildDir) throws Exception {
            return renameJhcrOutput(buildDir);
        }

        private File getCachedMapFileForTest() {
            return getCachedMapFile();
        }

        private File ensureCachedMapForTest(WurstGui gui) throws Exception {
            return ensureCachedMap(gui);
        }
    }
}
