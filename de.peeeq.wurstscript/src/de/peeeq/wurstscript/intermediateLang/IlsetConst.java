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

	@Override
	public void printJass(StringBuilder sb) {
		sb.append("set " + getResultVar().getName() + " = ");
		constant.printJass(sb);
		sb.append("\n");
	}

}
