package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.WurstTypeCode;
import de.peeeq.wurstscript.types.WurstType;

public class ILconstFuncRef extends ILconstAbstract {

	private String funcName;


	public ILconstFuncRef(String funcName) {
		this.funcName = funcName;
	}

	
	@Override
	public String print() {
		return "function " + funcName;
	}
	

	public WurstType getType() {
		return WurstTypeCode.instance();
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
