package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassSlots;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NativeFunc;
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
		if (v.getParent() instanceof ClassSlots) {
			return !v.attrIsStatic();
		}
		return false;
	}

	public static boolean calculate(LocalVarDef v) {
		return false;
	}

	public static boolean calculate(FuncDef f) {
		if (f.getParent() instanceof ClassSlots) {
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
