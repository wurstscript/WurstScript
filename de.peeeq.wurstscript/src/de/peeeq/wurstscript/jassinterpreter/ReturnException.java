package de.peeeq.wurstscript.jassinterpreter;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.intermediateLang.ILconst;

public class ReturnException extends Error {

	private @Nullable ILconst val;

	public ReturnException(@Nullable ILconst value) {
		this.val = value;
	}

	public @Nullable ILconst getVal() {
		return val;
	}
	
	public ReturnException setVal(ILconst val) {
		this.val = val;
		return this;
	}
}
