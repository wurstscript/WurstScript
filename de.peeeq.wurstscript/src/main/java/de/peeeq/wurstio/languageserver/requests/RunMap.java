package de.peeeq.wurstio.languageserver.requests;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.W3Utils;
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
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by peter on 16.05.16.
 */
public class RunMap extends UserRequest<Object> {

    private final String wc3Path;
    private final File map;
    private final List<String> compileArgs;
    private final WFile workspaceRoot;
    /** makes the compilation slower, but more safe by discarding results from the editor and working on a copy of the model */
    private SafetyLevel safeCompilation = SafetyLevel.QuickAndDirty;
    /** The patch version as double, e.g. 1.27, 1.28 */
    private double patchVersion;

    enum SafetyLevel {
        QuickAndDirty, KindOfSafe
    }

    public RunMap(WFile workspaceRoot, String wc3Path, File map, List<String> compileArgs) {
        this.workspaceRoot = workspaceRoot;
        this.wc3Path = wc3Path;
        this.map = map;
        this.compileArgs = compileArgs;
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

    @Override
    public Object execute(ModelManager modelManager) {

        // TODO use normal compiler for this, avoid code duplication
        WLogger.info("runMap " + map.getAbsolutePath() + " " + compileArgs);
        WurstGui gui = new WurstGuiImpl(workspaceRoot.getFile().getAbsolutePath());
        try {
            File gameExe = findGameExecutable();

            if (!map.exists()) {
                throw new RequestFailedException(MessageType.Error, map.getAbsolutePath() + " does not exist.");
            }

            gui.sendProgress("Copying map");

            // first we copy in same location to ensure validity
            File buildDir = getBuildDir();
            File testMap = new File(buildDir, "WurstRunMap.w3x");
            if (testMap.exists()) {
                boolean deleteOk = testMap.delete();
                if (!deleteOk) {
                    throw new RequestFailedException(MessageType.Error, "Could not delete old mapfile: " + testMap);
                }
            }
            Files.copy(map, testMap);

            // first compile the script:
            File compiledScript = compileScript(gui, modelManager, compileArgs, testMap, map);

            WurstModel model = modelManager.getModel();
            if (model == null || model.stream().noneMatch((CompilationUnit cu) -> cu.getFile().endsWith("war3map.j"))) {
                println("No 'war3map.j' file could be found inside the map nor inside the wurst folder");
                println("If you compile the map with WurstPack once, this file should be in your wurst-folder. ");
                println("We will try to start the map now, but it will probably fail. ");
            }

            gui.sendProgress("preparing testmap ... ");

            // then inject the script into the map
            gui.sendProgress("Injecting mapscript");
            try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(testMap)) {
                mpqEditor.deleteFile("war3map.j");
                mpqEditor.insertFile("war3map.j", Files.toByteArray(compiledScript));
            }


            String testMapName2 = copyToWarcraftMapDir(testMap);

            WLogger.info("Starting wc3 ... ");

            // now start the map
            List<String> cmd = Lists.newArrayList(gameExe.getAbsolutePath(), "-window", "-loadfile", "Maps\\Test\\" + testMapName2);

            if (!System.getProperty("os.name").startsWith("Windows")) {
                // run with wine
                cmd.add(0, "wine");
            }

            gui.sendProgress("running " + cmd);
            Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
        } catch (CompileError e) {
            throw new RequestFailedException(MessageType.Error, "There was an error when compiling the map: " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            gui.sendFinished();
        }
        return "ok"; // TODO
    }

    /**
     * Returns the executable for Warcraft III for starting maps
     * since it changed with 1.28.3
     */
    private File findGameExecutable() {
        return Stream.of("Frozen Throne.exe", "Warcraft III.exe")
                .map(exe -> new File(wc3Path, exe))
                .filter(File::exists)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No warcraft executatble found in path '" + wc3Path + "'. \n" +
                        "Please check your configuration."));
    }

    /**
     * Copies the map to the wc3 map directory
     * <p>
     * This directory depends on warcraft version and whether we are on windows or wine is used.
     */
    private String copyToWarcraftMapDir(File testMap) throws IOException {
        String documentPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + File.separator + "Warcraft III";
        if (!new File(documentPath).exists()) {
            WLogger.info("Warcraft folder " + documentPath + " does not exist.");
            // Try wine default:
            documentPath = System.getProperty("user.home")
                    + "/.wine/drive_c/users/" + System.getProperty("user.name") + "/My Documents/Warcraft III";
            if (!new File(documentPath).exists()) {
                WLogger.severe("Wine Warcraft folder " + documentPath + " does not exist.");
            }
        }

        patchVersion = W3Utils.parsePatchVersion(new File(wc3Path));

        String testMapName = "WurstTestMap.w3x";
        if (patchVersion <= 1.27) {
            // 1.27 and lower compat
            print("Version 1.27 or lower detected, changing file location");
            documentPath = wc3Path;
        } else {
            // For 1.28+ the wc3/maps/test folder must not contain a map of the same name
            File oldFile = new File(wc3Path, "Test" + File.separator + testMapName);
            if (oldFile.exists()) {
                if (!oldFile.delete()) {
                    WLogger.severe("Cannot delete old Wurst Test Map");
                }
            }
        }

        // copy the map to the appropriate directory
        File testFolder = new File(documentPath, "Maps" + File.separator + "Test");
        if (testFolder.mkdirs() || testFolder.exists()) {
            File testMap2 = new File(testFolder, testMapName);
            Files.copy(testMap, testMap2);
        } else {
            WLogger.severe("Could not create Test folder");
        }
        return testMapName;
    }



    private void print(String s) {
        WLogger.info(s);
    }

    private void println(String s) {
        WLogger.info(s);
    }

    private File compileScript(WurstGui gui, ModelManager modelManager, List<String> compileArgs, File mapCopy, File origMap) throws Exception {
        RunArgs runArgs = new RunArgs(compileArgs);
        print("Compile Script : ");
        for (File dep : modelManager.getDependencyWurstFiles()) {
            WLogger.info("dep: " + dep.getPath());
        }
        print("Dependencies done.");
        processMapScript(runArgs, gui, modelManager, mapCopy);
        print("Processed mapscript");
        if (safeCompilation != SafetyLevel.QuickAndDirty) {
            // it is safer to rebuild the project, instead of taking the current editor state
            gui.sendProgress("Cleaning project");
            modelManager.clean();
            gui.sendProgress("Building project");
            modelManager.buildProject();
        }

        if (modelManager.hasErrors()) {
            throw new RuntimeException("Model has errors");
        }

        WurstModel model = modelManager.getModel();
        if (safeCompilation != SafetyLevel.QuickAndDirty) {
            // compilation will alter the model (e.g. remove unused imports), 
            // so it is safer to create a copy
            model = model.copy();
        }

        MpqEditor mpqEditor = null;
        if (mapCopy != null) {
            mpqEditor = MpqEditorFactory.getEditor(mapCopy);
        }

        //WurstGui gui = new WurstGuiLogger();

        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, mpqEditor, runArgs);
        compiler.setMapFile(mapCopy);
        purgeUnimportedFiles(model);

        gui.sendProgress("Check program");
        compiler.checkProg(model);

        if (gui.getErrorCount() > 0) {
            print("Could not compile project\n");
            System.err.println("Could not compile project: " + gui.getErrorList().get(0));
            throw new RuntimeException("Could not compile project: " + gui.getErrorList().get(0));
        }

        print("translating program ... ");
        compiler.translateProgToIm(model);

        if (gui.getErrorCount() > 0) {
            print("Could not compile project (error in translation)\n");
            System.err.println("Could not compile project (error in translation): " + gui.getErrorList().get(0));
            throw new RuntimeException("Could not compile project (error in translation): " + gui.getErrorList().get(0));
        }

        if (runArgs.runCompiletimeFunctions()) {
            print("running compiletime functions ... ");
            // compile & inject object-editor data
            // TODO run optimizations later?
            gui.sendProgress("Running compiletime functions");
            CompiletimeFunctionRunner ctr = new CompiletimeFunctionRunner(compiler.getImProg(), compiler.getMapFile(), compiler.getMapfileMpqEditor(), gui,
                    FunctionFlagEnum.IS_COMPILETIME);
            ctr.setInjectObjects(runArgs.isInjectObjects());
            ctr.setOutputStream(new PrintStream(System.err));
            ctr.run();
        }

        if (runArgs.isInjectObjects()) {
            Preconditions.checkNotNull(mpqEditor);
            // add the imports
            ImportFile.importFilesFromImportDirectory(origMap, mpqEditor);
        }

        print("translating program to jass ... ");
        compiler.transformProgToJass();

        JassProg jassProg = compiler.getProg();
        if (jassProg == null) {
            print("Could not compile project\n");
            throw new RuntimeException("Could not compile project (error in JASS translation)");
        }

        gui.sendProgress("Printing program");
        JassPrinter printer = new JassPrinter(! runArgs.isOptimize(), jassProg);
        String compiledMapScript = printer.printProg();

        File buildDir = getBuildDir();
        File outFile = new File(buildDir, "compiled.j.txt");
        Files.write(compiledMapScript.getBytes(Charsets.UTF_8), outFile);
        if (mpqEditor != null) {
            mpqEditor.close();
        }
        return outFile;
    }

    private void processMapScript(RunArgs runArgs, WurstGui gui, ModelManager modelManager, File mapCopy) throws Exception {
        File existingScript = new File(new File(workspaceRoot.getFile(), "wurst"), "war3map.j");
        // If runargs are no extract, either use existing or throw error
        // Otherwise try loading from map, if map was saved with wurst, try existing script, otherwise error
        if (runArgs.isNoExtractMapScript()) {
            WLogger.info("flag -isNoExtractMapScript set");
            if (existingScript.exists()) {
                modelManager.syncCompilationUnit(WFile.create(existingScript));
                return;
            } else {
                throw new CompileError(new WPos(mapCopy.toString(), new LineOffsets(), 0, 0),
                        "RunArg noExtractMapScript is set but no mapscript is provided inside the wurst folder");
            }
        }
        WLogger.info("extracting mapscript");
        byte[] extractedScript;
        try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(mapCopy)) {
            extractedScript = mpqEditor.extractFile("war3map.j");
        }
        if (new String(extractedScript, StandardCharsets.UTF_8).startsWith(JassPrinter.WURST_COMMENT_RAW)) {
            WLogger.info("map has already been compiled with wurst");
            // file generated by wurst, do not use
            if (existingScript.exists()) {
                WLogger.info(
                        "Cannot use war3map.j from map file, because it already was compiled with wurst. " + "Using war3map.j from Wurst directory instead.");
            } else {
                CompileError err = new CompileError(new WPos(mapCopy.toString(), new LineOffsets(), 0, 0),
                        "Cannot use war3map.j from map file, because it already was compiled with wurst. " + "Please add war3map.j to the wurst directory.");
                gui.showInfoMessage(err.getMessage());
                WLogger.severe(err);
            }
        } else {
            WLogger.info("new map, use extracted");
            // write mapfile from map to workspace
            Files.write(extractedScript, existingScript);
        }

        // push war3map.j to modelmanager

        modelManager.syncCompilationUnit(WFile.create(existingScript));
    }

    private File getBuildDir() {
        File buildDir = new File(workspaceRoot.getFile(), "_build");
        buildDir.mkdirs();
        return buildDir;
    }

    /**
     * removes everything compilation unit which is neither
     * - inside a wurst folder
     * - a jass file
     * - imported by a file in a wurst folder
     */
    private void purgeUnimportedFiles(WurstModel model) {
        Set<CompilationUnit> inWurstFolder =
                model.stream()
                        .filter(cu -> isInWurstFolder(cu.getFile()) || cu.getFile().endsWith(".j"))
                        .collect(Collectors.toSet());

        Set<CompilationUnit> imported = new HashSet<>(inWurstFolder);
        addImports(imported, imported);

        model.removeIf(cu -> !imported.contains(cu));
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

    private boolean isInWurstFolder(String file) {
        Path p = Paths.get(file);
        Path w = workspaceRoot.getPath();
        return p.startsWith(w)
                && java.nio.file.Files.exists(p)
                && Utils.isWurstFile(file);
    }
}
