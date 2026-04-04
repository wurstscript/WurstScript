package de.peeeq.wurstio.intermediateLang.interpreter;


import config.WurstProjectConfigData;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

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

        if (!isMeleeOverride && dataStore.getObjs().containsKey(ObjId.valueOf(objIdString))) {
            globalState.compilationError("Object definition with id " + objIdString + " already exists.");
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
            return dataStore.addObj(id, null);
        }
        ObjId baseIdS = ObjId.valueOf(ObjectHelper.objectIdIntToString(base));
        ObjId newIdS = ObjId.valueOf(ObjectHelper.objectIdIntToString(newId));
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
        return new ILconstString(projectConfigData.getBuildMapData().getName());
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

    public ILconstInt sqlite_open(ILconstString path) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path.getVal());
            int handle = ++sqliteHandleCounter;
            sqliteConnections.put(handle, conn);
            return new ILconstInt(handle);
        } catch (SQLException e) {
            throw new InterpreterException("Failed to open SQLite database " + path.getVal() + ": " + e.getMessage());
        }
    }

    public ILconstInt sqlite_prepare(ILconstInt connection, ILconstString query) {
        Connection conn = sqliteConnections.get(connection.getVal());
        if (conn == null) throw new InterpreterException("Invalid SQLite connection handle: " + connection.getVal());
        try {
            PreparedStatement stmt = conn.prepareStatement(query.getVal());
            int handle = ++sqliteHandleCounter;
            sqliteStatements.put(handle, stmt);
            return new ILconstInt(handle);
        } catch (SQLException e) {
            throw new InterpreterException("Failed to prepare SQLite statement: " + e.getMessage());
        }
    }

    public void sqlite_bind_int(ILconstInt statement, ILconstInt index, ILconstInt value) {
        PreparedStatement stmt = sqliteStatements.get(statement.getVal());
        if (stmt == null) throw new InterpreterException("Invalid SQLite statement handle: " + statement.getVal());
        try {
            stmt.setInt(index.getVal(), value.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to bind int: " + e.getMessage());
        }
    }

    public void sqlite_bind_real(ILconstInt statement, ILconstInt index, ILconstReal value) {
        PreparedStatement stmt = sqliteStatements.get(statement.getVal());
        if (stmt == null) throw new InterpreterException("Invalid SQLite statement handle: " + statement.getVal());
        try {
            stmt.setDouble(index.getVal(), (double) value.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to bind real: " + e.getMessage());
        }
    }

    public void sqlite_bind_string(ILconstInt statement, ILconstInt index, ILconstString value) {
        PreparedStatement stmt = sqliteStatements.get(statement.getVal());
        if (stmt == null) throw new InterpreterException("Invalid SQLite statement handle: " + statement.getVal());
        try {
            stmt.setString(index.getVal(), value.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to bind string: " + e.getMessage());
        }
    }

    public ILconstBool sqlite_step(ILconstInt statement) {
        PreparedStatement stmt = sqliteStatements.get(statement.getVal());
        if (stmt == null) throw new InterpreterException("Invalid SQLite statement handle: " + statement.getVal());
        try {
            ResultSet rs = sqliteResultSets.get(statement.getVal());
            if (rs == null) {
                boolean hasResultSet = stmt.execute();
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
            PreparedStatement stmt = sqliteStatements.get(statement.getVal());
            if (stmt == null) throw new InterpreterException("Invalid SQLite statement handle: " + statement.getVal());
            java.sql.ResultSetMetaData meta = stmt.getMetaData();
            return new ILconstInt(meta == null ? 0 : meta.getColumnCount());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to get column count: " + e.getMessage());
        }
    }

    public ILconstInt sqlite_column_int(ILconstInt statement, ILconstInt index) {
        ResultSet rs = sqliteResultSets.get(statement.getVal());
        if (rs == null) throw new InterpreterException("No result set for statement handle: " + statement.getVal());
        try {
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
            String val = rs.getString(index.getVal() + 1);
            return new ILconstString(val == null ? "" : val);
        } catch (SQLException e) {
            throw new InterpreterException("Failed to get column string: " + e.getMessage());
        }
    }

    public void sqlite_reset(ILconstInt statement) {
        PreparedStatement stmt = sqliteStatements.get(statement.getVal());
        if (stmt != null) {
            try {
                ResultSet rs = sqliteResultSets.remove(statement.getVal());
                if (rs != null) rs.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    public void sqlite_finalize(ILconstInt statement) {
        PreparedStatement stmt = sqliteStatements.remove(statement.getVal());
        if (stmt != null) {
            try {
                ResultSet rs = sqliteResultSets.remove(statement.getVal());
                if (rs != null) rs.close();
                stmt.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    public void sqlite_close(ILconstInt connection) {
        Connection conn = sqliteConnections.remove(connection.getVal());
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    public void sqlite_exec(ILconstInt connection, ILconstString query) {
        Connection conn = sqliteConnections.get(connection.getVal());
        if (conn == null) throw new InterpreterException("Invalid SQLite connection handle: " + connection.getVal());
        try (java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute(query.getVal());
        } catch (SQLException e) {
            throw new InterpreterException("Failed to exec SQLite query: " + e.getMessage());
        }
    }
}
