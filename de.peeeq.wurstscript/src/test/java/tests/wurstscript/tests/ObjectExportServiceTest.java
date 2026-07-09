package tests.wurstscript.tests;

import de.peeeq.wurstio.objectreader.ObjectExportService;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.Wc3BinOutputStream;
import net.moonlightflower.wc3libs.bin.app.objMod.W3A;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.dataTypes.app.War3Int;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

public class ObjectExportServiceTest {

    @Test
    public void exportObjectsFromFolderMapMergesSkinWithoutDeletingIt() throws Exception {
        Path mapFolder = Files.createTempDirectory("wurst-object-export-map");
        Path outFolder = Files.createTempDirectory("wurst-object-export-out");

        W3A mapAbilities = new W3A();
        W3A.Obj slow = mapAbilities.addObj(ObjId.valueOf("A001"), ObjId.valueOf("Aslo"));
        addLvlReal(slow, "acdn", 1, 0, 10.0);
        writeObjMod(mapFolder.resolve("war3map.w3a"), mapAbilities);

        W3A skinAbilities = new W3A();
        W3A.Obj slowSkin = skinAbilities.addObj(ObjId.valueOf("A002"), ObjId.valueOf("Aslo"));
        addLvlReal(slowSkin, "acdn", 1, 0, 5.0);
        Path skinFile = mapFolder.resolve("war3mapSkin.w3a");
        writeObjMod(skinFile, skinAbilities);

        List<Path> written = ObjectExportService.exportObjects(mapFolder.toFile(), Optional.of(outFolder.toFile()));

        assertEquals(written.size(), 1);
        assertTrue(Files.exists(skinFile), "export must not mutate folder maps");

        String exported = Files.readString(outFolder.resolve("WurstExportedObjects_w3a.wurst"));
        assertTrue(exported.contains("new AbilityDefinitionSlow('A001')"), exported);
        assertTrue(exported.contains("new AbilityDefinitionSlow('A002')"), exported);
        assertTrue(exported.contains("..setCooldown(1, 10.0)"), exported);
        assertTrue(exported.contains("..setCooldown(1, 5.0)"), exported);
        assertFalse(exported.contains("createObjectDefinition(\"w3a\""), exported);
        assertFalse(exported.contains("// TODO no wrapper"), exported);
    }

    @Test
    public void exportObjectsClearsStalePerTypeFiles() throws Exception {
        Path mapFolder = Files.createTempDirectory("wurst-object-export-map");
        Path outFolder = Files.createTempDirectory("wurst-object-export-out");
        Path staleUnits = outFolder.resolve("WurstExportedObjects_w3u.wurst");
        Files.writeString(staleUnits, "package StaleUnits\n");

        W3A mapAbilities = new W3A();
        W3A.Obj slow = mapAbilities.addObj(ObjId.valueOf("A001"), ObjId.valueOf("Aslo"));
        addLvlReal(slow, "acdn", 1, 0, 10.0);
        writeObjMod(mapFolder.resolve("war3map.w3a"), mapAbilities);

        List<Path> written = ObjectExportService.exportObjects(mapFolder.toFile(), Optional.of(outFolder.toFile()));

        assertEquals(written.size(), 1);
        assertFalse(Files.exists(staleUnits), "stale per-type exports must not survive a later export");
        assertTrue(Files.exists(outFolder.resolve("WurstExportedObjects_w3a.wurst")));
    }

    @Test
    public void exportObjectsFromFolderMapUsesHighLevelUnitMappings() throws Exception {
        Path mapFolder = Files.createTempDirectory("wurst-object-export-map");
        Path outFolder = Files.createTempDirectory("wurst-object-export-out");

        W3U mapUnits = new W3U();
        W3U.Obj unit = mapUnits.addObj(ObjId.valueOf("e000"), ObjId.valueOf("efdr"));
        addInt(unit, "uhpm", 460);
        writeObjMod(mapFolder.resolve("war3map.w3u"), mapUnits);

        List<Path> written = ObjectExportService.exportObjects(mapFolder.toFile(), Optional.of(outFolder.toFile()));

        assertEquals(written.size(), 1);
        String exported = Files.readString(outFolder.resolve("WurstExportedObjects_w3u.wurst"));
        assertTrue(exported.contains("new UnitDefinition('e000', 'efdr')"), exported);
        assertTrue(exported.contains("..setHitPointsMaximumBase(460)"), exported);
        assertFalse(exported.contains("createObjectDefinition(\"w3u\""), exported);
        assertFalse(exported.contains("..setInt(\"uhpm\", 460)"), exported);
    }

    private static void addLvlReal(W3A.Obj obj, String fieldId, int level, int dataPtr, double value) {
        obj.addMod(new ObjMod.Obj.ExtendedMod(
            MetaFieldId.valueOf(fieldId),
            ObjMod.ValType.UNREAL,
            War3Real.valueOf(value),
            level,
            dataPtr
        ));
    }

    private static void addInt(ObjMod.Obj obj, String fieldId, int value) {
        obj.addMod(new ObjMod.Obj.Mod(
            MetaFieldId.valueOf(fieldId),
            ObjMod.ValType.INT,
            War3Int.valueOf(value)
        ));
    }

    private static void writeObjMod(Path file, ObjMod<? extends ObjMod.Obj> objMod) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Wc3BinOutputStream out = new Wc3BinOutputStream(bytes)) {
            objMod.write(out, ObjMod.EncodingFormat.OBJ_0x2);
        }
        Files.write(file, bytes.toByteArray());
    }
}
