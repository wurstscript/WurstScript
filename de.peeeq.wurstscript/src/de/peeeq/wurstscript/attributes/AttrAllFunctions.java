package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ClassSlots;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.utils.Utils;

public class AttrAllFunctions {

	/**
	 * calculates all functions in a class or module
	 * @param term
	 * @return
	 */
	public static Map<String, FuncDef> calculate(ClassOrModuleOrModuleInstanciation term) {
		Map<String, FuncDef> result = getAllFunctions(term.getSlots(), term.getSource());
		return result;
	}

	private static Map<String, FuncDef> getAllFunctions(ClassSlots slots, WPos mainSource) {
		
		Map<String, FuncDef> sameLevelFunctions = Maps.newHashMap();
		Multimap<String, FuncDef> moduleFunctions = HashMultimap.create();
		
		for (ClassSlot s : slots) {
			if (s instanceof FuncDef) {
				FuncDef f = (FuncDef) s;
				FuncDef prevDefined = sameLevelFunctions.put(f.getSignature().getName(), f);
				if (prevDefined != null) {
					attr.addError(f.getSource(), "The function " + f.getSignature().getName() + " is already defined.");
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
				CheckHelper.checkIfIsRefinement(overridingFunc, moduleFunctions.get(funcName));
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

	
	
	
//	public static Multimap<String, FunctionDefinition> calculate(ClassOrModule term) {
//
//		final Multimap<String, FunctionDefinition> result = ArrayListMultimap.create();
//		Multimap<String, FunctionDefinition> sameLevelFunctions = HashMultimap.create();
//		
//		ImmutableList<ClassOrModule> context = ImmutableList.of(term);
//		
//		for (ClassSlot e : term.getSlots()) {
//			if (e instanceof FunctionDefinition) {
//				FunctionDefinition f = (FunctionDefinition) e;
//				ImmutableList<ClassOrModule> usePath = ImmutableList.of(term);
//				sameLevelFunctions.put(f.getSignature().getName(), FunctionDefinition.create(f, usePath));
//			} else if (e instanceof ModuleUse) {
//				ModuleUse moduleUse = (ModuleUse) e;
//				ModuleDef usedModule = moduleUse.attrModuleDef();
//				if (usedModule == null) {
//					continue;
//				}
//				// add functions from module:
//				Multimap<String, FunctionDefinition> functionsInModule = usedModule.attrAllFunctions();
//				for (Entry<String, FunctionDefinition> f : functionsInModule.entries()) {
//					FunctionDefinition newF = f.getValue().inContext(context);
//					// TODO if module use is private, change visibility here
//					result.put(f.getKey(), newF);
//				}
//			}
//		}
//		// check that each function on this level is defined only once
//		for (String funcName : sameLevelFunctions.keySet()) {
//			Collection<FunctionDefinition> functionsWithName = sameLevelFunctions.get(funcName);
//			if (functionsWithName.size() > 1) {
//				WPos source = null;
//				for (FunctionDefinition f : functionsWithName) {
//					source  = f.getDef().getSource();
//				}
//				attr.addError(source, "Function " + funcName + " is already defined.");
//			}
//		}
//		// handle overriding and the like
//		for (Entry<String, FunctionDefinition> f : sameLevelFunctions.entries()) {
//			String funcName = f.getKey();
//			FunctionDefinition FunctionDefinition = f.getValue();
//			FunctionDefinition funcDef = FunctionDefinition.getDef();
//			if (funcDef.attrIsOverride()) {
//				// remove all the functions which are already defined
//				Collection<FunctionDefinition> removed =  Lists.newLinkedList();// result.removeAll(funcName); // TODO check if this function really removes everything
//				for (FunctionDefinition o_f : result.get(funcName)) {
//					if (!o_f.isPrivate()) {
//						removed.add(o_f);
//						
//					}
//				}
//				
//				for (FunctionDefinition removedF : removed) {
//					boolean r = result.remove(funcName, removedF);
//					if (!r) throw new Error("could not remove " + funcName);
//				}
//				
//				if (removed.size() == 0) {
//					attr.addError(funcDef.getSource(), "There is nothing to override here.");
//				}
//				for (FunctionDefinition overriddenFunction : removed) {
//					FunctionDefinition overriddenFuncDef = overriddenFunction.getDef();
//					CheckHelper.checkIfIsRefinement(funcDef, overriddenFuncDef);
//				}
//				
//			} 
//			result.put(funcName, FunctionDefinition);
//		}
//		
//		// check if we have functions in the result which are defined several times:
//		for (String funcName : result.keySet()) {
//			Collection<FunctionDefinition> functionsWithName = result.get(funcName);
//			if (functionsWithName.size() > 1) {
//				WPos source = null;
//				for (FunctionDefinition f : functionsWithName) {
//					source  = f.getDef().getSource();
//				}
//				attr.addError(source, "Function " + funcName + " is defined multiple times. " +
//						"Try to override it!");
//			}
//		}
//		
//		return result;
//		
//	}

}
