package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.utils.Utils;

public class ILsetBinary extends ILStatementSet {

	private Iloperator op;
	private ILvar left;
	private ILvar right;

	public ILsetBinary(ILvar resultVar, ILvar left, Iloperator op, ILvar right) {
		super(resultVar);
		this.op = op;
		this.left = left;
		this.right = right;
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


	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		if (op == Iloperator.DIV_REAL && left.getType() instanceof PScriptTypeInt && right.getType() instanceof PScriptTypeInt) {
			// we have to make clear that this operation should yield a real
			sb.append("set " + getResultVar().getName() 
					 + " = (1.0 * " + left.getName() + ")" );
			op.printJass(sb, 0);
			sb.append(right.getName() + "\n");
		} else {
			sb.append("set " + getResultVar().getName() 
					 + " = " + left.getName());
			op.printJass(sb, 0);
			sb.append(right.getName() + "\n");
		}
	}
	
	

}
