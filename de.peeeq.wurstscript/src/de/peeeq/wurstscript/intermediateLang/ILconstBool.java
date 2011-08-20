package de.peeeq.wurstscript.intermediateLang;

public class ILconstBool extends ILconst {

	private boolean val;

	public ILconstBool(String boolVal) {
		if (boolVal.equals("true")) {
			this.val = true;
		} else if (boolVal.equals("false")) {
			this.val = false;
		} else {
			throw new Error("unsupported boolean constant");
		}
		
	}
	
	public ILconstBool(boolean b) {
		val = b;
	}

	public boolean getVal() {
		return val;
	}

	@Override
	public String print() {
		return val ? "true" : "false";
	}

	public boolean not() {
		return !val;
	}

}
