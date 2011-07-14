package de.peeeq.pscript.intermediateLang;


public class ILcopy extends ILStatement {

	private ILvar resultVar;
	private ILvar var;

	public ILcopy(ILvar resultVar, ILvar var) {
		this.resultVar = resultVar;
		this.var = var;
	}

	public ILvar getResultVar() {
		return resultVar;
	}

	public ILvar getVar() {
		return var;
	}
	
	

}
