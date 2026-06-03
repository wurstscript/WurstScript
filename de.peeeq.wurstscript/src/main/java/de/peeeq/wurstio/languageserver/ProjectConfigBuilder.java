package de.peeeq.wurstio.languageserver;

import com.google.common.io.Files;
import org.wurstscript.projectconfig.*;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.languageserver.requests.RequestFailedException;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.W3InstallationData;
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

            // Extract w3i
            w3I = new W3I(mpq.extractFile("war3map.w3i"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Only apply buildMapData if config changed or name is present
        if (configNeedsApplying && StringUtils.isNotBlank(buildMapData.name())) {
            WLogger.info("Applying buildMapData config");
            applyBuildMapData(projectConfig, mapScript, buildDir, w3data, w3I, result, configHash, outputScriptName);
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
            } else if (StringUtils.isNotBlank(buildMapData.name())) {
                WLogger.info("war3map.j changed or cached script missing, re-injecting config");
                applyBuildMapData(projectConfig, mapScript, buildDir, w3data, w3I, result, configHash, outputScriptName);
            }
            // else result.script stays as mapScript (no wurst.build name configured)
        }

        result.w3i = new File(buildDir, "war3map.w3i");
        if (runArgs.isLua()) {
            WLogger.info("Applying lua w3i config");
            w3I.setScriptLang(W3I.ScriptLang.LUA);
            w3I.setFileVersion(W3I.EncodingFormat.W3I_0x1F.getVersion());
        }
        w3I.write(result.w3i);

        // Apply map header (this is cheap, so we always do it)
        applyMapHeader(projectConfig, targetMap);

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
                .append("\n");
            WurstBuildConfig buildConfig = buildConfigFromBuildDir(buildDir);
            sb.append("scriptMode:").append(buildConfig.scriptMode()).append("\n");
            sb.append("wc3Patch:").append(buildConfig.wc3PatchName()).append("\n");

            return ImportFile.calculateHash(sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            WLogger.warning("Could not calculate config hash: " + e.getMessage());
            // Return a timestamp-based hash as fallback (will always trigger rebuild)
            return String.valueOf(System.currentTimeMillis());
        }
    }

    private static void applyBuildMapData(WurstProjectConfigData projectConfig, File mapScript, File buildDir,
                                          W3InstallationData w3data, W3I w3I, MapRequest.CompilationResult result,
                                          String configHash, String outputScriptName) throws IOException {
        // Apply w3i config values
        prepareW3I(projectConfig, w3I);
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
            w3I.injectConfigsInJassScript(inputStream, sw, version);

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

    private static WurstBuildConfig buildConfigFromBuildDir(File buildDir) {
        java.nio.file.Path projectRoot = buildDir.toPath().getParent();
        if (projectRoot == null) {
            projectRoot = java.nio.file.Path.of(".");
        }
        return WurstBuildConfig.fromBuildFile(projectRoot.resolve(FILE_NAME));
    }


    private static void prepareW3I(WurstProjectConfigData projectConfig, W3I w3I) {
        WurstProjectBuildMapData buildMapData = projectConfig.buildMapData();
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

    private static void applyOptionFlags(WurstProjectConfigData projectConfig, W3I w3I) {
        WurstProjectBuildOptionFlagsData optionsFlags = projectConfig.buildMapData().optionsFlags();
        w3I.setFlag(MapFlag.HIDE_MINIMAP, optionsFlags.forcesFixed() || w3I.getFlag(MapFlag.HIDE_MINIMAP));
        w3I.setFlag(MapFlag.FIXED_PLAYER_FORCE_SETTING, optionsFlags.forcesFixed() || w3I.getFlag(MapFlag.FIXED_PLAYER_FORCE_SETTING));
        w3I.setFlag(MapFlag.MASKED_AREAS_PARTIALLY_VISIBLE, optionsFlags.forcesFixed() || w3I.getFlag(MapFlag.MASKED_AREAS_PARTIALLY_VISIBLE));
        w3I.setFlag(MapFlag.SHOW_WATER_WAVES_ON_CLIFF_SHORES, optionsFlags.showWavesOnCliffShores() || w3I.getFlag(MapFlag.SHOW_WATER_WAVES_ON_CLIFF_SHORES));
        w3I.setFlag(MapFlag.SHOW_WATER_WAVES_ON_ROLLING_SHORES, optionsFlags.showWavesOnRollingShores() || w3I.getFlag(MapFlag.SHOW_WATER_WAVES_ON_ROLLING_SHORES));
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
    }

    private static void applyMapHeader(WurstProjectConfigData projectConfig, File targetMap) throws IOException {
        boolean shouldWrite = false;
        MapHeader mapHeader = MapHeader.ofFile(targetMap);
        if (!projectConfig.buildMapData().players().isEmpty()) {
            mapHeader.setMaxPlayersCount(projectConfig.buildMapData().players().size());
            shouldWrite = true;
        }
        if (StringUtils.isNotBlank(projectConfig.buildMapData().name())) {
            mapHeader.setMapName(projectConfig.buildMapData().name());
            shouldWrite = true;
        }
        if (shouldWrite) {
            WLogger.info("Applying map header");
            mapHeader.writeToMapFile(targetMap);
        }
    }
}
