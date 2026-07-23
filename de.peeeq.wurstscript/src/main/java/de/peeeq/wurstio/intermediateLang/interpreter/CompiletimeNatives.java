package de.peeeq.wurstio.intermediateLang.interpreter;


import org.wurstscript.projectconfig.WurstProjectConfigData;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.ReflectionBasedNativeProvider;
import de.peeeq.wurstio.objectreader.ObjectHelper;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.app.War3Int;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConnection;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ucd") // ignore unused code detector warnings, because this class uses reflection
public class CompiletimeNatives extends ReflectionBasedNativeProvider implements NativesProvider {
    private final boolean isProd;
    private final ProgramStateIO globalState;
    private final WurstProjectConfigData projectConfigData;

    public CompiletimeNatives(ProgramStateIO globalState, WurstProjectConfigData projectConfigData, boolean isProd) {
        this.globalState = globalState;
        this.projectConfigData = projectConfigData;
        this.isProd = isProd;
    }


    private ILconstTuple makeKey(String key) {
        return new ILconstTuple(new ILconstString(key));
    }

    public ILconstTuple createObjectDefinition(ILconstString fileType, ILconstInt newUnitId, ILconstInt deriveFrom) {
        ObjMod<? extends ObjMod.Obj> dataStore = globalState.getDataStore(fileType.getVal());
        String objIdString = ObjectHelper.objectIdIntToString(newUnitId.getVal());
        boolean isMeleeOverride = newUnitId.getVal() == deriveFrom.getVal();

        if (!isMeleeOverride && !globalState.registerCreatedObjectDefinition(fileType.getVal(), objIdString)) {
            globalState.compilationError("Object definition with id " + objIdString + " is defined more than once.");
        }
        ObjMod.Obj objDef = newDefFromFiletype(dataStore, deriveFrom.getVal(), newUnitId.getVal(), isMeleeOverride);
        if (!isMeleeOverride) {
            // mark object with special field
            ObjMod.Obj.Mod mod = new ObjMod.Obj.Mod(MetaFieldId.valueOf("wurs"), ObjMod.ValType.INT, War3Int.valueOf(ProgramState.GENERATED_BY_WURST));
            objDef.addMod(mod);
        }
        String key = globalState.addObjectDefinition(objDef);

        return makeKey(key);
    }

    private ObjMod.Obj newDefFromFiletype(ObjMod<? extends ObjMod.Obj> dataStore, int base, int newId, boolean isMeleeOverride) {
        if (isMeleeOverride) {
            ObjId id = ObjId.valueOf(ObjectHelper.objectIdIntToString(newId));
            // same id => modify melee/original definition table
            ObjMod.Obj existing = dataStore.getObjs().get(id);
            if (existing != null) {
                return existing;
            }
            return dataStore.addObj(id, null);
        }
        ObjId baseIdS = ObjId.valueOf(ObjectHelper.objectIdIntToString(base));
        ObjId newIdS = ObjId.valueOf(ObjectHelper.objectIdIntToString(newId));
        dataStore.removeObj(newIdS);
        return dataStore.addObj(newIdS, baseIdS);
    }


    public void ObjectDefinition_setInt(ILconstTuple unitType, ILconstString modification, ILconstInt value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.INT, War3Int.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setString(ILconstTuple unitType, ILconstString modification, ILconstString value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.STRING, War3String.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setReal(ILconstTuple unitType, ILconstString modification, ILconstReal value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.REAL, War3Real.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setUnreal(ILconstTuple unitType, ILconstString modification, ILconstReal value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.UNREAL, War3Real.valueOf(value.getVal()));
    }


    public void ObjectDefinition_setLvlInt(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.INT, level.getVal(), War3Int.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setLvlString(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstString value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.STRING, level.getVal(), War3String.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setLvlReal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstReal value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.REAL, level.getVal(), War3Real.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setLvlUnreal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstReal value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.UNREAL, level.getVal(), War3Real.valueOf(value.getVal()));
    }


    public void ObjectDefinition_setLvlDataInt(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstInt value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.INT, level.getVal(), dataPointer.getVal(), War3Int.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setLvlDataString(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstString value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.STRING, level.getVal(), dataPointer.getVal(), War3String.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setLvlDataReal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstReal value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.REAL, level.getVal(), dataPointer.getVal(), War3Real.valueOf(value.getVal()));
    }

    public void ObjectDefinition_setLvlDataUnreal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstReal value) {
        ObjMod.Obj od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, ObjMod.ValType.UNREAL, level.getVal(), dataPointer.getVal(), War3Real.valueOf(value.getVal()));
    }


    private <T> void modifyObject(ObjMod.Obj od, ILconstString modification, ObjMod.ValType variableType, DataType value) {
        modifyObject(od, modification, variableType, 1, 0, value);
    }

