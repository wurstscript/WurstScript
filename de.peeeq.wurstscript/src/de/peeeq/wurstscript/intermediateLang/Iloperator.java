package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.utils.Utils;

public class Iloperator implements CodePrinting {

	public static final Iloperator OR = new Iloperator("or");
	public static final Iloperator AND = new Iloperator("and");
	public static final Iloperator NOT = new Iloperator("not");
	public static final Iloperator PLUS = new Iloperator("+");
	public static final Iloperator MINUS = new Iloperator("-");
	public static final Iloperator MULT = new Iloperator("*");
	public static final Iloperator DIV_INT = new Iloperator("/");
	public static final Iloperator DIV_REAL = new Iloperator("/");
	public static final Iloperator MOD_INT = new Iloperator("mod");
	public static final Iloperator MOD_REAL = new Iloperator("%");
	public static final Iloperator EQUALITY = new Iloperator("==");
	public static final Iloperator UNEQUALITY = new Iloperator("!=");
	public static final Iloperator LESS = new Iloperator("<");
	public static final Iloperator LESS_EQ = new Iloperator("<=");
	public static final Iloperator GREATER = new Iloperator(">");
	public static final Iloperator GREATER_EQ = new Iloperator(">=");
	
	private String name;
	

	private Iloperator(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Iloperator " + name; 
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append(" " + name + " ");
	}
	
	public boolean isComparisonOp() {
		return Utils.oneOf(this, EQUALITY, UNEQUALITY, LESS, LESS_EQ, GREATER, GREATER_EQ);
	}

}
