package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;

public class AttrUsedModules {

	public static Collection<ModuleDef> calculate(ClassOrModule c) {
		Collection<ModuleDef> result = Lists.newArrayList();
		for (ModuleUse moduleUse : c.getModuleUses()) {
			ModuleDef usedModule = moduleUse.attrModuleDef();
			if (usedModule != null) {
				result.add(usedModule);
			}
		}
		return result;
	}

}
