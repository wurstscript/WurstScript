package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PscriptType;

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
	public void printJass(StringBuilder sb, int indent) {
		sb.append(print());
	}
	
	@Override
	public PscriptType getType() {
		return PScriptTypeString.instance();
	}
	
}
