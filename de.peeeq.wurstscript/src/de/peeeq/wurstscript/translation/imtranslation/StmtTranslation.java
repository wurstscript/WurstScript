package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jasstranslation.ExprTranslationResult;
import de.peeeq.wurstscript.jasstranslation.JassTranslator;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModuleInstanciation;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreater;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMinus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpPlus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStatements;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtExitwhen;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtLoop;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSet;
import static de.peeeq.wurstscript.jassIm.JassIm.*;

public class StmtTranslation {

	public static ImStmt translate(Expr s, Translator t, ImFunction f) {
		return s.imTranslateExpr(t, f);
	}

	public static ImStmt translate(LocalVarDef s, Translator t, ImFunction f) {
		ImVar v = t.getVarFor(s);
		f.getLocals().add(v);
		if (s.getInitialExpr() instanceof Expr) {
			Expr inital = (Expr) s.getInitialExpr();
			return ImSet(v, inital.imTranslateExpr(t, f));
		} else {
			return ImNull();
		}
	}


	public static ImStmt translate(StmtDestroy s, Translator t, ImFunction f) {
		PscriptType typ = s.getDestroyedObj().attrTyp();
		ClassDef classDef;
		if (typ instanceof PscriptTypeClass) {
			PscriptTypeClass classType = (PscriptTypeClass) typ;
			classDef = classType.getClassDef();
		} else if (typ instanceof PscriptTypeModuleInstanciation) {
			PscriptTypeModuleInstanciation minsType = (PscriptTypeModuleInstanciation) typ;
			classDef = minsType.getDef().attrNearestClassDef();
		} else {
			// TODO destroy interfaces?
			throw new CompileError(s.getSource(), "cannot destroy object of type " + typ);
		}
		return ImCall(t.getDestroyFuncFor(classDef), ImExprs(s.getDestroyedObj().imTranslateExpr(t, f)));
	}


	public static ImStmt translate(StmtErr s, Translator t, ImFunction f) {
		throw new CompileError(s.getSource(), "Source contains errors.");
	}


	public static ImStmt translate(StmtExitwhen s, Translator t, ImFunction f) {
		return ImExitwhen(s.getCond().imTranslateExpr(t, f));
	}


	public static ImStmt translate(StmtForFrom s, Translator t, ImFunction f) {
		throw new CompileError(s.getSource(), "syntactic sugar");
	}


	public static ImStmt translate(StmtForIn s, Translator t, ImFunction f) {
		throw new CompileError(s.getSource(), "syntactic sugar");
	}


	public static ImStmt translate(StmtForRange s, Translator t, ImFunction f) {
		return case_StmtForRange(t, f, s.getLoopVar(), s.getFrom(), s.getTo(), s.getStep(), s.getBody(), Ast.OpPlus(),
				Ast.OpGreater());
	}


	public static ImStmt translate(StmtForRangeDown s, Translator t, ImFunction f) {
		return case_StmtForRange(t, f, s.getLoopVar(), s.getFrom(), s.getTo(), s.getStep(), s.getBody(),
				Ast.OpMinus(), Ast.OpLess());
	}

	private static ImStmt case_StmtForRange(Translator t, ImFunction f, LocalVarDef loopVar,
			Expr from, Expr to, Expr step, WStatements body, OpBinary opStep, OpBinary opCompare) {
		ImVar imLoopVar = t.getVarFor(loopVar);
		f.getLocals().add(imLoopVar);

		ImExpr fromExpr = from.imTranslateExpr(t, f);
		List<ImStmt> result = Lists.newArrayList();
		result.add(ImSet(imLoopVar, fromExpr));

		ImExpr toExpr = addCacheVariableSmart(t, f, result, to);
		ImExpr stepExpr = addCacheVariableSmart(t, f, result, step);

		ImStmts imBody = ImStmts();
		// exitwhen imLoopVar > toExpr
		imBody.add(ImExitwhen(ImCall(t.getFuncFor(opCompare), ImExprs(ImVarAccess(imLoopVar), toExpr))));
		// loop body:
		imBody.addAll(t.translateStatements(f, body));
		// set imLoopVar = imLoopVar + stepExpr
		imBody.add(ImSet(imLoopVar, ImCall(t.getFuncFor(opStep), ImExprs(ImVarAccess(imLoopVar), stepExpr))));
		result.add(ImLoop(imBody));
		return ImStatementExpr(ImStmts(result), ImNull());
	}
	

