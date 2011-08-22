package de.peeeq.wurstscript.intermediateLang;

public class ILconstString extends ILconst {

	private String val; // including the quotes

	public ILconstString(String strVal) {
		this.val = strVal;
	}

	public String getVal() {
		return val;
	}
	
	@Override
	public String print() {
		return "\"" + val + "\"";
	}

	@Override
	public void printJass(StringBuilder sb) {
		sb.append(print());
	}
	
}
