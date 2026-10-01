package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.W3InstallationData;
import de.peeeq.wurstscript.RunArgs;
import net.moonlightflower.wc3libs.bin.app.MapFlag;
import net.moonlightflower.wc3libs.bin.app.W3I;
import net.moonlightflower.wc3libs.dataTypes.app.Coords2DF;
import net.moonlightflower.wc3libs.port.GameVersion;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.wurstscript.projectconfig.WurstProjectConfigReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Exercises the full wurst.build -> ProjectConfigBuilder -> packed MPQ output path. */
public class WurstBuildMapOutputTests {
    private static final String SCRIPT_WITH_OLD_CONFIG = """
        function config takes nothing returns nothing
            call SetTeams(99)
        endfunction
        function main takes nothing returns nothing
        endfunction
        """;

    @Test
    public void fullWurstBuildConfigProducesCorrectW3iAndJassAndLuaConfigScripts() throws Exception {
        for (boolean lua : new boolean[]{false, true}) {
            Path projectRoot = Files.createTempDirectory("wurst-build-map-output");
            Path buildFile = projectRoot.resolve(ProjectConfigBuilder.FILE_NAME);
            Files.writeString(buildFile, completeBuildConfig(lua), StandardCharsets.UTF_8);
            WurstProjectConfigData projectConfig = WurstProjectConfigReader.load(buildFile);
            assertEquals(projectConfig.projectName(), "FullConfigFixture");
            assertEquals(projectConfig.dependencies(), java.util.List.of("fixture-dependency"));
            assertEquals(projectConfig.scriptMode().name(), lua ? "LUA" : "JASS");
            assertEquals(projectConfig.buildMapData().fileName(), "full-config-fixture");
            assertEquals(projectConfig.buildMapData().players().size(), 24);

            Path mapPath = projectRoot.resolve("input.w3x");
            createInputMap(mapPath);
            Path buildDir = Files.createDirectories(projectRoot.resolve("_build"));
            Path sourceScript = projectRoot.resolve(lua ? "war3map.lua" : "war3map.j");
            Files.writeString(sourceScript, lua ? luaScriptWithOldConfig() : SCRIPT_WITH_OLD_CONFIG,
                StandardCharsets.UTF_8);

            String outputScriptName = lua ? "war3map.lua" : "war3map.j";
            var result = ProjectConfigBuilder.apply(
                projectConfig,
                mapPath.toFile(),
                mapPath.toFile(),
                sourceScript.toFile(),
                buildDir.toFile(),
                new RunArgs(lua ? "-lua" : ""),
                new W3InstallationData(Optional.empty(), Optional.of(new GameVersion("3.0"))),
                outputScriptName
            );

            // Match MapRequest.injectMapData: put the generated script and binary W3I into the output MPQ.
            try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(mapPath.toFile()))) {
                mpq.insertFile(outputScriptName, result.script);
                mpq.insertFile("war3map.w3i", result.w3i);
            }

