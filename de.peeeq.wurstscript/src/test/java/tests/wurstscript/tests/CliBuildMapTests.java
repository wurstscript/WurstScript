package tests.wurstscript.tests;

import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.CliBuildMap;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

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

    /**
     * The CLI loads the project without checking it, so the check with the map script is the only gate: a type error it
     * finds fails the build before anything is compiled.
     */
    @Test
    public void theCliBuildFailsOnATypeErrorFoundByItsSingleCheck() throws Exception {
        File projectFolder = new File("./temp/testProject_cli_single_check_error/");
        File wurstFolder = new File(projectFolder, "wurst");
        wurstFolder.mkdirs();
        Files.writeString(new File(wurstFolder, "Wurst.wurst").toPath(), "package Wurst\n");
        Files.writeString(new File(wurstFolder, "Main.wurst").toPath(),
            "package Main\nfunction f() returns int\n    return missingVariable\n");
        Files.writeString(new File(wurstFolder, "war3map.j").toPath(),
            "function main takes nothing returns nothing\nendfunction\n");
        File configured = new File(projectFolder, "configured.j");
        Files.writeString(configured.toPath(), "function main takes nothing returns nothing\nendfunction\n");

        CountingModelManager modelManager = new CountingModelManager(projectFolder);
        modelManager.loadProject();
        assertFalse(modelManager.hasErrors(), "loading does not check");
        CapturingCliBuildMap request = new CapturingCliBuildMap(projectFolder);

        RuntimeException e = expectThrows(RuntimeException.class, () -> request.compile(modelManager, configured));

        assertTrue(e.getMessage().contains("missingVariable"), e.getMessage());
        assertEquals(modelManager.checks, 1, "the model is checked once");
        assertNull(request.compiledModel, "nothing is compiled when the check finds errors");
    }

    private static final String NAME_WARNING = "Function names should start with an lower case character.";
    /** Reported by the lexer, when the project is loaded: no check of the model reports it. */
    private static final String INDENTATION_WARNING = "Use an even number of spaces for indentation.";

    /**
     * A project whose code has a warning of the check and one of the parser, and the map script with the config
     * applied (returned).
     */
    private static File projectWithWarnings(String folder) throws Exception {
        File projectFolder = new File(folder);
        File wurstFolder = new File(projectFolder, "wurst");
        wurstFolder.mkdirs();
        Files.writeString(new File(wurstFolder, "Wurst.wurst").toPath(), "package Wurst\n");
        Files.writeString(new File(wurstFolder, "Main.wurst").toPath(),
            "package Main\npublic function NotLowerCase()\n    skip\n");
        Files.writeString(new File(wurstFolder, "Indented.wurst").toPath(),
            "package Indented\npublic function indentedByThree()\n   skip\n");
        Files.writeString(new File(wurstFolder, "war3map.j").toPath(),
            "function main takes nothing returns nothing\nendfunction\n");
        File configured = new File(projectFolder, "configured.j");
        Files.writeString(configured.toPath(),
            "function main takes nothing returns nothing\nendfunction\n"
                + "function config takes nothing returns nothing\nendfunction\n");
        return configured;
    }

    private static long warnings(WurstGui gui, String message) {
        return gui.getWarningList().stream().filter(w -> w.getMessage().equals(message)).count();
    }

    /**
     * Main prints the warnings of the request's gui after a build. The CLI checks the model once, and the compilation
     * takes that check over, so the warnings of the check are the ones to print, each once, with those the parser
     * reported when the project was loaded.
     */
    @Test
    public void theCliBuildReportsTheWarningsOfItsCheck() throws Exception {
        File configured = projectWithWarnings("./temp/testProject_cli_build_warnings/");
        File projectFolder = configured.getParentFile();
        ModelManagerImpl modelManager = new ModelManagerImpl(projectFolder, new BufferManager());
        modelManager.loadProject();
        WurstGuiCliImpl gui = new WurstGuiCliImpl();

        new CheckingCliBuildMap(projectFolder, List.of(), gui).compile(modelManager, configured);

        assertFalse(modelManager.hasErrors(), modelManager.getFirstErrorDescription());
        assertEquals(warnings(gui, NAME_WARNING), 1, "the warning is reported once: " + gui.getWarningList());
        assertEquals(warnings(gui, INDENTATION_WARNING), 1,
            "the warning of the parser is reported once: " + gui.getWarningList());
    }

    /** With the legacy Jass checks the compilation checks the model again, with its own checks, and reports that. */
    @Test
    public void withTheLegacyJassChecksTheCliBuildReportsTheWarningsOnce() throws Exception {
        File configured = projectWithWarnings("./temp/testProject_cli_build_warnings_legacy/");
        File projectFolder = configured.getParentFile();
        ModelManagerImpl modelManager = new ModelManagerImpl(projectFolder, new BufferManager());
        modelManager.loadProject();
        WurstGuiCliImpl gui = new WurstGuiCliImpl();

        new CheckingCliBuildMap(projectFolder, List.of("-legacyJassChecks"), gui).compile(modelManager, configured);

        assertEquals(warnings(gui, NAME_WARNING), 1, "the warning is reported once: " + gui.getWarningList());
        assertEquals(warnings(gui, INDENTATION_WARNING), 1,
            "the warning of the parser is reported once: " + gui.getWarningList());
    }

    private static final class CountingModelManager extends ModelManagerImpl {
        private int checks = 0;

        private CountingModelManager(File projectFolder) {
            super(projectFolder, new BufferManager());
        }

        @Override
        public void checkProject(WurstGui gui) {
            checks++;
            super.checkProject(gui);
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

    /** Builds with one gui, as Main does, and checks the model for the compilation as {@code compileMap} does. */
    private static final class CheckingCliBuildMap extends CliBuildMap {
        private final WurstGui gui;
        private final List<String> compileArgs;

        private CheckingCliBuildMap(File projectFolder, List<String> compileArgs, WurstGui gui) {
            super(WFile.create(projectFolder), Optional.of(new File(projectFolder, "input.w3x")), compileArgs,
                Optional.empty(), gui);
            this.gui = gui;
            this.compileArgs = compileArgs;
        }

        private void compile(ModelManager modelManager, File script) throws Exception {
            compileScript(gui, modelManager, compileArgs, Optional.empty(), WurstProjectConfigData.empty(), true, script);
        }

        @Override
        protected File compileMap(ModelManager modelManager, File projectFolder, WurstGui gui, Optional<File> mapCopy, RunArgs runArgs,
                                  WurstModel model, WurstProjectConfigData projectConfigData, boolean isProd) {
            WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(new TimeTaker.Default(), projectFolder, gui, null, runArgs);
            MapRequest.checkModel(modelManager, compiler, model, runArgs);
            return null;
        }
    }
}
