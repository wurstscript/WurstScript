package de.peeeq.wurstscript.translation.imtojass;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import static de.peeeq.wurstscript.jassAst.JassAst.*;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;

public class StatementTranslation {


	public static void translate(ImExitwhen imExitwhen, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		stmts.add(JassStmtExitwhen(imExitwhen.getCondition().translateSingle(translator)));
	}

	public static void translate(ImIf imIf, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		JassExpr cond = imIf.getCondition().translateSingle(translator);
		JassStatements thenBlock = JassStatements(); 
		imIf.getThenBlock().translate(thenBlock, f, translator);
		JassStatements elseBlock = JassStatements();
		imIf.getElseBlock().translate(elseBlock, f, translator);
		stmts.add(JassStmtIf(cond, thenBlock, elseBlock));
	}


	public static void translate(ImLoop imLoop, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		JassStatements body = JassStatements();
		imLoop.getBody().translate(body, f, translator);
		stmts.add(JassStmtLoop(body));
	}

	public static void translate(ImReturn imReturn, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		if (imReturn.getReturnValue() instanceof ImNoExpr) {
			stmts.add(JassStmtReturnVoid());
		} else {
			ImExpr e = (ImExpr) imReturn.getReturnValue();
			List<JassExpr> returnValues = e.translate(translator);
			
			
			List<String> retTypes = imReturn.getNearestFunc().getReturnType().translateType();
		
			// assign to temp return values
			for (int i=1; i<returnValues.size(); i++) {
				
				JassVar tempVar = translator.getTempReturnVar(retTypes.get(i), i);
				stmts.add(JassStmtSet(tempVar.getName(), returnValues.get(i)));
				
			}
			// XXX remark: this will evaluate the first expression of the tuple at the end
			JassExpr returnValue = returnValues.get(0);
			stmts.add(JassStmtReturn(returnValue));
		}
	}

	public static void translate(ImSet imSet, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		List<JassVar> vars = translator.getJassVarsFor(imSet.getLeft());
		List<JassExpr> exprs = imSet.getRight().translate(translator);
		if (vars.size() == 1) {
			stmts.add(JassStmtSet(vars.get(0).getName(), exprs.get(0)));			
		} else { // tuple assignment
			
			if (vars.size() != exprs.size()) {
				throw new Error("Expected " + vars.size() + " expressions, but found " + exprs.size());
			}
			
			// 1. assign to temp variables:
			List<JassVar> tempVars = Lists.newArrayListWithCapacity(vars.size());
			for (int i=0; i < vars.size(); i++) {
				JassVar tempVar = translator.newTempVar(f, vars.get(i).getType(), "temp_" + vars.get(i).getName());
				tempVars.add(i, tempVar);
				stmts.add(JassStmtSet(tempVar.getName(), exprs.get(i)));
			}
			// 2. assign to real variables
			for (int i=0; i < vars.size(); i++) {
				stmts.add(JassStmtSet(vars.get(i).getName(), JassExprVarAccess(tempVars.get(i).getName())));
			}
		}
	}

	public static void translate(ImExpr imExpr, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		List<JassExpr> exprs = imExpr.translate(translator);
		for (JassExpr expr : exprs) {
			if (expr instanceof JassExprFunctionCall) {
				JassExprFunctionCall fc = (JassExprFunctionCall) expr;
				stmts.add(JassAst.JassStmtCall(fc.getFuncName(), fc.getArguments().copy()));
			}
		}
	}

	

	public static void translate(ImSetArray imSet, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		List<JassVar> vars = translator.getJassVarsFor(imSet.getLeft());
		List<JassExpr> exprs = imSet.getRight().translate(translator);
		if (vars.size() == 1) {
			stmts.add(JassAst.JassStmtSetArray(vars.get(0).getName(), imSet.getIndex().translateSingle(translator), exprs.get(0)));			
		} else { // tuple assignment
			
			// 1. assign to temp variables:
			List<JassVar> tempVars = Lists.newArrayListWithCapacity(vars.size());
			for (int i=0; i < vars.size(); i++) {
				JassVar tempVar = translator.newTempVar(f, vars.get(i).getType(), "temp_" + vars.get(i).getName());
				tempVars.add(i, tempVar);
				stmts.add(JassStmtSet(tempVar.getName(), exprs.get(i)));
			}
			// 2. assign to real variables
			for (int i=0; i < vars.size(); i++) {
				stmts.add(JassStmtSetArray(vars.get(i).getName(), imSet.getIndex().translateSingle(translator), JassExprVarAccess(tempVars.get(i).getName())));
			}
		}
	}
	
	public static void translate(ImSetArrayTuple imSet, List<JassStatement> stmts, JassFunction f,
			ImToJassTranslator translator) {
		List<JassVar> vars = translator.getJassVarsFor(imSet.getLeft());
		String varName = vars.get(imSet.getTupleIndex()).getName();
		JassExpr index = imSet.getIndex().translateSingle(translator);
		JassExpr right = imSet.getRight().translateSingle(translator);
		stmts.add(JassAst.JassStmtSetArray(varName, index, right));
	}

	public static void translate(ImSetTuple imSet, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		List<JassVar> vars = translator.getJassVarsFor(imSet.getLeft());
		String varName = vars.get(imSet.getTupleIndex()).getName();
		JassExpr right = imSet.getRight().translateSingle(translator);
		stmts.add(JassAst.JassStmtSet(varName, right));
	}

	public static void translate(ImStmts imStmts, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		for (ImStmt s : imStmts) {
			s.translate(stmts, f, translator);
		}
	}

}
