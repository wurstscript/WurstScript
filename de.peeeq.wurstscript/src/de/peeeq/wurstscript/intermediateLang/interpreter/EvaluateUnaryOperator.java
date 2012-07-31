package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpNot;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;

public class EvaluateUnaryOperator {

	public static ILconst eval(OpNot opNot, ILconst val) {
		ILconstBool b = (ILconstBool) val;
		return ILconstBool.instance(!b.getVal());
	}

	public static ILconst eval(OpMinus opMinus, ILconst val) {
		if (val instanceof ILconstReal) {
			ILconstReal r = (ILconstReal) val;
			return new ILconstReal(-r.getVal()); 
		} else if (val instanceof ILconstInt) {
			ILconstInt i = (ILconstInt) val;
			return new ILconstInt(-i.getVal());
		}
		throw new Error();
	}

}
