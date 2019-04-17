package de.peeeq.wurstio.intermediateLang.interpreter;


import com.google.common.base.Preconditions;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.ReflectionBasedNativeProvider;
import de.peeeq.wurstio.objectreader.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.ILconstTuple;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.intermediatelang.interpreter.VariableType;

@SuppressWarnings("ucd") // ignore unused code detector warnings, because this class uses reflection
public class CompiletimeNatives extends ReflectionBasedNativeProvider implements NativesProvider {
    private ProgramStateIO globalState;

    public CompiletimeNatives(ProgramStateIO globalState) {
        this.globalState = globalState;
    }


    private ILconstTuple makeKey(String key) {
        return new ILconstTuple(new ILconstString(key));
    }

    public ILconstTuple createObjectDefinition(ILconstString fileType, ILconstInt newUnitId, ILconstInt deriveFrom) {
        ObjectFile unitStore = globalState.getDataStore(fileType.getVal());
        ObjectTable modifiedTable = unitStore.getModifiedTable();
        for (ObjectDefinition od : modifiedTable.getObjectDefinitions()) {
            if (od.getNewObjectId() == newUnitId.getVal()) {
                globalState.compilationError("Object definition with id " + ObjectHelper.objectIdIntToString(newUnitId.getVal()) + " already exists.");
            }
        }
        ObjectDefinition objDef = new ObjectDefinition(modifiedTable, deriveFrom.getVal(), newUnitId.getVal());
        // mark object with special field
        objDef.add(new ObjectModificationInt(objDef, "wurs", 0, 0, ProgramState.GENERATED_BY_WURST));
        String key = globalState.addObjectDefinition(objDef);
        modifiedTable.add(objDef);
        return makeKey(key);
    }


    public void ObjectDefinition_setInt(ILconstTuple unitType, ILconstString modification, ILconstInt value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.INTEGER, value.getVal());
    }

    public void ObjectDefinition_setString(ILconstTuple unitType, ILconstString modification, ILconstString value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.STRING, value.getVal());
    }

    public void ObjectDefinition_setReal(ILconstTuple unitType, ILconstString modification, ILconstReal value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.REAL, value.getVal());
    }

    public void ObjectDefinition_setUnreal(ILconstTuple unitType, ILconstString modification, ILconstReal value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.UNREAL, value.getVal());
    }


    public void ObjectDefinition_setLvlInt(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.INTEGER, level.getVal(), value.getVal());
    }

    public void ObjectDefinition_setLvlString(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstString value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.STRING, level.getVal(), value.getVal());
    }

    public void ObjectDefinition_setLvlReal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstReal value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.REAL, level.getVal(), value.getVal());
    }

    public void ObjectDefinition_setLvlUnreal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstReal value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.UNREAL, level.getVal(), value.getVal());
    }


    public void ObjectDefinition_setLvlDataInt(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstInt value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.INTEGER, level.getVal(), dataPointer.getVal(), value.getVal());
    }

    public void ObjectDefinition_setLvlDataString(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstString value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.STRING, level.getVal(), dataPointer.getVal(), value.getVal());
    }

    public void ObjectDefinition_setLvlDataReal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstReal value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.REAL, level.getVal(), dataPointer.getVal(), value.getVal());
    }

    public void ObjectDefinition_setLvlDataUnreal(ILconstTuple unitType, ILconstString modification, ILconstInt level, ILconstInt dataPointer, ILconstReal value) {
        ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
        modifyObject(od, modification, VariableType.UNREAL, level.getVal(), dataPointer.getVal(), value.getVal());
    }


    private <T> void modifyObject(ObjectDefinition od, ILconstString modification, VariableType<T> variableType, T value) {
        modifyObject(od, modification, variableType, 1, 0, value);
    }

    private <T> void modifyObject(ObjectDefinition od, ILconstString modification, VariableType<T> variableType, int level, T value) {
        modifyObject(od, modification, variableType, level, 0, value);
    }

    private <T> void modifyObject(ObjectDefinition od, ILconstString modification, VariableType<T> variableType, int level, int datapointer, T value) {
        String modificationId = modification.getVal();
        for (ObjectModification<?> m : od.getModifications()) {
            if (m.getModificationId().equals(modificationId) && m.getLevelCount() == level) {
                ObjectModification<T> m2 = m.castTo(value);
                m2.setData(value);
                return;
            }
        }
        // create new modification:
        od.add(ObjectModification.create(od, modificationId, variableType, level, datapointer, value));
    }

    private String getKey(ILconstTuple unitType) {
        return ((ILconstString) unitType.getValue(0)).getVal();
    }

    public void compileError(ILconstString msg) {
        throw new InterpreterException(msg.getVal());
    }

}
