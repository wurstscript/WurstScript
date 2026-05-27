package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstBuildConfig;
import de.peeeq.wurstio.languageserver.WurstCommands;
import net.moonlightflower.wc3libs.port.GameVersion;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
}
