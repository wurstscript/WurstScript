package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElementWithModuleInstanciations;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.StructureDefOrModuleInstanciation;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.utils.Utils;

public class AttrAllFunctions {

	/**
	 * calculates all functions in a class or module
	 * @param term
	 * @return
	 */
	public static Map<String, FuncDef> calculate(StructureDefOrModuleInstanciation term) {
		WPos mainSource = term.getSource();
		
		Map<String, FuncDef> sameLevelFunctions = Maps.newHashMap();
		Multimap<String, FuncDef> moduleFunctions = HashMultimap.create();
		
		for (FuncDef f : term.getMethods()) {
			FuncDef prevDefined = sameLevelFunctions.put(f.getName(), f);
			if (prevDefined != null) {
				f.addError("The function " + f.getName() + " is already defined.");
			}
		}
		if (term instanceof AstElementWithModuleInstanciations) {
			AstElementWithModuleInstanciations wmi = (AstElementWithModuleInstanciations) term;
			for (ModuleInstanciation mi : wmi.getModuleInstanciations()) {
				addNonPrivateMethodsFromModule(moduleFunctions, mi, mainSource);
			}
		}
	
		Map<String, FuncDef> result = Maps.newHashMap();
		// add module functions which are not overridden:
		for (String funcName : moduleFunctions.keySet()) {
			if (sameLevelFunctions.containsKey(funcName)) { 
				// -> there is a overriding function
				FuncDef overridingFunc = sameLevelFunctions.get(funcName);
				if (!overridingFunc.attrIsOverride()) {
					overridingFunc.addError("The function " + funcName + " must have the 'override' annotation.");
				}
				CheckHelper.checkIfIsRefinement(overridingFunc, moduleFunctions.get(funcName), "Cannot override function ");
				result.put(funcName, overridingFunc);
				sameLevelFunctions.remove(funcName);
			} else { // -> no overriding function
				Collection<FuncDef> funcs = moduleFunctions.get(funcName);
				if (funcs.size() > 1) {
					
					StringBuilder functions = new StringBuilder();
					int i=1;
					for (FuncDef f : funcs) {
						functions.append(i++ +". " + Utils.printElement(f.getParent().attrNearestNamedScope()) + "\n");
					}
					mainSource.addError("There are two or more functions with name " + funcName + " inherited from used modules. " +
					"You should override this function or rename one of them. The functions are:\n" + functions);
				}
				result.put(funcName, Utils.getFirst(funcs));
			}
		}
		
		// add the remaining functions from the same level:
		result.putAll(sameLevelFunctions);
		
		
		return result;
	}

	private static void addNonPrivateMethodsFromModule(Multimap<String, FuncDef> addTo, ModuleInstanciation m, WPos mainSource) {
		Map<String, FuncDef> mFuncs = m.attrAllFunctions();
		for (Entry<String, FuncDef> e : mFuncs.entrySet()) {
			if (!e.getValue().attrIsPrivate()) {
				addTo.put(e.getKey(), e.getValue());
			}
		}
	}

	
}
