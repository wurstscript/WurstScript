package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;




public class LocalState extends State {

	private ILconst returnVal;
	private static final ILconstError emptyReturn = new ILconstError("<no return value>");
	
	public LocalState(ILconst returnVal) {
		this.setReturnVal(returnVal);
	}

	public LocalState() {
		returnVal = emptyReturn;
	}

	public ILconst getReturnVal() {
		return returnVal;
	}

	public LocalState setReturnVal(ILconst returnVal) {
		this.returnVal = returnVal;
		return this;
	}

	

	

}
