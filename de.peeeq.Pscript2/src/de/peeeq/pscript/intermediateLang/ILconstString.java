package de.peeeq.pscript.intermediateLang;

public class ILconstString implements ILconst {

	private String val; // including the quotes

	public ILconstString(String strVal) {
		this.val = strVal;
	}

	@Override
	public String print() {
		return val;
	}

}
