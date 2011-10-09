package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstFuncRef extends ILconst {

	private ILfunction func;

	public ILconstFuncRef(ILfunction func) {
		this.func = func;
	}

	public ILfunction getFunc() {
		return func;
	}
	
	@Override
	public String print() {
		return "function " + func.getName();
	}
	
	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append(print());
	}

	@Override
	public PscriptType getType() {
		return PScriptTypeCode.instance();
	}
	
}
