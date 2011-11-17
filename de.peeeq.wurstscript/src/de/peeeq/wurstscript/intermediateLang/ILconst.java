package de.peeeq.wurstscript.intermediateLang;

public interface ILconst {

	public abstract String print();

	boolean isEqualTo(ILconst other);
	
	@Override
	public String toString();
	
}
