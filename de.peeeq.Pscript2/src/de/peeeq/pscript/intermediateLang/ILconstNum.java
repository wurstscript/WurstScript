package de.peeeq.pscript.intermediateLang;

import com.ibm.icu.math.BigDecimal;

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

}
