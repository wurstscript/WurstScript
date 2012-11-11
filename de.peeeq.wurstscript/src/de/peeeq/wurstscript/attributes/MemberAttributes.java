package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.StructureDef;

public class MemberAttributes {

	public static List<ModuleInstanciation> getModuleInstanciations(StructureDef c) {
		return c.getModuleInstanciations();
	}

	public static List<ConstructorDef> getConstructors(StructureDef c) {
		return c.getConstructors();
	}

}
