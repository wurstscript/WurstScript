package de.peeeq.pscript.intermediateLang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.peeeq.pscript.attributes.AttrVarDefType;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.NativeFunc;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.utils.NotNullList;
import de.peeeq.pscript.utils.Utils;

/**
 * represents the program in the intermediate language 
 *
 */
public class ILprog {
	List<ILvar> globals = new NotNullList<ILvar>();
	List<ILfunction> functions = new NotNullList<ILfunction>();
	Map<String, String> nativeTranslations = new HashMap<String, String>();
	Set<String> names = new HashSet<String>();
	
	private Map<VarDef, ILvar> globalVarsTable = new HashMap<VarDef, ILvar>();
	private Map<FuncDef, ILfunction> functionTable = new HashMap<FuncDef, ILfunction>();
	private AttributeManager attributeManager;
	
	
	
	public ILprog(AttributeManager attributeManager) {
		this.attributeManager= attributeManager;
	}
	
	public void addNativeTranslation(String name, String origName) {
		nativeTranslations.put(name, origName);		
	}

	
	public List<ILfunction> getFunctions() {
		return functions;
	}


	public Map<String, String> getNativeTranslations() {
		return nativeTranslations;
	}


	public Set<String> getNames() {
		return names;
	}


	public String lookupNativeTranslation(PscriptType t) {
		// TODO
		return t.getName();
	}
	

	public String getNewName(String name) {
		String newName = name;
		int i = 0;
		// maybe this could be more efficient but I do not expect too many name conflicts
		while (names.contains(newName)) {
			newName = newName + i++;
		}
		return name;
	}



	public List<ILvar> getGlobals() {
		return globals;
	}





	public ILvar getGlobalVarDef(VarDef decl) {
		if (globalVarsTable.containsKey(decl)) {
			return globalVarsTable.get(decl);
		}
		String name = getNewName(Utils.getParent(decl, PackageDeclaration.class).getName() + "_" + decl.getName() );
		
		PscriptType type = attributeManager.getAttValue(AttrVarDefType.class, decl);
		ILvar v = new ILvar(name, type);
		globalVarsTable.put(decl, v);
		globals.add(v);		
		return v;
	}


	public String getFuncDefName(FuncDef func) {
		return getFunc(func).getName();
	}


	public ILfunction getFunc(FuncDef func) {
		if (func instanceof NativeFunc) {
			return new ILfunction(func.getName());
		}
		
		if (functionTable.containsKey(func)) {
			return functionTable.get(func);
		}
		String name = getNewName(Utils.getParent(func, PackageDeclaration.class).getName() + "_" + func.getName() );
		ILfunction f = new ILfunction(name);
		functionTable.put(func, f);
		functions.add(f);
		return f;
	}

	public void addFunction(ILfunction function) {
		functions.add(function);
		// TODO anything more todo here?
	}


}
