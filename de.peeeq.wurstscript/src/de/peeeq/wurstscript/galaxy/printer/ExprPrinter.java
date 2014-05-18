package de.peeeq.wurstscript.galaxy.printer;

import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExpr;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprBinary;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprBoolVal;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprFuncRef;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprFunctionCall;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprIntVal;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprNull;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprRealVal;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprStringVal;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprUnary;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprVarAccess;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExprVarArrayAccess;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpAnd;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpMult;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpOr;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpPlus;
import de.peeeq.wurstscript.utils.Utils;
import static de.peeeq.wurstscript.galaxy.printer.GalaxyPrinter.comma;
import static de.peeeq.wurstscript.galaxy.printer.GalaxyPrinter.precedence;

public class ExprPrinter {

	public static void print(GalaxyExprBoolVal e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getValB() ? "true" : "false");
	}

	public static void print(GalaxyExprFuncRef e, StringBuilder sb, boolean withSpace) {
		sb.append("\"");
		sb.append(e.getFuncName());
		sb.append("\"");
	}
	public static void print(GalaxyExprIntVal e, StringBuilder sb, boolean withSpace) {
		String val = String.valueOf(e.getValI());
		sb.append(val);
	}
	
	
	
	public static void print(GalaxyExprNull e, StringBuilder sb, boolean withSpace) {
		sb.append("null");
	}
	public static void print(GalaxyExprRealVal e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getValR());
	}
	public static void print(GalaxyExprStringVal e, StringBuilder sb, boolean withSpace) {
		sb.append(Utils.escapeString(e.getValS()));
	}
	public static void print(GalaxyExprVarAccess e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getVarName());
	}
	public static void print(GalaxyExprVarArrayAccess e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getVarName());
		sb.append("[");
		e.getIndex().print(sb, withSpace);
		sb.append("]");
	}
	public static void print(GalaxyExprBinary e, StringBuilder sb, boolean withSpace) {
		boolean useParanthesesLeft = false;
		boolean useParanthesesRight = false;
		if (e.getLeftExpr() instanceof GalaxyExprBinary) {
			GalaxyExprBinary left = (GalaxyExprBinary) e.getLeftExpr();
			if (precedence(left.getOp()) < precedence(e.getOp())) {
				// if the precedence level on the left is _smaller_ we have to use parentheses
				useParanthesesLeft = true;
			} 
			// if the precedence level is equal we can assume left associativity of all operators
			// so they are treated correctly
		} else if (e.getLeftExpr() instanceof GalaxyExprUnary) {
			useParanthesesLeft = true;
		}
		if (e.getRight() instanceof GalaxyExprBinary) {
			GalaxyExprBinary right = (GalaxyExprBinary) e.getRight();
			if (precedence(right.getOp()) < precedence(e.getOp())) {
				// if the precedence level on the right is smaller we have to use parentheses
				useParanthesesRight = true;
			} else if (precedence(right.getOp()) == precedence(e.getOp())) {
				// if the precedence level is equal we have to parentheses as operators are
				// left associative but for commutative operators (+, *, and, or) we do not
				// need parentheses
				
				if (! ((right.getOp() instanceof GalaxyOpPlus && e.getOp() instanceof GalaxyOpPlus) 
					|| (right.getOp() instanceof GalaxyOpMult && e.getOp() instanceof GalaxyOpMult) 
					|| (right.getOp() instanceof GalaxyOpOr   && e.getOp() instanceof GalaxyOpOr) 
					|| (right.getOp() instanceof GalaxyOpAnd  && e.getOp() instanceof GalaxyOpAnd))) {
					// in other cases use parentheses
					// for example 
					useParanthesesRight = true;
				}
			}
		} else if (e.getRight() instanceof GalaxyExprUnary) {
			useParanthesesRight = true;
		}
		
		sb.append(useParanthesesLeft ? "(" : "");
		e.getLeftExpr().print(sb, withSpace);
		sb.append(useParanthesesLeft ? ")" : "");
		e.getOp().print(sb, withSpace, useParanthesesLeft, useParanthesesRight);
		sb.append(useParanthesesRight ? "(" : "");
		e.getRight().print(sb, withSpace);
		sb.append(useParanthesesRight ? ")" : "");
	}
	
	
	public static void print(GalaxyExprFunctionCall e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getFuncName());
		sb.append("(");
		boolean first = true;
		for (GalaxyExpr a : e.getArguments()) {
			if (!first) {
				sb.append(comma(withSpace));
			}
			a.print(sb, withSpace);
			first = false;
		}
		sb.append(")");
	}
	public static void print(GalaxyExprUnary e, StringBuilder sb, boolean withSpace) {
		boolean useParantheses = e.getRight() instanceof GalaxyExprBinary;
		e.getOpU().print(sb, withSpace, false, useParantheses);
		sb.append(useParantheses ? "(" : "");
		e.getRight().print(sb, withSpace);
		sb.append(useParantheses ? ")" : "");
	}

	
//	private static String intShort(String val){
//		int d = Integer.valueOf(val);
//		if ( d > 792646 && containsOnlyNumbers(val) ) {
//			String s = Integer.toHexString(d).toUpperCase();
//			return "$" + s;
//		}
//		return String.valueOf(d);
//	}
	

    
}
