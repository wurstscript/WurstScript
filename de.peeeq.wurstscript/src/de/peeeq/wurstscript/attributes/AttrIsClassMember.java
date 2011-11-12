package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.WParameter;

/**
 * calculates wether a variable is a class member 
 */
public class AttrIsClassMember {

	public static boolean calculate(WParameter v) {
		return false;
	}

	public static boolean calculate(GlobalVarDef v) {
		if (v.getParent() instanceof ClassSlot) {
			// TODO static members?
			return true;
		}
		return false;
	}

	public static boolean calculate(LocalVarDef v) {
		return false;
	}

}
