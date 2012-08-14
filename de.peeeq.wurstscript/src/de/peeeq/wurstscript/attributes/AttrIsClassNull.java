package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;

public class AttrIsClassNull {

	// ClassNull or handlenull
	public static boolean calculate(ExprNull expr) {
		return expr.attrExpectedTyp() instanceof WurstTypeNamedScope
				|| expr.attrExpectedTyp() instanceof WurstTypeTypeParam;
	}

}
