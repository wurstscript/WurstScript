package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PscriptType;
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
//		Utils.printIndent(sb, indent);
		if (op == Iloperator.MOD_INT) {
			sb.append("ModuloInteger(");
			left.printJassExpr(sb, 0);
			sb.append(", ");
			right.printJassExpr(sb, 0);
			sb.append(")");
			return;
		} 
		if (op == Iloperator.MOD_REAL) {
			sb.append("ModuloReal(");
			left.printJassExpr(sb, 0);
			sb.append(", ");
			right.printJassExpr(sb, 0);
			sb.append(")");
			return;
		} 
		if (op == Iloperator.DIV_REAL) {
			if (left.getType() instanceof PScriptTypeInt && right.getType() instanceof PScriptTypeInt) {
				sb.append("(");
				left.printJassExpr(sb, 0);
				sb.append("*1.0/ ");
				right.printJassExpr(sb, 0);
				sb.append(")");
				return;
			}
		} 
			sb.append("(");
			left.printJassExpr(sb, 0);
			op.printJass(sb, 0);
			right.printJassExpr(sb, 0);
			sb.append(")");
	}


	@Override
	public void printJassExpr(StringBuilder sb, int indent) {
		printJass(sb, indent);
	}


	@Override
	public PscriptType getType() {
		if (op == Iloperator.DIV_REAL) {
			return PScriptTypeReal.instance();
		}
		if (op == Iloperator.MOD_REAL) {
			return PScriptTypeReal.instance();
		}
		
		if (op.isComparisonOp()) {
			return PScriptTypeBool.instance();
		}
		
		if (left.getType() instanceof PScriptTypeReal || right.getType() instanceof PScriptTypeReal) {
			return PScriptTypeReal.instance();
		}
		
		return left.getType();
	}
	
	

}
