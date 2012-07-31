package de.peeeq.wurstscript.jassinterpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;

public class ReturnException extends Error {

	private ILconst val;

	public ReturnException(ILconst value) {
		this.val = value;
	}

	public ILconst getVal() {
		return val;
	}
	
	public ReturnException setVal(ILconst val) {
		this.val = val;
		return this;
	}
}
