package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;

public class ReturnException extends Error {

	private ILconst val;

	public ReturnException(ILconst value) {
		this.val = value;
	}

	ILconst getVal() {
		return val;
	}
	
	ReturnException setVal(ILconst val) {
		this.val = val;
		return this;
	}
}
