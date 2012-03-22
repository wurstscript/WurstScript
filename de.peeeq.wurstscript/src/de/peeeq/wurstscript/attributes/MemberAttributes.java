package de.peeeq.wurstscript.attributes;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.StructureDefOrModuleInstanciation;

public class MemberAttributes {

	public static List<ModuleInstanciation> getModuleInstanciations(StructureDefOrModuleInstanciation c) {
		return c.getModuleInstanciations();
	}

	public static List<ConstructorDef> getConstructors(StructureDefOrModuleInstanciation c) {
		return c.getConstructors();
	}

}
