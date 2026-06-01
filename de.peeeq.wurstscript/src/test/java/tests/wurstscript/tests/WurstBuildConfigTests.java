package tests.wurstscript.tests;

import config.WurstProjectConfigData;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.ProjectConfigBuilder;
import de.peeeq.wurstio.languageserver.WurstBuildConfig;
import de.peeeq.wurstio.languageserver.WurstCommands;
import net.moonlightflower.wc3libs.port.GameVersion;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class WurstBuildConfigTests {

    @Test
    public void readsScriptModeAndPatchFromWurstBuild() throws Exception {
        Path project = Files.createTempDirectory("wurst-build-config");
        Files.writeString(project.resolve("wurst.build"), """
            projectName: Test
            dependencies:
              - https://github.com/wurstscript/wurstStdlib2:pre1.29
            scriptMode: lua
            wc3Patch: pre1.29
            """);

        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));

        assertEquals(config.scriptMode().orElseThrow(), WurstBuildConfig.ScriptMode.LUA);
        assertEquals(config.wc3Patch().orElseThrow(), WurstBuildConfig.Wc3Patch.PRE_129);
        assertEquals(config.fallbackGameVersion(), new GameVersion("1.28"));
        assertTrue(config.shouldUseClassicWindowArg(Optional.empty()));
        assertFalse(config.shouldUseReforgedLaunchArgs(Optional.empty()));
        assertFalse(config.shouldUseInstallDirForMaps(Optional.empty()));
    }

    @Test
    public void installDirMapPathIsOnlyForDetected127OrLower() throws Exception {
        Path project = Files.createTempDirectory("wurst-build-config-map-path");
        Files.writeString(project.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: pre1.29
            """);

        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));

        assertTrue(config.shouldUseInstallDirForMaps(Optional.of(new GameVersion("1.27"))));
        assertFalse(config.shouldUseInstallDirForMaps(Optional.of(new GameVersion("1.28"))));
        assertFalse(config.shouldUseInstallDirForMaps(Optional.empty()));
    }

    @Test
    public void understandsFriendlyAndJassHistoryPatchNames() throws Exception {
        Path reforgedProject = Files.createTempDirectory("wurst-build-config-reforged");
        Files.writeString(reforgedProject.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: v2.0
            """);
        WurstBuildConfig reforged = WurstBuildConfig.fromWorkspaceRoot(WFile.create(reforgedProject.toFile()));

        assertEquals(reforged.wc3Patch().orElseThrow(), WurstBuildConfig.Wc3Patch.REFORGED);
        assertEquals(reforged.wc3PatchName().orElseThrow(), "v2.0");
        assertEquals(reforged.configuredGameVersion().orElseThrow(), new GameVersion("2.0"));
        assertTrue(reforged.shouldUseReforgedLaunchArgs(Optional.empty()));

        Path classicProject = Files.createTempDirectory("wurst-build-config-classic");
        Files.writeString(classicProject.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: TFT-v1.31.1.12173
            """);
        WurstBuildConfig classic = WurstBuildConfig.fromWorkspaceRoot(WFile.create(classicProject.toFile()));

        assertEquals(classic.wc3Patch().orElseThrow(), WurstBuildConfig.Wc3Patch.CLASSIC);
        assertEquals(classic.configuredGameVersion().orElseThrow(), new GameVersion("1.31"));
        assertFalse(classic.shouldUseReforgedLaunchArgs(Optional.empty()));
        assertTrue(classic.shouldCopyRunMapToWarcraftMapDir(Optional.empty()));

        Path legacyProject = Files.createTempDirectory("wurst-build-config-legacy");
        Files.writeString(legacyProject.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: v1.27b
            """);
        WurstBuildConfig legacy = WurstBuildConfig.fromWorkspaceRoot(WFile.create(legacyProject.toFile()));

        assertEquals(legacy.wc3Patch().orElseThrow(), WurstBuildConfig.Wc3Patch.PRE_129);
        assertEquals(legacy.configuredGameVersion().orElseThrow(), new GameVersion("1.27"));
        assertTrue(legacy.shouldUseClassicWindowArg(Optional.empty()));
        assertTrue(legacy.shouldUseInstallDirForMaps(Optional.empty()));
    }

    @Test
    public void acceptsOlderPatchSpellingAndAliases() throws Exception {
        Path numericProject = Files.createTempDirectory("wurst-build-config-numeric");
        Files.writeString(numericProject.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: 1.36
            """);
        WurstBuildConfig numeric = WurstBuildConfig.fromWorkspaceRoot(WFile.create(numericProject.toFile()));

        assertEquals(numeric.wc3Patch().orElseThrow(), WurstBuildConfig.Wc3Patch.REFORGED);
        assertEquals(numeric.configuredGameVersion().orElseThrow(), new GameVersion("1.36"));

        Path classicProject = Files.createTempDirectory("wurst-build-config-classic-alias");
        Files.writeString(classicProject.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: classic
            """);
        WurstBuildConfig classic = WurstBuildConfig.fromWorkspaceRoot(WFile.create(classicProject.toFile()));

        assertEquals(classic.wc3Patch().orElseThrow(), WurstBuildConfig.Wc3Patch.CLASSIC);
        assertEquals(classic.configuredGameVersion().orElseThrow(), new GameVersion("1.31"));
    }

    @Test
    public void ignoresUnknownLegacyBuildSettings() throws Exception {
        Path project = Files.createTempDirectory("wurst-build-config-unknown");
        Files.writeString(project.resolve("wurst.build"), """
            projectName: Test
            oldSetting: whatever
            scriptMode: not-a-mode
            wc3Patch: some-old-custom-value
            buildMapData:
              staleNestedThing: true
            """);

        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));

        assertFalse(config.scriptMode().isPresent());
        assertFalse(config.wc3Patch().isPresent());
        assertEquals(config.fallbackGameVersion(), GameVersion.VERSION_1_32);
    }

    @Test
    public void preservesCommentsInsideQuotedPatchValues() throws Exception {
        Path project = Files.createTempDirectory("wurst-build-config-quoted-comment");
        Files.writeString(project.resolve("wurst.build"), """
            scriptMode: "lua#not-a-comment"
            wc3Patch: "1.36#not-a-comment"
            """);

        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));

        assertFalse(config.scriptMode().isPresent());
        assertEquals(config.configuredGameVersion().orElseThrow(), new GameVersion("1.36"));
    }

    @Test
    public void compileArgsFollowConfiguredScriptMode() throws Exception {
        Path project = Files.createTempDirectory("wurst-build-config-args");
        Files.writeString(project.resolve("wurst.build"), """
            projectName: Test
            scriptMode: jass
            """);
        Files.writeString(project.resolve("wurst_run.args"), "-runcompiletimefunctions\n-lua\n");

        List<String> args = WurstCommands.getCompileArgs(WFile.create(project.toFile()));

        assertTrue(args.contains("-runcompiletimefunctions"));
        assertFalse(args.contains("-lua"));
    }

    @Test
    public void projectConfigHashIncludesExactPatchTarget() throws Exception {
        Path project132 = Files.createTempDirectory("wurst-build-config-hash-132");
        Files.createDirectories(project132.resolve("_build"));
        Files.writeString(project132.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: v1.32
            """);

        Path project20 = Files.createTempDirectory("wurst-build-config-hash-20");
        Files.createDirectories(project20.resolve("_build"));
        Files.writeString(project20.resolve("wurst.build"), """
            projectName: Test
            wc3Patch: v2.0
            """);

        String hash132 = calculateProjectConfigHash(project132.resolve("_build").toFile());
        String hash20 = calculateProjectConfigHash(project20.resolve("_build").toFile());

        assertNotEquals(hash132, hash20);
    }

    private static String calculateProjectConfigHash(File buildDir) throws Exception {
        Method method = ProjectConfigBuilder.class.getDeclaredMethod(
            "calculateProjectConfigHash",
            WurstProjectConfigData.class,
            File.class
        );
        method.setAccessible(true);
        return (String) method.invoke(null, new WurstProjectConfigData(), buildDir);
    }
}
