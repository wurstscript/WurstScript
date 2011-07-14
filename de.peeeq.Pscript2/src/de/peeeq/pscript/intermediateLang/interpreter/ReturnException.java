package de.peeeq.pscript.intermediateLang.interpreter;

import de.peeeq.pscript.intermediateLang.ILconst;

public class ReturnException extends Error {

	private ILconst val;

	public ReturnException(ILconst value) {
		this.val = value;
	}

	ILconst getVal() {
		return val;
	}
}
