package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.RequestFailedException;
import net.moonlightflower.wc3libs.bin.app.MapFlag;
import net.moonlightflower.wc3libs.bin.app.W3I;
import org.wurstscript.projectconfig.WurstProjectBuildMapData;
import org.wurstscript.projectconfig.WurstProjectBuildOptionFlagsData;
import org.wurstscript.projectconfig.WurstProjectBuildPlayer;
import org.wurstscript.projectconfig.WurstProjectBuildV3ReforgedData;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class ProjectConfigBuilderTests {

    @Test
    public void pinnedPatchDowngradesSourceFormatWhenItIsTooNew() throws Exception {
        assertW3IVersion("1.30", W3I.EncodingFormat.W3I_0x19.getVersion());
        assertW3IVersion("1.31", W3I.EncodingFormat.W3I_0x1C.getVersion());
        assertW3IVersion("1.32", W3I.EncodingFormat.W3I_0x1F.getVersion());
        assertW3IVersion("2.0", W3I.EncodingFormat.W3I_0x21.getVersion());
        assertW3IVersion("3.0", W3I.EncodingFormat.W3I_0x27.getVersion());
    }

    @Test
    public void olderTargetWarnsBeforeDiscardingNewerW3iInformation() throws Exception {
        Path project = Files.createTempDirectory("w3i-downgrade-warning");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: 1.30\n");
        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());

        List<String> warnings = new ArrayList<>();
        Optional<String> warning = ProjectConfigBuilder.applyW3IVersion(config, w3i, false, warnings::add);

        assertTrue(warning.isPresent());
        assertEquals(warnings, List.of(warning.orElseThrow()));
        assertTrue(warning.orElseThrow().contains("selected Warcraft III target 1.30"));
        assertTrue(warning.orElseThrow().contains("information added in newer patches may be lost"));
        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x19.getVersion());

        Path downgradedW3i = Files.createTempFile("w3i-downgraded", ".w3i");
        w3i.write(downgradedW3i.toFile(), W3I.EncodingFormat.AS_DEFINED);
        W3I written = new W3I(Files.readAllBytes(downgradedW3i));
        assertEquals(written.getFileVersion(), W3I.EncodingFormat.W3I_0x19.getVersion());
    }

    @Test
    public void reignOfChaosTargetDowngradesToItsSupportedW3iFormat() throws Exception {
        Path project = Files.createTempDirectory("w3i-roc-downgrade");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: ROC-v1.28.5.7680\n");
        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());

        Optional<String> warning = ProjectConfigBuilder.applyW3IVersion(config, w3i, false);

        assertTrue(config.isReignOfChaosTarget());
        assertTrue(warning.orElseThrow().contains("supports (format 18)"));
        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x12.getVersion());
    }

    @Test
    public void rejectsLuaForTargetsThatDoNotSupportItBeforeChangingW3i() throws Exception {
        for (String patch : new String[]{"1.30", "1.31"}) {
            Path project = Files.createTempDirectory("w3i-lua-old-target");
            Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: " + patch + "\n");
            WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
            W3I w3i = new W3I();
            w3i.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());

            RequestFailedException error;
            try {
                ProjectConfigBuilder.applyW3IVersion(config, w3i, true);
                throw new AssertionError("Expected Lua build to fail for target " + patch);
            } catch (RequestFailedException e) {
                error = e;
            }

            assertTrue(error.getMessage().contains("Lua map scripts require Warcraft III 1.32 or newer"));
            assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion(),
                "unsupported Lua target must fail before changing source map metadata");
        }
    }

    @Test
    public void noPinnedPatchPreservesSourceW3iVersion() throws Exception {
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());

        ProjectConfigBuilder.applyW3IVersion(WurstBuildConfig.empty(), w3i, false);

        assertFalse(ProjectConfigBuilder.w3iDowngradeWarning(WurstBuildConfig.empty(), w3i).isPresent());
        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
    }

    @Test
    public void readsW3iFromSourceMapInsteadOfDowngradedCache() throws Exception {
        Path sourceMap = Files.createTempDirectory("source-map");
        Path sourceW3i = sourceMap.resolve("war3map.w3i");
        W3I original = new W3I();
        original.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());
        original.write(sourceW3i.toFile());

        W3I loaded = ProjectConfigBuilder.readW3I(sourceMap.toFile());
        ProjectConfigBuilder.applyW3IVersion(WurstBuildConfig.empty(), loaded, false);

        assertEquals(loaded.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
    }

    @Test
    public void luaKeepsScriptLanguageFieldInOlderSourceW3i() throws Exception {
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x19.getVersion());

        ProjectConfigBuilder.applyW3IVersion(WurstBuildConfig.empty(), w3i, true);

        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x1F.getVersion());
        assertEquals(w3i.getScriptLang(), W3I.ScriptLang.LUA);
    }

    @Test
    public void configuredV3GameDataPromotesAndRoundTripsW3iFormat39() throws Exception {
        Path project = Files.createTempDirectory("w3i-v3-data-target");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: 3.0\n");
        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        W3I source = new W3I();
        source.setFileVersion(W3I.EncodingFormat.W3I_0x1F.getVersion());
        Path oldFile = Files.createTempFile("w3i-v2-source", ".w3i");
        source.write(oldFile.toFile(), W3I.EncodingFormat.W3I_0x1F);
        W3I w3i = new W3I(Files.readAllBytes(oldFile));
        WurstProjectBuildMapData mapData = new WurstProjectBuildMapData(
            "", "", "", null, null, List.of(), List.of(), "FORSAKEN_KINGDOM"
        );

        ProjectConfigBuilder.applyGameDataVersion(mapData, w3i);
        ProjectConfigBuilder.applyW3IVersion(config, w3i, false, true, ignored -> {});

        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
        Path file = Files.createTempFile("w3i-v3-data", ".w3i");
        w3i.write(file.toFile(), W3I.EncodingFormat.AS_DEFINED);
        W3I written = new W3I(Files.readAllBytes(file));
        assertEquals(written.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
        assertEquals(written.getGameDataVersion(), W3I.GameDataVersion.FORSAKEN_KINGDOM);
    }

    @Test
    public void configuredW3IV39FieldsPromoteAndRoundTrip() throws Exception {
        Path project = Files.createTempDirectory("w3i-v39-settings");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: 3.0\n");
        WurstBuildConfig buildConfig = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        WurstProjectBuildV3ReforgedData reforgedV3Data = new WurstProjectBuildV3ReforgedData(
            4, 2, true, 1200.5f, 4000.25f, 0.75f, 80.0f,
            4, 96, 12, 3, 42, 15, 90, 112233, 77, -1
        );
        WurstProjectBuildOptionFlagsData flags = new WurstProjectBuildOptionFlagsData(
            false, false, false, false, false, false, true, true, true
        );
        WurstProjectBuildPlayer player = new WurstProjectBuildPlayer(0, null, null, null, null, 64);
        WurstProjectBuildMapData mapData = new WurstProjectBuildMapData(
            "", "", "", null, flags, List.of(player), List.of(), "FORSAKEN_KINGDOM", reforgedV3Data
        );
        WurstProjectConfigData projectConfig = new WurstProjectConfigData("test", List.of(), mapData, null, "3.0");

        W3I source = new W3I();
        source.setFileVersion(W3I.EncodingFormat.W3I_0x1F.getVersion());
        Path oldFile = Files.createTempFile("w3i-v2-settings", ".w3i");
        source.write(oldFile.toFile(), W3I.EncodingFormat.W3I_0x1F);
        W3I w3i = new W3I(Files.readAllBytes(oldFile));
        ProjectConfigBuilder.prepareW3I(projectConfig, w3i);
        assertTrue(ProjectConfigBuilder.requiresReforgedV3Data(mapData));
        ProjectConfigBuilder.applyW3IVersion(buildConfig, w3i, false,
            ProjectConfigBuilder.requiresReforgedV3Data(mapData), ignored -> {});

        Path file = Files.createTempFile("w3i-v39-settings", ".w3i");
        w3i.write(file.toFile(), W3I.EncodingFormat.AS_DEFINED);
        W3I written = new W3I(Files.readAllBytes(file));
        assertEquals(written.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
        assertEquals(written.getGameDataVersion(), W3I.GameDataVersion.FORSAKEN_KINGDOM);
        assertEquals(written.getLoadingScreenCrestRace(), 4);
        assertEquals(written.getTerrainFogStyle(), 2);
        assertTrue(written.getDrawTerrainFogOverSky());
        assertEquals(written.getTerrainFogLinearStart(), 1200.5f);
        assertEquals(written.getTerrainFogLinearEnd(), 4000.25f);
        assertEquals(written.getTerrainFogMaxOpacity(), 0.75f);
        assertEquals(written.getTerrainFogHeight(), 80.0f);
        assertEquals(written.getWaterMinOpacity(), 4);
        assertEquals(written.getWaterMaxOpacity(), 96);
        assertEquals(written.getWaterReflectivity(), 12);
        assertEquals(written.getWaterEmissivity(), 3);
        assertEquals(written.getWaterEdgeSoftness(), 42);
        assertEquals(written.getWaterWavesVertexDisplacement(), 15);
        assertEquals(written.getWaterWavesNormalMapStrength(), 90);
        assertEquals(written.getWaterOverrideColor(), 112233);
        assertEquals(written.getWaterEnvMapReflectivity(), 77);
        assertEquals(written.getWaterUnknown(), -1);
        assertTrue(written.getFlag(MapFlag.USE_ALPHA_TILE_MINIMAP_COLOR));
        assertTrue(written.getFlag(MapFlag.USE_DYNAMIC_MINIMAP));
        assertTrue(written.getFlag(MapFlag.USE_WATER_OVERRIDE_COLOR));
        assertEquals(written.getPlayers().get(0).getHudSkin(), 64);
    }

    @Test
    public void configuredGameDataVersionRequiresATargetSupportingW3i39() throws Exception {
        Path project = Files.createTempDirectory("w3i-v3-target");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: 2.0\n");
        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        W3I w3i = new W3I();

        RuntimeException error = expectThrows(RuntimeException.class,
            () -> ProjectConfigBuilder.applyW3IVersion(config, w3i, false, true, ignored -> {}));

        assertTrue(error.getMessage().contains("v3ReforgedData and other Reforged 3 settings require Warcraft III 3.0 or newer"));
    }

    @Test
    public void configuredReforgedV3MetadataRequiresATargetSupportingReforged3() throws Exception {
        Path project = Files.createTempDirectory("w3i-v39-target");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: 2.0\n");
        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        W3I w3i = new W3I();
        WurstProjectBuildMapData mapData = new WurstProjectBuildMapData(
            "", "", "", null, null, List.of(), List.of(), null,
            new WurstProjectBuildV3ReforgedData(null, 2, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null)
        );

        assertTrue(ProjectConfigBuilder.requiresReforgedV3Data(mapData));

        RuntimeException error = expectThrows(RuntimeException.class,
            () -> ProjectConfigBuilder.applyW3IVersion(config, w3i, false,
                ProjectConfigBuilder.requiresReforgedV3Data(mapData), ignored -> {}));

        assertTrue(error.getMessage().contains("v3ReforgedData and other Reforged 3 settings require Warcraft III 3.0 or newer"));
        assertTrue(error.getMessage().contains("target 2.0 is older and cannot use these settings"));
    }

    @Test
    public void invalidConfiguredGameDataVersionHasAnActionableError() {
        WurstProjectBuildMapData mapData = new WurstProjectBuildMapData(
            "", "", "", null, null, List.of(), List.of(), "FUTURE_VERSION"
        );

        RuntimeException error = expectThrows(RuntimeException.class,
            () -> ProjectConfigBuilder.applyGameDataVersion(mapData, new W3I()));

        assertTrue(error.getMessage().contains("Supported values are ROC, TFT, and FORSAKEN_KINGDOM"));
    }

    private static void assertW3IVersion(String patch, int expected) throws Exception {
        Path project = Files.createTempDirectory("w3i-version");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: " + patch + "\n");
        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());

        Optional<String> warning = ProjectConfigBuilder.w3iDowngradeWarning(config, w3i);
        ProjectConfigBuilder.applyW3IVersion(config, w3i, false);

        assertEquals(w3i.getFileVersion(), expected, "unexpected W3I format for patch " + patch);
        assertEquals(warning.isPresent(), expected < W3I.EncodingFormat.W3I_0x27.getVersion(),
            "unexpected downgrade warning for patch " + patch);
    }
}
