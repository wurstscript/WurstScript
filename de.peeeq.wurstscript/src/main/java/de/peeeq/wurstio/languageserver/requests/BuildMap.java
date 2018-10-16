package de.peeeq.wurstio.languageserver.requests;

import com.google.common.io.Files;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import file.WurstProjectConfig;
import file.WurstProjectConfigData;
import net.moonlightflower.wc3libs.bin.app.MapHeader;
import net.moonlightflower.wc3libs.bin.app.W3I;
import org.eclipse.lsp4j.MessageType;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by peter on 16.05.16.
 */
public class BuildMap extends MapRequest {
    private static final String FILE_NAME = "wurst.build";

    /**
     * makes the compilation slower, but more safe by discarding results from the editor and working on a copy of the model
     */
    private SafetyLevel safeCompilation = SafetyLevel.KindOfSafe;

    enum SafetyLevel {
        QuickAndDirty, KindOfSafe
    }

    public BuildMap(WFile workspaceRoot, File map, List<String> compileArgs) {
        super(map, compileArgs, workspaceRoot);
    }


    @Override
    public Object execute(ModelManager modelManager) throws IOException {
        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before building a release.");
        }

        WurstProjectConfigData projectConfig = WurstProjectConfig.INSTANCE.loadProject(workspaceRoot.getFile().toPath().resolve(FILE_NAME));
        if(projectConfig == null) {
            throw new RequestFailedException(MessageType.Error, FILE_NAME + " file doesn't exist or is invalid. " +
                    "Please refresh your project using an up to date wurst setup tool.");
        }

        // TODO use normal compiler for this, avoid code duplication
        WLogger.info("buildMap " + map.getAbsolutePath() + " " + compileArgs);
        WurstGui gui = new WurstGuiImpl(workspaceRoot.getFile().getAbsolutePath());
        try {
            if (!map.exists()) {
                throw new RequestFailedException(MessageType.Error, map.getAbsolutePath() + " does not exist.");
            }

            gui.sendProgress("Copying map");

            // first we copy in same location to ensure validity
            File buildDir = getBuildDir();
            File targetMap = new File(buildDir, projectConfig.getBuildMapData().getFileName() + ".w3x");
            if (targetMap.exists()) {
                boolean deleteOk = targetMap.delete();
                if (!deleteOk) {
                    throw new RequestFailedException(MessageType.Error, "Could not delete old mapfile: " + targetMap);
                }
            }
            Files.copy(map, targetMap);

            // first compile the script:
            File compiledScript = compileScript(gui, modelManager, compileArgs, targetMap, map);

            WurstModel model = modelManager.getModel();
            if (model == null || model.stream().noneMatch((CompilationUnit cu) -> cu.getFile().endsWith("war3map.j"))) {
                println("No 'war3map.j' file could be found inside the map nor inside the wurst folder");
                println("If you compile the map with WurstPack once, this file should be in your wurst-folder. ");
                println("We will try to start the map now, but it will probably fail. ");
            }


            // then inject the script into the map
            gui.sendProgress("Injecting mapscript");
            try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(targetMap)) {
                mpqEditor.deleteFile("war3map.j");
                mpqEditor.insertFile("war3map.j", compiledScript);
            }

            gui.sendProgress("Applying Map Config...");
            applyProjectConfig(projectConfig, targetMap);
            gui.sendProgress("Done.");
        } catch (CompileError e) {
            WLogger.info(e);
            throw new RequestFailedException(MessageType.Error, "There was an error when compiling the map:\n" + e);
        } catch (RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            gui.sendFinished();
        }
        return "ok"; // TODO
    }

    private void applyProjectConfig(WurstProjectConfigData projectConfig, File targetMap) throws IOException {
        if (projectConfig.getBuildMapData().getFileName().isEmpty()) {
            throw new RequestFailedException(MessageType.Error, "wurst.build is missing mapFileName");
        }

        if (!projectConfig.getBuildMapData().getName().isEmpty()) {
            try (MpqEditor mpq = MpqEditorFactory.getEditor((targetMap))) {
                W3I w3I = new W3I(mpq.extractFile("war3map.w3i"));
                w3I.setMapName(projectConfig.getBuildMapData().getName());
                w3I.setMapAuthor(projectConfig.getBuildMapData().getAuthor());
                File w3iFile = new File("w3iFile");
                w3I.write(w3iFile);

                mpq.deleteFile("war3map.w3i");
                mpq.insertFile("war3map.w3i", java.nio.file.Files.readAllBytes(w3iFile.toPath()));
                w3iFile.delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        MapHeader mapHeader = MapHeader.ofFile(targetMap);
        mapHeader.setMaxPlayersCount(projectConfig.getBuildMapData().getPlayerCount());
        mapHeader.setMapName(projectConfig.getBuildMapData().getFileName());
        mapHeader.writeToMapFile(targetMap);
    }

    private File compileScript(WurstGui gui, ModelManager modelManager, List<String> compileArgs, File mapCopy, File origMap) throws Exception {
        gui.sendProgress("Compiling mapscript");
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
            for (CompileError compileError : modelManager.getParseErrors()) {
                gui.sendError(compileError);
            }
            throw new RequestFailedException(MessageType.Warning, "Cannot run code with syntax errors.");
        }

        WurstModel model = modelManager.getModel();
        if (safeCompilation != SafetyLevel.QuickAndDirty) {
            // compilation will alter the model (e.g. remove unused imports), 
            // so it is safer to create a copy
            model = ModelManager.copy(model);
        }

        return compileMap(modelManager.getProjectPath(), gui, mapCopy, origMap, runArgs, model);
    }

}
