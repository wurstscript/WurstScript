package de.peeeq.wurstscript.intermediateLang;

public class IlsetConst extends ILStatementSet {

	private ILconst constant;

	public IlsetConst(ILvar resultVar, ILconst constant) {
		super(resultVar);
		this.constant = constant;
	}

	public ILconst getConstant() {
		return constant;
	}

}
