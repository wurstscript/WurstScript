package de.peeeq.pscript.intermediateLang;

import com.ibm.icu.math.BigDecimal;

public class ILconstNum extends ILconst {

	private BigDecimal val;

	public ILconstNum(String numVal) {
		this.val = new BigDecimal(numVal);
	}

	@Override
	public String print() {
		return val.toString();
	}

}
