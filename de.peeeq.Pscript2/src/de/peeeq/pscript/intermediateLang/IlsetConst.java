package de.peeeq.pscript.intermediateLang;

public class IlsetConst extends ILStatement {

	private ILvar resultVar;
	private ILconst constant;

	public IlsetConst(ILvar resultVar, ILconst constant) {
		this.resultVar = resultVar;
		this.constant = constant;
	}

	public ILvar getResultVar() {
		return resultVar;
	}

	public ILconst getConstant() {
		return constant;
	}

}
