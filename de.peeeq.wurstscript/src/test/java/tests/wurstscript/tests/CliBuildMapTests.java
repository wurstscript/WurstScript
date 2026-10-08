package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.CliBuildMap;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CliBuildMapTests {

    /**
     * A command line build is a single run which has just built the model itself, so there is no
     * editor state to protect and a copy of the model would only be checked and translated again
     * from scratch: the copy has none of the attributes the model has already computed.
     */
    @Test
    public void theCliCompilesTheModelItBuiltAndNotACopy() throws Exception {
        File projectFolder = new File("./temp/testProject_cli_build_no_copy/");
        File wurstFolder = new File(projectFolder, "wurst");
        wurstFolder.mkdirs();
        Files.writeString(new File(wurstFolder, "Wurst.wurst").toPath(), "package Wurst\n");
        Files.writeString(new File(wurstFolder, "Main.wurst").toPath(),
            "package Main\npublic function answer() returns int\n    return 42\n");
        File script = new File(projectFolder, "war3map.j");
        Files.writeString(script.toPath(), "function main takes nothing returns nothing\nendfunction\n");

        ModelManagerImpl modelManager = new ModelManagerImpl(projectFolder, new BufferManager());
        modelManager.buildProject();
        assertFalse(modelManager.hasErrors(), modelManager.getFirstErrorDescription());
        assertNotNull(modelManager.getModel());
        WurstModel built = modelManager.getModel();

        CapturingCliBuildMap request = new CapturingCliBuildMap(projectFolder);
        request.compile(modelManager, script);

        assertTrue(request.compiledModel == built, "the CLI compiled a copy of the model it had built");
    }

    private static final class CapturingCliBuildMap extends CliBuildMap {
        private WurstModel compiledModel;

        private CapturingCliBuildMap(File projectFolder) {
            super(WFile.create(projectFolder), Optional.of(new File(projectFolder, "input.w3x")), List.of(),
                Optional.empty(), new WurstGuiCliImpl());
        }

        private void compile(ModelManager modelManager, File script) throws Exception {
            compileScript(new WurstGuiCliImpl(), modelManager, List.of(), Optional.empty(), WurstProjectConfigData.empty(), true, script);
        }

        @Override
        protected File compileMap(ModelManager modelManager, File projectFolder, WurstGui gui, Optional<File> mapCopy, RunArgs runArgs,
                                  WurstModel model, WurstProjectConfigData projectConfigData, boolean isProd) {
            compiledModel = model;
            return null;
        }
    }
}
