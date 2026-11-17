package de.peeeq.wurstio.map;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.app.ObjMerger;
import net.moonlightflower.wc3libs.app.objMerge.BundledGameData;
import net.moonlightflower.wc3libs.app.objMerge.LayeredGameData;
import net.moonlightflower.wc3libs.app.objMerge.MemoryGameData;
import net.moonlightflower.wc3libs.app.objMerge.ObjType;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.Wc3BinInputStream;
import net.moonlightflower.wc3libs.bin.app.MapFlag;
import net.moonlightflower.wc3libs.bin.app.W3I;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Moves a built map's object modifications ({@code war3map.w3u} and the others) into SLK tables and profile files
 * with wc3libs' {@link ObjMerger} ({@code -slkobjects}). A map's table replaces the game's table of the same path,
 * so whole tables are written, starting from the bundled game data of the data set the map's {@code war3map.w3i}
 * selects, under the tables and profile files the map already ships. What the formats cannot hold stays in a
 * residual object modification file.
 */
public final class MapObjectMerger {
    private MapObjectMerger() {
    }

    /**
     * Merges the object modifications of the map open in {@code mpq} in place.
     *
     * @return the merge report, or null when the map has no object modifications
     */
    public static ObjMerger.Report merge(MpqEditor mpq) throws Exception {
        String w3iPath = W3I.GAME_PATH.getName();
        if (!mpq.hasFile(w3iPath)) {
            throw new IOException("Cannot merge object data: the map has no " + w3iPath
                + ", which selects the game data its objects are based on.");
        }
        W3I w3i = new W3I(mpq.extractFile(w3iPath));

        // Tables and profile files the map ships replace the game's, so they are the base of the merge.
        MemoryGameData mapOverrides = new MemoryGameData();
        for (String path : ObjMerger.gameDataPaths()) {
            if (mpq.hasFile(path)) {
                mapOverrides.put(path, mpq.extractFile(path));
            }
        }
        ObjMerger merger = new ObjMerger(new LayeredGameData(mapOverrides,
            BundledGameData.forDataSet(w3i.getGameDataSet(), w3i.getFlag(MapFlag.MELEE_MAP))));

        for (ObjType type : ObjType.values()) {
            String path = type.getObjModPath().getName();
            if (!mpq.hasFile(path)) {
                continue;
            }
            ObjMod<?> objMod = ObjMod.create(type.getObjModClass());
            try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(mpq.extractFile(path)))) {
                objMod.read(in);
            }
            merger.add(objMod);
        }
        if (merger.getObjMods().isEmpty()) {
            WLogger.info("Object merge: the map has no object modifications");
            return null;
        }

        ObjMerger.Result result = merger.merge();
        for (Map.Entry<String, byte[]> file : result.getFiles().entrySet()) {
            if (mpq.hasFile(file.getKey())) {
                mpq.deleteFile(file.getKey());
            }
            mpq.insertFile(file.getKey(), file.getValue());
        }
        for (String path : result.getRemovedFiles()) {
            if (mpq.hasFile(path)) {
                mpq.deleteFile(path);
            }
        }
        logReport(result);
        return result.getReport();
    }

    private static void logReport(ObjMerger.Result result) throws IOException {
        ObjMerger.Report report = result.getReport();
        WLogger.info("Object merge (game data " + BundledGameData.getVersion() + "): " + report);
        for (Map.Entry<ObjType, List<String>> merged : report.getMerged().entrySet()) {
            if (!merged.getValue().isEmpty()) {
                WLogger.info("  merged " + merged.getValue().size() + " " + merged.getKey() + " objects");
            }
        }
        for (ObjMerger.Report.Skipped skipped : report.getSkipped()) {
            WLogger.info("  kept in object data: " + skipped);
        }
        WLogger.info("  wrote " + String.join(", ", result.getFiles().keySet())
            + (result.getRemovedFiles().isEmpty() ? "" : "; removed " + String.join(", ", result.getRemovedFiles())));
    }
}
