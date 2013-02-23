package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassIm.JassIm.ImExitwhen;
import static de.peeeq.wurstscript.jassIm.JassIm.ImExprs;
import static de.peeeq.wurstscript.jassIm.JassIm.ImIf;
import static de.peeeq.wurstscript.jassIm.JassIm.ImLoop;
import static de.peeeq.wurstscript.jassIm.JassIm.ImReturn;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSet;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSetArray;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSetTuple;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;
import static de.peeeq.wurstscript.jassIm.JassIm.ImTupleExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImTupleSelection;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarArrayAccess;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImConst;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImFlatExpr;
import de.peeeq.wurstscript.jassIm.ImFlatExprOpt;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;

/**
 * 
 * flattening expressions and statements
 * after flattening there will be no more StatementExprs
 * for expressions there might be a StatementExpr on the top level 
 * 
 * TODO wait, its not that easy: you have to make sure that the execution order is not changed for functions and global variables
 * 
 * e.g. take
 * 
 * y = x + StatementExpr(setX(4), 2)
 * 
 * this should be translated to:
 * 
 * temp = x
 * setX(4)
 * y = temp + 2
 * 
 * 
 * alternative: relax language semantics
 *  
 */
public class Flatten {

	
	public static void flatten(ImExpr s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExpr e = s.flattenExpr(stmts, t, f);
		
		exprToStatements(stmts, e, t, f);
	}

	private static void exprToStatements(List<ImStmt> result, JassImElement e, ImTranslator t, ImFunction f) {
		if (e instanceof ImCall) {
			result.add((ImStmt) ((ImStmt) e).copy());			
		} else if (e instanceof ImStatementExpr) {
			ImStatementExpr e2 = (ImStatementExpr) e;
			flattenStatements(result, e2.getStatements(), t, f);
			exprToStatements(result, e2, t, f);
		} else {
			// visit children:
			for (int i=0; i<e.size(); i++) {
				exprToStatements(result, e.get(i), t, f);
			}
		}
	}

	private static void flattenStatements(List<ImStmt> result, ImStmts statements, ImTranslator t, ImFunction f) {
		for (ImStmt s : statements) {
			s.flatten(result, t, f);
		}
	}

	public static void flatten(ImExitwhen s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExpr cond = s.getCondition().flattenExpr(stmts, t, f);
		stmts.add(ImExitwhen(s.getTrace(), cond));
	}


	public static void flatten(ImIf s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExpr cond = s.getCondition().flattenExpr(stmts, t, f);
		stmts.add(ImIf(s.getTrace(), cond, flattenStatements(s.getThenBlock(), t, f), flattenStatements(s.getElseBlock(), t, f)));
	}

	private static ImStmts flattenStatements(ImStmts statements, ImTranslator t, ImFunction f) {
		ImStmts result = ImStmts();
		flattenStatements(result, statements, t, f);
		return result;
	}

	public static void flatten(ImLoop s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		stmts.add(ImLoop(s.getTrace(), flattenStatements(s.getBody(), t, f)));
	}

	public static void flatten(ImReturn s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExprOpt imExpr = s.getReturnValue().flattenExprOpt(stmts, t, f);
		stmts.add(ImReturn(s.getTrace(), imExpr));
	}


	public static void flatten(ImSet s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		e.setParent(null);
		stmts.add(ImSet(s.getTrace(), s.getLeft(), e));
	}

	public static void flatten(ImSetArray s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExpr i = s.getIndex().flattenExpr(stmts, t, f);
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		stmts.add(ImSetArray(s.getTrace(), s.getLeft(), i, e));
	}

	public static void flatten(ImSetArrayTuple s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExpr i = s.getIndex().flattenExpr(stmts, t, f);
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		stmts.add(JassIm.ImSetArrayTuple(s.getTrace(), s.getLeft(), i, s.getTupleIndex(), e));
	}

	public static void flatten(ImSetTuple s, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		stmts.add(ImSetTuple(s.getTrace(), s.getLeft(), s.getTupleIndex(), e));
	}

	public static ImFlatExpr flattenExpr(ImFunctionCall e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		return JassIm.ImFunctionCall(e.getTrace(), e.getFunc(), ImExprs(flattenArgs(e, stmts, t, f)));
	}

	public static ImFlatExpr flattenExpr(ImOperatorCall e, List<ImStmt> stmts, ImTranslator t,	ImFunction f) {
		return JassIm.ImOperatorCall(e.getOp(), ImExprs(flattenArgs(e, stmts, t, f)));
	}

	private static List<ImExpr> flattenArgs(ImCall e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		List<ImExpr> args = Lists.newArrayList();
		for (ImExpr a : e.getArguments()) {
			a = a.flattenExpr(stmts, t, f);
			args.add(a);
		}
		return args;
	}
	

	public static ImFlatExpr flattenExpr(ImConst e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		e.setParent(null);
		return e;
	}


	public static ImFlatExpr flattenExpr(ImStatementExpr e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		flattenStatements(stmts, e.getStatements(), t, f);
		return e.getExpr().flattenExpr(stmts, t, f);
	}


	public static ImFlatExpr flattenExpr(ImTupleExpr e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		List<ImExpr> exprs = Lists.newArrayList();
		for (ImExpr expr : e.getExprs()) {
			expr = expr.flattenExpr(stmts, t, f);
			exprs.add(expr);
		}
		return ImTupleExpr(ImExprs(exprs));
	}


	public static ImFlatExpr flattenExpr(ImTupleSelection e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImFlatExpr tupleExpr = e.getTupleExpr().flattenExpr(stmts, t, f);
		return ImTupleSelection(tupleExpr, e.getTupleIndex());
	}


	public static ImFlatExpr flattenExpr(ImVarAccess e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		e.setParent(null);
		return e;
	}


	public static ImFlatExpr flattenExpr(ImVarArrayAccess e, List<ImStmt> stmts, ImTranslator t, ImFunction f) {
		ImFlatExpr index = e.getIndex().flattenExpr(stmts, t, f);
		return ImVarArrayAccess(e.getVar(), index);
	}


	public static ImFlatExprOpt flattenExpr(ImNoExpr e, List<ImStmt> stmts, ImTranslator translator, ImFunction f) {
		e.setParent(null);
		return e;
	}

	public static void flattenFunc(ImFunction f, ImTranslator translator) {
		ImStmts newBody = flattenStatements(f.getBody(), translator, f);
		f.setBody(newBody);
	}

	public static void flattenProg(ImProg imProg, ImTranslator translator) {
		for (ImFunction f : imProg.getFunctions()) {
			f.flatten(translator);
		}
		translator.assertProperties(AssertProperty.FLAT);
	}

	

		
	
}