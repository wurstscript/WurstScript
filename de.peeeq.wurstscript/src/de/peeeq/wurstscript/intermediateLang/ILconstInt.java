package de.peeeq.wurstscript.intermediateLang;

public class ILconstInt extends ILconst {

	private int val;

	public ILconstInt(int intVal) {
		this.val = intVal;
	}

	public int getVal() {
		return val;
	}
	
	public int negate() {
		return val = -val;
	}
	
	@Override
	public String print() {
		return val+"";
	}
	

}
