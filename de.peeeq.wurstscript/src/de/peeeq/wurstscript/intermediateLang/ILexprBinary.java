package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.utils.Utils;

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


	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("(");
		left.printJass(sb, 0);
		op.printJass(sb, 0);
		right.printJass(sb, 0);
		sb.append(")");
	}
	
	

}
