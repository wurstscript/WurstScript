package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.utils.Utils;

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
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("set " + getResultVar().getName() + " = ");
		constant.printJass(sb, 0);
		sb.append("\n");
	}

}
