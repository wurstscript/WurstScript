package de.peeeq.wurstio.languageserver.requests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import config.WurstProjectConfigData;
import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ProjectConfigBuilder;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.W3InstallationData;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;
import net.moonlightflower.wc3libs.bin.app.W3I;
import net.moonlightflower.wc3libs.port.GameVersion;
import net.moonlightflower.wc3libs.port.Orient;
import org.apache.commons.lang.StringUtils;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class MapRequest extends UserRequest<Object> {
    protected final WurstLanguageServer langServer;
    protected final Optional<File> map;
    protected final List<String> compileArgs;
    protected final WFile workspaceRoot;
    protected final RunArgs runArgs;
    protected final Optional<String> wc3Path;
    protected final W3InstallationData w3data;
    protected final TimeTaker timeTaker;

    public static long mapLastModified = 0L;
    public static String mapPath = "";

    private static Long lastMapModified = 0L;
    private static String lastMapPath = "";

    public static final String BUILD_CONFIGURED_SCRIPT_NAME = "01_war3mapj_with_config.j.txt";
    public static final String BUILD_COMPILED_JASS_NAME = "02_compiled.j.txt";
    public static final String BUILD_COMPILED_LUA_NAME = "02_compiled.lua";
    public static final String BUILD_JHCR_SCRIPT_NAME = "03_jhcr_war3map.j";

    /**
     * makes the compilation slower, but more safe by discarding results from the editor and working on a copy of the model
     */
    protected SafetyLevel safeCompilation = SafetyLevel.KindOfSafe;

    public TimeTaker getTimeTaker() {
        return timeTaker;
    }


    enum SafetyLevel {
        QuickAndDirty, KindOfSafe
    }

    public static class CompilationResult {
        public File script;
        public File w3i;
    }

    public MapRequest(WurstLanguageServer langServer, Optional<File> map, List<String> compileArgs, WFile workspaceRoot,
                      Optional<String> wc3Path, Optional<String> gameExePath) {
        this.langServer = langServer;
        this.map = map;
        this.compileArgs = compileArgs;
        this.workspaceRoot = workspaceRoot;
        this.runArgs = new RunArgs(compileArgs);
        this.wc3Path = wc3Path;
        if (gameExePath.isPresent() && StringUtils.isNotBlank(gameExePath.get())) {
            this.w3data = new W3InstallationData(Optional.of(new File(gameExePath.get())), Optional.of(GameVersion.VERSION_1_29));
        } else {
            this.w3data = getBestW3InstallationData();
        }
        if (runArgs.isMeasureTimes()) {
            this.timeTaker = new TimeTaker.Recording();
        } else {
            this.timeTaker = new TimeTaker.Default();
        }
    }

    @Override
    public void handleException(LanguageClient languageClient, Throwable err, CompletableFuture<Object> resFut) {
        if (err instanceof RequestFailedException) {
            RequestFailedException rfe = (RequestFailedException) err;
            languageClient.showMessage(new MessageParams(rfe.getMessageType(), rfe.getMessage()));
            resFut.complete(new Object());
        } else {
            super.handleException(languageClient, err, resFut);
        }
    }

    protected File compileMap(File projectFolder, WurstGui gui, Optional<File> mapCopy, RunArgs runArgs, WurstModel model,
                              WurstProjectConfigData projectConfigData, boolean isProd) {
        try (@Nullable MpqEditor mpqEditor = MpqEditorFactory.getEditor(mapCopy)) {
            if (mpqEditor != null && !mpqEditor.canWrite()) {
                WLogger.severe("The supplied map is invalid/corrupted/protected and Wurst cannot write to it.\n" +
                    "Please supply a valid .w3x input map that can be opened in the world editor.");
                throw new NonWritableChannelException();
            }
            WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(timeTaker, projectFolder, gui, mpqEditor, runArgs);
            compiler.setMapFile(mapCopy);
            purgeUnimportedFiles(model);

            gui.sendProgress("Check program");
            compiler.checkProg(model);

            if (gui.getErrorCount() > 0) {
                throw new RequestFailedException(MessageType.Warning, "Could not compile project: ", gui.getErrorList().get(0));
            }

            print("translating program ... ");
            compiler.translateProgToIm(model);

            if (gui.getErrorCount() > 0) {
                throw new RequestFailedException(MessageType.Error, "Could not compile project (error in translation): " + gui.getErrorList().get(0));
            }

            timeTaker.measure("Runinng Compiletime Functions", () -> compiler.runCompiletime(projectConfigData, isProd, runArgs.isCompiletimeCache()));

            if (runArgs.isLua()) {
                print("Translating program to Lua ... ");
                Optional<LuaCompilationUnit> luaCode = Optional.ofNullable(compiler.transformProgToLua());

                if (!luaCode.isPresent()) {
                    print("Could not compile project\n");
                    throw new RuntimeException("Could not compile project (error in LUA translation)");
                }

                StringBuilder sb = new StringBuilder();
                luaCode.get().print(sb, 0);

                String compiledMapScript = sb.toString();
                File buildDir = getBuildDir();
                File outFile = new File(buildDir, BUILD_COMPILED_LUA_NAME);
                Files.write(compiledMapScript.getBytes(Charsets.UTF_8), outFile);
                return outFile;

            } else {
                print("Translating program to jass ... ");
                compiler.transformProgToJass();

                Optional<JassProg> jassProg = Optional.ofNullable(compiler.getProg());
                if (!jassProg.isPresent()) {
                    print("Could not compile project\n");
                    throw new RuntimeException("Could not compile project (error in JASS translation)");
                }

                gui.sendProgress("Printing program");
                JassPrinter printer = new JassPrinter(!runArgs.isOptimize(), jassProg.get());
                String compiledMapScript = printer.printProg();
                File buildDir = getBuildDir();
                File outFile = new File(buildDir, BUILD_COMPILED_JASS_NAME);
                Files.write(compiledMapScript.getBytes(Charsets.UTF_8), outFile);

                if (!runArgs.isDisablePjass()) {
                    gui.sendProgress("Running PJass");
                    timeTaker.beginPhase("Pjass execution");
                    Pjass.Result pJassResult = Pjass.runPjass(outFile,
                        new File(buildDir, "common.j").getAbsolutePath(),
                        new File(buildDir, "blizzard.j").getAbsolutePath());
                    WLogger.info(pJassResult.getMessage());
                    if (!pJassResult.isOk()) {
                        for (CompileError err : pJassResult.getErrors()) {
                            gui.sendError(err);
                        }
                        throw new RuntimeException("Could not compile project (PJass error)");
                    }
                    timeTaker.endPhase();
                }

                if (runArgs.isHotStartmap()) {
                    gui.sendProgress("Running JHCR");
                    return runJassHotCodeReload(outFile);
                }
                return outFile;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected File runJassHotCodeReload(File mapScript) throws IOException, InterruptedException {
        File buildDir = getBuildDir();
        File commonJ = new File(buildDir, "common.j");
        File blizzardJ = new File(buildDir, "blizzard.j");

        if (!commonJ.exists()) {
            throw new IOException("Could not find file " + commonJ.getAbsolutePath());
        }

        if (!blizzardJ.exists()) {
            throw new IOException("Could not find file " + blizzardJ.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder(langServer.getConfigProvider().getJhcrExe(), "init", commonJ.getName(), blizzardJ.getName(), mapScript.getName());
        pb.directory(buildDir);
        Utils.exec(pb, Duration.ofSeconds(30), System.err::println);
        return renameJhcrOutput(buildDir);
    }

    /**
     * removes everything compilation unit which is neither
     * - inside a wurst folder
     * - a jass file
     * - imported by a file in a wurst folder
     */
    private void purgeUnimportedFiles(WurstModel model) {

        Set<CompilationUnit> imported = model.stream()
            .filter(cu -> isInWurstFolder(cu.getCuInfo().getFile()) || cu.getCuInfo().getFile().endsWith(".j")).collect(Collectors.toSet());
        addImports(imported, imported);

        model.removeIf(cu -> !imported.contains(cu));
    }

    private boolean isInWurstFolder(String file) {
        Path p = Paths.get(file);
        Path w;
        try {
            w = workspaceRoot.getPath();
        } catch (FileNotFoundException e) {
            return false;
        }
        return p.startsWith(w)
            && java.nio.file.Files.exists(p)
            && Utils.isWurstFile(file);
    }

    protected File getBuildDir() {
        File buildDir;
        try {
            buildDir = new File(workspaceRoot.getFile(), "_build");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot get build dir", e);
        }
        if (!buildDir.exists()) {
            UtilsIO.mkdirs(buildDir);
        }
        return buildDir;
    }

    private void addImports(Set<CompilationUnit> result, Set<CompilationUnit> toAdd) {
        Set<CompilationUnit> imported =
            toAdd.stream()
                .flatMap((CompilationUnit cu) -> cu.getPackages().stream())
                .flatMap((WPackage p) -> p.getImports().stream())
                .map(WImport::attrImportedPackage)
                .filter(Objects::nonNull)
                .map(WPackage::attrCompilationUnit)
                .collect(Collectors.toSet());
        boolean changed = result.addAll(imported);
        if (changed) {
            // recursive call terminates, as there are only finitely many compilation units
            addImports(result, imported);
        }
    }

    protected void print(String s) {
        WLogger.info(s);
    }

    protected void println(String s) {
        WLogger.info(s);
    }


    protected File compileScript(WurstGui gui, ModelManager modelManager, List<String> compileArgs, Optional<File> mapCopy,
                                 WurstProjectConfigData projectConfigData, boolean isProd, File scriptFile) throws Exception {
        RunArgs runArgs = new RunArgs(compileArgs);

        gui.sendProgress("Compiling Script");
        print("Compile Script : ");
        for (File dep : modelManager.getDependencyWurstFiles()) {
            WLogger.info("dep: " + dep.getPath());
        }
        print("Dependencies done.");
        if (safeCompilation != RunMap.SafetyLevel.QuickAndDirty) {
            // it is safer to rebuild the project, instead of taking the current editor state
            gui.sendProgress("Cleaning project");
            modelManager.clean();
            gui.sendProgress("Building project");
            modelManager.buildProject();
        }

        replaceBaseScriptWithConfig(modelManager, scriptFile);

        if (modelManager.hasErrors()) {
            for (CompileError compileError : modelManager.getParseErrors()) {
                gui.sendError(compileError);
            }
            throw new RequestFailedException(MessageType.Warning, "Cannot run code with syntax errors.");
        }

        WurstModel model = modelManager.getModel();
        if (safeCompilation != RunMap.SafetyLevel.QuickAndDirty) {
            // compilation will alter the model (e.g. remove unused imports),
            // so it is safer to create a copy
            model = ModelManager.copy(model);
        }

        return compileMap(modelManager.getProjectPath(), gui, mapCopy, runArgs, model, projectConfigData, isProd);
    }

    private static void replaceBaseScriptWithConfig(ModelManager modelManager, File scriptFile) throws IOException {
        Optional<CompilationUnit> war3mapJ = modelManager.getModel()
            .stream()
            .filter((CompilationUnit cu) -> cu.getCuInfo().getFile().endsWith("war3map.j"))
            .findFirst();

        if (war3mapJ.isPresent()) {
            modelManager.syncCompilationUnitContent(WFile.create(war3mapJ.get().getCuInfo().getFile()),
                java.nio.file.Files.readString(scriptFile.toPath()));
        }
    }

    /**
     * Checks if we can use the cached map
     */
    protected boolean canUseCachedMap(File cachedMap) {
        if (!cachedMap.exists()) {
            WLogger.info("No cached map found");
            return false;
        }

        // Check if source map is newer
        if (map.isPresent() && map.get().lastModified() > cachedMap.lastModified()) {
            WLogger.info("Source map is newer than cache");
            return false;
        }

        WLogger.info("Using cached map from previous build");
        return true;
    }


    /**
     * Gets or creates the cached map file location
     */
    protected File getCachedMapFile() {
        File buildDir = getBuildDir();
        File cacheDir = new File(buildDir, "cache");
        if (!cacheDir.exists()) {
            UtilsIO.mkdirs(cacheDir);
        }
        return new File(cacheDir, "cached_map.w3x");
    }

    /**
     * Ensures cached map exists and is up to date
     */
    protected File ensureCachedMap(WurstGui gui) throws IOException {
        File cachedMap = getCachedMapFile();

        if (!map.isPresent()) {
            throw new RequestFailedException(MessageType.Error, "No source map provided");
        }

        File sourceMap = map.get();

        // If cached map doesn't exist or source is newer, update cache
        if (!cachedMap.exists() || sourceMap.lastModified() > cachedMap.lastModified()) {
            WLogger.info("Updating cached map from source");
            gui.sendProgress("Updating cached map");
            Files.copy(sourceMap, cachedMap);
        }

        return cachedMap;
    }

    protected CompilationResult compileScript(ModelManager modelManager, WurstGui gui, Optional<File> testMap,
                                              WurstProjectConfigData projectConfigData, File buildDir,
                                              boolean isProd) throws Exception {

        if (!runArgs.isHotReload()) {
            // Ensure we're working with the cached map
            File cachedMap = ensureCachedMap(gui);

            // Update testMap to point to cached map
            testMap = Optional.of(cachedMap);
        }

        CompilationResult result;

        if (runArgs.isHotReload()) {
            result = new CompilationResult();
            result.script = new File(buildDir, BUILD_CONFIGURED_SCRIPT_NAME);
            if (!result.script.exists()) {
                result.script = new File(new File(workspaceRoot.getFile(), "wurst"), "war3map.j");
            }
            if (!result.script.exists()) {
                throw new RequestFailedException(MessageType.Error, "Could not find war3map.j file");
            }
        } else {
            timeTaker.beginPhase("load map script");
            File scriptFile = loadMapScript(getMapForScriptExtraction(testMap), modelManager, gui);
            timeTaker.endPhase();
            result = applyProjectConfig(gui, testMap, buildDir, projectConfigData, scriptFile, BUILD_CONFIGURED_SCRIPT_NAME);
        }

        // Compile the script
        result.script = compileScript(gui, modelManager, compileArgs, testMap, projectConfigData, isProd, result.script);

        Optional<WurstModel> model = Optional.ofNullable(modelManager.getModel());
        if (!model.isPresent()
            || model
            .get()
            .stream()
            .noneMatch((CompilationUnit cu) -> cu.getCuInfo().getFile().endsWith("war3map.j"))) {
            println("No 'war3map.j' file could be found inside the map nor inside the wurst folder");
            println("If you compile the map with WurstPack once, this file should be in your wurst-folder. ");
            println("We will try to start the map now, but it will probably fail. ");
        }
        return result;
    }

    protected void injectMapData(WurstGui gui, Optional<File> testMap, CompilationResult result) throws Exception {
        gui.sendProgress("Injecting map data");
        timeTaker.beginPhase("Injecting map data");

        // Work directly with the cached map
        File cachedMap = getCachedMapFile();

        if (!cachedMap.exists()) {
            throw new RequestFailedException(MessageType.Error, "Cached map does not exist");
        }

        try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(Optional.of(cachedMap))) {
            String mapScriptName;
            if (runArgs.isLua()) {
                mapScriptName = "war3map.lua";
                injectExternalLuaFiles(result.script);
            } else {
                mapScriptName = "war3map.j";
            }

            // Delete old scripts
            if (mpqEditor.hasFile("war3map.j")) {
                mpqEditor.deleteFile("war3map.j");
            }
            if (mpqEditor.hasFile("war3map.lua")) {
                mpqEditor.deleteFile("war3map.lua");
            }

            // Insert new script
            mpqEditor.insertFile(mapScriptName, result.script);

            // Insert w3i if it changed
            if (result.w3i != null) {
                String w3iHash = ImportFile.calculateFileHash(result.w3i);

                Optional<ImportFile.CacheManifest> manifestOpt = ImportFile.getCachedManifest(mpqEditor);
                boolean w3iChanged = true;

                if (manifestOpt.isPresent() && manifestOpt.get().w3iConfigMatches(w3iHash)) {
                    WLogger.info("W3I file unchanged, skipping injection");
                    w3iChanged = false;
                }

                if (w3iChanged) {
                    WLogger.info("W3I file changed, injecting");
                    if (mpqEditor.hasFile(W3I.GAME_PATH.getName())) {
                        mpqEditor.deleteFile(W3I.GAME_PATH.getName());
                    }
                    mpqEditor.insertFile(W3I.GAME_PATH.getName(), result.w3i);

                    // Update manifest
                    ImportFile.CacheManifest manifest = manifestOpt.orElse(new ImportFile.CacheManifest());
                    manifest.setW3iConfig(w3iHash);
                    ImportFile.saveManifest(mpqEditor, manifest);
                }
            }

            // CRITICAL: Import files into THIS mpq editor instance
            gui.sendProgress("Importing resource files");
            timeTaker.beginPhase("Importing files");
            try {
                ImportFile.ImportResult importResult = ImportFile.importFilesFromImports(
                    workspaceRoot.getFile(),
                    mpqEditor
                );
                WLogger.info("Import result: " + importResult.toString());
            } catch (Exception e) {
                WLogger.severe("Failed to import files: " + e.getMessage());
                throw e;
            }
            timeTaker.endPhase();
        }

        timeTaker.endPhase();

        WLogger.info("Cached map size after injection: " + (cachedMap.length() / 1024 / 1024) + " MB");
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    protected File loadMapScript(Optional<File> mapCopy, ModelManager modelManager, WurstGui gui) throws Exception {
        File scriptFile = new File(new File(workspaceRoot.getFile(), "wurst"), "war3map.j");
        // If runargs are no extract, either use existing or throw error
        // Otherwise try loading from map, if map was saved with wurst, try existing script, otherwise error
        if (!mapCopy.isPresent() || runArgs.isNoExtractMapScript()) {
            System.out.println("No extract map script enabled - not extracting.");
            if (scriptFile.exists()) {
                System.out.println("war3map.j exists at wurst root.");
                CompilationUnit compilationUnit = modelManager.getCompilationUnit(WFile.create(scriptFile));
                if (compilationUnit == null) {
                    modelManager.syncCompilationUnit(WFile.create(scriptFile));
                }
                return scriptFile;
            } else {
                throw new CompileError(new WPos("", new LineOffsets(), 0, 0),
                    "RunArg noExtractMapScript is set but no war3map.j is provided inside the wurst folder");
            }
        }
        if (MapRequest.mapLastModified > lastMapModified || !MapRequest.mapPath.equals(lastMapPath)) {
            lastMapModified = MapRequest.mapLastModified;
            lastMapPath = MapRequest.mapPath;
            System.out.println("Map not cached yet, extracting script");
            byte[] extractedScript = extractMapScript(mapCopy);
            if (extractedScript == null) {
                if (scriptFile.exists()) {
                    String msg = "No war3map.j in map file, using old extracted file";
                    WLogger.info(msg);
                } else {
                    CompileError err = new CompileError(new WPos(mapCopy.toString(), new LineOffsets(), 0, 0),
                        "No war3map.j found in map file.");
                    gui.showInfoMessage(err.getMessage());
                    WLogger.severe(err);
                }
            } else if (startsWith(extractedScript, JassPrinter.WURST_COMMENT_RAW.getBytes(StandardCharsets.UTF_8))) {
                WLogger.info("map has already been compiled with wurst");
                // file generated by wurst, do not use
                if (scriptFile.exists()) {
                    String msg = "Cannot use war3map.j from map file, because it already was compiled with wurst. " + "Using war3map.j from Wurst directory instead.";
                    WLogger.info(msg);
                } else {
                    CompileError err = new CompileError(new WPos(mapCopy.toString(), new LineOffsets(), 0, 0),
                        "Cannot use war3map.j from map file, because it already was compiled with wurst. " + "Please add war3map.j to the wurst directory.");
                    gui.showInfoMessage(err.getMessage());
                    WLogger.severe(err);
                }
            } else {
                WLogger.info("new map, use extracted");
                // write mapfile from map to workspace
                Files.write(extractedScript, scriptFile);
            }
        } else {
            System.out.println("Map not modified, not extracting script");
        }


        return scriptFile;
    }

    protected CompilationResult applyProjectConfig(WurstGui gui, Optional<File> testMap, File buildDir, WurstProjectConfigData projectConfig, File scriptFile,
                                                   String outputScriptName) {
        AtomicReference<CompilationResult> result = new AtomicReference<>();
        gui.sendProgress("Applying Map Config...");
        timeTaker.measure("Applying Map Config", () -> {
            try {
                result.set(ProjectConfigBuilder.apply(projectConfig, testMap.get(), scriptFile, buildDir, runArgs, w3data, outputScriptName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return result.get();
    }

    protected Optional<File> getMapForScriptExtraction(Optional<File> mapCopy) {
        if (map.isPresent()) {
            return map;
        }
        return mapCopy;
    }

    protected byte[] extractMapScript(Optional<File> mapCopy) throws Exception {
        if (!mapCopy.isPresent()) {
            return null;
        }
        try (@Nullable MpqEditor mpqEditor = MpqEditorFactory.getEditor(mapCopy, true)) {
            if (mpqEditor.hasFile("war3map.j")) {
                return mpqEditor.extractFile("war3map.j");
            }
        }
        return null;
    }

    protected File renameJhcrOutput(File buildDir) throws IOException {
        File rawJhcrScript = new File(buildDir, "jhcr_war3map.j");
        if (!rawJhcrScript.exists()) {
            throw new IOException("Could not find file " + rawJhcrScript.getAbsolutePath());
        }
        File renamedJhcrScript = new File(buildDir, BUILD_JHCR_SCRIPT_NAME);
        java.nio.file.Files.move(rawJhcrScript.toPath(), renamedJhcrScript.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return renamedJhcrScript;
    }

    private W3InstallationData getBestW3InstallationData() throws RequestFailedException {
        if (Orient.isLinuxSystem()) {
            // no Warcraft installation supported on Linux
            return new W3InstallationData(Optional.empty(), Optional.empty());
        }
        if (wc3Path.isPresent() && StringUtils.isNotBlank(wc3Path.get())) {
            W3InstallationData w3data = new W3InstallationData(langServer, new File(wc3Path.get()),
                this instanceof RunMap && !runArgs.isHotReload());
            if (w3data.getWc3PatchVersion().isEmpty()) {
                throw new RequestFailedException(MessageType.Error, "Could not find Warcraft III installation at specified path: " + wc3Path);
            }

            return w3data;
        } else {
            return new W3InstallationData(langServer, this instanceof RunMap && !runArgs.isHotReload());
        }
    }


    private void injectExternalLuaFiles(File script) {
        File luaDir;
        try {
            luaDir = new File(workspaceRoot.getFile(), "lua");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot get build dir", e);
        }
        if (luaDir.exists()) {
            File[] children = luaDir.listFiles();
            if (children != null) {
                Arrays.stream(children).forEach(child -> {
                    try {
                        byte[] bytes = java.nio.file.Files.readAllBytes(child.toPath());
                        if (child.getName().startsWith("pre_")) {
                            byte[] existingBytes = java.nio.file.Files.readAllBytes(script.toPath());
                            java.nio.file.Files.write(
                                script.toPath(),
                                bytes);
                            java.nio.file.Files.write(
                                script.toPath(),
                                existingBytes,
                                StandardOpenOption.APPEND);
                        } else {
                            java.nio.file.Files.write(
                                script.toPath(),
                                bytes,
                                StandardOpenOption.APPEND);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }


}
