package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;

public class ILconstError implements ILconst {

	private final String msg;

	public ILconstError(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String print() {
		return msg;
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		return false;
	}
	
	@Override
	public String toString() {
		return "<err>";
	}

}