            byte[] outputW3iBytes;
            byte[] outputScriptBytes;
            try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(mapPath.toFile()), true)) {
                outputW3iBytes = mpq.extractFile("war3map.w3i");
                outputScriptBytes = mpq.extractFile(outputScriptName);
            }

            W3I outputW3i = new W3I(outputW3iBytes);
            assertW3i(outputW3i);
            assertConfigScript(new String(outputScriptBytes, StandardCharsets.UTF_8), lua);
        }
    }

    private static void createInputMap(Path mapPath) throws Exception {
        MpqEditorFactory.createEmptyArchive(mapPath.toFile());
        W3I inputW3i = new W3I();
        inputW3i.setFileVersion(W3I.EncodingFormat.W3I_0x21.getVersion());
        for (int id = 0; id < 24; id++) {
            W3I.Player player = new W3I.Player();
            player.setNum(id);
            player.setName("Input " + id);
            player.setStartPos(new Coords2DF(id * 128f, id * -64f));
            inputW3i.addPlayer(player);
        }
        Path w3iPath = Files.createTempFile("wurst-build-input", ".w3i");
        inputW3i.write(w3iPath.toFile(), W3I.EncodingFormat.AS_DEFINED);
        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(mapPath.toFile()))) {
            mpq.insertFile("war3map.w3i", w3iPath.toFile());
            mpq.insertFile("war3map.j", SCRIPT_WITH_OLD_CONFIG.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String completeBuildConfig(boolean lua) {
        StringBuilder yaml = new StringBuilder("""
            projectName: FullConfigFixture
            dependencies:
              - fixture-dependency
            scriptMode: %s
            wc3Patch: 3.0
            buildMapData:
              name: "Wurst build output fixture"
              fileName: full-config-fixture
              author: "Wurst test suite"
              gameDataVersion: FORSAKEN_KINGDOM
              scenarioData:
                description: "Every setting is present."
                suggestedPlayers: "24"
                loadingScreen:
                  background: ORC
                  title: "Loading title"
                  subTitle: "Loading subtitle"
                  text: "Loading screen text"
              optionsFlags:
                hideMinimapPreview: true
                forcesFixed: true
                maskedAreasPartiallyVisible: true
                showWavesOnCliffShores: true
                showWavesOnRollingShores: true
                useItemClassificationSystem: true
                useAlphaTileMinimapColor: true
                useDynamicMinimap: true
                useWaterOverrideColor: true
              v3ReforgedData:
                loadingScreenCrestRace: 4
                terrainFogStyle: 2
                drawTerrainFogOverSky: true
                terrainFogLinearStart: 1200.5
                terrainFogLinearEnd: 4000.25
                terrainFogMaxOpacity: 0.75
                terrainFogHeight: 80.0
                waterMinOpacity: 4
                waterMaxOpacity: 96
                waterReflectivity: 12
                waterEmissivity: 3
                waterEdgeSoftness: 42
                waterWavesVertexDisplacement: 15
                waterWavesNormalMapStrength: 90
                waterOverrideColor: 112233
                waterEnvMapReflectivity: 77
                waterUnknown: -1
              players:
            """.formatted(lua ? "LUA" : "JASS"));

        String[] races = {"HUMAN", "ORC", "UNDEAD", "NIGHT_ELF", "SELECTABLE"};
        String[] controllers = {"USER", "COMPUTER", "NEUTRAL", "RESCUABLE"};
        for (int id = 0; id < 24; id++) {
            yaml.append("    - id: ").append(id).append('\n')
                .append("      name: \"Configured Player ").append(String.format("%02d", id)).append("\"\n")
                .append("      race: ").append(races[id % races.length]).append('\n')
                .append("      controller: ").append(controllers[id % controllers.length]).append('\n')
                .append("      fixedStartLoc: ").append(id % 2 == 0).append('\n')
                .append("      hudSkin: ").append(64 + id).append('\n');
        }

        yaml.append("  forces:\n");
        for (int force = 0; force < 4; force++) {
            int firstPlayer = force * 6;
            yaml.append("    - name: \"Force ").append(force + 1).append("\"\n")
                .append("      flags:\n")
                .append("        allied: ").append(force != 3).append('\n')
                .append("        alliedVictory: ").append(force % 2 == 0).append('\n')
                .append("        sharedVision: ").append(force != 1).append('\n')
                .append("        sharedControl: ").append(force == 1).append('\n')
                .append("        sharedControlAdvanced: ").append(force == 2).append('\n')
                .append("      playerIds: [");
            for (int offset = 0; offset < 6; offset++) {
                if (offset > 0) yaml.append(", ");
                yaml.append(firstPlayer + offset);
            }
            yaml.append("]\n");
        }
        return yaml.toString();
    }

    private static String luaScriptWithOldConfig() {
        return "function config()\n    SetTeams(99)\nend\nfunction main()\nend\n";
    }

    private static void assertW3i(W3I w3i) {
        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
        assertEquals(w3i.getMapName(), "Wurst build output fixture");
        assertEquals(w3i.getMapAuthor(), "Wurst test suite");
        assertEquals(w3i.getMapDescription(), "Every setting is present.");
        assertEquals(w3i.getPlayersRecommendedAmount(), "24");
        assertEquals(w3i.getGameDataVersion(), W3I.GameDataVersion.FORSAKEN_KINGDOM);
        assertEquals(w3i.getPlayers().size(), 24);
        String[] races = {"HUMAN", "ORC", "UNDEAD", "NIGHT_ELF", "SELECTABLE"};
        String[] controllers = {"USER", "COMPUTER", "NEUTRAL", "RESCUABLE"};
        for (int id = 0; id < 24; id++) {
            W3I.Player player = w3i.getPlayers().get(id);
            assertEquals(player.getNum(), id);
            assertEquals(player.getName(), "Configured Player " + String.format("%02d", id));
            assertEquals(player.getHudSkin(), 64 + id);
            assertEquals(player.getStartPos().getX().getVal(), id * 128f, 0f);
            assertEquals(player.getStartPos().getY().getVal(), id * -64f, 0f);
            assertEquals(player.getStartPosFixed(), id % 2 == 0 ? 1 : 0);
            assertEquals(player.getRace(), W3I.Player.UnitRace.valueOf(races[id % races.length]));
            assertEquals(player.getType(), net.moonlightflower.wc3libs.dataTypes.app.Controller.valueOf(
                controllers[id % controllers.length]));
        }

        assertEquals(w3i.getForces().size(), 4);
        for (int forceIndex = 0; forceIndex < 4; forceIndex++) {
            W3I.Force force = w3i.getForces().get(forceIndex);
            assertEquals(force.getName(), "Force " + (forceIndex + 1));
            assertEquals(force.getPlayerNums().size(), 6);
            assertEquals(force.getFlag(W3I.Force.Flags.Flag.ALLIED), forceIndex != 3);
            assertEquals(force.getFlag(W3I.Force.Flags.Flag.ALLIED_VICTORY), forceIndex % 2 == 0);
            assertEquals(force.getFlag(W3I.Force.Flags.Flag.SHARED_VISION), forceIndex != 1);
            assertEquals(force.getFlag(W3I.Force.Flags.Flag.SHARED_UNIT_CONTROL), forceIndex == 1);
            assertEquals(force.getFlag(W3I.Force.Flags.Flag.SHARED_UNIT_CONTROL_ADVANCED), forceIndex == 2);
            for (int id = forceIndex * 6; id < forceIndex * 6 + 6; id++) {
                assertTrue(force.getPlayerNums().contains(id));
            }
        }

        assertTrue(w3i.getFlag(MapFlag.HIDE_MINIMAP));
        assertTrue(w3i.getFlag(MapFlag.FIXED_PLAYER_FORCE_SETTING));
        assertTrue(w3i.getFlag(MapFlag.MASKED_AREAS_PARTIALLY_VISIBLE));
        assertTrue(w3i.getFlag(MapFlag.SHOW_WATER_WAVES_ON_CLIFF_SHORES));
        assertTrue(w3i.getFlag(MapFlag.SHOW_WATER_WAVES_ON_ROLLING_SHORES));
        assertTrue(w3i.getFlag(MapFlag.USE_ITEM_CLASSIFICATION_SYSTEM));
        assertTrue(w3i.getFlag(MapFlag.USE_ALPHA_TILE_MINIMAP_COLOR));
        assertTrue(w3i.getFlag(MapFlag.USE_DYNAMIC_MINIMAP));
        assertTrue(w3i.getFlag(MapFlag.USE_WATER_OVERRIDE_COLOR));
        assertTrue(w3i.getFlag(MapFlag.USE_CUSTOM_FORCES));
        assertEquals(w3i.getLoadingScreenCrestRace(), 4);
        assertEquals(w3i.getTerrainFogStyle(), 2);
        assertTrue(w3i.getDrawTerrainFogOverSky());
        assertEquals(w3i.getTerrainFogLinearStart(), 1200.5f);
        assertEquals(w3i.getTerrainFogLinearEnd(), 4000.25f);
        assertEquals(w3i.getTerrainFogMaxOpacity(), 0.75f);
        assertEquals(w3i.getTerrainFogHeight(), 80f);
        assertEquals(w3i.getWaterMinOpacity(), 4);
        assertEquals(w3i.getWaterMaxOpacity(), 96);
        assertEquals(w3i.getWaterReflectivity(), 12);
        assertEquals(w3i.getWaterEmissivity(), 3);
        assertEquals(w3i.getWaterEdgeSoftness(), 42);
        assertEquals(w3i.getWaterWavesVertexDisplacement(), 15);
        assertEquals(w3i.getWaterWavesNormalMapStrength(), 90);
        assertEquals(w3i.getWaterOverrideColor(), 112233);
        assertEquals(w3i.getWaterEnvMapReflectivity(), 77);
        assertEquals(w3i.getWaterUnknown(), -1);
        assertEquals(w3i.getLoadingScreen().getTitle(), "Loading title");
        assertEquals(w3i.getLoadingScreen().getSubtitle(), "Loading subtitle");
        assertEquals(w3i.getLoadingScreen().getText(), "Loading screen text");
        assertEquals(w3i.getLoadingScreen().getBackground().toString(), "WESTRING_CAMPAIGNSCREEN_ORC");
    }

    private static void assertConfigScript(String script, boolean lua) {
        String compact = script.replaceAll("\\s+", "");
        assertFalse(compact.contains("SetTeams(99)"), "stale config() body must be replaced");
        assertTrue(compact.contains(lua ? "SetTeams(24)" : "callSetTeams(24)"), compact);
        assertTrue(compact.contains(lua ? "SetPlayerRaceSkin(Player(0)," : "callSetPlayerRaceSkin(Player(0),"), compact);
        assertTrue(compact.contains(lua ? "SetPlayerRaceSkin(Player(23)," : "callSetPlayerRaceSkin(Player(23),"), compact);
        assertTrue(compact.contains("SetPlayerAllianceStateControlBJ(Player(6),Player(7),true)"), compact);
        assertTrue(compact.contains("SetPlayerAllianceStateFullControlBJ(Player(12),Player(13),true)"), compact);
        assertTrue(compact.contains("SetPlayerState(Player(0),PLAYER_STATE_ALLIED_VICTORY,1)"), compact);
        assertTrue(compact.contains("SetPlayerAllianceStateVisionBJ(Player(0),Player(1),true)"), compact);
        assertTrue(compact.contains("SetGamePlacement(MAP_PLACEMENT_TEAMS_TOGETHER)"), compact);
        for (int id = 0; id < 24; id++) {
            assertTrue(Pattern.compile("(?:call)?DefineStartLocation\\(" + id + ",").matcher(compact).find(),
                "missing generated start-location call for player " + id + ":\n" + script);
            assertTrue(compact.contains("Player(" + id + ")"), "missing generated player setup for " + id);
            assertTrue(compact.contains("SetPlayerRaceSkin(Player(" + id + "),ConvertRacePref(" + (64 + id) + "))"),
                "wrong/missing configured HUD skin for player " + id + ":\n" + script);
        }
        assertTrue(compact.contains("SetMapName("), compact);
        assertTrue(compact.contains("SetMapDescription("), compact);
    }
}
