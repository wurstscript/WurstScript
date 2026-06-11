package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstBuildConfig;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.languageserver.requests.RunMap;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstio.utils.W3InstallationData;
import net.moonlightflower.wc3libs.port.GameVersion;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PrintStream previousOut = System.out;
        PrintStream previousErr = System.err;
        try {
            System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(stderr, true, StandardCharsets.UTF_8));
            W3InstallationData data = new W3InstallationData(Optional.of(fakeExe.toFile()), Optional.empty());

            assertFalse(data.getWc3PatchVersion().isPresent());
        } finally {
            System.setOut(previousOut);
            System.setErr(previousErr);
        }

        String output = stdout.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Could not parse game version from configured executable"));

        // The wc3libs PE-parser trace previously leaked to stderr via printStackTrace; it must not appear on
        // either stream. This guards the wc3libs dependency pin from regressing back to the noisy behaviour.
        String combined = output + stderr.toString(StandardCharsets.UTF_8);
        assertFalse(combined.contains("VersionExtractionException"), combined);
        assertFalse(combined.contains("dorkbox.peParser"), combined);
        assertFalse(combined.contains("\tat "), combined);
    }

    @Test
    public void launchFailureMessageIsActionableWithoutStackTrace() throws Exception {
        File exe = new File("C:\\Program Files (x86)\\Warcraft III\\Warcraft III.exe");
        IOException failure = new IOException("Cannot run program \"" + exe.getAbsolutePath()
            + "\": CreateProcess error=216, This version of %1 is not compatible with the version of Windows you're running.");

        String message = launchFailureMessage(exe, failure);

        assertTrue(message.contains(exe.getAbsolutePath()), message);
        assertTrue(message.contains("wc3path"), message);
        assertTrue(message.contains("wc3Patch"), message);
        assertTrue(message.contains("error=216"), message);
        // Must stay short and human-readable: no leaked stack trace.
        assertFalse(message.contains("\tat "), message);
        assertFalse(message.contains("VersionExtractionException"), message);
    }

    @Test
    public void launchFailureMessageHandlesUnknownExecutable() throws Exception {
        String message = launchFailureMessage(null, new IOException("CreateProcess error=216"));

        assertTrue(message.contains("Could not launch Warcraft III"), message);
        assertTrue(message.contains("wc3path"), message);
        assertFalse(message.contains("null"), message);
    }

    private static String launchFailureMessage(File gameExe, IOException failure) throws Exception {
        Method method = RunMap.class.getDeclaredMethod("launchFailureMessage", File.class, IOException.class);
        method.setAccessible(true);
        return (String) method.invoke(null, gameExe, failure);
    }

    @Test
    public void patchComplianceRequiresKnownMatchingClientWhenPinned() throws Exception {
        // No pinned patch: nothing to validate against, so any auto-detected client is acceptable.
        assertTrue(isPatchCompliant(Optional.empty(), Optional.of(new GameVersion("1.31"))));
        assertTrue(isPatchCompliant(Optional.empty(), Optional.empty()));

        // Pinned Reforged: only a Reforged-family client is compliant.
        assertTrue(isPatchCompliant(Optional.of(WurstBuildConfig.Wc3Patch.REFORGED), Optional.of(GameVersion.VERSION_1_32)));
        assertFalse(isPatchCompliant(Optional.of(WurstBuildConfig.Wc3Patch.REFORGED), Optional.of(new GameVersion("1.31"))));

        // Pinned Classic: a 1.31 client matches; a pre-1.29 client does not.
        assertTrue(isPatchCompliant(Optional.of(WurstBuildConfig.Wc3Patch.CLASSIC), Optional.of(new GameVersion("1.31"))));
        assertFalse(isPatchCompliant(Optional.of(WurstBuildConfig.Wc3Patch.CLASSIC), Optional.of(new GameVersion("1.28"))));

        // Pinned patch but undetectable client version: treated as non-compliant so the user is asked to choose,
        // rather than blind-launching a possibly-wrong client.
        assertFalse(isPatchCompliant(Optional.of(WurstBuildConfig.Wc3Patch.REFORGED), Optional.empty()));
        assertFalse(isPatchCompliant(Optional.of(WurstBuildConfig.Wc3Patch.CLASSIC), Optional.empty()));
        assertFalse(isPatchCompliant(Optional.of(WurstBuildConfig.Wc3Patch.PRE_129), Optional.empty()));
    }

    @Test
    public void retryLaunchSelectionRechecksPatchCompliance() throws Exception {
        Path project = projectWithPatch("v1.27b");
        W3InstallationData reforgedRetry = installationData("warcraft-reforged-retry", "Warcraft III.exe", GameVersion.VERSION_1_32);
        W3InstallationData legacySelection = installationData("warcraft-legacy-selection", "war3.exe", new GameVersion("1.27"));
        Path fakeInitialExe = Files.writeString(Files.createTempFile("not-warcraft-initial", ".exe"), "not a PE file");

        PatchComplianceRunMap request = new PatchComplianceRunMap(project, legacySelection, fakeInitialExe.toString());

        W3InstallationData resolved = request.resolveAfterRetry(reforgedRetry);

        assertEquals(request.mismatchPrompts, 1);
        assertEquals(
            resolved.getGameExe().orElseThrow().getAbsoluteFile(),
            legacySelection.getGameExe().orElseThrow().getAbsoluteFile()
        );
    }

    private static W3InstallationData installationData(String folderPrefix, String exeName, GameVersion version) throws IOException {
        Path install = Files.createTempDirectory(folderPrefix);
        Path exe = Files.writeString(install.resolve(exeName), "not a PE file");
        return new W3InstallationData(Optional.of(exe.toFile()), Optional.of(version));
    }

    private static boolean isPatchCompliant(Optional<WurstBuildConfig.Wc3Patch> projectKind,
                                            Optional<GameVersion> clientVersion) throws Exception {
        Method method = RunMap.class.getDeclaredMethod("isPatchCompliant", Optional.class, Optional.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, projectKind, clientVersion);
    }

    @Test
    public void pre124PatchPropagatesLegacyJassChecksToCompileArgs() throws Exception {
        // Pre-1.24: the flag must be carried in compileArgs so the fresh RunArgs that
        // compileScript/compileMap build (RunArgs(compileArgs)) actually relaxes Jass checks + skips PJass.
        TestMapRequest legacy = new TestMapRequest(projectWithPatch("v1.23"), Optional.empty(), Optional.empty());
        assertTrue(legacy.exposedCompileArgs().contains("-legacyJassChecks"),
            "pre-1.24 build must carry -legacyJassChecks in compileArgs: " + legacy.exposedCompileArgs());
        assertTrue(new RunArgs(legacy.exposedCompileArgs().toArray(new String[0])).isLegacyJassTypeChecks(),
            "a RunArgs rebuilt from compileArgs must report legacy Jass checks");

        // Modern target: flag must not be injected.
        TestMapRequest modern = new TestMapRequest(projectWithPatch("v2.0"), Optional.empty(), Optional.empty());
        assertFalse(modern.exposedCompileArgs().contains("-legacyJassChecks"),
            "modern build must not carry the legacy flag: " + modern.exposedCompileArgs());
    }

    @Test
    public void devBuildFlagMakesBuildPipelineNonProduction() throws Exception {
        TestMapRequest dev = new TestMapRequest(projectWithPatch("v2.0"), Optional.empty(), Optional.empty(), List.of("-dev"));
        assertFalse(dev.exposedIsProductionBuild(), "CLI build with -dev should expose isProductionBuild() = false to compiletime");

        TestMapRequest release = new TestMapRequest(projectWithPatch("v2.0"), Optional.empty(), Optional.empty());
        assertTrue(release.exposedIsProductionBuild(), "CLI build without -dev should remain production by default");
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
            this(projectRoot, wc3Path, gameExePath, List.of());
        }

        TestMapRequest(Path projectRoot, Optional<String> wc3Path, Optional<String> gameExePath, List<String> compileArgs) {
            super(null, Optional.empty(), compileArgs, WFile.create(projectRoot), wc3Path, gameExePath);
        }

        Optional<GameVersion> detectedVersion() {
            return w3data.getWc3PatchVersion();
        }

        Optional<File> gameExe() {
            return w3data.getGameExe();
        }

        List<String> exposedCompileArgs() {
            return compileArgs;
        }

        boolean exposedIsProductionBuild() {
            return isProductionBuild();
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

    private static final class PatchComplianceRunMap extends RunMap {
        private final W3InstallationData alternateSelection;
        private int mismatchPrompts = 0;

        PatchComplianceRunMap(Path projectRoot, W3InstallationData alternateSelection, String gameExePath) {
            super(null, WFile.create(projectRoot), Optional.empty(), Optional.empty(), List.of(), Optional.of(gameExePath));
            this.alternateSelection = alternateSelection;
        }

        W3InstallationData resolveAfterRetry(W3InstallationData retrySelection) {
            return resolveLaunchData(retrySelection);
        }

        @Override
        protected MismatchChoice chooseMismatchAction(String message) {
            mismatchPrompts++;
            return MismatchChoice.CHOOSE_OTHER;
        }

        @Override
        protected Optional<W3InstallationData> chooseAlternateGamePath() {
            return Optional.of(alternateSelection);
        }
    }
}
