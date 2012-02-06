package de.peeeq.wurstscript.jassprinter;

import de.peeeq.wurstscript.jassAst.*;
import static de.peeeq.wurstscript.jassprinter.JassPrinter.*;

public class ExprPrinter {

	public static void print(JassExprBoolVal e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getValB() ? "true" : "false");
	}

	public static void print(JassExprFuncRef e, StringBuilder sb, boolean withSpace) {
		sb.append("function ");
		sb.append(e.getFuncName());
	}
	public static void print(JassExprIntVal e, StringBuilder sb, boolean withSpace) {
		String val = String.valueOf(e.getValI());
		if(!withSpace) {
			val = intShort(val);
		}
		sb.append(val);
	}
	
	
	
	public static void print(JassExprNull e, StringBuilder sb, boolean withSpace) {
		sb.append("null");
	}
	public static void print(JassExprRealVal e, StringBuilder sb, boolean withSpace) {
		String val = String.valueOf(e.getValR());
		if(!withSpace) {
			if( val.substring(0,2).equals("0.")) {
				sb.append(val.substring(1,val.length()));
			}else if( val.substring(val.length()-2,val.length()).equals(".0")) {
				sb.append(val.substring(0,val.length()-1));
			}else{
				sb.append(val);
			}
		}else{
			sb.append(val);
		}
	}
	public static void print(JassExprStringVal e, StringBuilder sb, boolean withSpace) {
		sb.append("\"");
		String v = e.getValS();
		for (int i=0;i<v.length(); i++) {
			char c = v.charAt(i);
			switch (c) {
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				default:
					sb.append(c);
			}
		}
		sb.append("\"");
	}
	public static void print(JassExprVarAccess e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getVarName());
	}
	public static void print(JassExprVarArrayAccess e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getVarName());
		sb.append("[");
		e.getIndex().print(sb, withSpace);
		sb.append("]");
	}
	public static void print(JassExprBinary e, StringBuilder sb, boolean withSpace) {
		boolean useParanthesesLeft = false;
		boolean useParanthesesRight = false;
		if (e.getLeftExpr() instanceof JassExprBinary) {
			JassExprBinary left = (JassExprBinary) e.getLeftExpr();
			if (precedence(left.getOp()) < precedence(e.getOp())) {
				// if the precedence level on the left is _smaller_ we have to use parentheses
				useParanthesesLeft = true;
			} 
			// if the precedence level is equal we can assume left associativity of all operators
			// so they are treated correctly
		}
		if (e.getRight() instanceof JassExprBinary) {
			JassExprBinary right = (JassExprBinary) e.getRight();
			if (precedence(right.getOp()) < precedence(e.getOp())) {
				// if the precedence level on the right is smaller we have to use parentheses
				useParanthesesRight = true;
			} else if (precedence(right.getOp()) == precedence(e.getOp())) {
				// if the precedence level is equal we have to parentheses as operators are
				// left associative but for commutative operators (+, *, and, or) we do not
				// need parentheses
				
				if (! ((right.getOp() instanceof JassOpPlus && e.getOp() instanceof JassOpPlus) 
					|| (right.getOp() instanceof JassOpMult && e.getOp() instanceof JassOpMult) 
					|| (right.getOp() instanceof JassOpOr   && e.getOp() instanceof JassOpOr) 
					|| (right.getOp() instanceof JassOpAnd  && e.getOp() instanceof JassOpAnd))) {
					// in other cases use parentheses
					// for example 
					useParanthesesRight = true;
				}
			}
		}
		
		sb.append(useParanthesesLeft ? "(" : "");
		e.getLeftExpr().print(sb, withSpace);
		sb.append(useParanthesesLeft ? ")" : "");
		e.getOp().print(sb, withSpace, useParanthesesLeft, useParanthesesRight);
		sb.append(useParanthesesRight ? "(" : "");
		e.getRight().print(sb, withSpace);
		sb.append(useParanthesesRight ? ")" : "");
	}
	
	
	public static void print(JassExprFunctionCall e, StringBuilder sb, boolean withSpace) {
		sb.append(e.getFuncName());
		sb.append("(");
		boolean first = true;
		for (JassExpr a : e.getArguments()) {
			if (!first) {
				sb.append(comma(withSpace));
			}
			a.print(sb, withSpace);
			first = false;
		}
		sb.append(")");
	}
	public static void print(JassExprUnary e, StringBuilder sb, boolean withSpace) {
		boolean useParantheses = e.getRight() instanceof JassExprBinary;
		e.getOpU().print(sb, withSpace, false, useParantheses);
		sb.append(useParantheses ? "(" : "");
		e.getRight().print(sb, withSpace);
		sb.append(useParantheses ? ")" : "");
	}

	
	private static String intShort(String val){
		int d = Integer.valueOf(val);
		if ( d > 792646 && containsOnlyNumbers(val) ) {
			String s = Integer.toHexString(d).toUpperCase();
			return "$" + s;
		}
		return String.valueOf(d);
	}
	
	/**
     * This method checks if a String contains only numbers
     */
    private static boolean containsOnlyNumbers(String str) {
        
        //It can't contain only numbers if it's null or empty...
        if (str == null || str.length() == 0)
            return false;
        
        for (int i = 0; i < str.length(); i++) {

            //If we find a non-digit character we return false.
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        
        return true;
    }
    
    
    
}
