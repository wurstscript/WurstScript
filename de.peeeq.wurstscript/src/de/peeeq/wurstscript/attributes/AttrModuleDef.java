package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;

public class AttrModuleDef {

	public static ModuleDef calculate(ModuleUse moduleUse) {
		String moduleName = moduleUse.getModuleName();
		List<ModuleDef> modules = NameResolution.searchTypedName(ModuleDef.class, moduleName, moduleUse.attrNearestPackage());
		if (modules.size() == 0) {
			attr.addError(moduleUse.getSource(), "Module " + moduleUse.getModuleName() + " could not be found.");
			return null;
		} else {
			return modules.get(0);
		}
		
	}

}
