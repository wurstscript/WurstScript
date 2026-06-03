package de.peeeq.wurstio.languageserver.requests;

import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.wurstscript.projectconfig.WurstProjectConfigReader;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import org.eclipse.lsp4j.MessageType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;

/**
 * Created by peter on 16.05.16.
 */
public class BuildMap extends MapRequest {

    public BuildMap(WurstLanguageServer languageServer, WFile workspaceRoot, Optional<String> wc3Path, Optional<File> map,
                    List<String> compileArgs) {
        super(languageServer, map, compileArgs, workspaceRoot, wc3Path, Optional.empty());
    }

    @Override
    public Object execute(ModelManager modelManager) throws IOException {
        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before building a release.\n" + modelManager.getFirstErrorDescription());
        }

        WurstProjectConfigData projectConfig = WurstProjectConfigReader.load(workspaceRoot.getFile().toPath().resolve(FILE_NAME));
        if (projectConfig == null) {
            throw new RequestFailedException(MessageType.Error, FILE_NAME + " file doesn't exist or is invalid. " +
                "Please install your project using grill or the wurst setup tool.");
        }

        WLogger.info("buildMap " + map + " " + compileArgs);
        WurstGui gui = new WurstGuiImpl(workspaceRoot.getFile().getAbsolutePath());
        try {
            executeBuildMapPipeline(modelManager, gui, projectConfig);
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
        return "ok"; // TODO
    }
}
