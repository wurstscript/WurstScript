package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;

public class AttrUsedModules {

	public static Collection<ModuleDef> calculate(ClassOrModule c) {
		Collection<ModuleDef> result = Lists.newLinkedList();
		for (ClassSlot s : c.getSlots()) {
			if (s instanceof ModuleUse) {
				ModuleUse moduleUse = (ModuleUse) s;
				ModuleDef usedModule = moduleUse.attrModuleDef();
				if (usedModule != null) {
					result.add(usedModule);
				}
			}
		}
		return result;
	}

}
