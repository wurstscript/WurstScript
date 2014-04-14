package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;

public class AttrConstantValue {
	
	
	public static class ConstantValueCalculationException extends RuntimeException {

		public ConstantValueCalculationException(String msg) {
			super(msg);
		}
		
	}

	public static ILconst calculate(Expr e) {
		// by default, throw an exception
		// special cases for certain expressions
		throw new ConstantValueCalculationException(e.toString());
	}
	
	
	public static ILconst calculate(ExprIntVal e) {
		// for an integer, just return the int
		return new ILconstInt(e.getValI());
	}

	public static ILconst calculate(ExprVarAccess e) {
		NameDef v = e.attrNameDef();
		if (v instanceof GlobalVarDef) {
			GlobalVarDef g = (GlobalVarDef) v;
			if (g.attrIsConstant() && g.getInitialExpr() instanceof Expr) {
				// when this is a global constant:
				Expr initial = (Expr) g.getInitialExpr();
				// return the value of the initial expr
				return initial.attrConstantValue();
			}
		}
		// otherwise we cannot calculate the value
		throw new ConstantValueCalculationException(e.toString());
	}
	
	
}
