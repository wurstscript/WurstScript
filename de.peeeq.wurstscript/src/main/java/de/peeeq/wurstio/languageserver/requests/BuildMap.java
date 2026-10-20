package de.peeeq.wurstio.languageserver.requests;

import java.util.concurrent.CancellationException;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.wurstscript.projectconfig.WurstProjectConfigReader;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.eclipse.lsp4j.MessageType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;

/**
 * Created by peter on 16.05.16.
 */
public class BuildMap extends MapRequest {

    private final boolean exportObjects;

    public BuildMap(WurstLanguageServer languageServer, WFile workspaceRoot, Optional<String> wc3Path, Optional<File> map,
                    List<String> compileArgs) {
        this(languageServer, workspaceRoot, wc3Path, map, compileArgs, false);
    }

    public BuildMap(WurstLanguageServer languageServer, WFile workspaceRoot, Optional<String> wc3Path, Optional<File> map,
                    List<String> compileArgs, boolean exportObjects) {
        super(languageServer, map, exportObjects ? objectExportArgs(compileArgs) : compileArgs, workspaceRoot, wc3Path, Optional.empty());
        this.exportObjects = exportObjects;
    }

    private static List<String> objectExportArgs(List<String> compileArgs) {
        ArrayList<String> args = new ArrayList<>(compileArgs);
        args.removeAll(List.of("-hotreload", "-hotstart", "-injectobjects"));
        args.add("-runcompiletimefunctions");
        args.add("-exportw3o");
        return args;
    }

    @Override
    protected boolean isObjectExportOnly() {
        return exportObjects;
    }

    @Override
    public Object execute(ModelManager modelManager) throws IOException {
        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before "
                + (exportObjects ? "exporting objects.\n" : "building a release.\n") + modelManager.getFirstErrorDescription());
        }

        WurstProjectConfigData projectConfig = WurstProjectConfigReader.load(workspaceRoot.getFile().toPath().resolve(FILE_NAME));
        if (projectConfig == null) {
            throw new RequestFailedException(MessageType.Error, FILE_NAME + " file doesn't exist or is invalid. " +
                "Please install your project using grill or the wurst setup tool.");
        }

        WLogger.info("buildMap " + map + " " + compileArgs);
        WurstGui gui = createGui(modelManager, exportObjects ? "Exporting objects for World Editor" : "Building Wurst map");
        try {
            if (exportObjects) {
                CompilationResult result = compileScript(modelManager, gui, map, projectConfig, getBuildDir(), false);
                CompilationCancellation.check();
                File exported = new File(getBuildDir(), "wurstCreatedObjects.w3o");
                Files.copy(result.script.toPath(), exported.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return exported.getAbsolutePath();
            }
            executeBuildMapPipeline(modelManager, gui, projectConfig);
        } catch (CompileError e) {
            WLogger.info(e);
            gui.sendError(e);
            throw new RequestFailedException(MessageType.Error, "Map build failed. See the Problems panel for details.");
        } catch (CancellationException e) {
            throw e;
        } catch (RequestFailedException e) {
            throw e;
        } catch (Exception e) {
            WLogger.warning("Exception occurred", e);
            throw new RequestFailedException(MessageType.Error, gui.getErrorCount() > 0
                ? "Map build failed. See the Problems panel for details."
                : "Map build failed: " + e.getMessage());
        } finally {
            gui.sendFinished();
        }
        return "ok"; // TODO
    }
}
