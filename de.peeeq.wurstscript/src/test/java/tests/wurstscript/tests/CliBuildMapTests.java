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

import static org.testng.Assert.assertEquals;
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

    /**
     * The map script is swapped for the one with the project config applied, and the swap invalidates every
     * attribute of the model. So the CLI loads the project without checking it, swaps the script in and checks
     * once: the model is checked with the script it is compiled with, and no check is repeated.
     */
    @Test
    public void theCliChecksTheModelOnceWithTheScriptTheConfigWasAppliedTo() throws Exception {
        File projectFolder = new File("./temp/testProject_cli_single_check/");
        File wurstFolder = new File(projectFolder, "wurst");
        wurstFolder.mkdirs();
        Files.writeString(new File(wurstFolder, "Wurst.wurst").toPath(), "package Wurst\n");
        // calls a function which only the script with the config applied declares
        Files.writeString(new File(wurstFolder, "Main.wurst").toPath(),
            "package Main\nfunction callConfigured()\n    configuredByTheProject()\n");
        Files.writeString(new File(wurstFolder, "war3map.j").toPath(),
            "function main takes nothing returns nothing\nendfunction\n");
        File configured = new File(projectFolder, "configured.j");
        Files.writeString(configured.toPath(),
            "function main takes nothing returns nothing\nendfunction\n"
                + "function configuredByTheProject takes nothing returns nothing\nendfunction\n");

        CountingModelManager modelManager = new CountingModelManager(projectFolder);
        modelManager.loadProject();
        assertFalse(modelManager.hasErrors(), "loading does not check: " + modelManager.getFirstErrorDescription());
        assertEquals(modelManager.checks, 0);
        WurstModel loaded = modelManager.getModel();

        CapturingCliBuildMap request = new CapturingCliBuildMap(projectFolder);
        request.compile(modelManager, configured);

        assertFalse(modelManager.hasErrors(), modelManager.getFirstErrorDescription());
        assertEquals(modelManager.checks, 1, "the model is checked once");
        assertTrue(modelManager.isFullyChecked(loaded), "and the compilation takes that check over");
    }

    /**
     * A project without a map script in its wurst folder (a fresh checkout) gets it from the map the first time.
     * Taking it into the model must not check the model: the script is still the one of the map, the build is
     * about to swap in the one with the project config applied, and a check of the unconfigured one finds errors
     * in code which calls what the config adds.
     */
    @Test
    public void extractingTheMapScriptDoesNotCheckTheModel() throws Exception {
        File projectFolder = new File("./temp/testProject_cli_extract_script/");
        File wurstFolder = new File(projectFolder, "wurst");
        wurstFolder.mkdirs();
        Files.writeString(new File(wurstFolder, "Wurst.wurst").toPath(), "package Wurst\n");
        Files.writeString(new File(wurstFolder, "Main.wurst").toPath(),
            "package Main\nfunction callConfigured()\n    configuredByTheProject()\n");
        File extracted = new File(wurstFolder, "war3map.j");
        Files.deleteIfExists(extracted.toPath());
        File mapFolder = new File(projectFolder, "input.w3x");
        mapFolder.mkdirs();
        Files.writeString(new File(mapFolder, "war3map.j").toPath(),
            "function main takes nothing returns nothing\nendfunction\n");

        CountingModelManager modelManager = new CountingModelManager(projectFolder);
        modelManager.loadProject();
        WurstModel loaded = modelManager.getModel();

        new CapturingCliBuildMap(projectFolder).extractMapScript(modelManager, mapFolder);

        assertTrue(extracted.exists(), "the script is taken from the map");
        assertTrue(modelManager.getCompilationUnit(WFile.create(extracted)) != null, "and is in the model");
        assertFalse(modelManager.isFullyChecked(loaded), "the model is not checked yet");
        assertFalse(modelManager.hasErrors(),
            "no check has run against the unconfigured script: " + modelManager.getFirstErrorDescription());
    }

    private static final class CountingModelManager extends ModelManagerImpl {
        private int checks = 0;

        private CountingModelManager(File projectFolder) {
            super(projectFolder, new BufferManager());
        }

        @Override
        public void checkProject() {
            checks++;
            super.checkProject();
        }
    }

    private static final class CapturingCliBuildMap extends CliBuildMap {
        private WurstModel compiledModel;

        private CapturingCliBuildMap(File projectFolder) {
            super(WFile.create(projectFolder), Optional.of(new File(projectFolder, "input.w3x")), List.of(),
                Optional.empty(), new WurstGuiCliImpl());
        }

        private void extractMapScript(ModelManager modelManager, File mapFolder) throws Exception {
            loadMapScript(Optional.of(mapFolder), modelManager, new WurstGuiCliImpl());
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
