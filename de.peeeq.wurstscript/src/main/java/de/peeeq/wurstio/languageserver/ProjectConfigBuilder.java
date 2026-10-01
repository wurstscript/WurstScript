package de.peeeq.wurstio.languageserver;

import com.google.common.io.Files;
import org.wurstscript.projectconfig.*;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.languageserver.requests.RequestFailedException;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.W3InstallationData;
import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.bin.app.MapFlag;
import net.moonlightflower.wc3libs.bin.app.MapHeader;
import net.moonlightflower.wc3libs.bin.app.W3I;
import net.moonlightflower.wc3libs.bin.app.W3I.Force;
import net.moonlightflower.wc3libs.bin.app.W3I.Player;
import net.moonlightflower.wc3libs.dataTypes.app.Controller;
import net.moonlightflower.wc3libs.dataTypes.app.LoadingScreenBackground;
import net.moonlightflower.wc3libs.port.GameVersion;
import org.apache.commons.lang.StringUtils;
import org.eclipse.lsp4j.MessageType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ProjectConfigBuilder {
    public static final String FILE_NAME = "wurst.build";

    /**
     * Apply project configuration with intelligent caching
     */
    public static MapRequest.CompilationResult apply(WurstProjectConfigData projectConfig, File targetMap,
                                                     File mapScript, File buildDir,
                                                     RunArgs runArgs, W3InstallationData w3data) throws IOException {
        return apply(projectConfig, targetMap, mapScript, buildDir, runArgs, w3data, MapRequest.BUILD_CONFIGURED_SCRIPT_NAME);
    }

    public static MapRequest.CompilationResult apply(WurstProjectConfigData projectConfig, File targetMap,
                                                     File mapScript, File buildDir,
                                                     RunArgs runArgs, W3InstallationData w3data,
                                                     String outputScriptName) throws IOException {
        return apply(projectConfig, targetMap, targetMap, mapScript, buildDir, runArgs, w3data, outputScriptName);
    }

    public static MapRequest.CompilationResult apply(WurstProjectConfigData projectConfig, File targetMap, File sourceMap,
                                                     File mapScript, File buildDir,
                                                     RunArgs runArgs, W3InstallationData w3data,
                                                     String outputScriptName) throws IOException {
        return apply(projectConfig, targetMap, sourceMap, mapScript, buildDir, runArgs, w3data, outputScriptName, ignored -> {});
    }

    public static MapRequest.CompilationResult apply(WurstProjectConfigData projectConfig, File targetMap, File sourceMap,
                                                     File mapScript, File buildDir,
                                                     RunArgs runArgs, W3InstallationData w3data,
                                                     String outputScriptName, Consumer<String> warningConsumer) throws IOException {
        if (projectConfig.projectName().isEmpty()) {
            throw new RequestFailedException(MessageType.Error, "wurst.build is missing projectName.");
        }

        WurstProjectBuildMapData buildMapData = projectConfig.buildMapData();
        MapRequest.CompilationResult result = new MapRequest.CompilationResult();
        result.script = mapScript;

        // Calculate hash of the project config for caching
        String configHash = calculateProjectConfigHash(projectConfig, buildDir);

        W3I w3I;
        boolean configNeedsApplying = false;

        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(targetMap), true)) {
            // Load the cache manifest
            Optional<ImportFile.CacheManifest> manifestOpt = ImportFile.getCachedManifest(mpq);

            // Check if we need to apply config
            if (manifestOpt.isPresent() && manifestOpt.get().mapConfigMatches(configHash)) {
                WLogger.info("Map configuration unchanged, skipping w3i injection");
                configNeedsApplying = false;
            } else {
                WLogger.info("Map configuration changed or not cached, applying config");
                configNeedsApplying = true;
            }

            // Start from the original map metadata. The cached map can already contain a
            // downgraded W3I from an earlier target patch, which would permanently lose
            // fields if a later build targets a newer patch or removes the pin.
            w3I = readW3I(sourceMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Apply metadata before regenerating script config: generated player setup is derived from W3I.
        boolean hasW3IOverrides = hasW3IOverrides(buildMapData);
        if (hasW3IOverrides) {
            prepareW3I(projectConfig, w3I);
        }
        applyW3IVersion(WurstBuildConfig.fromProject(projectConfig, null), w3I, runArgs.isLua(),
            requiresReforgedV3Data(buildMapData), warningConsumer);

        if (configNeedsApplying && hasW3IOverrides) {
            WLogger.info("Applying buildMapData config");
            applyBuildMapData(mapScript, buildDir, w3data, w3I, result, outputScriptName, runArgs.isLua());
        } else if (!configNeedsApplying) {
            WLogger.info("Using cached w3i configuration");
            // Prefer the previously-injected script (with correct config() body) over the
            // raw map script. If it doesn't exist yet (e.g. first Lua build after a JASS-only
            // cache), fall through to re-inject so the config() body is never stale.
            // Also re-inject if war3map.j was modified after the cached script was written.
            File cachedInjectedScript = new File(buildDir, outputScriptName);
            boolean cachedScriptStale = !cachedInjectedScript.exists()
                || mapScript.lastModified() > cachedInjectedScript.lastModified();
            if (!cachedScriptStale) {
                result.script = cachedInjectedScript;
            } else if (hasW3IOverrides) {
                WLogger.info("war3map.j changed or cached script missing, re-injecting config");
                applyBuildMapData(mapScript, buildDir, w3data, w3I, result, outputScriptName, runArgs.isLua());
            }
            // else result.script stays as mapScript (no configured W3I overrides)
        }

        result.w3i = new File(buildDir, "war3map.w3i");
        w3I.write(result.w3i, W3I.EncodingFormat.AS_DEFINED);

        // Apply map header (this is cheap, so we always do it)
        applyMapHeader(projectConfig, targetMap, w3I.getPlayers().size(), w3I.getMapName(), w3I.getFlags().toInt());

        // Update the manifest with new config hash (must open writable to insert)
        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(targetMap), false)) {
            ImportFile.CacheManifest manifest = ImportFile.getCachedManifest(mpq).orElse(new ImportFile.CacheManifest());
            manifest.setMapConfig(configHash);
            ImportFile.saveManifest(mpq, manifest);
        } catch (Exception e) {
            WLogger.warning("Could not update manifest with config hash: " + e.getMessage());
        }

        return result;
    }

    static W3I readW3I(File map) throws Exception {
        if (map.isDirectory()) {
            return new W3I(java.nio.file.Files.readAllBytes(new File(map, "war3map.w3i").toPath()));
        }
        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(map), true)) {
            return new W3I(mpq.extractFile("war3map.w3i"));
        }
    }

    /**
     * Calculate a hash of the project configuration to detect changes
     */
    private static String calculateProjectConfigHash(WurstProjectConfigData projectConfig, File buildDir) {
        try {
            // Serialize the relevant parts of the config
            StringBuilder sb = new StringBuilder();
            WurstProjectBuildMapData buildMapData = projectConfig.buildMapData();

            sb.append("name:").append(buildMapData.name()).append("\n");
            sb.append("author:").append(buildMapData.author()).append("\n");
            sb.append("gameDataVersion:").append(buildMapData.gameDataVersion()).append("\n");
            sb.append("v3ReforgedData:").append(buildMapData.v3ReforgedData()).append("\n");

            // Scenario data
            WurstProjectBuildScenarioData scenario = buildMapData.scenarioData();
            sb.append("suggestedPlayers:").append(scenario.suggestedPlayers()).append("\n");
            sb.append("description:").append(scenario.description()).append("\n");

            if (scenario.loadingScreen() != null) {
                WurstProjectBuildLoadingScreenData ls = scenario.loadingScreen();
                sb.append("loadingScreen.model:").append(ls.model()).append("\n");
                sb.append("loadingScreen.background:").append(ls.background()).append("\n");
                sb.append("loadingScreen.title:").append(ls.title()).append("\n");
                sb.append("loadingScreen.subtitle:").append(ls.subTitle()).append("\n");
                sb.append("loadingScreen.text:").append(ls.text()).append("\n");
            }

            // Players
            for (WurstProjectBuildPlayer player : buildMapData.players()) {
                sb.append("player:").append(player.id())
                    .append(",").append(player.name())
                    .append(",").append(player.race())
                    .append(",").append(player.controller())
                    .append(",").append(player.fixedStartLoc())
                    .append(",").append(player.hudSkin())
                    .append("\n");
            }

            // Forces
            for (WurstProjectBuildForce force : buildMapData.forces()) {
                sb.append("force:").append(force.name())
                    .append(",").append(force.flags().allied())
                    .append(",").append(force.flags().alliedVictory())
                    .append(",").append(force.flags().sharedVision())
                    .append(",").append(force.flags().sharedControl())
                    .append(",").append(force.flags().sharedControlAdvanced())
                    .append("players:");
                for (int id : force.playerIds()) {
                    sb.append(id).append(",");
                }
                sb.append("\n");
            }

            // Option flags
            WurstProjectBuildOptionFlagsData flags = buildMapData.optionsFlags();
            sb.append("flags:").append(flags.forcesFixed())
                .append(",").append(flags.showWavesOnCliffShores())
                .append(",").append(flags.showWavesOnRollingShores())
                .append(",").append(flags.useWaterOverrideColor())
                .append(",").append(flags.useAlphaTileMinimapColor())
                .append(",").append(flags.useDynamicMinimap())
                .append("\n");
            WurstBuildConfig buildConfig = buildConfigFromBuildDir(buildDir);
            sb.append("scriptMode:").append(buildConfig.scriptMode()).append("\n");
            sb.append("wc3Patch:").append(buildConfig.wc3PatchName()).append("\n");
            // Reapply metadata after compiler upgrades; W3I serialization rules can change
            // without any project configuration changing.
            sb.append("compilerVersion:").append(CompileTimeInfo.version).append("\n");

            return ImportFile.calculateHash(sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            WLogger.warning("Could not calculate config hash: " + e.getMessage());
            // Return a timestamp-based hash as fallback (will always trigger rebuild)
            return String.valueOf(System.currentTimeMillis());
        }
    }

    private static void applyBuildMapData(File mapScript, File buildDir, W3InstallationData w3data, W3I w3I,
                                          MapRequest.CompilationResult result, String outputScriptName,
                                          boolean isLua) throws IOException {
        result.script = new File(buildDir, outputScriptName);

        try (FileInputStream inputStream = new FileInputStream(mapScript)) {
            StringWriter sw = new StringWriter();

            WurstBuildConfig buildConfig = buildConfigFromBuildDir(buildDir);
            GameVersion version = effectiveConfigInjectionVersion(buildDir, w3data);
            if (buildConfig.configuredGameVersion().isPresent()) {
                WLogger.info("Using wurst.build patch target for map config injection: " + version);
            } else if (w3data.getWc3PatchVersion().isPresent()) {
                WLogger.info("Using detected game version for map config injection: " + version);
            } else {
                WLogger.info("Failed to determine installed game version. Falling back to default patch target: " + version);
            }
            if (isLua) {
                w3I.injectConfigsInLuaScript(inputStream, sw);
            } else {
                w3I.injectConfigsInJassScript(inputStream, sw, version);
            }

            byte[] scriptBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
            Files.write(scriptBytes, result.script);
        }
    }

    private static GameVersion effectiveConfigInjectionVersion(File buildDir, W3InstallationData w3data) {
        WurstBuildConfig buildConfig = buildConfigFromBuildDir(buildDir);
        return buildConfig.configuredGameVersion()
            .or(() -> w3data.getWc3PatchVersion())
            .orElseGet(buildConfig::fallbackGameVersion);
    }

    public static Optional<String> applyW3IVersion(WurstBuildConfig buildConfig, W3I w3I, boolean lua) {
        return applyW3IVersion(buildConfig, w3I, lua, ignored -> {});
    }

    static Optional<String> applyW3IVersion(WurstBuildConfig buildConfig, W3I w3I, boolean lua,
                                            Consumer<String> warningConsumer) {
        return applyW3IVersion(buildConfig, w3I, lua, false, warningConsumer);
    }

    static Optional<String> applyW3IVersion(WurstBuildConfig buildConfig, W3I w3I, boolean lua,
                                            boolean requiresW3IV39, Consumer<String> warningConsumer) {
        Optional<GameVersion> targetVersion = buildConfig.configuredGameVersion();
        if (lua && targetVersion.filter(version -> version.compareTo(new GameVersion("1.32")) < 0).isPresent()) {
            GameVersion version = targetVersion.orElseThrow();
            String targetName = buildConfig.wc3PatchName().orElse(version.toString());
            throw new RequestFailedException(MessageType.Error,
                "Cannot target Warcraft III " + targetName + " with Lua: Lua map scripts require Warcraft III 1.32 or newer.");
        }

        if (requiresW3IV39) {
            int v3Format = W3I.EncodingFormat.W3I_0x27.getVersion();
            if (targetVersion.filter(version -> maxW3IVersionFor(buildConfig, version) < v3Format).isPresent()) {
                String targetName = buildConfig.wc3PatchName().orElse(targetVersion.orElseThrow().toString());
                throw new RequestFailedException(MessageType.Error,
                    "wurst.build buildMapData.v3ReforgedData and other Reforged 3 settings require Warcraft III 3.0 or newer; target "
                        + targetName + " is older and cannot use these settings.");
            }
            // These settings are only present in W3I format 39. Promote older source maps so the
            // configured values are actually serialized instead of silently disappearing.
            w3I.setFileVersion(Math.max(w3I.getFileVersion(), v3Format));
        }

        Optional<String> downgradeWarning = w3iDowngradeWarning(buildConfig, w3I);
        downgradeWarning.ifPresent(warning -> {
            warningConsumer.accept(warning);
            w3I.setFileVersion(maxW3IVersionFor(buildConfig, targetVersion.orElseThrow()));
        });

        if (lua) {
            WLogger.info("Applying lua w3i config");
            w3I.setScriptLang(W3I.ScriptLang.LUA);
        }

        // Keep the source encoding unless it exceeds the selected target's supported format.
        // When build config adds fields that require a newer W3I format, promote the format
        // at the point where those fields are applied.
        if (lua && w3I.getFileVersion() < W3I.EncodingFormat.W3I_0x1F.getVersion()) {
            w3I.setFileVersion(W3I.EncodingFormat.W3I_0x1F.getVersion());
        }
        return downgradeWarning;
    }

    static Optional<String> w3iDowngradeWarning(WurstBuildConfig buildConfig, W3I w3I) {
        return buildConfig.configuredGameVersion()
            .filter(target -> w3I.getFileVersion() > maxW3IVersionFor(buildConfig, target))
            .map(target -> {
                int maxVersion = maxW3IVersionFor(buildConfig, target);
                String targetName = buildConfig.wc3PatchName().orElse(target.toString());
                return "The input map uses W3I format " + w3I.getFileVersion()
                    + ", newer than the selected Warcraft III target " + targetName
                    + " supports (format " + maxVersion + "). Wurst will downgrade it to format "
                    + maxVersion + "; information added in newer patches may be lost.";
            });
    }

    private static int maxW3IVersionFor(WurstBuildConfig buildConfig, GameVersion version) {
        if (buildConfig.isReignOfChaosTarget()) {
            return W3I.EncodingFormat.W3I_0x12.getVersion();
        }
        if (version.compareTo(new GameVersion("1.31")) < 0) {
            return W3I.EncodingFormat.W3I_0x19.getVersion();
        }
        if (version.compareTo(new GameVersion("1.32")) < 0) {
            return W3I.EncodingFormat.W3I_0x1C.getVersion();
        }
        if (version.compareTo(new GameVersion("2.0")) < 0) {
            return W3I.EncodingFormat.W3I_0x1F.getVersion();
        }
        if (version.compareTo(new GameVersion("3.0")) < 0) {
            return W3I.EncodingFormat.W3I_0x21.getVersion();
        }
        return W3I.EncodingFormat.W3I_0x27.getVersion();
    }

    private static WurstBuildConfig buildConfigFromBuildDir(File buildDir) {
        java.nio.file.Path projectRoot = buildDir.toPath().getParent();
        if (projectRoot == null) {
            projectRoot = java.nio.file.Path.of(".");
        }
        return WurstBuildConfig.fromBuildFile(projectRoot.resolve(FILE_NAME));
    }


    static void prepareW3I(WurstProjectConfigData projectConfig, W3I w3I) {
        WurstProjectBuildMapData buildMapData = projectConfig.buildMapData();
        applyGameDataVersion(buildMapData, w3I);
        applyReforgedV3Data(buildMapData.v3ReforgedData(), w3I);
        if (StringUtils.isNotBlank(buildMapData.name())) {
            w3I.setMapName(buildMapData.name());
        }
        if (StringUtils.isNotBlank(buildMapData.author())) {
            w3I.setMapAuthor(buildMapData.author());
        }
        applyScenarioData(w3I, buildMapData);

        if (!buildMapData.players().isEmpty()) {
            applyPlayers(projectConfig, w3I);
        }
        if (!buildMapData.forces().isEmpty()) {
            applyForces(projectConfig, w3I);
        }
        applyOptionFlags(projectConfig, w3I);
    }

    static boolean hasW3IOverrides(WurstProjectBuildMapData data) {
        WurstProjectBuildOptionFlagsData flags = data.optionsFlags();
        WurstProjectBuildScenarioData scenario = data.scenarioData();
        WurstProjectBuildLoadingScreenData loadingScreen = scenario.loadingScreen();
        return StringUtils.isNotBlank(data.name())
            || StringUtils.isNotBlank(data.author())
            || StringUtils.isNotBlank(data.gameDataVersion())
            || data.v3ReforgedData().isConfigured()
            || !data.players().isEmpty()
            || !data.forces().isEmpty()
            || StringUtils.isNotBlank(scenario.description())
            || StringUtils.isNotBlank(scenario.suggestedPlayers())
            || (loadingScreen != null && (StringUtils.isNotBlank(loadingScreen.model())
                || StringUtils.isNotBlank(loadingScreen.background())
                || StringUtils.isNotBlank(loadingScreen.title())
                || StringUtils.isNotBlank(loadingScreen.subTitle())
                || StringUtils.isNotBlank(loadingScreen.text())))
            || flags.hideMinimapPreview()
            || flags.forcesFixed()
            || flags.maskedAreasPartiallyVisible()
            || flags.showWavesOnCliffShores()
            || flags.showWavesOnRollingShores()
            || flags.useItemClassificationSystem()
            || flags.useAlphaTileMinimapColor()
            || flags.useDynamicMinimap()
            || flags.useWaterOverrideColor();
    }

    static boolean requiresReforgedV3Data(WurstProjectBuildMapData data) {
        return StringUtils.isNotBlank(data.gameDataVersion())
            || data.v3ReforgedData().isConfigured()
            || data.players().stream().anyMatch(player -> player.hudSkin() != null)
            || data.optionsFlags().useAlphaTileMinimapColor()
            || data.optionsFlags().useDynamicMinimap()
            || data.optionsFlags().useWaterOverrideColor();
    }

    private static void applyReforgedV3Data(WurstProjectBuildV3ReforgedData data, W3I w3I) {
        if (data.loadingScreenCrestRace() != null) w3I.setLoadingScreenCrestRace(data.loadingScreenCrestRace());
        if (data.terrainFogStyle() != null) w3I.setTerrainFogStyle(data.terrainFogStyle());
        if (data.drawTerrainFogOverSky() != null) w3I.setDrawTerrainFogOverSky(data.drawTerrainFogOverSky());
        if (data.terrainFogLinearStart() != null) w3I.setTerrainFogLinearStart(data.terrainFogLinearStart());
        if (data.terrainFogLinearEnd() != null) w3I.setTerrainFogLinearEnd(data.terrainFogLinearEnd());
        if (data.terrainFogMaxOpacity() != null) w3I.setTerrainFogMaxOpacity(data.terrainFogMaxOpacity());
        if (data.terrainFogHeight() != null) w3I.setTerrainFogHeight(data.terrainFogHeight());
        if (data.waterMinOpacity() != null) w3I.setWaterMinOpacity(data.waterMinOpacity());
        if (data.waterMaxOpacity() != null) w3I.setWaterMaxOpacity(data.waterMaxOpacity());
        if (data.waterReflectivity() != null) w3I.setWaterReflectivity(data.waterReflectivity());
        if (data.waterEmissivity() != null) w3I.setWaterEmissivity(data.waterEmissivity());
        if (data.waterEdgeSoftness() != null) w3I.setWaterEdgeSoftness(data.waterEdgeSoftness());
        if (data.waterWavesVertexDisplacement() != null) w3I.setWaterWavesVertexDisplacement(data.waterWavesVertexDisplacement());
        if (data.waterWavesNormalMapStrength() != null) w3I.setWaterWavesNormalMapStrength(data.waterWavesNormalMapStrength());
        if (data.waterOverrideColor() != null) w3I.setWaterOverrideColor(data.waterOverrideColor());
        if (data.waterEnvMapReflectivity() != null) w3I.setWaterEnvMapReflectivity(data.waterEnvMapReflectivity());
        if (data.waterUnknown() != null) w3I.setWaterUnknown(data.waterUnknown());
    }

    static void applyGameDataVersion(WurstProjectBuildMapData buildMapData, W3I w3I) {
        if (StringUtils.isBlank(buildMapData.gameDataVersion())) {
            return;
        }
        try {
            W3I.GameDataVersion version = W3I.GameDataVersion.valueOf(
                buildMapData.gameDataVersion().trim().toUpperCase(java.util.Locale.ROOT));
            if (version == W3I.GameDataVersion.UNKNOWN) {
                throw new IllegalArgumentException("Unknown W3I game-data version");
            }
            w3I.setGameDataVersion(version);
        } catch (IllegalArgumentException e) {
            throw new RequestFailedException(MessageType.Error,
                "Invalid wurst.build buildMapData.gameDataVersion '" + buildMapData.gameDataVersion()
                    + "'. Supported values are ROC, TFT, and FORSAKEN_KINGDOM.");
        }
    }

    private static void applyOptionFlags(WurstProjectConfigData projectConfig, W3I w3I) {
        WurstProjectBuildOptionFlagsData optionsFlags = projectConfig.buildMapData().optionsFlags();
        w3I.setFlag(MapFlag.HIDE_MINIMAP, optionsFlags.forcesFixed() || w3I.getFlag(MapFlag.HIDE_MINIMAP));
        w3I.setFlag(MapFlag.FIXED_PLAYER_FORCE_SETTING, optionsFlags.forcesFixed() || w3I.getFlag(MapFlag.FIXED_PLAYER_FORCE_SETTING));
        w3I.setFlag(MapFlag.MASKED_AREAS_PARTIALLY_VISIBLE, optionsFlags.forcesFixed() || w3I.getFlag(MapFlag.MASKED_AREAS_PARTIALLY_VISIBLE));
        w3I.setFlag(MapFlag.SHOW_WATER_WAVES_ON_CLIFF_SHORES, optionsFlags.showWavesOnCliffShores() || w3I.getFlag(MapFlag.SHOW_WATER_WAVES_ON_CLIFF_SHORES));
        w3I.setFlag(MapFlag.SHOW_WATER_WAVES_ON_ROLLING_SHORES, optionsFlags.showWavesOnRollingShores() || w3I.getFlag(MapFlag.SHOW_WATER_WAVES_ON_ROLLING_SHORES));
        w3I.setFlag(MapFlag.USE_ITEM_CLASSIFICATION_SYSTEM, optionsFlags.useItemClassificationSystem() || w3I.getFlag(MapFlag.USE_ITEM_CLASSIFICATION_SYSTEM));
        w3I.setFlag(MapFlag.USE_ALPHA_TILE_MINIMAP_COLOR, optionsFlags.useAlphaTileMinimapColor() || w3I.getFlag(MapFlag.USE_ALPHA_TILE_MINIMAP_COLOR));
        w3I.setFlag(MapFlag.USE_DYNAMIC_MINIMAP, optionsFlags.useDynamicMinimap() || w3I.getFlag(MapFlag.USE_DYNAMIC_MINIMAP));
        w3I.setFlag(MapFlag.USE_WATER_OVERRIDE_COLOR, optionsFlags.useWaterOverrideColor() || w3I.getFlag(MapFlag.USE_WATER_OVERRIDE_COLOR));
    }

    private static void applyScenarioData(W3I w3I, WurstProjectBuildMapData buildMapData) {
        WurstProjectBuildScenarioData scenarioData = buildMapData.scenarioData();
        if (StringUtils.isNotBlank(scenarioData.suggestedPlayers())) {
            w3I.setPlayersRecommendedAmount(scenarioData.suggestedPlayers());
        }
        if (StringUtils.isNotBlank(scenarioData.description())) {
            w3I.setMapDescription(scenarioData.description());
        }
        if (scenarioData.loadingScreen() != null) {
            applyLoadingScreen(w3I, scenarioData.loadingScreen());
        }
    }

    private static void applyLoadingScreen(W3I w3I, WurstProjectBuildLoadingScreenData loadingScreenData) {
        if (StringUtils.isNotBlank(loadingScreenData.model())) {
            w3I.getLoadingScreen().setBackground(new LoadingScreenBackground.CustomBackground(new File(loadingScreenData.model())));
        } else if (StringUtils.isNotBlank(loadingScreenData.background())) {
            w3I.getLoadingScreen().setBackground(LoadingScreenBackground.PresetBackground.findByName(loadingScreenData.background()));
        }

        w3I.getLoadingScreen().setTitle(loadingScreenData.title());
        w3I.getLoadingScreen().setSubtitle(loadingScreenData.subTitle());
        w3I.getLoadingScreen().setText(loadingScreenData.text());
    }

    private static void applyForces(WurstProjectConfigData projectConfig, W3I w3I) {
        w3I.clearForces();
        List<WurstProjectBuildForce> forces = projectConfig.buildMapData().forces();
        for (WurstProjectBuildForce wforce : forces) {
            W3I.Force force = new Force();
            force.setName(wforce.name());
            force.setFlag(W3I.Force.Flags.Flag.ALLIED, wforce.flags().allied());
            force.setFlag(W3I.Force.Flags.Flag.ALLIED_VICTORY, wforce.flags().alliedVictory());
            force.setFlag(W3I.Force.Flags.Flag.SHARED_VISION, wforce.flags().sharedVision());
            force.setFlag(W3I.Force.Flags.Flag.SHARED_UNIT_CONTROL, wforce.flags().sharedControl());
            force.setFlag(W3I.Force.Flags.Flag.SHARED_UNIT_CONTROL_ADVANCED, wforce.flags().sharedControlAdvanced());
            force.addPlayerNums(wforce.playerIds().stream().mapToInt(Integer::intValue).toArray());
            w3I.addForce(force);
        }
        w3I.setFlag(MapFlag.USE_CUSTOM_FORCES, true);
    }

    private static void applyPlayers(WurstProjectConfigData projectConfig, W3I w3I) {
        List<W3I.Player> existing = new ArrayList<>(w3I.getPlayers());
        w3I.getPlayers().clear();
        List<WurstProjectBuildPlayer> players = projectConfig.buildMapData().players();
        for (WurstProjectBuildPlayer wplayer : players) {
            Optional<Player> old = Optional.empty();
            for (Player player2 : existing) {
                if (player2.getNum() == wplayer.id()) {
                    old = Optional.of(player2);
                    break;
                }
            }
            W3I.Player player = new Player();
            player.setNum(wplayer.id());
            w3I.addPlayer(player);

            old.ifPresent(player1 -> applyExistingPlayerConfig(player1, player));

            setVolatilePlayerConfig(wplayer, player);
        }
    }

    private static void applyExistingPlayerConfig(W3I.Player oldPlayer, W3I.Player player) {
        player.setStartPos(oldPlayer.getStartPos());
        player.setName(oldPlayer.getName());
        player.setRace(oldPlayer.getRace());
        player.setType(oldPlayer.getType());
        player.setStartPosFixed(oldPlayer.getStartPosFixed());
        player.setAllyLowPrioFlags(oldPlayer.getAllyLowPrioFlags());
        player.setAllyHighPrioFlags(oldPlayer.getAllyHighPrioFlags());
        player.setEnemyLowPrioFlags(oldPlayer.getEnemyLowPrioFlags());
        player.setEnemyHighPrioFlags(oldPlayer.getEnemyHighPrioFlags());
        player.setHudSkin(oldPlayer.getHudSkin());
    }

    private static void setVolatilePlayerConfig(WurstProjectBuildPlayer wplayer, W3I.Player player) {
        if (wplayer.name() != null) {
            player.setName(wplayer.name());
        }

        if (wplayer.race() != null) {
            W3I.Player.UnitRace val = W3I.Player.UnitRace.valueOf(wplayer.race().toString());
            if (val != null) {
                player.setRace(val);
            }
        }
        if (wplayer.controller() != null) {
            net.moonlightflower.wc3libs.dataTypes.app.Controller val1 = Controller.valueOf(wplayer.controller().toString());
            if (val1 != null) {
                player.setType(val1);
            }
        }
        if (wplayer.fixedStartLoc() != null) {
            player.setStartPosFixed(wplayer.fixedStartLoc() ? 1 : 0);
        }
        if (wplayer.hudSkin() != null) {
            player.setHudSkin(wplayer.hudSkin());
        }
    }

    private static void applyMapHeader(WurstProjectConfigData projectConfig, File targetMap,
                                       int existingPlayerCount, String existingMapName,
                                       int existingMapFlags) throws IOException {
        boolean shouldWrite = false;
        WurstProjectBuildMapData buildMapData = projectConfig.buildMapData();
        if (buildMapData.players().isEmpty() && StringUtils.isBlank(buildMapData.name())) {
            return;
        }

        // A Warcraft III map may omit the optional 512-byte HM3W prefix and start
        // directly with its MPQ archive. MapHeader.ofFile only reads the prefix,
        // so use a new header in that case; writeToMapFile will insert it before
        // the archive.
        boolean hasNoMapHeader = startsWithMpqArchive(targetMap);
        MapHeader mapHeader = hasNoMapHeader
            ? new MapHeader()
            : MapHeader.ofFile(targetMap);
        if (!buildMapData.players().isEmpty()) {
            mapHeader.setMaxPlayersCount(buildMapData.players().size());
            shouldWrite = true;
        } else if (hasNoMapHeader) {
            mapHeader.setMaxPlayersCount(existingPlayerCount);
        }
        if (hasNoMapHeader && StringUtils.isBlank(buildMapData.name())) {
            mapHeader.setMapName(existingMapName);
        }
        if (hasNoMapHeader) {
            mapHeader.setFlags(existingMapFlags);
        }
        if (StringUtils.isNotBlank(buildMapData.name())) {
            mapHeader.setMapName(buildMapData.name());
            shouldWrite = true;
        }
        if (shouldWrite) {
            WLogger.info("Applying map header");
            mapHeader.writeToMapFile(targetMap);
        }
    }

    private static boolean startsWithMpqArchive(File targetMap) throws IOException {
        try (InputStream input = new FileInputStream(targetMap)) {
            byte[] startToken = input.readNBytes(4);
            return startToken.length == 4
                && new String(startToken, StandardCharsets.US_ASCII).startsWith("MPQ");
        }
    }
}