    private <T> void modifyObject(ObjMod.Obj od, ILconstString modification, ObjMod.ValType variableType, int level, DataType value) {
        modifyObject(od, modification, variableType, level, 0, value);
    }

    private void modifyObject(ObjMod.Obj od, ILconstString modification, ObjMod.ValType variableType, int level, int datapointer, DataType value) {
        String modificationId = modification.getVal();
        ObjMod.Obj.Mod foundMod = null;
        for (ObjMod.Obj.Mod m : od.getMods()) {
            if (m instanceof ObjMod.Obj.ExtendedMod) {
                ObjMod.Obj.ExtendedMod extMod = (ObjMod.Obj.ExtendedMod) m;
                if (extMod.getId().getVal().equals(modificationId) && extMod.getLevel() == level && extMod.getDataPt() == datapointer) {
                    // How to set data???
                    foundMod = extMod;
                    break;
                }
            }

        }

        // create new modification:
        if (foundMod != null) {
            od.remove(foundMod);
        }
        od.addMod(new ObjMod.Obj.ExtendedMod(MetaFieldId.valueOf(modificationId), variableType, value, level, datapointer));

    }

    private String getKey(ILconstTuple unitType) {
        return ((ILconstString) unitType.getValue(0)).getVal();
    }

    public void compileError(ILconstString msg) {
        throw new InterpreterException(msg.getVal());
    }

    public ILconstString getMapName() {
        return new ILconstString(projectConfigData.buildMapData().name());
    }

    public ILconstString getBuildDate() {
        return new ILconstString(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString());
    }

    public ILconstBool isProductionBuild() {
        return isProd ? ILconstBool.TRUE : ILconstBool.FALSE;
    }

    private int sqliteHandleCounter = 0;
    private final Map<Integer, Connection> sqliteConnections = new HashMap<>();
    private final Map<Integer, PreparedStatement> sqliteStatements = new HashMap<>();
    private final Map<Integer, ResultSet> sqliteResultSets = new HashMap<>();
    private final Map<Integer, Integer> sqliteStatementConnections = new HashMap<>();
    private final Set<Integer> sqliteExecutedStatements = new HashSet<>();

    private Connection sqliteConnection(int handle) {
        Connection connection = sqliteConnections.get(handle);
        if (connection == null) {
            throw new InterpreterException("Invalid SQLite connection handle: " + handle);
        }
        return connection;
    }

    private PreparedStatement sqliteStatement(int handle) {
        PreparedStatement statement = sqliteStatements.get(handle);
        if (statement == null) {
            throw new InterpreterException("Invalid SQLite statement handle: " + handle);
        }
        return statement;
    }

