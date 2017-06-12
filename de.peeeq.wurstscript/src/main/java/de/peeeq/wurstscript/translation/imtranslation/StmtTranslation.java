package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassIm.JassIm.ImExitwhen;
import static de.peeeq.wurstscript.jassIm.JassIm.ImExprs;
import static de.peeeq.wurstscript.jassIm.JassIm.ImIf;
import static de.peeeq.wurstscript.jassIm.JassIm.ImLoop;
import static de.peeeq.wurstscript.jassIm.JassIm.ImNull;
import static de.peeeq.wurstscript.jassIm.JassIm.ImOperatorCall;
import static de.peeeq.wurstscript.jassIm.JassIm.ImReturn;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSet;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSetArray;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSetArrayTuple;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSetTuple;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStatementExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarAccess;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.EndFunctionStatement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NoDefaultCase;
import de.peeeq.wurstscript.ast.StartFunctionStatement;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.StmtExitwhen;
import de.peeeq.wurstscript.ast.StmtForFrom;
import de.peeeq.wurstscript.ast.StmtForIn;
import de.peeeq.wurstscript.ast.StmtForRangeDown;
import de.peeeq.wurstscript.ast.StmtForRangeUp;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtSkip;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.SwitchCase;
import de.peeeq.wurstscript.ast.SwitchDefaultCaseStatements;
import de.peeeq.wurstscript.ast.SwitchStmt;
import de.peeeq.wurstscript.ast.WBlock;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.ImConst;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayMultiAccess;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

public class StmtTranslation {

	public static ImStmt translate(Expr s, ImTranslator t, ImFunction f) {
		return s.imTranslateExpr(t, f);
	}

	public static ImStmt translate(LocalVarDef s, ImTranslator t, ImFunction f) {
		ImVar v = t.getVarFor(s);
		f.getLocals().add(v);
		if (s.getInitialExpr() instanceof Expr) {
			Expr inital = (Expr) s.getInitialExpr();
			return ImSet(s, v, inital.imTranslateExpr(t, f));
		} else {
			return ImNull();
		}
	}



	public static ImStmt translate(StmtErr s, ImTranslator t, ImFunction f) {
		throw new CompileError(s.getSource(), "Source contains errors.");
	}


	public static ImStmt translate(StmtExitwhen s, ImTranslator t, ImFunction f) {
		return ImExitwhen(s, s.getCond().imTranslateExpr(t, f));
	}


	public static ImStmt translate(StmtForFrom s, ImTranslator t, ImFunction f) {
		throw new CompileError(s.getSource(), "syntactic sugar");
	}


	public static ImStmt translate(StmtForIn s, ImTranslator t, ImFunction f) {
		throw new CompileError(s.getSource(), "syntactic sugar");
	}


	public static ImStmt translate(StmtForRangeUp s, ImTranslator t, ImFunction f) {
		return case_StmtForRange(t, f, s.getLoopVar(), s.getTo(), s.getStep(), s.getBody(), WurstOperator.PLUS,
				WurstOperator.GREATER, s);
	}


	public static ImStmt translate(StmtForRangeDown s, ImTranslator t, ImFunction f) {
		return case_StmtForRange(t, f, s.getLoopVar(), s.getTo(), s.getStep(), s.getBody(),
				WurstOperator.MINUS, WurstOperator.LESS, s);
	}

	private static ImStmt case_StmtForRange(ImTranslator t, ImFunction f, LocalVarDef loopVar,
			Expr to, Expr step, WStatements body, WurstOperator opStep, WurstOperator opCompare, Element trace) {
		ImVar imLoopVar = t.getVarFor(loopVar);
		f.getLocals().add(imLoopVar);

		Expr from = (Expr) loopVar.getInitialExpr();
		ImExpr fromExpr = from.imTranslateExpr(t, f);
		List<ImStmt> result = Lists.newArrayList();
		result.add(ImSet(loopVar, imLoopVar, fromExpr));

		ImExpr toExpr = addCacheVariableSmart(t, f, result, to, TypesHelper.imInt());
		ImExpr stepExpr = addCacheVariableSmart(t, f, result, step, TypesHelper.imInt());

		ImStmts imBody = ImStmts();
		// exitwhen imLoopVar > toExpr
		imBody.add(ImExitwhen(trace, ImOperatorCall(opCompare, ImExprs(ImVarAccess(imLoopVar), toExpr))));
		// loop body:
		imBody.addAll(t.translateStatements(f, body));
		// set imLoopVar = imLoopVar + stepExpr
		imBody.add(ImSet(trace, imLoopVar, ImOperatorCall(opStep, ImExprs(ImVarAccess(imLoopVar), stepExpr))));
		result.add(ImLoop(trace, imBody));
		return ImStatementExpr(ImStmts(result), ImNull());
	}
	

