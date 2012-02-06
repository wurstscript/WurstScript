package de.peeeq.wurstscript.jassprinter;

import de.peeeq.wurstscript.jassAst.*;

public class OpPrinter {

	public static void print(JassOp op, StringBuilder sb, boolean withSpace, boolean parLeft, boolean parRight) {
		String opString = op.asString();
		
		if (withSpace || !parLeft && Character.isLetter(opString.charAt(0))) {
			// if we have no parantheses on the left and an operator like and/or we need a space:
			sb.append(" ");
		}
		sb.append(opString);
		if (withSpace || !parRight && Character.isLetter(opString.charAt(0))) {
			// if we have no parantheses on the right and an operator like and/or we need a space:
			sb.append(" ");
		}
	}

	public static String  asString(JassOpUnequals jassOpUnequals) {
		return "!=";
	}
	
	
	public static String  asString(JassOpPlus jassOpPlus) {
		return "+";
	}
	
	
	public static String  asString(JassOpOr jassOpOr) {
		return "or";
	}
	
	
	public static String  asString(JassOpNot jassOpNot) {
		return "not";
	}
	
	
	public static String  asString(JassOpMult jassOpMult) {
		return "*";
	}
	
	
	public static String  asString(JassOpMinus jassOpMinus) {
		return "-";
	}
	
	
	public static String  asString(JassOpLessEq jassOpLessEq) {
		return "<=";
	}
	
	
	public static String  asString(JassOpLess jassOpLess) {
		return "<";
	}
	
	
	public static String  asString(JassOpGreaterEq jassOpGreaterEq) {
		return ">=";
	}
	
	
	public static String  asString(JassOpGreater jassOpGreater) {
		return ">";
	}
	
	
	public static String  asString(JassOpEquals jassOpEquals) {
		return "==";
	}
	
	
	public static String  asString(JassOpDiv jassOpDiv) {
		return "/";
	}
	
	
	public static String  asString(JassOpAnd jassOpAnd) {
		return "and";
	}

}
