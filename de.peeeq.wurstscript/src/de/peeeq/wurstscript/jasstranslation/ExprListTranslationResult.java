package de.peeeq.wurstscript.jasstranslation;

import java.util.List;

import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassStatement;

public class ExprListTranslationResult {
	private final List<JassStatement> statements;
	private List<JassExpr> exprs;
	
	public ExprListTranslationResult(List<JassStatement> statements, List<JassExpr> exprs) {
		this.statements = statements;
		this.exprs = exprs;
	}

	public List<JassStatement> getStatements() {
		return statements;
	}


	public List<JassExpr> getExprs() {
		return exprs;
	}

}
