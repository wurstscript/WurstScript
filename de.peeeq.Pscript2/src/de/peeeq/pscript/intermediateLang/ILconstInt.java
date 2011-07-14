package de.peeeq.pscript.intermediateLang;

public class ILconstInt extends ILconst {

	private int val;

	public ILconstInt(int intVal) {
		this.val = intVal;
	}

	public int getVal() {
		return val;
	}
	
	@Override
	public String print() {
		return val+"";
	}
	

}
