package de.peeeq.wurstscript.intermediateLang;

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
	public void printJass(StringBuilder sb) {
		sb.append("set " + getResultVar().getName() + " = " );
		op.printJass(sb);
		sb.append(right.getName() + "\n");
	}
	
	

}
