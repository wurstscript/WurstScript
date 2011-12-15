package de.peeeq.wurstscript.attributes;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;

public class MemberAttributes {

	public static List<ModuleInstanciation> getModuleInstanciations(ClassOrModuleOrModuleInstanciation c) {
		List<ModuleInstanciation> result = Lists.newLinkedList();
		for (ClassSlot s : c.getSlots()) {
			if (s instanceof ModuleInstanciation) {
				result.add((ModuleInstanciation) s);
			}
		}
		return result;
	}

	public static List<ConstructorDef> getConstructors(ClassOrModuleOrModuleInstanciation c) {
		List<ConstructorDef> result = Lists.newLinkedList();
		for (ClassSlot s : c.getSlots()) {
			if (s instanceof ConstructorDef) {
				result.add((ConstructorDef)s);
			}
		}
		return result;
	}

}
