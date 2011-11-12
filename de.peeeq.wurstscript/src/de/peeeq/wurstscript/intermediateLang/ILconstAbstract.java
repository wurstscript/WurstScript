package de.peeeq.wurstscript.intermediateLang;

public abstract class ILconstAbstract implements ILconst, ILexpr, CodePrinting {

	public abstract String print();

	
	@Override
	public String toString() {
		return print();
	}
	
	@Override
	public void printJassExpr(StringBuilder sb, int indent) {
		printJass(sb, indent);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ILconst) {
			return isEqualTo((ILconst) other);
		}
		return false;
	}
	
}
