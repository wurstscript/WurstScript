package de.peeeq.wurstio.intermediateLang.interpreter;


import config.WurstProjectConfigData;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.ReflectionBasedNativeProvider;
import de.peeeq.wurstio.objectreader.ObjectHelper;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.app.War3Int;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

        if (dataStore.getObjs().containsKey(ObjId.valueOf(objIdString))) {
            globalState.compilationError("Object definition with id " + objIdString + " already exists.");
        }
        ObjMod.Obj objDef = newDefFromFiletype(dataStore, deriveFrom.getVal(), newUnitId.getVal());
        // mark object with special field
        ObjMod.Obj.Mod mod = new ObjMod.Obj.Mod(MetaFieldId.valueOf("wurs"), ObjMod.ValType.INT, War3Int.valueOf(ProgramState.GENERATED_BY_WURST));
        objDef.addMod(mod);
        String key = globalState.addObjectDefinition(objDef);

        return makeKey(key);
    }

    private W3U.Obj newDefFromFiletype(ObjMod<? extends ObjMod.Obj> dataStore, int base, int newId) {
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
                if (extMod.getId().getVal().equals(modificationId) && extMod.getLevel() == level) {
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
}
