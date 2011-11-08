package de.peeeq.wurstscript.intermediateLang;

public interface ILconst extends ILexpr, CodePrinting {

	public abstract String print();

	boolean isEqualTo(ILconst other);
	
	@Override
	public String toString();
	
	@Override
	public void printJassExpr(StringBuilder sb, int indent);
}
