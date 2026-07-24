package tests.wurstscript.tests;

import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
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
import net.moonlightflower.wc3libs.bin.app.objMod.W3A;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        modifyObject.invoke(natives, obj, new ILconstString(unrealId.getVal()), ObjMod.ValType.UNREAL, 1, 0, War3Real.valueOf(1.0));
        modifyObject.invoke(natives, obj, new ILconstString(unrealId.getVal()), ObjMod.ValType.UNREAL, 1, 1, War3Real.valueOf(2.0));

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

        natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));
        natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));

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

        natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));

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

        var first = natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hfoo), new ILconstInt(hfoo));
        natives.ObjectDefinition_setString(first, new ILconstString("unam"), new ILconstString("first"));
        var second = natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hfoo), new ILconstInt(hfoo));
        natives.ObjectDefinition_setString(second, new ILconstString("utip"), new ILconstString("second"));

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
    public void testCloseSqliteResourcesOnProviderClose() {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        ILconstInt connHandle = natives.sqlite_open(new ILconstString(":memory:"));
        natives.sqlite_exec(connHandle, new ILconstString("CREATE TABLE Test (id INT);"));
        ILconstInt stmtHandle = natives.sqlite_prepare(connHandle, new ILconstString("INSERT INTO Test VALUES (1);"));
        natives.sqlite_step(stmtHandle);

        natives.close();

        try {
            natives.sqlite_prepare(connHandle, new ILconstString("SELECT * FROM Test;"));
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

        ILconstInt connHandle = natives.sqlite_open(new ILconstString(":memory:"));
        natives.sqlite_exec(connHandle, new ILconstString("CREATE TABLE Test (id INT);"));

        state.close();

        try {
            natives.sqlite_prepare(connHandle, new ILconstString("SELECT * FROM Test;"));
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

        ILconstInt db = natives.sqlite_open(new ILconstString(":memory:"));
        // Multi-statement script including a trigger BEGIN...END body (whose inner ';'
        // terminators would break a naive splitter) and a bracket-quoted identifier
        // containing ';'. sqlite3_exec delegates to SQLite's own parser, so both work.
        natives.sqlite_exec(db, new ILconstString(
                "CREATE TABLE src (id INTEGER);"
                        + "CREATE TABLE dst (id INTEGER);"
                        + "CREATE TRIGGER mirror AFTER INSERT ON src BEGIN INSERT INTO dst VALUES (NEW.id); END;"
                        + "CREATE TABLE [we;ird] (x INTEGER);"
                        + "INSERT INTO src VALUES (1); INSERT INTO src VALUES (2);"));

        ILconstInt q = natives.sqlite_prepare(db, new ILconstString(
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
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString(
                "CREATE TABLE T (i INTEGER, r REAL, s TEXT, n TEXT);"
                        + "INSERT INTO T VALUES (42, 2.5, 'hello', NULL);"));
        ILconstInt q = n.sqlite_prepare(db, new ILconstString("SELECT i, r, s, n FROM T"));
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

    @Test
    public void sqliteResetRewindsSelectResultSet() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString(
                "CREATE TABLE T (id INTEGER); INSERT INTO T VALUES (10); INSERT INTO T VALUES (20);"));
        ILconstInt q = n.sqlite_prepare(db, new ILconstString("SELECT id FROM T ORDER BY id"));
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
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString("CREATE TABLE T (v INTEGER); INSERT INTO T VALUES (5000000000);"));
        ILconstInt q = n.sqlite_prepare(db, new ILconstString("SELECT v FROM T"));
        assertTrue(n.sqlite_step(q).getVal());
        // WurstScript int is 32-bit: a 64-bit INTEGER wraps to its low 32 bits (documented).
        assertEquals(n.sqlite_column_int(q, i(0)).getVal(), (int) 5000000000L);
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteBindRoundTripAndRebindAfterStepReexecutes() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString("CREATE TABLE T (i INTEGER, r REAL, s TEXT)"));
        ILconstInt ins = n.sqlite_prepare(db, new ILconstString("INSERT INTO T VALUES (?, ?, ?)"));
        n.sqlite_bind_int(ins, i(1), i(1));
        n.sqlite_bind_real(ins, i(2), new ILconstReal(1.5f));
        n.sqlite_bind_string(ins, i(3), new ILconstString("a"));
        assertFalse(n.sqlite_step(ins).getVal());
        // rebind i and s WITHOUT a reset: must re-execute; r keeps its previous binding
        n.sqlite_bind_int(ins, i(1), i(2));
        n.sqlite_bind_string(ins, i(3), new ILconstString("b"));
        assertFalse(n.sqlite_step(ins).getVal());
        n.sqlite_finalize(ins);

        ILconstInt q = n.sqlite_prepare(db, new ILconstString("SELECT i, r, s FROM T ORDER BY i"));
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
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString("CREATE TABLE T (a INTEGER, b TEXT)"));
        ILconstInt ins = n.sqlite_prepare(db, new ILconstString("INSERT INTO T VALUES (?, ?)"));
        n.sqlite_bind_int(ins, i(1), i(7));
        n.sqlite_bind_string(ins, i(2), new ILconstString("x"));
        n.sqlite_clear_bindings(ins);
        assertFalse(n.sqlite_step(ins).getVal());
        n.sqlite_finalize(ins);

        ILconstInt q = n.sqlite_prepare(db, new ILconstString("SELECT a, b FROM T"));
        assertTrue(n.sqlite_step(q).getVal());
        assertTrue(n.sqlite_column_is_null(q, i(0)).getVal());
        assertTrue(n.sqlite_column_is_null(q, i(1)).getVal());
        n.sqlite_finalize(q);
        n.sqlite_close(db);
    }

    @Test
    public void sqliteFinalizeInvalidatesStatementHandle() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString("CREATE TABLE T (id INTEGER)"));
        ILconstInt stmt = n.sqlite_prepare(db, new ILconstString("INSERT INTO T VALUES (1)"));
        n.sqlite_finalize(stmt);
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_step(stmt));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_finalize(stmt));
        n.sqlite_close(db);
    }

    @Test
    public void sqliteCloseInvalidatesConnectionAndItsStatements() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString("CREATE TABLE T (id INTEGER)"));
        ILconstInt stmt = n.sqlite_prepare(db, new ILconstString("SELECT id FROM T"));
        n.sqlite_close(db);
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_prepare(db, new ILconstString("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_exec(db, new ILconstString("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_close(db));
        // statements belonging to the closed connection were finalized/invalidated too
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_step(stmt));
    }

    @Test
    public void sqliteInvalidHandlesAndBadSqlThrow() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_bind_int(i(999), i(1), i(1)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_bind_real(i(999), i(1), new ILconstReal(1f)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_bind_string(i(999), i(1), new ILconstString("x")));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_step(i(999)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_reset(i(999)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_clear_bindings(i(999)));
        assertInterpreterError("Invalid SQLite statement handle", () -> n.sqlite_finalize(i(999)));
        assertInterpreterError("No result set", () -> n.sqlite_column_int(i(999), i(0)));
        assertInterpreterError("No result set", () -> n.sqlite_column_is_null(i(999), i(0)));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_prepare(i(999), new ILconstString("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_exec(i(999), new ILconstString("SELECT 1")));
        assertInterpreterError("Invalid SQLite connection handle", () -> n.sqlite_close(i(999)));
        assertInterpreterError("Failed to prepare SQLite statement", () -> n.sqlite_prepare(db, new ILconstString("NOT VALID SQL")));
        assertInterpreterError("Failed to exec SQLite query", () -> n.sqlite_exec(db, new ILconstString("NOT VALID SQL")));
        n.sqlite_close(db);
    }

    @Test
    public void sqliteColumnAccessWithoutResultSetThrows() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        n.sqlite_exec(db, new ILconstString("CREATE TABLE T (id INTEGER); INSERT INTO T VALUES (1)"));
        ILconstInt q = n.sqlite_prepare(db, new ILconstString("SELECT id FROM T"));
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
                () -> n.sqlite_open(new ILconstString("file::memory:?enable_load_extension=true")));
    }

    @Test
    public void sqliteLoadExtensionIsBlocked() {
        CompiletimeNatives n = newSqliteNatives();
        ILconstInt db = n.sqlite_open(new ILconstString(":memory:"));
        // extension loading is disabled on the connection, so load_extension is not authorized
        // (this is what closes the dlopen-arbitrary-native-code vector at compiletime).
        assertInterpreterError("not authorized",
                () -> n.sqlite_exec(db, new ILconstString("SELECT load_extension('nonexistent_extension')")));
        n.sqlite_close(db);
    }

    private ImProg emptyProg() {
        Element trace = Ast.NoExpr();
        return JassIm.ImProg(trace, JassIm.ImVars(), JassIm.ImFunctions(), JassIm.ImMethods(), JassIm.ImClasses(), JassIm.ImTypeClassFuncs(), new HashMap<>());
    }
}
