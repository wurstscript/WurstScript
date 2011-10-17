package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.utils.Utils;

public class IlsetUnary extends ILStatementSet {

	private Iloperator op;
	private ILvar right;

	public IlsetUnary(ILvar resultVar, Iloperator op, ILvar right) {
		super(resultVar);
		this.op = op;
		this.right = right;
	}


	public Iloperator getOp() {
		return op;
	}

	public ILvar getRight() {
		return right;
	}


	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("set " + getResultVar().getName() + " = " );
		op.printJass(sb, 0);
		sb.append(right.getName() + "\n");
	}
	
	

}
