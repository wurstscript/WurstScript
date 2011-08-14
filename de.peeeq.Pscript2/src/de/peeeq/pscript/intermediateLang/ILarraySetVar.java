package de.peeeq.pscript.intermediateLang;

public class ILarraySetVar extends ILStatementSet {

	private ILvar index;
	private ILvar var;

	public ILarraySetVar(ILvar resultVar, ILvar index, ILvar var) {
		super(resultVar);
		this.index = index;
		this.var = var;
	}

	public ILvar getIndex() {
		return index;
	}

	public ILvar getVar() {
		return var;
	}

	
	
}
