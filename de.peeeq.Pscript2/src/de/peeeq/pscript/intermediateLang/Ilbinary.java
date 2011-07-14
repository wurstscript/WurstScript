package de.peeeq.pscript.intermediateLang;

public class Ilbinary extends ILStatement {

	private ILvar resultVar;
	private Iloperator op;
	private ILvar left;
	private ILvar right;

	public Ilbinary(ILvar resultVar, ILvar left, Iloperator op, ILvar right) {
		this.resultVar = resultVar;
		this.op = op;
		this.left = left;
		this.right = right;
	}

	public ILvar getResultVar() {
		return resultVar;
	}

	public Iloperator getOp() {
		return op;
	}

	public ILvar getLeft() {
		return left;
	}

	public ILvar getRight() {
		return right;
	}
	
	

}
