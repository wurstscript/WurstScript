package de.peeeq.wurstscript.parser;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.ast.TypeParams;
import de.peeeq.wurstscript.attributes.attr;

public class AstHelper {

	public static TypeParamDefs transformTypeParamsToTyppeParamDefs(TypeParams p) {
		TypeParamDefs result = Ast.TypeParamDefs();
		for (TypeExpr t : p) {
			if (t instanceof TypeExprSimple) {
				TypeExprSimple ts = (TypeExprSimple) t;
				result.add(Ast.TypeParamDef(t.getSource().copy(), Ast.Modifiers(), ts.getTypeName()));
			} else {
				attr.addError(t.getSource(), "Type Parameters must be simple names.");
			}
		}
		return result;
	}

}
