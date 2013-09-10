package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;




public class LocalState extends State {

	private ILconst returnVal = null;
	
	public LocalState(ILconst returnVal) {
		this.setReturnVal(returnVal);
	}

	public LocalState() {
	}

	public ILconst getReturnVal() {
		return returnVal;
	}

	public LocalState setReturnVal(ILconst returnVal) {
		this.returnVal = returnVal;
		return this;
	}

	

	

}
