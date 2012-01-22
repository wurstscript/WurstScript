package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ClassSlots;
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
		Map<String, FuncDef> result = getAllFunctions(term.getSlots(), term.getSource());
		return result;
	}

	private static Map<String, FuncDef> getAllFunctions(ClassSlots slots, WPos mainSource) {
		
		Map<String, FuncDef> sameLevelFunctions = Maps.newHashMap();
		Multimap<String, FuncDef> moduleFunctions = HashMultimap.create();
		
		for (ClassSlot s : slots) {
			if (s instanceof FuncDef) {
				FuncDef f = (FuncDef) s;
				FuncDef prevDefined = sameLevelFunctions.put(f.getName(), f);
				if (prevDefined != null) {
					attr.addError(f.getSource(), "The function " + f.getName() + " is already defined.");
				}
			} else if (s instanceof ModuleInstanciation) {
				addNonPrivateMethodsFromModule(moduleFunctions, (ModuleInstanciation) s, mainSource);
			}
		}
	
		Map<String, FuncDef> result = Maps.newHashMap();
		// add module functions which are not overridden:
		for (String funcName : moduleFunctions.keySet()) {
			if (sameLevelFunctions.containsKey(funcName)) { 
				// -> there is a overriding function
				FuncDef overridingFunc = sameLevelFunctions.get(funcName);
				if (!overridingFunc.attrIsOverride()) {
					attr.addError(overridingFunc.getSource(), "The function " + funcName + " must have the 'override' annotation.");
				}
				CheckHelper.checkIfIsRefinement(overridingFunc, moduleFunctions.get(funcName), "Cannot override function ");
				result.put(funcName, overridingFunc);
				sameLevelFunctions.remove(funcName);
			} else { // -> no overriding function
				Collection<FuncDef> funcs = moduleFunctions.get(funcName);
				if (funcs.size() > 1) {
					attr.addError(mainSource, "There are two or more functions with name " + funcName + " inherited from used modules. " +
							"You should override this function or rename one of them.");
				}
				result.put(funcName, Utils.getFirst(funcs));
			}
		}
		
		// add the remaining functions from the same level:
		result.putAll(sameLevelFunctions);
		
		
		return result;
	}

	private static void addNonPrivateMethodsFromModule(Multimap<String, FuncDef> addTo, ModuleInstanciation m, WPos mainSource) {
		Map<String, FuncDef> mFuncs = getAllFunctions(m.getSlots(), mainSource);
		for (Entry<String, FuncDef> e : mFuncs.entrySet()) {
			if (!e.getValue().attrIsPrivate()) {
				addTo.put(e.getKey(), e.getValue());
			}
		}
	}

	
}
