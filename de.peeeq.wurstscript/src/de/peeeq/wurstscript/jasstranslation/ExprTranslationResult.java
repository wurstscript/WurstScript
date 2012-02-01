package de.peeeq.wurstscript.jasstranslation;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassStatement;

/**
 * the result of translating an expression
 * usually this will just be a JassExpr but sometimes 
 * other statements are needed...  
 */
public class ExprTranslationResult {
	private final List<JassStatement> statements;
	private final List<JassExpr> expressions;
	
	public ExprTranslationResult(List<JassStatement> statements, JassExpr ... exprs) {
		this.statements = statements;
		this.expressions = Lists.newArrayList(exprs);
	}

	public ExprTranslationResult(JassExpr ... exprs) {
		this.statements = Lists.newArrayList();
		this.expressions = Lists.newArrayList(exprs);
	}

	/**
	 * attention: statements and expressions are captured!
	 * @param statements2
	 * @param expressions2
	 */
	public ExprTranslationResult(List<JassStatement> statements, List<JassExpr> expressions) {
		this.statements = statements;
		this.expressions = expressions;
	}

	public List<JassStatement> getStatements() {
		return statements;
	}

	public List<JassExpr> getExpressions() {
		return expressions;
	}

	public int exprCount() {
		return expressions.size();
	}

	public JassExpr getExprSingle() {
		if (expressions.size() != 1) throw new Error("size == " + expressions.size());
		return expressions.get(0);
	}

	public ExprTranslationResult plus(JassExpr e) {
		ArrayList<JassExpr> expressions2 = Lists.newArrayList(expressions);
		expressions2.add(e);
		return new ExprTranslationResult(statements, expressions2);
	}
	
	
}