    public ILconstInt sqlite_open(ILconstString path) {
        String dbPath = path.getVal();
        // SQLite "file:" URI paths can carry query parameters such as
        // "?enable_load_extension=true" that would turn on extension loading and let
        // load_extension() dlopen arbitrary native code on the build machine at compiletime.
        // Reject query parameters on the URI form. Plain file paths may legitimately contain
        // '?' (e.g. on Linux), so only the URI form is restricted; the explicit
        // enableLoadExtension(false) below is the defense-in-depth backstop for every path.
        if (dbPath.startsWith("file:") && dbPath.indexOf('?') >= 0) {
            throw new InterpreterException("Invalid SQLite database URI (query parameters are not allowed): " + dbPath);
        }
        try {
            // Defense-in-depth: explicitly disable extension loading regardless of the path.
            SQLiteConfig config = new SQLiteConfig();
            config.enableLoadExtension(false);
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath, config.toProperties());
            int handle = ++sqliteHandleCounter;
            sqliteConnections.put(handle, conn);
            return new ILconstInt(handle);
        } catch (SQLException e) {
            throw new InterpreterException("Failed to open SQLite database " + dbPath + ": " + e.getMessage());
        }
    }

    public ILconstInt sqlite_prepare(ILconstInt connection, ILconstString query) {
        Connection conn = sqliteConnection(connection.getVal());
        try {
            PreparedStatement stmt = conn.prepareStatement(query.getVal());
            int handle = ++sqliteHandleCounter;
            sqliteStatements.put(handle, stmt);
            sqliteStatementConnections.put(handle, connection.getVal());
            return new ILconstInt(handle);
        } catch (SQLException e) {
            throw new InterpreterException("Failed to prepare SQLite statement: " + e.getMessage());
        }
    }

    /**
     * Invalidates any prior execution of the statement so that the next sqlite_step
     * re-executes it. Called after a parameter is (re)bound: without this, binding new
     * values after a step (but before a sqlite_reset) would be silently ignored and the
     * new values never queried/written. Note this also closes any open result set, so a
     * column must be read before the next bind — the contract is bind, step, read, then
     * (re)bind and step again.
     */
    private void markStatementForReexecution(int handle) {
        sqliteExecutedStatements.remove(handle);
        ResultSet rs = sqliteResultSets.remove(handle);
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
    }

    // Parameter indices are 1-based, mirroring the SQLite C API (sqlite3_bind_*).
    public void sqlite_bind_int(ILconstInt statement, ILconstInt index, ILconstInt value) {
        PreparedStatement stmt = sqliteStatement(statement.getVal());
        try {
            stmt.setInt(index.getVal(), value.getVal());
            markStatementForReexecution(statement.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to bind int: " + e.getMessage());
        }
    }

    public void sqlite_bind_real(ILconstInt statement, ILconstInt index, ILconstReal value) {
        PreparedStatement stmt = sqliteStatement(statement.getVal());
        try {
            stmt.setDouble(index.getVal(), (double) value.getVal());
            markStatementForReexecution(statement.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to bind real: " + e.getMessage());
        }
    }

    public void sqlite_bind_string(ILconstInt statement, ILconstInt index, ILconstString value) {
        PreparedStatement stmt = sqliteStatement(statement.getVal());
        try {
            stmt.setString(index.getVal(), value.getVal());
            markStatementForReexecution(statement.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to bind string: " + e.getMessage());
        }
    }

    public ILconstBool sqlite_step(ILconstInt statement) {
        PreparedStatement stmt = sqliteStatement(statement.getVal());
        try {
            ResultSet rs = sqliteResultSets.get(statement.getVal());
            if (rs == null) {
                if (sqliteExecutedStatements.contains(statement.getVal())) {
                    return ILconstBool.FALSE;
                }
                boolean hasResultSet = stmt.execute();
                sqliteExecutedStatements.add(statement.getVal());
                if (hasResultSet) {
                    rs = stmt.getResultSet();
                    sqliteResultSets.put(statement.getVal(), rs);
                    boolean hasRow = rs.next();
                    return hasRow ? ILconstBool.TRUE : ILconstBool.FALSE;
                } else {
                    return ILconstBool.FALSE;
                }
            } else {
                boolean hasRow = rs.next();
                return hasRow ? ILconstBool.TRUE : ILconstBool.FALSE;
            }
        } catch (SQLException e) {
            throw new InterpreterException("Failed to step SQLite statement: " + e.getMessage());
        }
    }

    public ILconstInt sqlite_column_count(ILconstInt statement) {
        try {
            ResultSet rs = sqliteResultSets.get(statement.getVal());
            if (rs != null) {
                return new ILconstInt(rs.getMetaData().getColumnCount());
            }
            PreparedStatement stmt = sqliteStatement(statement.getVal());
            java.sql.ResultSetMetaData meta = stmt.getMetaData();
            return new ILconstInt(meta == null ? 0 : meta.getColumnCount());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to get column count: " + e.getMessage());
        }
    }

    // Column indices are 0-based, mirroring the SQLite C API (sqlite3_column_*); the +1
    // converts to JDBC's 1-based ResultSet indexing. Note this asymmetry with the 1-based
    // bind indices above — both match the C API.
    public ILconstInt sqlite_column_int(ILconstInt statement, ILconstInt index) {
        ResultSet rs = sqliteResultSets.get(statement.getVal());
        if (rs == null) throw new InterpreterException("No result set for statement handle: " + statement.getVal());
        try {
            // WurstScript int is 32-bit; a 64-bit SQLite INTEGER is truncated by getInt.
            return new ILconstInt(rs.getInt(index.getVal() + 1));
        } catch (SQLException e) {
            throw new InterpreterException("Failed to get column int: " + e.getMessage());
        }
    }

    public ILconstReal sqlite_column_real(ILconstInt statement, ILconstInt index) {
        ResultSet rs = sqliteResultSets.get(statement.getVal());
        if (rs == null) throw new InterpreterException("No result set for statement handle: " + statement.getVal());
        try {
            return new ILconstReal((float) rs.getDouble(index.getVal() + 1));
        } catch (SQLException e) {
            throw new InterpreterException("Failed to get column real: " + e.getMessage());
        }
    }

    public ILconstString sqlite_column_string(ILconstInt statement, ILconstInt index) {
        ResultSet rs = sqliteResultSets.get(statement.getVal());
        if (rs == null) throw new InterpreterException("No result set for statement handle: " + statement.getVal());
        try {
            // A SQL NULL maps to "" here; use sqlite_column_is_null to distinguish NULL
            // from an empty string / zero value.
            String val = rs.getString(index.getVal() + 1);
            return new ILconstString(val == null ? "" : val);
        } catch (SQLException e) {
            throw new InterpreterException("Failed to get column string: " + e.getMessage());
        }
    }

    public ILconstBool sqlite_column_is_null(ILconstInt statement, ILconstInt index) {
        ResultSet rs = sqliteResultSets.get(statement.getVal());
        if (rs == null) throw new InterpreterException("No result set for statement handle: " + statement.getVal());
        try {
            rs.getObject(index.getVal() + 1);
            return rs.wasNull() ? ILconstBool.TRUE : ILconstBool.FALSE;
        } catch (SQLException e) {
            throw new InterpreterException("Failed to check column null: " + e.getMessage());
        }
    }

    public void sqlite_reset(ILconstInt statement) {
        // Mirror SQLite's sqlite3_reset(): reset execution state so the statement can be
        // stepped again, but leave bound parameters in place. Clearing bindings here would
        // break the common reuse pattern of binding once and re-stepping. Use
        // sqlite_clear_bindings to explicitly clear parameters (sqlite3_clear_bindings()).
        sqliteStatement(statement.getVal());
        try {
            ResultSet rs = sqliteResultSets.remove(statement.getVal());
            if (rs != null) rs.close();
            sqliteExecutedStatements.remove(statement.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to reset SQLite statement: " + e.getMessage());
        }
    }

    public void sqlite_clear_bindings(ILconstInt statement) {
        PreparedStatement stmt = sqliteStatement(statement.getVal());
        try {
            stmt.clearParameters();
        } catch (SQLException e) {
            throw new InterpreterException("Failed to clear SQLite statement bindings: " + e.getMessage());
        }
    }

    public void sqlite_finalize(ILconstInt statement) {
        PreparedStatement stmt = sqliteStatements.remove(statement.getVal());
        if (stmt == null) throw new InterpreterException("Invalid SQLite statement handle: " + statement.getVal());
        sqliteStatementConnections.remove(statement.getVal());
        sqliteExecutedStatements.remove(statement.getVal());
        try {
            ResultSet rs = sqliteResultSets.remove(statement.getVal());
            if (rs != null) rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new InterpreterException("Failed to finalize SQLite statement: " + e.getMessage());
        }
    }

    public void sqlite_close(ILconstInt connection) {
        Connection conn = sqliteConnections.remove(connection.getVal());
        if (conn == null) throw new InterpreterException("Invalid SQLite connection handle: " + connection.getVal());
        // Finalize every statement belonging to this connection, then always close the
        // connection itself. A failure finalizing one statement must not abort the loop or
        // skip conn.close() — otherwise the connection (already removed from the map above)
        // would leak permanently, unreachable even by closeAllSqliteResources().
        List<String> errors = new ArrayList<>();
        for (int statement : sqliteStatementConnections.entrySet().stream()
                .filter(entry -> entry.getValue().intValue() == connection.getVal())
                .map(Map.Entry::getKey)
                .toList()) {
            try {
                sqlite_finalize(new ILconstInt(statement));
            } catch (InterpreterException e) {
                errors.add(e.getMessage());
            }
        }
        try {
            conn.close();
        } catch (SQLException e) {
            errors.add("Failed to close SQLite connection: " + e.getMessage());
        }
        if (!errors.isEmpty()) {
            throw new InterpreterException("Errors while closing SQLite connection: " + String.join("; ", errors));
        }
    }

    public void sqlite_exec(ILconstInt connection, ILconstString query) {
        Connection conn = sqliteConnection(connection.getVal());
        try {
            // Delegate to SQLite's native sqlite3_exec (via the xerial driver's low-level
            // DB handle) so SQLite's own parser handles statement boundaries. JDBC's
            // Statement.execute runs only the FIRST statement of a ';'-separated script,
            // and a hand-rolled splitter cannot correctly handle trigger BEGIN...END
            // bodies, CASE...END, or every identifier-quoting form ([id], `id`, "id").
            SQLiteConnection sqliteConn = conn.unwrap(SQLiteConnection.class);
            sqliteConn.getDatabase()._exec(query.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to exec SQLite query: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        closeAllSqliteResources();
    }

    /**
     * Closes all open SQLite resources (result sets, statements, connections).
     * Called by the interpreter on shutdown to prevent resource leaks.
     */
    public void closeAllSqliteResources() {
        for (ResultSet rs : sqliteResultSets.values()) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        sqliteResultSets.clear();
        for (PreparedStatement stmt : sqliteStatements.values()) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        sqliteStatements.clear();
        sqliteStatementConnections.clear();
        sqliteExecutedStatements.clear();
        for (Connection conn : sqliteConnections.values()) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
        sqliteConnections.clear();
        // Deliberately do NOT reset sqliteHandleCounter: keeping it monotonic means a stale
        // handle from a previous run can never silently alias a freshly-allocated resource
        // if this provider instance is ever reused after close().
    }
}
