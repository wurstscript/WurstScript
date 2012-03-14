package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.StructureDefOrModuleInstanciation;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.WParameter;

/**
 * calculates wether a variable is a class member 
 */
public class AttrIsClassMember {

	public static boolean calculate(WParameter v) {
		return false;
	}

	public static boolean calculate(GlobalVarDef v) {
		if (v.attrNearestNamedScope() instanceof StructureDefOrModuleInstanciation) {
			return !v.attrIsStatic();
		}
		return false;
	}

	public static boolean calculate(LocalVarDef v) {
		return false;
	}

	public static boolean calculate(FuncDef f) {
		if (f.attrNearestNamedScope() instanceof StructureDefOrModuleInstanciation) {
			return !f.attrIsStatic();
		}
		return false;
	}

	public static boolean calculate(ExtensionFuncDef f) {
		return false;
	}

	public static boolean calculate(NativeFunc nativeFunc) {
		return false;
	}

	public static boolean calculate(TupleDef t) {
		return false;
	}

}
