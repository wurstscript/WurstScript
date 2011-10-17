package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.utils.Utils;


@ILextended
public class ILsetExpr extends ILStatementSet {

	private ILexpr expr;

	public ILsetExpr(ILvar resultVar, ILexpr expr) {
		super(resultVar);
		this.expr = expr;
	}


	public ILexpr getExpr() {
		return expr;
	}


	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("set " + getResultVar().getName() 
				 + " = ");
		expr.printJass(sb, indent);
		sb.append("\n");
	}
	
	

}
