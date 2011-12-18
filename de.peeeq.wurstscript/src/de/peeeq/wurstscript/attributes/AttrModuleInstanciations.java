package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.PackageOrGlobal;

public class AttrModuleInstanciations {

	public static ModuleDef getModuleOrigin(ModuleInstanciation mi) {
		PackageOrGlobal p = mi.getParent().attrNearestPackage();
		NameDef def = p.attrScopeNames().get(mi.getName());
		if (def instanceof ModuleDef) {
			return (ModuleDef) def;
		}
		attr.addError(mi.getSource(), "Could not resolve module definition of instanciation for module " + mi.getName());
		return null;
	}

}
