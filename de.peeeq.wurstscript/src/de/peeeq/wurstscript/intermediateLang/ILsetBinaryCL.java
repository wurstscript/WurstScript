package de.peeeq.wurstscript.intermediateLang;


/**
 * a binary exrpression with a constant on the left side 
 * 
 * set x = 1 + y
 * 
 */
public class ILsetBinaryCL extends ILStatementSet {

	private Iloperator op;
	private ILconst left;
	private ILvar right;

	public ILsetBinaryCL(ILvar resultVar, ILconst left, Iloperator op, ILvar right) {
		super(resultVar);
		this.op = op;
		this.left = left;
		this.right = right;
	}


	public Iloperator getOp() {
		return op;
	}

	public ILconst getLeft() {
		return left;
	}

	public ILvar getRight() {
		return right;
	}
	
	

}
