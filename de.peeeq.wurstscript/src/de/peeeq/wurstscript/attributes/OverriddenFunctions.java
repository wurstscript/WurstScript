package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.NotExtensionFunction;

public class OverriddenFunctions {

	public static Collection<FunctionDefinition> calculate(FunctionDefinition f) {
		if (!f.attrIsOverride()) {
			return Collections.emptyList();
		}
		String funcName = f.getSignature().getName();
		Collection<FunctionDefinition> result = Lists.newLinkedList();

		ClassOrModule c = f.attrNearestClassOrModule();
		for (ClassSlot s : c.getSlots()) {
			if (s instanceof ModuleUse) {
				ModuleUse moduleUse = (ModuleUse) s;
				ModuleDef usedModule = moduleUse.attrModuleDef();
				Map<String, NotExtensionFunction> functionsInModule = usedModule.attrScopePublicFunctions();
				FunctionDefinition overridenFunction = functionsInModule.get(funcName);
				result.add(overridenFunction);
				for (FunctionDefinition f3 : overridenFunction.attrOverriddenFunctions()) {
					result.add(f3);
				}

			}
		}
		return result;
	}

}
