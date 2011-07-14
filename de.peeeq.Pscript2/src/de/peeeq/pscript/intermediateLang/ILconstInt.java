package de.peeeq.pscript.intermediateLang;

public class ILconstInt implements ILconst {

	private int val;

	public ILconstInt(int intVal) {
		this.val = intVal;
	}

	@Override
	public String print() {
		return val+"";
	}

}
