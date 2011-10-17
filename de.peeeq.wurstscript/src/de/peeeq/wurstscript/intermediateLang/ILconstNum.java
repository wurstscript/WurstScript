package de.peeeq.wurstscript.intermediateLang;

import java.math.BigDecimal;

import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PscriptType;


public class ILconstNum extends ILconst {

	private BigDecimal val;

	public ILconstNum(String numVal) {
		this.val = new BigDecimal(numVal);
	}

	public ILconstNum(double numVal) {
		this.val = new BigDecimal(numVal);
	}
	
	public float negate() {
		return val.negate().floatValue();
	}

	@Override
	public String print() {
		return val.toString();
	}

	public BigDecimal getVal() {
		return val;
	}

	
	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append(print());
	}
	
	@Override
	public PscriptType getType() {
		return PScriptTypeReal.instance();
	}
}
