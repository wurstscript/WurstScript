package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.OnDestroyDef;

public class IsDynamicContext {

	public static boolean calculate(AstElement elem) {
		while (elem != null) {
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
		}
		return false;
	}

}
