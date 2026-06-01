package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.languageserver.requests.RunMap;
import de.peeeq.wurstio.utils.W3InstallationData;
import net.moonlightflower.wc3libs.port.GameVersion;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MapRequestPatchTargetTests {

    @Test
    public void pinnedBuildUsesWurstBuildPatchWithoutWarcraftExeDiscovery() throws Exception {
        Path project = projectWithPatch("v2.0");

        TestMapRequest request = new TestMapRequest(
            project,
            Optional.of("Z:\\Definitely\\Not\\Warcraft"),
            Optional.empty()
        );

        assertEquals(request.detectedVersion().orElseThrow(), new GameVersion("2.0"));
        assertFalse(request.gameExe().isPresent(), "Pinned build requests should not need a Warcraft executable");
    }

    @Test
    public void pinnedExplicitGameExeUsesPatchVersionWithoutParsingExecutable() throws Exception {
        Path project = projectWithPatch("TFT-v1.31.1.12173");
        Path fakeExe = Files.writeString(Files.createTempFile("not-warcraft", ".exe"), "not a PE file");

        TestMapRequest request = new TestMapRequest(
            project,
            Optional.empty(),
            Optional.of(fakeExe.toString())
        );

        assertEquals(request.detectedVersion().orElseThrow(), new GameVersion("1.31"));
        assertEquals(request.gameExe().orElseThrow().getAbsoluteFile(), fakeExe.toFile().getAbsoluteFile());
    }

    @Test
    public void pinnedRunMapUsesPatchVersionWithoutParsingExecutable() throws Exception {
        Path project = projectWithPatch("v1.27b");
        Path fakeExe = Files.writeString(Files.createTempFile("not-warcraft-run", ".exe"), "not a PE file");

        TestRunMap request = new TestRunMap(project, fakeExe.toString());

        assertFalse(request.detectedVersion().isPresent());
        assertEquals(request.gameExe().orElseThrow().getAbsoluteFile(), fakeExe.toFile().getAbsoluteFile());
    }

    @Test
    public void runMapInfersReforgedClientFromRetailExecutablePath() throws Exception {
        Path project = projectWithPatch("v1.27b");
        Path install = Files.createTempDirectory("warcraft-reforged");
        Path exe = Files.createDirectories(install.resolve("_retail_").resolve("x86_64")).resolve("Warcraft III.exe");
        Files.writeString(exe, "not a PE file");

        TestRunMap request = new TestRunMap(project, exe.toString());

        assertEquals(request.detectedVersion().orElseThrow(), GameVersion.VERSION_1_32);
    }

    @Test
    public void runMapInfersClassicClientFromWar3ExecutablePath() throws Exception {
        Path project = projectWithPatch("v2.0");
        Path exe = Files.writeString(Files.createTempDirectory("warcraft-classic").resolve("war3.exe"), "not a PE file");

        TestRunMap request = new TestRunMap(project, exe.toString());

        assertEquals(request.detectedVersion().orElseThrow(), new GameVersion("1.28"));
    }

    @Test
    public void legacyRunMapPlacementUsesSelectedLaunchInstall() throws Exception {
        Path selectedInstall = Files.createTempDirectory("warcraft-selected-legacy");
        Path exe = Files.writeString(selectedInstall.resolve("war3.exe"), "not a PE file");
        Path staleFallback = Files.createTempDirectory("warcraft-stale-fallback");
        W3InstallationData launchData = new W3InstallationData(
            Optional.of(exe.toFile()),
            Optional.of(new GameVersion("1.27"))
        );

        Method placementRoot = RunMap.class.getDeclaredMethod(
            "mapInstallDirectoryForLegacyLaunch",
            W3InstallationData.class,
            Optional.class
        );
        placementRoot.setAccessible(true);

        @SuppressWarnings("unchecked")
        Optional<String> result = (Optional<String>) placementRoot.invoke(null, launchData, Optional.of(staleFallback.toString()));

        assertEquals(result.orElseThrow(), selectedInstall.toFile().getAbsolutePath());
    }

    @Test
    public void configuredVersionSurvivesManualPathLoad() throws Exception {
        W3InstallationData data = new W3InstallationData(Optional.empty(), Optional.of(new GameVersion("2.0")));
        Method loadFromPath = W3InstallationData.class.getDeclaredMethod("loadFromPath", File.class);
        loadFromPath.setAccessible(true);

        loadFromPath.invoke(data, Files.createTempDirectory("not-warcraft-folder").toFile());

        assertEquals(data.getWc3PatchVersion().orElseThrow(), new GameVersion("2.0"));
    }

    @Test
    public void gameVersionParseFailurePrintsShortMessageOnly() throws Exception {
        Path fakeExe = Files.writeString(Files.createTempFile("bad-warcraft", ".exe"), "not a PE file");
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PrintStream previousOut = System.out;
        try {
            System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
            W3InstallationData data = new W3InstallationData(Optional.of(fakeExe.toFile()), Optional.empty());

            assertFalse(data.getWc3PatchVersion().isPresent());
        } finally {
            System.setOut(previousOut);
        }

        String output = stdout.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Could not parse game version from configured executable"));
        assertFalse(output.contains("VersionExtractionException"), output);
        assertFalse(output.contains("dorkbox.peParser"), output);
        assertFalse(output.contains("\tat "), output);
    }

    private static Path projectWithPatch(String patch) throws Exception {
        Path project = Files.createTempDirectory("wurst-map-request-patch");
        Files.createDirectories(project.resolve("wurst"));
        Files.writeString(project.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: %s
            """.formatted(patch));
        return project;
    }

    private static final class TestMapRequest extends MapRequest {
        TestMapRequest(Path projectRoot, Optional<String> wc3Path, Optional<String> gameExePath) {
            super(null, Optional.empty(), List.of(), WFile.create(projectRoot), wc3Path, gameExePath);
        }

        Optional<GameVersion> detectedVersion() {
            return w3data.getWc3PatchVersion();
        }

        Optional<File> gameExe() {
            return w3data.getGameExe();
        }

        @Override
        public Object execute(ModelManager modelManager) {
            return null;
        }
    }

    private static final class TestRunMap extends RunMap {
        TestRunMap(Path projectRoot, String gameExePath) {
            super(null, WFile.create(projectRoot), Optional.empty(), Optional.empty(), List.of(), Optional.of(gameExePath));
        }

        Optional<GameVersion> detectedVersion() {
            return w3data.getWc3PatchVersion();
        }

        Optional<File> gameExe() {
            return w3data.getGameExe();
        }
    }
}
