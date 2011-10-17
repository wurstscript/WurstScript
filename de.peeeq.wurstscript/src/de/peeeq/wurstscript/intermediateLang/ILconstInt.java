package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstInt extends ILconst {

	private int val;

	public ILconstInt(int intVal) {
		this.val = intVal;
	}

	public int getVal() {
		return val;
	}
	
	public int negate() {
		return val = -val;
	}
	
	@Override
	public String print() {
		return val+"";
	}
	
	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append(print());
	}
	
	@Override
	public PscriptType getType() {
		return PScriptTypeInt.instance();
	}

}
