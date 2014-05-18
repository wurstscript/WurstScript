package de.peeeq.wurstscript.galaxy.printer;

import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOp;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpAnd;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpDiv;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpEquals;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpGreater;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpGreaterEq;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpLess;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpLessEq;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpMinus;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpMult;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpNot;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpOr;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpPlus;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpUnequals;

public class OpPrinter {

	public static void print(GalaxyOp op, StringBuilder sb, boolean withSpace, boolean parLeft, boolean parRight) {
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

	public static String  asString(GalaxyOpUnequals jassOpUnequals) {
		return "!=";
	}
	
	
	public static String  asString(GalaxyOpPlus jassOpPlus) {
		return "+";
	}
	
	
	public static String  asString(GalaxyOpOr jassOpOr) {
		return "||";
	}
	
	
	public static String  asString(GalaxyOpNot jassOpNot) {
		return "!";
	}
	
	
	public static String  asString(GalaxyOpMult jassOpMult) {
		return "*";
	}
	
	
	public static String  asString(GalaxyOpMinus jassOpMinus) {
		return "-";
	}
	
	
	public static String  asString(GalaxyOpLessEq jassOpLessEq) {
		return "<=";
	}
	
	
	public static String  asString(GalaxyOpLess jassOpLess) {
		return "<";
	}
	
	
	public static String  asString(GalaxyOpGreaterEq jassOpGreaterEq) {
		return ">=";
	}
	
	
	public static String  asString(GalaxyOpGreater jassOpGreater) {
		return ">";
	}
	
	
	public static String  asString(GalaxyOpEquals jassOpEquals) {
		return "==";
	}
	
	
	public static String  asString(GalaxyOpDiv jassOpDiv) {
		return "/";
	}
	
	
	public static String  asString(GalaxyOpAnd jassOpAnd) {
		return "&&";
	}
}
