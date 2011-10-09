package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeNull;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstNull extends ILconst {

	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append("null");
	}

	@Override
	public String print() {
		return "null";
	}
	
	@Override
	public PscriptType getType() {
		return PScriptTypeNull.instance();
	}

}
