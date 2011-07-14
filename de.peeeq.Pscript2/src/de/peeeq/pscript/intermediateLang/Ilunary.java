package de.peeeq.pscript.intermediateLang;

public class Ilunary extends ILStatement {

	private ILvar resultVar;
	private Iloperator op;
	private ILvar right;

	public Ilunary(ILvar resultVar, Iloperator op, ILvar right) {
		this.resultVar = resultVar;
		this.op = op;
		this.right = right;
	}

	public ILvar getResultVar() {
		return resultVar;
	}

	public Iloperator getOp() {
		return op;
	}

	public ILvar getRight() {
		return right;
	}
	
	

}
