package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.PackageOrGlobal;

public class AttrModuleInstanciations {

	public static ModuleDef getModuleOrigin(ModuleInstanciation mi) {
		return NameResolution.searchTypedNameGetOne(ModuleDef.class, mi.getName(), mi.getParent());
	}

}
