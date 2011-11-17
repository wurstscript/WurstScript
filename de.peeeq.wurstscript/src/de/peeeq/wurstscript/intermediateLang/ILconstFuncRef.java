package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstFuncRef extends ILconstAbstract {

	private String funcName;


	public ILconstFuncRef(String funcName) {
		this.funcName = funcName;
	}

	
	@Override
	public String print() {
		return "function " + funcName;
	}
	

	public PscriptType getType() {
		return PScriptTypeCode.instance();
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		if (other instanceof ILconstFuncRef) {
			ILconstFuncRef f = (ILconstFuncRef) other;
			return f.funcName.equals(funcName);
		}
		return false;
	}
	
}
