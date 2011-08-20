package de.peeeq.wurstscript.intermediateLang;

public abstract class ILconst implements ILexpr {

	public abstract String print();

	
	@Override
	public String toString() {
		return print();
	}
}
