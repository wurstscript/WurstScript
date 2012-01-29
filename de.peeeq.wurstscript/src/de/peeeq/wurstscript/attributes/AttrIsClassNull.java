package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.types.PscriptTypeTypeParam;

public class AttrIsClassNull {

	public static boolean calculate(ExprNull expr) {
		return expr.attrExpectedTyp() instanceof PscriptTypeNamedScope
				|| expr.attrExpectedTyp() instanceof PscriptTypeTypeParam;
	}

}
