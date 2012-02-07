package de.peeeq.wurstscript.jassoptimizer;

import java.util.Collection;
import java.util.Collections;
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

	// a list of all functions which should be inlined
	private Collection<JassFunction> functionsToInline;

	// counter for the number of inlined functions, could be used for fixed point analysis
	private int inlinedFunctions = 0;
	
	public FunctionInliner(Collection<JassFunction> functionsToInline) {
		this.functionsToInline =  functionsToInline;
	}
	
	/**
	 * inline functions in f
	 */
	public void inlineFunctions(JassFunction f) {
		inlineJassFunctions(f, f.getBody());
	}

	private void inlineJassFunctions(JassFunction f, List<JassStatement> statements) {
//		ListIterator<JassStatement> statementsIterator = statements.listIterator();
//		while (statementsIterator.hasNext()) {
//			JassStatement s = statementsIterator.next();
//			List<JassStatement> newStatements = inlineJassFunctions(f, s);
//			for (JassStatement n : newStatements) {
//				statementsIterator.add(n);
//			}
//		}
		List<JassStatement> newStatements = Lists.newArrayList();
		for (JassStatement s : statements) {
			newStatements.addAll(inlineJassFunctions(f, s));
		}
		statements.clear();
		statements.addAll(newStatements);
	}

	private List<JassStatement> inlineJassFunctions(JassFunction f, JassStatement s) {
		if (s instanceof JassStmtCall) {
//			return inlineJassStmtCall(f, (JassStmtCall) s);
		}
		
		// TODO replace function calls as expressions

		// process children
		inlineJassFunctions(f, getChildStatements(s));
		
		return Collections.singletonList(s);
	}
	
	


	/**
	 * get all child statements of a given JassStatement
	 * @param s
	 * @return
	 */
	private List<JassStatement> getChildStatements(JassStatement s) {
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
