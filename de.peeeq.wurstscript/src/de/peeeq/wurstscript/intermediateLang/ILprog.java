package de.peeeq.wurstscript.intermediateLang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.NotNullList;

/**
 * represents the program in the intermediate language 
 *
 */
public class ILprog {
	List<ILvar> globals = new NotNullList<ILvar>();
	List<ILfunction> functions = new NotNullList<ILfunction>();
	Map<String, String> nativeTranslations = new HashMap<String, String>();
	Set<String> names = new HashSet<String>();
	
	
	
	
	public ILprog() {
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


	public void addFunction(ILfunction function) {
		functions.add(function);
		// TODO anything more todo here?
	}


}
