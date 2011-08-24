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


	@Override
	public void printJass(StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}
	
	

}
