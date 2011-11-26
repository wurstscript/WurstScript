package de.peeeq.wurstscript.attributes;

import java.util.List;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;

public class OverriddenFunctions {

	public static Collection<FuncDefInstance> calculate(FunctionDefinition f) {
		if (!f.attrIsOverride()) {
			return Collections.emptyList();
		}
		String funcName = f.getSignature().getName();
		Collection<FuncDefInstance> result = Lists.newLinkedList();
		
		ClassOrModule c = f.attrNearestClassOrModule();
		for (ClassSlot s : c.getSlots()) {
			if (s instanceof ModuleUse) {
				ModuleUse moduleUse = (ModuleUse) s;
				ModuleDef usedModule = moduleUse.attrModuleDef();
				ImmutableList<ClassOrModule> context = ImmutableList.of(c, usedModule);
				Multimap<String, FuncDefInstance> functionsInModule = usedModule.attrScopePublicFunctions();
				Collection<FuncDefInstance> overridenFunctions = functionsInModule.get(funcName);
				for (FuncDefInstance f2 : overridenFunctions) {
					result.add(FuncDefInstance.create(f2.getDef(), context.cons(f2.getContext())));
					for (FuncDefInstance f3 : f2.getDef().attrOverriddenFunctions()) {
						result.add(FuncDefInstance.create(f3.getDef(), context.cons(f3.getContext())));
					}
 				}
				
			}
		}
		return result;
	}

}