	private static ImExpr addCacheVariableSmart(ImTranslator t, ImFunction f, List<ImStmt> result, Expr toCache, ImType type) {
		ImExpr r = toCache.imTranslateExpr(t, f);
		if (r instanceof ImConst) {
			return r;
		}
		ImVar tempVar = JassIm.ImVar(toCache, type, "temp", false);
		f.getLocals().add(tempVar);
		result.add(ImSet(toCache, tempVar, r));
		return ImVarAccess(tempVar);
	}

	public static ImStmt translate(StmtIf s, ImTranslator t, ImFunction f) {
		return ImIf(s, s.getCond().imTranslateExpr(t, f), ImStmts(t.translateStatements(f, s.getThenBlock())), ImStmts(t.translateStatements(f, s.getElseBlock())));
	}


	public static ImStmt translate(StmtLoop s, ImTranslator t, ImFunction f) {
		return ImLoop(s, ImStmts(t.translateStatements(f, s.getBody())));
	}


	public static ImStmt translate(StmtReturn s, ImTranslator t, ImFunction f) {
		return ImReturn(s, s.getReturnedObj().imTranslateExprOpt(t, f));
	}


	public static ImStmt translate(StmtSet s, ImTranslator t, ImFunction f) {
		// 4 cases for left side:
		// 	1. normal var
		// 	2. array var
		// 	3. tuple var
		// 	4. tuple array var
		
		ImExpr updated = s.getUpdatedExpr().imTranslateExpr(t, f);
		
		List<ImStmt> statements = Lists.newArrayList();
		updated = flatten(updated, statements);
		
		ImExpr right = s.getRight().imTranslateExpr(t, f);
		
		return translateAssignment(s, updated, right);
	}