	private static ImExpr addCacheVariableSmart(Translator t, ImFunction f, List<ImStmt> result, Expr toCache) {
		ImExpr r = toCache.imTranslateExpr(t, f);
		if (r instanceof ImConst) {
			return r;
		}
		ImVar tempVar = t.getNewTempVar();
		result.add(ImSet(tempVar, r));
		return ImVarAccess(tempVar);
	}

	public static ImStmt translate(StmtIf s, Translator t, ImFunction f) {
		return ImIf(s.getCond().imTranslateExpr(t, f), ImStmts(t.translateStatements(f, s.getThenBlock())), ImStmts(t.translateStatements(f, s.getElseBlock())));
	}


	public static ImStmt translate(StmtLoop s, Translator t, ImFunction f) {
		return ImLoop(ImStmts(t.translateStatements(f, s.getBody())));
	}


	public static ImStmt translate(StmtReturn s, Translator t, ImFunction f) {
		return ImReturn(s.getReturnedObj().imTranslateExprOpt(t, f));
	}


	public static ImStmt translate(StmtSet s, Translator t, ImFunction f) {
		// 4 cases for left side:
		// 	1. normal var
		// 	2. array var
		// 	3. tuple var
		// 	4. tuple array var
		
		ImExpr updated = s.getUpdatedExpr().imTranslateExpr(t, f);
		
		List<ImStmt> statements = Lists.newArrayList();
		updated = flatten(updated, statements);
		
		
		ImExpr right = s.getRight().imTranslateExpr(t, f);
		
		if (updated instanceof ImTupleSelection) {
			ImTupleSelection tupleSelection = (ImTupleSelection) updated;
			if (tupleSelection.getTupleExpr() instanceof ImVarAccess) {
				// case: tuple var
				ImVarAccess va = (ImVarAccess) tupleSelection.getTupleExpr();
				return ImSetTuple(va.getVar(), tupleSelection.getTupleIndex(), right);
			} else if (tupleSelection.getTupleExpr() instanceof ImVarArrayAccess) {
				// case: tuple array var
				ImVarArrayAccess va = (ImVarArrayAccess) tupleSelection.getTupleExpr();
				return ImSetArrayTuple(va.getVar(), (ImExpr) va.getIndex().copy(), tupleSelection.getTupleIndex(), right);				
			} else {
				throw new CompileError(s.getSource(), "Cannot translate tuple access");
			}
		} else if (updated instanceof ImVarAccess) {
			ImVarAccess va = (ImVarAccess) updated;
			return ImSet(va.getVar(), right);
		} else if (updated instanceof ImVarArrayAccess) {
			ImVarArrayAccess va = (ImVarArrayAccess) updated;
			return ImSetArray(va.getVar(), (ImExpr) va.getIndex().copy(), right);
		} else {
			throw new CompileError(s.getSource(), "Cannot translate set statement.");
		}
	}


	private static ImExpr flatten(ImExpr updated, List<ImStmt> statements) {
		while (updated instanceof ImStatementExpr) {
			ImStatementExpr se = (ImStatementExpr) updated;
			statements.addAll(se.getStatements().removeAll());
			updated = se.getExpr();
		}
		return updated;
	}

	public static ImStmt translate(StmtWhile s, Translator t, ImFunction f) {
		List<ImStmt> body = Lists.newArrayList();
		// exitwhen not while_condition
		body.add(ImExitwhen(ImCall(t.getFuncFor(Ast.OpNot()), ImExprs(s.getCond().imTranslateExpr(t, f)))));
		body.addAll(t.translateStatements(f, s.getBody()));
		return ImLoop(ImStmts(body));
	}


		
}
