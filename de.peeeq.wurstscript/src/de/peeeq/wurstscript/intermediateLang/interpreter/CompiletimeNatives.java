package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.ILconstTuple;
import de.peeeq.wurstscript.objectreader.BinaryDataOutputStream;
import de.peeeq.wurstscript.objectreader.ObjectDefinition;
import de.peeeq.wurstscript.objectreader.ObjectFile;
import de.peeeq.wurstscript.objectreader.ObjectFileType;
import de.peeeq.wurstscript.objectreader.ObjectModification;
import de.peeeq.wurstscript.objectreader.ObjectModificationInt;
import de.peeeq.wurstscript.objectreader.ObjectModificationString;

public class CompiletimeNatives implements NativesProvider {

	

	private ProgramState globalState;

	public CompiletimeNatives(ProgramState globalState) {
		this.globalState = globalState;
	}

	@Override
	public ILconst invoke(String funcname, ILconst[] args) {
		nextMethod: for (Method method : this.getClass().getMethods()) {
			if (method.getName().equals(funcname)) {
				// TODO check parameter types, handle overloading, only public methods
				int i = 0;
				for (Class<?> paramType : method.getParameterTypes()) {
					if (!paramType.isAssignableFrom(args[i].getClass())) {
						continue nextMethod;
					}
					i++;
				}
				
				Object r = null;
				try {
					r = method.invoke(this, (Object[]) args);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
					throw new Error(e);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof Error) {
						throw (Error) e.getCause();
					}
					throw new Error(e.getCause());
				}
				return (ILconst) r;
			}
		}
		throw new InterprationError("Compiletime function " + funcname + " is not implemented yet.");
	}
	
	public void compileError(ILconstString msg) {
		AstElement trace = globalState.getLastStatement().attrTrace();
		globalState.getGui().sendError(new CompileError(trace.attrSource(), msg.getVal()));
	}
	
	
	public ILconstTuple createUnitType(ILconstString newUnitId, ILconstString deriveFrom) {
		ObjectFile unitStore = globalState.getUnitStore();
		for (ObjectDefinition od : unitStore.getModifiedTable().getObjectDefinitions()) {
			if (od.getNewObjectId().equals(newUnitId.getVal())) {
				throw new Error("Unit with id " + newUnitId.getVal() + " already exists.");
			}
		}
		ObjectDefinition objDef = new ObjectDefinition(deriveFrom.getVal(), newUnitId.getVal());
		objDef.add(new ObjectModificationInt(objDef, "wurs", 0, 0, ProgramState.GENERATED_BY_WURST));
		String key = globalState.addObjectDefinition(objDef);		
		unitStore.getModifiedTable().add(objDef);
		return makeKey(key);
	}

	private ILconstTuple makeKey(String key) {
		return new ILconstTuple(new ILconstString(key));
	}
	
	public void unitType_setInt(ILconstTuple unitType, ILconstString modification, ILconstInt value) {
		ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
		modifyObject(od, modification, VariableType.INTEGER, value.getVal());
	}
	
	public void unitType_setString(ILconstTuple unitType, ILconstString modification, ILconstString value) {
		ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
		modifyObject(od, modification, VariableType.STRING, value.getVal());
	}
	
	public void unitType_setReal(ILconstTuple unitType, ILconstString modification, ILconstReal value) {
		ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
		modifyObject(od, modification, VariableType.REAL, value.getVal());
	}
	
	public void unitType_setUnreal(ILconstTuple unitType, ILconstString modification, ILconstReal value) {
		ObjectDefinition od = globalState.getObjectDefinition(getKey(unitType));
		modifyObject(od, modification, VariableType.UNREAL, value.getVal());
	}

	private <T> void modifyObject(ObjectDefinition od, ILconstString modification, VariableType<T> variableType, T value) {
		String modificationId = modification.getVal();
		for (ObjectModification<?> m : od.getModifications()) {
			if (m.getModificationId().equals(modificationId)) {
				ObjectModification<T> m2 = m.castTo(value);
				m2.setData(value);
				return;
			}
		}
		// create new modification:
		od.add(ObjectModification.create(od, modificationId, variableType, 0, 0, value));
	}

	private String getKey(ILconstTuple unitType) {
		return ((ILconstString)unitType.getValue(0)).getVal();
	}
	
}
