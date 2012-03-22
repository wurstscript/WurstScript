package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.OpAnd;
import de.peeeq.wurstscript.ast.OpDivInt;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OpGreater;
import de.peeeq.wurstscript.ast.OpGreaterEq;
import de.peeeq.wurstscript.ast.OpLess;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpMult;
import de.peeeq.wurstscript.ast.OpNot;
import de.peeeq.wurstscript.ast.OpOr;
import de.peeeq.wurstscript.ast.OpPlus;
import de.peeeq.wurstscript.ast.OpUnequals;

public class PrettyPrinter {

	public static String toSymbol(OpAnd opAnd) {
		return "and";
	}
	
	public static String toSymbol(OpDivInt opAnd) {
		return "div";
	}
	
	public static String toSymbol(OpDivReal opAnd) {
		return "/";
	}
	
	public static String toSymbol(OpEquals opAnd) {
		return "==";
	}
	
	public static String toSymbol(OpGreater opAnd) {
		return ">";
	}
	
	public static String toSymbol(OpGreaterEq opAnd) {
		return ">=";
	}
	
	public static String toSymbol(OpLess opAnd) {
		return "<";
	}
	
	public static String toSymbol(OpLessEq opAnd) {
		return "<=";
	}
	
	public static String toSymbol(OpMinus opAnd) {
		return "-";
	}
	
	public static String toSymbol(OpModInt opAnd) {
		return "mod";
	}
	
	public static String toSymbol(OpModReal opAnd) {
		return "%";
	}
	
	public static String toSymbol(OpMult opAnd) {
		return "*";
	}
	
	public static String toSymbol(OpOr opAnd) {
		return "or";
	}
	
	public static String toSymbol(OpPlus opAnd) {
		return "+";
	}
	
	public static String toSymbol(OpUnequals opAnd) {
		return "!=";
	}
	
	public static String toSymbol(OpNot opAnd) {
		return "not";
	}
	
	
}
