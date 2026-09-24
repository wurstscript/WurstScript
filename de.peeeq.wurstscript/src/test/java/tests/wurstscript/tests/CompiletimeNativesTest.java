package tests.wurstscript.tests;

import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.objectreader.ObjectHelper;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.JassIm;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.Wc3BinInputStream;
import net.moonlightflower.wc3libs.bin.Wc3BinOutputStream;
import net.moonlightflower.wc3libs.bin.app.objMod.W3A;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CompiletimeNativesTest {

    @Test
    public void modifyObjectKeepsDifferentDataPointers() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A001"), ObjId.valueOf("Abas"));

        Method modifyObject = CompiletimeNatives.class.getDeclaredMethod(
                "modifyObject",
                ObjMod.Obj.class,
                ILconstString.class,
                ObjMod.ValType.class,
                int.class,
                int.class,
                DataType.class);
        modifyObject.setAccessible(true);

        MetaFieldId unrealId = MetaFieldId.valueOf("unat");
        modifyObject.invoke(natives, obj, ILconstString.fromText(unrealId.getVal()), ObjMod.ValType.UNREAL, 1, 0, War3Real.valueOf(1.0));
        modifyObject.invoke(natives, obj, ILconstString.fromText(unrealId.getVal()), ObjMod.ValType.UNREAL, 1, 1, War3Real.valueOf(2.0));

        List<ObjMod.Obj.ExtendedMod> unrealMods = obj.getMods().stream()
                .filter(m -> m instanceof ObjMod.Obj.ExtendedMod)
                .map(m -> (ObjMod.Obj.ExtendedMod) m)
                .filter(m -> unrealId.equals(m.getId()) && m.getLevel() == 1)
                .collect(Collectors.toList());

        assertEquals(unrealMods.size(), 2, "Mods with distinct data pointers should both be kept");
        Set<Integer> dataPointers = unrealMods.stream()
                .map(ObjMod.Obj.ExtendedMod::getDataPt)
                .collect(Collectors.toSet());
        assertTrue(dataPointers.contains(0));
        assertTrue(dataPointers.contains(1));
    }

    @Test
    public void sameIdObjectDefinitionUsesOriginalTable() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3U w3u = new W3U();

        Method newDefFromFiletype = CompiletimeNatives.class.getDeclaredMethod(
            "newDefFromFiletype",
            ObjMod.class,
            int.class,
            int.class,
            boolean.class
        );
        newDefFromFiletype.setAccessible(true);

        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        ObjMod.Obj obj = (ObjMod.Obj) newDefFromFiletype.invoke(natives, w3u, hfoo, hfoo, true);

        assertEquals(obj.getId().getVal(), "hfoo");
        assertEquals(obj.getBaseId(), null, "Melee overwrite should be written as original-table mod");
        assertEquals(obj.getNewId(), null, "Melee overwrite should not create a custom/new id");
        assertEquals(w3u.getOrigObjs().size(), 1);
        assertEquals(w3u.getCustomObjs().size(), 0);
    }

    @Test
    public void differentIdsObjectDefinitionUsesCustomTable() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3U w3u = new W3U();

        Method newDefFromFiletype = CompiletimeNatives.class.getDeclaredMethod(
            "newDefFromFiletype",
            ObjMod.class,
            int.class,
            int.class,
            boolean.class
        );
        newDefFromFiletype.setAccessible(true);

        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");
        ObjMod.Obj obj = (ObjMod.Obj) newDefFromFiletype.invoke(natives, w3u, hfoo, hf01, false);

        assertEquals(obj.getId().getVal(), "hf01");
        assertEquals(obj.getBaseId().getVal(), "hfoo");
        assertEquals(obj.getNewId().getVal(), "hf01");
        assertEquals(w3u.getOrigObjs().size(), 0);
        assertEquals(w3u.getCustomObjs().size(), 1);
    }

    @Test
    public void differentIdsObjectDefinitionOverwritesExistingMapObject() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3U w3u = new W3U();
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");
        W3U.Obj existing = w3u.addObj(ObjId.valueOf("hf01"), ObjId.valueOf("hpea"));
        existing.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("old map object")));

        Method newDefFromFiletype = CompiletimeNatives.class.getDeclaredMethod(
            "newDefFromFiletype",
            ObjMod.class,
            int.class,
            int.class,
            boolean.class
        );
        newDefFromFiletype.setAccessible(true);

        ObjMod.Obj obj = (ObjMod.Obj) newDefFromFiletype.invoke(natives, w3u, hfoo, hf01, false);

        assertEquals(w3u.getCustomObjs().size(), 1, "Existing map object should be replaced instead of duplicated");
        assertEquals(obj.getId().getVal(), "hf01");
        assertEquals(obj.getBaseId().getVal(), "hfoo");
        assertEquals(obj.getMods().size(), 0, "Overwritten map object mods should not leak into the Wurst-created object");
    }

    @Test
    public void duplicateCodeObjectDefinitionsReportError() {
        WurstGuiLogger gui = new WurstGuiLogger();
        ProgramStateIO state = new ProgramStateIO(Optional.empty(), null, gui, emptyProg(), true);
        CompiletimeNatives natives = new CompiletimeNatives(state, null, false);
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");

        natives.createObjectDefinition(ILconstString.fromText("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));
        natives.createObjectDefinition(ILconstString.fromText("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));

        assertEquals(gui.getErrorCount(), 1);
        assertTrue(gui.getErrors().contains("Object definition with id hf01 is defined more than once."));
    }

    @Test
    public void createObjectDefinitionDoesNotReportExistingMapObjectAsError() throws Exception {
        WurstGuiLogger gui = new WurstGuiLogger();
        ProgramStateIO state = new ProgramStateIO(Optional.empty(), null, gui, emptyProg(), true);
        CompiletimeNatives natives = new CompiletimeNatives(state, null, false);
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");

        Method getDataStore = ProgramStateIO.class.getDeclaredMethod("getDataStore", String.class);
        getDataStore.setAccessible(true);
        W3U w3u = (W3U) getDataStore.invoke(state, "w3u");
        W3U.Obj existing = w3u.addObj(ObjId.valueOf("hf01"), ObjId.valueOf("hpea"));
        existing.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("old map object")));

        natives.createObjectDefinition(ILconstString.fromText("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));

        assertEquals(gui.getErrorCount(), 0);
        assertEquals(w3u.getCustomObjs().size(), 1);
        ObjMod.Obj obj = w3u.getCustomObjs().get(0);
        assertEquals(obj.getId().getVal(), "hf01");
        assertEquals(obj.getBaseId().getVal(), "hfoo");
        assertFalse(obj.getMods().stream().anyMatch(m -> m.getId().getVal().equals("unam")));
    }

    @Test
    public void sameIdObjectDefinitionsMergeModsWithoutDuplicateError() throws Exception {
        WurstGuiLogger gui = new WurstGuiLogger();
        ProgramStateIO state = new ProgramStateIO(Optional.empty(), null, gui, emptyProg(), true);
        CompiletimeNatives natives = new CompiletimeNatives(state, null, false);
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");

        var first = natives.createObjectDefinition(ILconstString.fromText("w3u"), new ILconstInt(hfoo), new ILconstInt(hfoo));
        natives.ObjectDefinition_setString(first, ILconstString.fromText("unam"), ILconstString.fromText("first"));
        var second = natives.createObjectDefinition(ILconstString.fromText("w3u"), new ILconstInt(hfoo), new ILconstInt(hfoo));
        natives.ObjectDefinition_setString(second, ILconstString.fromText("utip"), ILconstString.fromText("second"));

        Method getDataStore = ProgramStateIO.class.getDeclaredMethod("getDataStore", String.class);
        getDataStore.setAccessible(true);
        W3U w3u = (W3U) getDataStore.invoke(state, "w3u");

        assertEquals(gui.getErrorCount(), 0);
        assertEquals(w3u.getOrigObjs().size(), 1);
        ObjMod.Obj obj = w3u.getOrigObjs().get(0);
        assertTrue(obj.getMods().stream().anyMatch(m -> m.getId().getVal().equals("unam")));
        assertTrue(obj.getMods().stream().anyMatch(m -> m.getId().getVal().equals("utip")));
    }

    @Test
    public void modifyObjectReplacesPlainModsReadFromMapFile() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3U w3u = new W3U();
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("hfoo"), null);
        // Duplicates left behind by earlier runs, as they are read back from a war3map.w3u.
        obj.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("old 1")));
        obj.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("old 2")));
        obj.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("utip"), ObjMod.ValType.STRING, War3String.valueOf("tip")));

        Method modifyObject = CompiletimeNatives.class.getDeclaredMethod(
                "modifyObject",
                ObjMod.Obj.class,
                ILconstString.class,
                ObjMod.ValType.class,
                int.class,
                int.class,
                DataType.class);
        modifyObject.setAccessible(true);
        modifyObject.invoke(natives, obj, ILconstString.fromText("unam"), ObjMod.ValType.STRING, 1, 0, War3String.valueOf("new"));

        List<ObjMod.Obj.Mod> names = obj.getModsOfField(MetaFieldId.valueOf("unam"));
        assertEquals(names.size(), 1);
        assertEquals(((War3String) names.get(0).getVal()).getVal(), "new");
        assertEquals(obj.getModsOfField(MetaFieldId.valueOf("utip")).size(), 1, "Other fields must be kept");
    }

    @Test
    public void meleeOverrideDoesNotGrowMapAcrossRuns() throws Exception {
        File map = newArchive(tempDir(), "map.w3x", null);
        long[] sizes = new long[4];
        for (int run = 0; run < sizes.length; run++) {
            runCompiletime(map, null, natives -> overrideFootman(natives, "Footman X"));
            byte[] w3u = readFromArchive(map, "war3map.w3u");
            sizes[run] = w3u.length;
            ObjMod.Obj footman = readW3U(w3u).getObjs().get(ObjId.valueOf("hfoo"));
            assertEquals(footman.getMods().size(), 2, "run " + run + " duplicated fields: " + footman.getMods());
        }
        for (long size : sizes) {
            assertEquals(size, sizes[0], "war3map.w3u grew across runs");
        }
    }

    @Test
    public void removedMeleeOverrideIsDroppedFromCachedMap() throws Exception {
        File dir = tempDir();
        W3U editorObjects = new W3U();
        editorObjects.addObj(ObjId.valueOf("hpea"), null)
            .addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("Editor peasant")));
        File source = newArchive(dir, "source.w3x", writeObjMod(editorObjects));
        File cached = new File(dir, "cached.w3x");
        java.nio.file.Files.copy(source.toPath(), cached.toPath());

        runCompiletime(cached, source, natives -> overrideFootman(natives, "Footman X"));
        assertTrue(readW3U(readFromArchive(cached, "war3map.w3u")).getObjs().containsKey(ObjId.valueOf("hfoo")));

        runCompiletime(cached, source, natives -> {});
        W3U result = readW3U(readFromArchive(cached, "war3map.w3u"));
        assertFalse(result.getObjs().containsKey(ObjId.valueOf("hfoo")), "Override removed from code must not survive in the cache");
        ObjMod.Obj peasant = result.getObjs().get(ObjId.valueOf("hpea"));
        assertEquals(((War3String) peasant.get(MetaFieldId.valueOf("unam"))).getVal(), "Editor peasant",
            "Object data from the source map must be kept");
    }

    @Test
    public void removedMeleeOverrideIsDroppedWhenSourceMapHasNoObjectData() throws Exception {
        File dir = tempDir();
        File source = newArchive(dir, "source.w3x", null);
        File cached = new File(dir, "cached.w3x");
        java.nio.file.Files.copy(source.toPath(), cached.toPath());

        runCompiletime(cached, source, natives -> overrideFootman(natives, "Footman X"));
        runCompiletime(cached, source, natives -> {});

        W3U result = readW3U(readFromArchive(cached, "war3map.w3u"));
        assertTrue(result.getObjsList().isEmpty(), "Stale objects left in cache: " + result.getObjs().keySet());
    }

    private static void overrideFootman(CompiletimeNatives natives, String name) {
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        var footman = natives.createObjectDefinition(ILconstString.fromText("w3u"), new ILconstInt(hfoo), new ILconstInt(hfoo));
        natives.ObjectDefinition_setString(footman, ILconstString.fromText("unam"), ILconstString.fromText(name));
        natives.ObjectDefinition_setString(footman, ILconstString.fromText("utip"), ILconstString.fromText("Tooltip"));
    }

    /** One compiletime run against {@code map}, the way a map request runs it against the cached map. */
    private void runCompiletime(File map, File objectDataSource, Consumer<CompiletimeNatives> compiletime) throws Exception {
        WurstGuiLogger gui = new WurstGuiLogger();
        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(map))) {
            ProgramStateIO state = new ProgramStateIO(Optional.of(map), mpq, gui, emptyProg(), true);
            state.setObjectDataSource(objectDataSource);
            compiletime.accept(new CompiletimeNatives(state, null, false));
            state.writeBack(true);
        }
        assertEquals(gui.getErrorCount(), 0, gui.getErrors());
    }

    private static File tempDir() throws IOException {
        File dir = Files.createTempDirectory("wurst-object-cache").toFile();
        dir.deleteOnExit();
        return dir;
    }

    private static File newArchive(File dir, String name, byte[] w3u) throws Exception {
        File archive = new File(dir, name);
        MpqEditorFactory.createEmptyArchive(archive);
        if (w3u != null) {
            try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(archive))) {
                mpq.insertFile("war3map.w3u", w3u);
            }
        }
        return archive;
    }

    private static byte[] readFromArchive(File archive, String name) throws Exception {
        try (MpqEditor mpq = MpqEditorFactory.getEditor(Optional.of(archive), true)) {
            return mpq.extractFile(name);
        }
    }

    private static byte[] writeObjMod(ObjMod<?> objMod) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Wc3BinOutputStream out = new Wc3BinOutputStream(baos)) {
            objMod.write(out, ObjMod.EncodingFormat.OBJ_0x2);
        }
        return baos.toByteArray();
    }

    private static W3U readW3U(byte[] data) throws Exception {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3U(in);
        }
    }

    @Test
    public void testCloseSqliteResourcesOnProviderClose() {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        ILconstInt connHandle = natives.sqlite_open(ILconstString.fromText(":memory:"));
        natives.sqlite_exec(connHandle, ILconstString.fromText("CREATE TABLE Test (id INT);"));
        ILconstInt stmtHandle = natives.sqlite_prepare(connHandle, ILconstString.fromText("INSERT INTO Test VALUES (1);"));
        natives.sqlite_step(stmtHandle);

        natives.close();

        try {
            natives.sqlite_prepare(connHandle, ILconstString.fromText("SELECT * FROM Test;"));
            org.testng.Assert.fail("Expected InterpreterException for invalid connection handle after close");
        } catch (de.peeeq.wurstio.jassinterpreter.InterpreterException e) {
            assertTrue(e.getMessage().contains("Invalid SQLite connection handle"));
        }
    }

    @Test
    public void testCloseSqliteResourcesFromProgramStateClose() throws Exception {
        WurstGuiLogger gui = new WurstGuiLogger();
        ProgramStateIO state = new ProgramStateIO(Optional.empty(), null, gui, emptyProg(), true);
        CompiletimeNatives natives = new CompiletimeNatives(state, null, false);
        state.addNativeProvider(natives);

        ILconstInt connHandle = natives.sqlite_open(ILconstString.fromText(":memory:"));
        natives.sqlite_exec(connHandle, ILconstString.fromText("CREATE TABLE Test (id INT);"));

        state.close();

        try {
            natives.sqlite_prepare(connHandle, ILconstString.fromText("SELECT * FROM Test;"));
            org.testng.Assert.fail("Expected InterpreterException for invalid connection handle after ProgramState close");
        } catch (de.peeeq.wurstio.jassinterpreter.InterpreterException e) {
            assertTrue(e.getMessage().contains("Invalid SQLite connection handle"));
        }
    }

    @Test
    public void sqliteExecRunsMultiStatementScriptWithTrigger() {
        WurstGuiLogger gui = new WurstGuiLogger();
        ProgramStateIO state = new ProgramStateIO(Optional.empty(), null, gui, emptyProg(), true);
        CompiletimeNatives natives = new CompiletimeNatives(state, null, false);
        state.addNativeProvider(natives);

        ILconstInt db = natives.sqlite_open(ILconstString.fromText(":memory:"));
        // Multi-statement script including a trigger BEGIN...END body (whose inner ';'
        // terminators would break a naive splitter) and a bracket-quoted identifier
        // containing ';'. sqlite3_exec delegates to SQLite's own parser, so both work.
        natives.sqlite_exec(db, ILconstString.fromText(
                "CREATE TABLE src (id INTEGER);"
                        + "CREATE TABLE dst (id INTEGER);"
                        + "CREATE TRIGGER mirror AFTER INSERT ON src BEGIN INSERT INTO dst VALUES (NEW.id); END;"
                        + "CREATE TABLE [we;ird] (x INTEGER);"
                        + "INSERT INTO src VALUES (1); INSERT INTO src VALUES (2);"));

        ILconstInt q = natives.sqlite_prepare(db, ILconstString.fromText(
                "SELECT (SELECT COUNT(*) FROM dst), "
                        + "(SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='we;ird')"));
        assertTrue(natives.sqlite_step(q).getVal());
        assertEquals(natives.sqlite_column_int(q, new ILconstInt(0)).getVal(), 2); // trigger mirrored both inserts
        assertEquals(natives.sqlite_column_int(q, new ILconstInt(1)).getVal(), 1); // weird-named table created
        natives.sqlite_finalize(q);
        natives.sqlite_close(db);
        state.close();
    }

    // ---- SQLite API coverage: every native's intended behavior + error contract ----

    private static CompiletimeNatives newSqliteNatives() {
        // globalState is unused by the sqlite_* natives, so null is fine here.
        return new CompiletimeNatives(null, null, false);
    }

    private static ILconstInt i(int v) {
        return new ILconstInt(v);
    }

    private static void assertInterpreterError(String expectedSubstring, Runnable action) {
        try {
            action.run();
            org.testng.Assert.fail("Expected InterpreterException containing '" + expectedSubstring + "'");
        } catch (de.peeeq.wurstio.jassinterpreter.InterpreterException e) {
            assertTrue(e.getMessage().contains(expectedSubstring),
                    "Expected message to contain '" + expectedSubstring + "' but was: " + e.getMessage());
        }
    }

    @Test
    public void sqliteReadsBackEveryColumnTypeIncludingNull() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText(
                "CREATE TABLE T (i INTEGER, r REAL, s TEXT, n TEXT);"
                        + "INSERT INTO T VALUES (42, 2.5, 'hello', NULL);"));
        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT i, r, s, n FROM T"));
        // column_count works before any step (metadata branch) ...
        assertEquals(n.sqlite_column_count(q).getVal(), 4);
        assertTrue(n.sqlite_step(q).getVal());
        // ... and after a step (result-set branch)
        assertEquals(n.sqlite_column_count(q).getVal(), 4);
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 42);
        assertEquals((double) n.sqlite_column_real(q, i(1)).getVal(), 2.5, 0.0);
        assertEquals(n.sqlite_column_string(q, i(2)).getVal(), "hello");
        // a genuine SQL NULL reads back as "" but is flagged by sqlite_column_is_null
        assertEquals(n.sqlite_column_string(q, i(3)).getVal(), "");
        assertTrue(n.sqlite_column_is_null(q, i(3)).getVal());
        assertFalse(n.sqlite_column_is_null(q, i(0)).getVal());
        // stepping past the last row returns false
        assertFalse(n.sqlite_step(q).getVal());
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    /**
     * A string is held as bytes inside the interpreter and as text by the driver, so every value
     * crossing into SQLite has to be decoded and everything read back encoded again. Binding text
     * without decoding stores the bytes as though each were a character, and it comes back a
     * different string than the one that went in.
     */
    @Test
    public void sqliteRoundTripsNonAsciiText() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (s TEXT)"));

        ILconstInt insert = n.sqlite_prepare(db, ILconstString.fromText("INSERT INTO T VALUES (?)"));
        ILconstString written = ILconstString.fromText("Grüße 日本");
        n.sqlite_bind_string(insert, i(1), written);
        n.sqlite_step(insert);
        n.sqlite_finalize(insert);

        ILconstInt read = n.sqlite_prepare(db, ILconstString.fromText("SELECT s FROM T"));
        assertTrue(n.sqlite_step(read).getVal());
        ILconstString readBack = n.sqlite_column_string(read, i(0));
        assertEquals(readBack.text(), "Grüße 日本");
        // and the same bytes, so a length taken either side of the round trip agrees
        assertEquals(readBack.getVal(), written.getVal());
        n.sqlite_finalize(read);

        // a non-ascii literal in the SQL itself takes the same path
        ILconstInt matched = n.sqlite_prepare(db,
            ILconstString.fromText("SELECT count(*) FROM T WHERE s = 'Grüße 日本'"));
        assertTrue(n.sqlite_step(matched).getVal());
        assertEquals(n.sqlite_column_int(matched, i(0)).getVal(), 1);
        n.sqlite_finalize(matched);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteResetRewindsSelectResultSet() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText(
                "CREATE TABLE T (id INTEGER); INSERT INTO T VALUES (10); INSERT INTO T VALUES (20);"));
        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT id FROM T ORDER BY id"));
        assertTrue(n.sqlite_step(q).getVal());
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 10);
        assertTrue(n.sqlite_step(q).getVal());
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 20);
        // reset rewinds: the next step reads the first row again
        n.sqlite_reset(q);
        assertTrue(n.sqlite_step(q).getVal());
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 10);
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteColumnIntTruncates64BitValueToInt() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (v INTEGER); INSERT INTO T VALUES (5000000000);"));
        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT v FROM T"));
        assertTrue(n.sqlite_step(q).getVal());
        // WurstScript int is 32-bit: a 64-bit INTEGER wraps to its low 32 bits (documented).
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), (int) 5000000000L);
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteBindRoundTripAndRebindAfterStepReexecutes() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (i INTEGER, r REAL, s TEXT)"));
        ILconstInt ins = n.sqlite_prepare(db, ILconstString.fromText("INSERT INTO T VALUES (?, ?, ?)"));
        n.sqlite_bind_int(ins, i(1), i(1));
        n.sqlite_bind_real(ins, i(2), new ILconstReal(1.5f));
        n.sqlite_bind_string(ins, i(3), ILconstString.fromText("a"));
        assertFalse(n.sqlite_step(ins).getVal());
        // rebind i and s WITHOUT a reset: must re-execute; r keeps its previous binding
        n.sqlite_bind_int(ins, i(1), i(2));
        n.sqlite_bind_string(ins, i(3), ILconstString.fromText("b"));
        assertFalse(n.sqlite_step(ins).getVal());
        n.sqlite_finalize(ins);

        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT i, r, s FROM T ORDER BY i"));
        assertTrue(n.sqlite_step(q).getVal());
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 1);
        assertEquals((double) n.sqlite_column_real(q, i(1)).getVal(), 1.5, 0.0);
        assertEquals(n.sqlite_column_string(q, i(2)).getVal(), "a");
        assertTrue(n.sqlite_step(q).getVal());
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 2);
        assertEquals((double) n.sqlite_column_real(q, i(1)).getVal(), 1.5, 0.0); // preserved binding
        assertEquals(n.sqlite_column_string(q, i(2)).getVal(), "b");
        assertFalse(n.sqlite_step(q).getVal());
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteClearBindingsResetsParametersToNull() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (a INTEGER, b TEXT)"));
        ILconstInt ins = n.sqlite_prepare(db, ILconstString.fromText("INSERT INTO T VALUES (?, ?)"));
        n.sqlite_bind_int(ins, i(1), i(7));
        n.sqlite_bind_string(ins, i(2), ILconstString.fromText("x"));
        n.sqlite_clear_bindings(ins);
        assertFalse(n.sqlite_step(ins).getVal());
        n.sqlite_finalize(ins);

        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT a, b FROM T"));
        assertTrue(n.sqlite_step(q).getVal());
        assertTrue(n.sqlite_column_is_null(q, i(0)).getVal());
        assertTrue(n.sqlite_column_is_null(q, i(1)).getVal());
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteClearBindingsAfterStepReexecutesWithoutReset() {
        // Regression: sqlite_clear_bindings must invalidate a prior execution just like
        // sqlite_bind_*, so a step after clearing re-runs the statement with NULL params
        // WITHOUT an explicit sqlite_reset. Otherwise the second step silently no-ops.
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (v INTEGER)"));
        ILconstInt ins = n.sqlite_prepare(db, ILconstString.fromText("INSERT INTO T VALUES (?)"));
        n.sqlite_bind_int(ins, i(1), i(100));
        assertFalse(n.sqlite_step(ins).getVal());   // inserts 100
        n.sqlite_clear_bindings(ins);               // no explicit reset
        assertFalse(n.sqlite_step(ins).getVal());   // must re-execute → inserts NULL
        n.sqlite_finalize(ins);

        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT count(*), count(v) FROM T"));
        assertTrue(n.sqlite_step(q).getVal());
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 2); // two rows total
        assertEquals(n.sqlite_column_int(q, i(1)).getVal(), 1); // one non-NULL (the 100)
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteClearBindingsAfterStepDiscardsOldResultSet() {
        // Companion to the above for the SELECT path: after a step opens a result set,
        // clear_bindings must discard it so the next step RE-RUNS the query with the now
        // NULL parameter, rather than continuing to walk the stale result set.
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (v INTEGER)"));
        n.sqlite_exec(db, ILconstString.fromText("INSERT INTO T VALUES (1), (2), (3)"));
        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT v FROM T WHERE v <> ? ORDER BY v"));
        n.sqlite_bind_int(q, i(1), i(2));           // excludes 2 → rows {1, 3}
        assertTrue(n.sqlite_step(q).getVal());
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), 1);
        // Without the fix, the next step continues the old result set and returns row 3.
        // With the fix, it re-executes: "v <> NULL" matches nothing → no rows.
        n.sqlite_clear_bindings(q);
        assertFalse(n.sqlite_step(q).getVal());
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteFinalizeInvalidatesStatementHandle() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (id INTEGER)"));
        ILconstInt stmt = n.sqlite_prepare(db, ILconstString.fromText("INSERT INTO T VALUES (1)"));
        n.sqlite_finalize(stmt);
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_step(stmt));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_finalize(stmt));
        n.sqlite_close(db);
    }

    @Test
    public void sqliteCloseInvalidatesConnectionAndItsStatements() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (id INTEGER)"));
        ILconstInt stmt = n.sqlite_prepare(db, ILconstString.fromText("SELECT id FROM T"));
        n.sqlite_close(db);
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_prepare(db, ILconstString.fromText("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_exec(db, ILconstString.fromText("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_close(db));
        // statements belonging to the closed connection were finalized/invalidated too
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_step(stmt));
    }

    @Test
    public void sqliteInvalidHandlesAndBadSqlThrow() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_bind_int(i(999), i(1), i(1)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_bind_real(i(999), i(1), new ILconstReal(1f)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_bind_string(i(999), i(1), ILconstString.fromText("x")));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_step(i(999)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_reset(i(999)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_clear_bindings(i(999)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_finalize(i(999)));
        assertInterpreterError("No result set", () -> n.sqlite_column_int(i(999), i(0)));
        assertInterpreterError("No result set", () -> n.sqlite_column_is_null(i(999), i(0)));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_prepare(i(999), ILconstString.fromText("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_exec(i(999), ILconstString.fromText("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_close(i(999)));
        assertInterpreterError("Failed to prepare SQLite statement", () -> n.sqlite_prepare(db, ILconstString.fromText("NOT VALID SQL")));
        assertInterpreterError("Failed to exec SQLite query", () -> n.sqlite_exec(db, ILconstString.fromText("NOT VALID SQL")));
        n.sqlite_close(db);
    }

    @Test
    public void sqliteColumnAccessWithoutResultSetThrows() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        n.sqlite_exec(db, ILconstString.fromText("CREATE TABLE T (id INTEGER); INSERT INTO T VALUES (1)"));
        ILconstInt q = n.sqlite_prepare(db, ILconstString.fromText("SELECT id FROM T"));
        // reading a column before any step -> no result set yet
        assertInterpreterError("No result set", () -> n.sqlite_column_int(q, i(0)));
        assertInterpreterError("No result set", () -> n.sqlite_column_is_null(q, i(0)));
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteOpenRejectsFileUriWithQueryParameters() {
        CompiletimeNatives n = newSqliteNatives();
        assertInterpreterError("query parameters are not allowed",
                () -> n.sqlite_open(ILconstString.fromText("file::memory:?enable_load_extension=true")));
    }

    @Test
    public void sqliteLoadExtensionIsBlocked() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(ILconstString.fromText(":memory:"));
        // extension loading is disabled on the connection, so load_extension is not authorized
        // (this is what closes the dlopen-arbitrary-native-code vector at compiletime).
        assertInterpreterError("not authorized",
                () -> n.sqlite_exec(db, ILconstString.fromText("SELECT load_extension('nonexistent_extension')")));
        n.sqlite_close(db);
    }

    private ImProg emptyProg() {
        Element trace = Ast.NoExpr();
        return JassIm.ImProg(trace, JassIm.ImVars(), JassIm.ImFunctions(), JassIm.ImMethods(), JassIm.ImClasses(), JassIm.ImTypeClassFuncs(), new HashMap<>());
    }
}
