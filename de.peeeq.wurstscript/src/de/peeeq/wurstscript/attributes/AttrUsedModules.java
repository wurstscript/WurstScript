package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;

public class AttrUsedModules {

	public static ImmutableCollection<ModuleDef> calculate(ClassOrModule c) {
		ImmutableCollection.Builder<ModuleDef> result = ImmutableList.builder();
		for (ModuleUse moduleUse : c.getModuleUses()) {
			ModuleDef usedModule = moduleUse.attrModuleDef();
			if (usedModule != null) {
				result.add(usedModule);
			}
		}
		return result.build();
	}

}
