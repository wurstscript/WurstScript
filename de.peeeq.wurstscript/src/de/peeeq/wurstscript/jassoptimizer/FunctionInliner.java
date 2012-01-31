package de.peeeq.wurstscript.jassoptimizer;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.jassAst.JassStmtExitwhen;
import de.peeeq.wurstscript.jassAst.JassStmtIf;
import de.peeeq.wurstscript.jassAst.JassStmtLoop;
import de.peeeq.wurstscript.jassAst.JassStmtReturn;
import de.peeeq.wurstscript.jassAst.JassStmtReturnVoid;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassStmtSetArray;
import de.peeeq.wurstscript.jasstranslation.ExprTranslationResult;

public class FunctionInliner {

	
	private Collection<JassFunction> functionsToInline;

	// counter for the number of inlined functions, could be used for fixed point analysis
	private int inlinedFunctions = 0;
	
	public FunctionInliner(Collection<JassFunction> functionsToInline) {
		this.functionsToInline =  functionsToInline;
	}
	
	public void inlineFunctions(JassFunction f) {
		inlineJassFunctions(f, f.getBody());
	}

	private void inlineJassFunctions(JassFunction f, Iterable<JassStatement> statements) {
		for (JassStatement s : statements) {
			inlineJassFunctions(f, s);
		}
		
	}

	private void inlineJassFunctions(JassFunction f, JassStatement s) {
		if (s instanceof JassStmtCall) {
			inlineJassStmtCall(f, (JassStmtCall) s);
		}
		
		// TODO replace function calls as expressions

		// process children
		inlineJassFunctions(f, getChildStatements(s));
	}

	private void inlineJassStmtCall(JassFunction f, JassStmtCall s) {
		ExprTranslationResult r = inlineFunctionCall(f, s.attrFuncDef(), s.getArguments());
		
		List<JassStatement> newStatements = r.getStatements();
		
		if (r.getExpressions().get(0) instanceof JassExprFunctionCall) {
			JassExprFunctionCall e = (JassExprFunctionCall) r.getExpressions().get(0);
			newStatements.add(JassAst.JassStmtCall(e.getFuncName(), e.getArguments()));
		}
		replaceStatement(s, newStatements);
	}

	private void replaceStatement(JassStmtCall orig, List<JassStatement> newStatements) {
		JassStatements statements = (JassStatements) orig.getParent();
		ListIterator<JassStatement> it = statements.listIterator();
		while (it.hasNext()) {
			JassStatement s = it.next();
			if (s == orig) {
				// replace this
				it.remove();
				for (JassStatement newS : newStatements) {
					it.add(newS);
				}
				return;
			}
		}
		throw new Error("Statement not attached to its parent.");
	}

	private ExprTranslationResult inlineFunctionCall(JassFunction f, JassFunction attrFuncDef, JassExprlist arguments) {
		// TODO Auto-generated method stub
		throw new Error();
	}

	/**
	 * get all child statements of a given JassStatement
	 * @param s
	 * @return
	 */
	private Iterable<JassStatement> getChildStatements(JassStatement s) {
		final List<JassStatement> result = Lists.newArrayList();
		s.match(new JassStatement.MatcherVoid() {
			
			@Override
			public void case_JassStmtSetArray(JassStmtSetArray jassStmtSetArray) {
								
			}
			
			@Override
			public void case_JassStmtSet(JassStmtSet jassStmtSet) {
				
			}
			
			@Override
			public void case_JassStmtReturnVoid(JassStmtReturnVoid jassStmtReturnVoid) {
				
			}
			
			@Override
			public void case_JassStmtReturn(JassStmtReturn jassStmtReturn) {
				
			}
			
			@Override
			public void case_JassStmtLoop(JassStmtLoop jassStmtLoop) {
				result.addAll(jassStmtLoop.getBody());
			}
			
			@Override
			public void case_JassStmtIf(JassStmtIf jassStmtIf) {
				result.addAll(jassStmtIf.getThenBlock());
				result.addAll(jassStmtIf.getElseBlock());
			}
			
			@Override
			public void case_JassStmtExitwhen(JassStmtExitwhen jassStmtExitwhen) {
				
			}
			
			@Override
			public void case_JassStmtCall(JassStmtCall jassStmtCall) {
			}
			
		});
		return result;
	}
	
	
}
