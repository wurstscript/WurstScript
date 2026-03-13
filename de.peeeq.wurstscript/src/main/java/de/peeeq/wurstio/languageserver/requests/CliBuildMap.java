package de.peeeq.wurstio.languageserver.requests;

import config.WurstProjectConfig;
import config.WurstProjectConfigData;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import org.eclipse.lsp4j.MessageType;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;

/**
 * CLI entry point for build-map that reuses the same backend map pipeline as the language server.
 */
public class CliBuildMap extends MapRequest {
    private final WurstGui gui;

    public CliBuildMap(WFile workspaceRoot, Optional<File> map, List<String> compileArgs, Optional<String> wc3Path, WurstGui gui) {
        super(null, map, compileArgs, workspaceRoot, wc3Path, Optional.empty());
        this.gui = gui;
    }

    @Override
    public Object execute(ModelManager modelManager) throws IOException {
        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before building a release.\n" + modelManager.getFirstErrorDescription());
        }

        WurstProjectConfigData projectConfig = WurstProjectConfig.INSTANCE.loadProject(workspaceRoot.getFile().toPath().resolve(FILE_NAME));
        if (projectConfig == null) {
            throw new RequestFailedException(MessageType.Error, FILE_NAME + " file doesn't exist or is invalid. " +
                "Please install your project using grill or the wurst setup tool.");
        }

        WLogger.info("cli buildMap " + map + " " + compileArgs);
        try {
            File targetMapFile = executeBuildMapPipeline(modelManager, gui, projectConfig);
            return targetMapFile.getAbsolutePath();
        } catch (CompileError e) {
            WLogger.info(e);
            throw new RequestFailedException(MessageType.Error, "A compilation error occurred when building the map:\n" + e);
        } catch (Exception e) {
            WLogger.warning("Exception occurred", e);
            throw new RequestFailedException(MessageType.Error, "An exception was thrown when building the map:\n" + e);
        } finally {
            if (gui.getErrorCount() == 0) {
                gui.sendFinished();
            }
        }
    }

    @Override
    protected File ensureWritableBuildOutput(File targetFile, boolean isFinalWrite) {
        if (!targetFile.exists()) {
            return targetFile;
        }
        try (FileChannel ignored = FileChannel.open(targetFile.toPath(), StandardOpenOption.WRITE)) {
            return targetFile;
        } catch (IOException e) {
            throw new RequestFailedException(MessageType.Warning,
                "The output map file is in use and cannot be replaced: " + targetFile.getAbsolutePath());
        }
    }
}
