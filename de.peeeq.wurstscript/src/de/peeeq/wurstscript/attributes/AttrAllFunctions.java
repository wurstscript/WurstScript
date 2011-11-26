package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.utils.Utils;

public class AttrAllFunctions {

	public static Multimap<String, FuncDefInstance> calculate(ClassOrModule term) {
//		final Multimap<String, FuncDefInstance> result = HashMultimap.create();
//		ImmutableList<ClassOrModule> context = ImmutableList.of(c);
//		for (ClassSlot s : c.getSlots()) {
//			if (s instanceof ModuleUse) {
//				ModuleUse moduleUse = (ModuleUse) s;
//				ModuleDef usedModule = moduleUse.attrModuleDef();
//				for (Entry<String, FuncDefInstance> f : usedModule.attrAllFunctions().entries()) {
//					result.put(f.getKey(), f.getValue().inContext(context));
//				}
//			}
//		}
//		for (ClassSlot s : c.getSlots()) {
//			if (s instanceof FuncDef) {
//				FuncDef funcDef = (FuncDef) s;
//				// FIXME handle overriding of public functions
//				result.put(funcDef.getSignature().getName(), FuncDefInstance.create(funcDef, context));
//			}
//		}
//		return result ;
		final Multimap<String, FuncDefInstance> result = ArrayListMultimap.create();
		Multimap<String, FuncDefInstance> sameLevelFunctions = HashMultimap.create();
		
		ImmutableList<ClassOrModule> context = ImmutableList.of(term);
		
		for (ClassSlot e : term.getSlots()) {
			if (e instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) e;
				ImmutableList<ClassOrModule> usePath = ImmutableList.of(term);
				sameLevelFunctions.put(f.getSignature().getName(), FuncDefInstance.create(f, usePath));
			} else if (e instanceof ModuleUse) {
				ModuleUse moduleUse = (ModuleUse) e;
				ModuleDef usedModule = moduleUse.attrModuleDef();
				if (usedModule == null) {
					continue;
				}
				// add functions from module:
				Multimap<String, FuncDefInstance> functionsInModule = usedModule.attrAllFunctions();
				for (Entry<String, FuncDefInstance> f : functionsInModule.entries()) {
					FuncDefInstance newF = f.getValue().inContext(context);
					// TODO if module use is private, change visibility here
					result.put(f.getKey(), newF);
				}
			}
		}
		// check that each function on this level is defined only once
		for (String funcName : sameLevelFunctions.keySet()) {
			Collection<FuncDefInstance> functionsWithName = sameLevelFunctions.get(funcName);
			if (functionsWithName.size() > 1) {
				WPos source = null;
				for (FuncDefInstance f : functionsWithName) {
					source  = f.getDef().getSource();
				}
				attr.addError(source, "Function " + funcName + " is already defined.");
			}
		}
		// handle overriding and the like
		for (Entry<String, FuncDefInstance> f : sameLevelFunctions.entries()) {
			String funcName = f.getKey();
			FuncDefInstance funcDefInstance = f.getValue();
			FunctionDefinition funcDef = funcDefInstance.getDef();
			if (funcDef.attrIsOverride()) {
				// remove all the functions which are already defined
				Collection<FuncDefInstance> removed =  Lists.newLinkedList();// result.removeAll(funcName); // TODO check if this function really removes everything
				for (FuncDefInstance o_f : result.get(funcName)) {
					System.out.println("o_f = " + Utils.printContext(o_f.getContext()) + " " + o_f.getDef().getSignature().getName());
					System.out.println("	" + o_f.getVisibility());
					if (!o_f.isPrivate()) {
						System.out.println("	remove");
						removed.add(o_f);
						
					}
				}
				
				for (FuncDefInstance removedF : removed) {
					boolean r = result.remove(funcName, removedF);
					if (!r) throw new Error("could not remove " + funcName);
				}
				
				if (removed.size() == 0) {
					attr.addError(funcDef.getSource(), "There is nothing to override here.");
				}
				for (FuncDefInstance overriddenFunction : removed) {
					FunctionDefinition overriddenFuncDef = overriddenFunction.getDef();
					CheckHelper.checkIfIsRefinement(funcDef, overriddenFuncDef);
				}
				
			} 
			result.put(funcName, funcDefInstance);
		}
		
		// check if we have functions in the result which are defined several times:
		for (String funcName : result.keySet()) {
			Collection<FuncDefInstance> functionsWithName = result.get(funcName);
			if (functionsWithName.size() > 1) {
				WPos source = null;
				for (FuncDefInstance f : functionsWithName) {
					source  = f.getDef().getSource();
				}
				attr.addError(source, "Function " + funcName + " is defined multiple times. " +
						"Try to override it!");
			}
		}
		
		return result;
		
	}

}
