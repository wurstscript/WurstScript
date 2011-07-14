package de.peeeq.pscript.intermediateLang;

public abstract class ILconst {

	public abstract String print();

	
	@Override
	public String toString() {
		return print();
	}
}