    private static ImStmt translateAssignment(AstElementWithSource s, ImExpr updated, ImExpr right) throws CompileError {
        if (updated instanceof ImTupleSelection) {
			ImTupleSelection tupleSelection = (ImTupleSelection) updated;
			ImExpr tupleExpr = tupleSelection.getTupleExpr();
			
            if (tupleExpr instanceof ImVarAccess) {
				// case: tuple var
				ImVarAccess va = (ImVarAccess) tupleExpr;
				return ImSetTuple(s, va.getVar(), tupleSelection.getTupleIndex(), right);
			} else if (tupleExpr instanceof ImVarArrayAccess) {
				// case: tuple array var
				ImVarArrayAccess va = (ImVarArrayAccess) tupleExpr;
				return ImSetArrayTuple(s, va.getVar(), (ImExpr) va.getIndex().copy(), tupleSelection.getTupleIndex(), right);	
			} else {
				throw new CompileError(s.getSource(), "Cannot translate tuple access");
			}
		} else if (updated instanceof ImVarAccess) {
			ImVarAccess va = (ImVarAccess) updated;
			return ImSet(s, va.getVar(), right);
		} else if (updated instanceof ImVarArrayAccess) {
			ImVarArrayAccess va = (ImVarArrayAccess) updated;
			return ImSetArray(s, va.getVar(), (ImExpr) va.getIndex().copy(), right);
		} else if (updated instanceof ImVarArrayMultiAccess) {
			ImVarArrayMultiAccess va = (ImVarArrayMultiAccess) updated;
			return JassIm.ImSetArrayMulti(s, va.getVar(),JassIm.ImExprs((ImExpr)va.getIndex1().copy(),(ImExpr) va.getIndex2().copy()), right);
		} else if (updated instanceof ImTupleExpr) {
		    // TODO this could lead to expressions being evaluated twice
            ImTupleExpr te = (ImTupleExpr) updated;
            ImStmts stmts = JassIm.ImStmts();
            for (int i=0; i<te.getExprs().size(); i++) {
                ImExpr l = (ImExpr) te.getExprs().get(i).copy();
                ImExpr r;
                if (right instanceof ImTupleExpr) {
                    ImTupleExpr rt = (ImTupleExpr) right;
                    r = (ImExpr) rt.getExprs().get(i).copy();                 
                } else {
                    r = JassIm.ImTupleSelection((ImExpr) right.copy(), i);
                }
                stmts.add(translateAssignment(s, l, r));
            }
            return JassIm.ImStatementExpr(stmts, JassIm.ImNull());
		} else {
			throw new CompileError(s.getSource(), "Cannot translate set statement, updated = " + updated.getClass().getSimpleName());
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

	public static ImStmt translate(StmtWhile s, ImTranslator t, ImFunction f) {
		List<ImStmt> body = Lists.newArrayList();
		// exitwhen not while_condition
		body.add(ImExitwhen(s.getCond(), ImOperatorCall(WurstOperator.NOT, ImExprs(s.getCond().imTranslateExpr(t, f)))));
		body.addAll(t.translateStatements(f, s.getBody()));
		return ImLoop(s, ImStmts(body));
	}

	public static ImStmt translate(StmtSkip s, ImTranslator translator,	ImFunction f) {
		return JassIm.ImNull();
	}

	public static ImStmt translate(SwitchStmt switchStmt, ImTranslator t, ImFunction f) {
		List<ImStmt> result = Lists.newArrayList();
		ImType type = switchStmt.getExpr().attrTyp().imTranslateType();
		ImExpr tempVar = addCacheVariableSmart(t, f, result, switchStmt.getExpr(), type);
		// generate ifs
		// leerer Block:
		//ImStmts();
		// if else
		//ImIf(trace, condition, thenBlock, elseBlock);
		// vergleich
		//ImOperatorCall(Ast.OpEquals(), ImExprs(a,b))
		
		ImIf lastIf = null;
		SwitchCase cse;
		for ( int i = 0; i < switchStmt.getCases().size(); i++ ) {
			cse = switchStmt.getCases().get(i);
			if (lastIf == null) {
				lastIf = ImIf(switchStmt, ImOperatorCall(WurstOperator.EQ, ImExprs((ImExpr)tempVar.copy(), cse.getExpr().imTranslateExpr(t, f)) ), ImStmts(t.translateStatements(f, cse.getStmts())), ImStmts() );
				result.add(lastIf);
			} else {
				ImIf tmp = ImIf(switchStmt, ImOperatorCall(WurstOperator.EQ, ImExprs((ImExpr)tempVar.copy(), cse.getExpr().imTranslateExpr(t, f)) ), ImStmts(t.translateStatements(f, cse.getStmts())), ImStmts() );
				lastIf.setElseBlock(ImStmts(tmp));
				lastIf = tmp;
			}
		}
		
		if (lastIf == null) {
			throw new CompileError(switchStmt.attrSource(), "No cases in switch?");
		}
		
//		WLogger.info("it is a " + switchStmt.getSwitchDefault().getClass());
		if (switchStmt.getSwitchDefault() instanceof SwitchDefaultCaseStatements) {
			
//			WLogger.info("indeed it is");
			SwitchDefaultCaseStatements dflt = (SwitchDefaultCaseStatements) switchStmt.getSwitchDefault();
			lastIf.setElseBlock(ImStmts(t.translateStatements(f,dflt.getStmts())));
		}else if (switchStmt.getSwitchDefault() instanceof NoDefaultCase) {
//			WLogger.info("wtf?");
		}
		
		
		
		return ImStatementExpr(ImStmts(result), ImNull());
	}

	public static ImStmt translate(EndFunctionStatement endFunctionStatement, ImTranslator translator, ImFunction f) {
		return ImNull();
	}

	public static ImStmt translate(StartFunctionStatement startFunctionStatement, ImTranslator translator, ImFunction f) {
		return ImNull();
	}

	public static ImStmt translate(WBlock block, ImTranslator translator, ImFunction f) {
		ImStmts stmts = ImStmts();
		for (WStatement s: block.getBody()) {
			stmts.add(s.imTranslateStmt(translator, f));
		}
		return JassIm.ImStatementExpr(stmts, ImNull());
	}



		
}
