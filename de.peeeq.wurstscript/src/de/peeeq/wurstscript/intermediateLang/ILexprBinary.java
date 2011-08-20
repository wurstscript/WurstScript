package de.peeeq.wurstscript.intermediateLang;

/**
 * a binary expression like A + B where A and B are arbitrary expressions 
 */
public class ILexprBinary implements ILexpr {

	private Iloperator op;
	private ILexpr left;
	private ILexpr right;

	public ILexprBinary( ILexpr left, Iloperator op, ILexpr right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}


	public Iloperator getOp() {
		return op;
	}

	public ILexpr getLeft() {
		return left;
	}

	public ILexpr getRight() {
		return right;
	}
	
	

}
