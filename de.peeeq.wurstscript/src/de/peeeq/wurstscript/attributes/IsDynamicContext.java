package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.OnDestroyDef;

/**
 * a dynamic context is a place where we have a 'this' variable 
 */
public class IsDynamicContext {

	public static boolean calculate(AstElement elem) {
		if (elem instanceof FuncDef) {
			FuncDef funcDef = (FuncDef) elem;
			return funcDef.attrIsDynamicClassMember();
		} else if (elem instanceof ConstructorDef) {
			return true;
		} else if (elem instanceof OnDestroyDef) {
			return true;
		} else if (elem instanceof GlobalVarDef) {
			GlobalVarDef g = (GlobalVarDef) elem;
			return g.attrIsDynamicClassMember();
		}
		elem = elem.getParent();
		if (elem != null) {
			return elem.attrIsDynamicContext();
		}
		return false;
	}
}
