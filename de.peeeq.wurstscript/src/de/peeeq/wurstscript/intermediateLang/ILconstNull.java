package de.peeeq.wurstscript.intermediateLang;

public class ILconstNull extends ILconst {

	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append("null");
	}

	@Override
	public String print() {
		return "null";
	}

}
