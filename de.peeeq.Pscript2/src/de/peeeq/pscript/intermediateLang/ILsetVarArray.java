package de.peeeq.pscript.intermediateLang;

public class ILsetVarArray extends ILStatementSet {

	private ILvar var;
	private ILvar index;

	public ILsetVarArray(ILvar resultVar, ILvar var, ILvar index) {
		super(resultVar);
		this.var = var;
		this.index = index;
	}

	public ILvar getVar() {
		return var;
	}

	public ILvar getIndex() {
		return index;
	}

}
