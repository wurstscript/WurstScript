package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;

import com.google.common.collect.Lists;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImConst;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
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

public class Flatten {

	public static List<ImStmt> flatten(ImExpr s, Translator t, ImFunction f) {
		ImExpr e = s.flattenExpr(t, f);
		List<ImStmt> result = Lists.newArrayList();
		exprToStatements(result, e, t, f);
		return result;
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
			result.addAll(s.flatten(t, f));
		}
	}

	public static List<ImStmt> flatten(ImExitwhen s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		ImExpr cond = s.getCondition().flattenExpr(t, f);
		cond = extractStatements(result, cond, t, f);
		result.add(ImExitwhen(cond));
		return result;
	}

	private static ImExpr extractStatements(List<ImStmt> result, ImExpr e, Translator t, ImFunction f) {
		if (e instanceof ImStatementExpr) {
			ImStatementExpr e2 = (ImStatementExpr) e;
			flattenStatements(result, e2.getStatements(), t, f);
			return extractStatements(result, e2, t, f);
		} 
		return e;
	}

	public static List<ImStmt> flatten(ImIf s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		ImExpr cond = s.getCondition().flattenExpr(t, f);
		cond = extractStatements(result, cond, t, f);
		result.add(ImIf(cond, flattenStatements(s.getThenBlock(), t, f), flattenStatements(s.getElseBlock(), t, f)));
		return result;
	}

	private static ImStmts flattenStatements(ImStmts statements, Translator t, ImFunction f) {
		ImStmts result = ImStmts();
		flattenStatements(result, statements, t, f);
		return result;
	}

	public static List<ImStmt> flatten(ImLoop s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		// TODO
		return result;
	}

	public static List<ImStmt> flatten(ImReturn s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		// TODO
		return result;
	}

	public static List<ImStmt> flatten(ImSet s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		// TODO
		return result;
	}

	public static List<ImStmt> flatten(ImSetArray s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		// TODO
		return result;
	}

	public static List<ImStmt> flatten(ImSetArrayTuple s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		// TODO
		return result;
	}

	public static List<ImStmt> flatten(ImSetTuple s, Translator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		// TODO
		return result;
	}

	public static ImExpr flattenExpr(ImCall e, Translator t, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


	public static ImExpr flattenExpr(ImConst e, Translator t, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


	public static ImExpr flattenExpr(ImStatementExpr e, Translator t, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


	public static ImExpr flattenExpr(ImTupleExpr e, Translator t, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


	public static ImExpr flattenExpr(ImTupleSelection e, Translator t, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


	public static ImExpr flattenExpr(ImVarAccess e, Translator t, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


	public static ImExpr flattenExpr(ImVarArrayAccess e, Translator t, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

		
	
}
