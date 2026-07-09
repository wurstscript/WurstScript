package tests.wurstscript.tests;

import de.peeeq.wurstio.objectreader.ObjectExportService;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.Wc3BinOutputStream;
import net.moonlightflower.wc3libs.bin.app.objMod.W3A;
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
        W3A.Obj bloodlust = skinAbilities.addObj(ObjId.valueOf("A002"), ObjId.valueOf("Ablo"));
        addLvlReal(bloodlust, "acdn", 1, 0, 5.0);
        Path skinFile = mapFolder.resolve("war3mapSkin.w3a");
        writeObjMod(skinFile, skinAbilities);

        List<Path> written = ObjectExportService.exportObjects(mapFolder.toFile(), Optional.of(outFolder.toFile()));

        assertEquals(written.size(), 1);
        assertTrue(Files.exists(skinFile), "export must not mutate folder maps");

        String exported = Files.readString(outFolder.resolve("WurstExportedObjects_w3a.wurst"));
        assertTrue(exported.contains("createObjectDefinition(\"w3a\", 'A001', 'Aslo')"), exported);
        assertTrue(exported.contains("createObjectDefinition(\"w3a\", 'A002', 'Ablo')"), exported);
        assertTrue(exported.contains("..setLvlDataUnreal(\"acdn\", 1, 0, 10.0)"), exported);
        assertTrue(exported.contains("..setLvlDataUnreal(\"acdn\", 1, 0, 5.0)"), exported);
        assertFalse(exported.contains("// TODO no wrapper"), exported);
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

    private static void writeObjMod(Path file, ObjMod<? extends ObjMod.Obj> objMod) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Wc3BinOutputStream out = new Wc3BinOutputStream(bytes)) {
            objMod.write(out, ObjMod.EncodingFormat.OBJ_0x2);
        }
        Files.write(file, bytes.toByteArray());
    }
}
