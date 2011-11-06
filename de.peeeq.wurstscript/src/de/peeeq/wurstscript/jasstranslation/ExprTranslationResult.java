package de.peeeq.wurstscript.jasstranslation;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprRealVal;
import de.peeeq.wurstscript.jassAst.JassStatement;

/**
 * the result of translating an expression
 * usually this will just be a JassExpr but sometimes 
 * other statements are needed...  
 */
public class ExprTranslationResult {
	private final List<JassStatement> statements;
	private final JassExpr expr;
	
	public ExprTranslationResult(List<JassStatement> statements, JassExpr expr) {
		this.statements = statements;
		this.expr = expr;
	}

	public ExprTranslationResult(JassExpr expr) {
		this.statements = Lists.newLinkedList();
		this.expr = expr;
	}

	public List<JassStatement> getStatements() {
		return statements;
	}

	public JassExpr getExpr() {
		return expr;
	}
	
	
}
