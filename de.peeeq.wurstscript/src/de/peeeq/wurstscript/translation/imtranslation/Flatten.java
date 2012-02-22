package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;

import com.google.common.collect.Lists;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImConst;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImFlatExpr;
import de.peeeq.wurstscript.jassIm.ImFlatExprOpt;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
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

	public static void flatten(ImExpr s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExpr e = s.flattenExpr(stmts, t, f);
		
		exprToStatements(stmts, e, t, f);
	}

	private static void exprToStatements(List<ImStmt> result, ImExpr e, Translator t, ImFunction f) {
		if (e instanceof ImStatementExpr) {
			ImStatementExpr e2 = (ImStatementExpr) e;
			flattenStatements(result, e2.getStatements(), t, f);
			exprToStatements(result, e2, t, f);
		} else if (e instanceof ImCall) {
			result.add(e);			
		}
	}

	private static void flattenStatements(List<ImStmt> result, ImStmts statements, Translator t, ImFunction f) {
		for (ImStmt s : statements) {
			s.flatten(result, t, f);
		}
	}

	public static void flatten(ImExitwhen s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExpr cond = s.getCondition().flattenExpr(stmts, t, f);
		stmts.add(ImExitwhen(cond));
	}


	public static void flatten(ImIf s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExpr cond = s.getCondition().flattenExpr(stmts, t, f);
		stmts.add(ImIf(cond, flattenStatements(s.getThenBlock(), t, f), flattenStatements(s.getElseBlock(), t, f)));
	}

	private static ImStmts flattenStatements(ImStmts statements, Translator t, ImFunction f) {
		ImStmts result = ImStmts();
		flattenStatements(result, statements, t, f);
		return result;
	}

	public static void flatten(ImLoop s, List<ImStmt> stmts, Translator t, ImFunction f) {
		stmts.add(ImLoop(flattenStatements(s.getBody(), t, f)));
	}

	public static void flatten(ImReturn s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExprOpt imExpr = s.getReturnValue().flattenExprOpt(stmts, t, f);
		stmts.add(ImReturn(imExpr));
	}


	public static void flatten(ImSet s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		stmts.add(ImSet(s.getLeft(), e));
	}

	public static void flatten(ImSetArray s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExpr i = s.getIndex().flattenExpr(stmts, t, f);
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		stmts.add(ImSetArray(s.getLeft(), i, e));
	}

	public static void flatten(ImSetArrayTuple s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExpr i = s.getIndex().flattenExpr(stmts, t, f);
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		stmts.add(JassIm.ImSetArrayTuple(s.getLeft(), i, s.getTupleIndex(), e));
	}

	public static void flatten(ImSetTuple s, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImExpr e = s.getRight().flattenExpr(stmts, t, f);
		stmts.add(ImSetTuple(s.getLeft(), s.getTupleIndex(), e));
	}

	public static ImFlatExpr flattenExpr(ImCall e, List<ImStmt> stmts, Translator t, ImFunction f) {
		List<ImExpr> args = Lists.newArrayList();
		for (ImExpr a : e.getArguments()) {
			a = a.flattenExpr(stmts, t, f);
			args.add(a);
		}
		return JassIm.ImCall(e.getFunc(), ImExprs(args));
	}


	public static ImFlatExpr flattenExpr(ImConst e, List<ImStmt> stmts, Translator t, ImFunction f) {
		return e;
	}


	public static ImFlatExpr flattenExpr(ImStatementExpr e, List<ImStmt> stmts, Translator t, ImFunction f) {
		flattenStatements(stmts, e.getStatements(), t, f);
		return e.getExpr().flattenExpr(stmts, t, f);
	}


	public static ImFlatExpr flattenExpr(ImTupleExpr e, List<ImStmt> stmts, Translator t, ImFunction f) {
		List<ImExpr> exprs = Lists.newArrayList();
		for (ImExpr expr : e.getExprs()) {
			expr = expr.flattenExpr(stmts, t, f);
			exprs.add(expr);
		}
		return ImTupleExpr(ImExprs(exprs));
	}


	public static ImFlatExpr flattenExpr(ImTupleSelection e, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImFlatExpr tupleExpr = e.getTupleExpr().flattenExpr(stmts, t, f);
		return ImTupleSelection(tupleExpr, e.getTupleIndex());
	}


	public static ImFlatExpr flattenExpr(ImVarAccess e, List<ImStmt> stmts, Translator t, ImFunction f) {
		return e;
	}


	public static ImFlatExpr flattenExpr(ImVarArrayAccess e, List<ImStmt> stmts, Translator t, ImFunction f) {
		ImFlatExpr index = e.getIndex().flattenExpr(stmts, t, f);
		return ImVarArrayAccess(e.getVar(), index);
	}


	public static ImFlatExprOpt flattenExpr(ImNoExpr e, List<ImStmt> stmts, Translator translator, ImFunction f) {
		return e;
	}

		
	
}
