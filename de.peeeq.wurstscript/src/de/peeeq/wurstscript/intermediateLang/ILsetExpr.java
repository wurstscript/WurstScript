package de.peeeq.wurstscript.intermediateLang;


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
	
	

}
