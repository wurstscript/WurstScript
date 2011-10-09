package de.peeeq.wurstscript.intermediateLang;

public abstract class ILconst implements ILexpr, CodePrinting {

	public abstract String print();

	
	@Override
	public String toString() {
		return print();
	}
	
	@Override
	public void printJassExpr(StringBuilder sb, int indent) {
		printJass(sb, indent);
	}
}
