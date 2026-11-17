package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.languageserver.requests.RequestFailedException;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import net.moonlightflower.wc3libs.app.objMerge.SlkTable;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.Wc3BinInputStream;
import net.moonlightflower.wc3libs.bin.Wc3BinOutputStream;
import net.moonlightflower.wc3libs.bin.app.W3I;
import net.moonlightflower.wc3libs.bin.app.objMod.W3H;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.dataTypes.app.War3Int;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/** {@code -mergeobjects}: the build moves the output map's object modifications into SLK tables and profile files. */
public class MergeObjectsBuildTests {
    private static final String UNIT_BALANCE = "Units\\UnitBalance.slk";
    private static final String UNIT_FUNC = "Units\\CampaignUnitFunc.txt";
    private static final String BUFF_DATA = "Units\\AbilityBuffData.slk";

    @Test
    public void mergeObjectsMovesTheObjectDataOfTheOutputMapIntoTables() throws Exception {
        Project project = new Project("v2.0");
        File output = project.build("-mergeobjects");

        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(output), true)) {
            assertEquals(SlkTable.parse(mpq.extractFile(UNIT_BALANCE)).getRow("h000").get("HP"), "1234");
            assertTrue(mpq.hasFile(UNIT_FUNC));
            // A string table reference cannot be written into a profile file, so the name stays in a reduced w3u.
            W3U residual = read(new W3U(), mpq.extractFile("war3map.w3u"));
            List<String> fields = residual.getObj(ObjId.valueOf("h000")).getMods().stream()
                .map(mod -> mod.getId().toString()).toList();
            assertEquals(fields, List.of("unam"));
            assertTrue(mpq.hasFile(BUFF_DATA));
            assertFalse(mpq.hasFile("war3map.w3h"), "the buff file is left empty and removed");
        }
        // The next build compares the object files of the cached map with the ones it generates.
        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(project.cachedMap), true)) {
            assertEquals(mpq.extractFile("war3map.w3u"), project.w3u);
            assertEquals(mpq.extractFile("war3map.w3h"), project.w3h);
            assertFalse(mpq.hasFile(UNIT_BALANCE));
        }
    }

    @Test
    public void withoutMergeObjectsTheOutputMapKeepsItsObjectFiles() throws Exception {
        Project project = new Project("v2.0");
        File output = project.build();

        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(output), true)) {
            assertEquals(mpq.extractFile("war3map.w3u"), project.w3u);
            assertEquals(mpq.extractFile("war3map.w3h"), project.w3h);
            assertFalse(mpq.hasFile(UNIT_BALANCE));
            assertFalse(mpq.hasFile(UNIT_FUNC));
            assertFalse(mpq.hasFile(BUFF_DATA));
        }
    }

    @Test
    public void mergeObjectsRefusesAClassicTargetBeforeCompiling() throws Exception {
        Project project = new Project("1.30");
        RequestFailedException e = expectThrows(RequestFailedException.class, () -> project.build("-mergeobjects"));

        assertTrue(e.getMessage().contains("-mergeobjects needs a Reforged target"), e.getMessage());
        assertEquals(project.compilations.get(), 0);
    }

    /** A project whose cached map holds a w3i, a script and object data, built through the shared pipeline. */
    private static final class Project {
        final Path root;
        final File cachedMap;
        final byte[] w3u;
        final byte[] w3h;
        final AtomicInteger compilations = new AtomicInteger();

        Project(String wc3Patch) throws Exception {
            root = Files.createTempDirectory("wurst-merge-objects");
            Files.writeString(root.resolve(ProjectConfigBuilder.FILE_NAME), "projectName: Merge\nwc3Patch: " + wc3Patch + "\n");
            cachedMap = root.resolve("cached.w3x").toFile();

            W3U units = new W3U();
            ObjMod.Obj unit = units.addObj(ObjId.valueOf("h000"), ObjId.valueOf("hfoo"));
            unit.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("uhpm"), ObjMod.ValType.INT, War3Int.valueOf(1234)));
            unit.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("TRIGSTR_012")));
            W3H buffs = new W3H();
            buffs.addObj(ObjId.valueOf("B000"), ObjId.valueOf("BHbd"))
                .addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("ftip"), ObjMod.ValType.STRING, War3String.valueOf("Chill")));
            w3u = bytes(units);
            w3h = bytes(buffs);

            W3I w3i = new W3I();
            w3i.setFileVersion(W3I.EncodingFormat.W3I_0x21.getVersion());
            Path w3iFile = root.resolve("war3map.w3i");
            w3i.write(w3iFile.toFile(), W3I.EncodingFormat.AS_DEFINED);

            MpqEditorFactory.createEmptyArchive(cachedMap);
            try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(cachedMap))) {
                mpq.insertFile("war3map.w3i", w3iFile.toFile());
                mpq.insertFile("war3map.j", "function main takes nothing returns nothing\nendfunction\n".getBytes(StandardCharsets.UTF_8));
                mpq.insertFile("war3map.w3u", w3u);
                mpq.insertFile("war3map.w3h", w3h);
            }
        }

        File build(String... args) throws Exception {
            File[] output = new File[1];
            class Request extends MapRequest {
                Request() {
                    super(null, Optional.of(cachedMap), List.of(args), WFile.create(root.toFile()), Optional.empty(), Optional.empty());
                }

                @Override
                public Object execute(ModelManager manager) {
                    return null;
                }

                @Override
                protected File getBuildDir() {
                    return root.toFile();
                }

                @Override
                protected File getCachedMapFile() {
                    return cachedMap;
                }

                @Override
                protected CompilationResult compileScript(ModelManager manager, WurstGui gui, Optional<File> map,
                                                          WurstProjectConfigData config, File buildDir, boolean production) {
                    compilations.incrementAndGet();
                    return new CompilationResult();
                }

                @Override
                protected void injectMapData(WurstGui gui, Optional<File> map, CompilationResult result) {
                }

                @Override
                protected File ensureWritableBuildOutput(File target, boolean finalWrite) {
                    output[0] = target;
                    return target;
                }

                void build() throws Exception {
                    executeBuildMapPipeline(null, new WurstGuiLogger(), WurstProjectConfigData.empty());
                }
            }
            new Request().build();
            return output[0];
        }
    }

    private static byte[] bytes(ObjMod<?> objMod) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Wc3BinOutputStream stream = new Wc3BinOutputStream(out)) {
            objMod.write(stream, ObjMod.EncodingFormat.AS_DEFINED);
        }
        return out.toByteArray();
    }

    private static <T extends ObjMod<?>> T read(T objMod, byte[] bytes) throws Exception {
        try (Wc3BinInputStream stream = new Wc3BinInputStream(new ByteArrayInputStream(bytes))) {
            objMod.read(stream);
        }
        return objMod;
    }
}
