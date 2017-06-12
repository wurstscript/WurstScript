package de.peeeq.wurstscript.attributes;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.utils.Utils;

public class AttrModuleDef {

	public static @Nullable ModuleDef calculate(ModuleUse moduleUse) {
		String moduleName = moduleUse.getModuleName();
		TypeDef def = moduleUse.lookupType(moduleName);
		if (def instanceof ModuleDef) {
			return (ModuleDef) def;
		} else if (def != null) {
			moduleUse.addError("Found " + Utils.printElement(def) + " but wanted a module.");
		} else {
			moduleUse.addError("Module " + moduleUse.getModuleName() + " could not be found.");
		}
		return null;
	}

}
