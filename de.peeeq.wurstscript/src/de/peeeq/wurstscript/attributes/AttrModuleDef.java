package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.NameDef;

public class AttrModuleDef {

	public static ModuleDef calculate(ModuleUse moduleUse) {
		Map<String, NameDef> names = moduleUse.attrNearestPackage().attrScopeNames();
		NameDef def = names.get(moduleUse.getModuleName());
		if (def == null) {
			attr.addError(moduleUse.getSource(), "Module " + moduleUse.getModuleName() + " could not be found.");
		} else if (def instanceof ModuleDef) {
			return (ModuleDef) def;
		} else {
			attr.addError(moduleUse.getSource(), "Only modules can be used. " + moduleUse.getModuleName() + " is a " + def.getClass().getSimpleName());
		}
		return null;
	}

}
