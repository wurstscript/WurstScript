package de.peeeq.wurstscript.validation;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.OpUpdateAssign;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.StmtExitwhen;
import de.peeeq.wurstscript.ast.StmtForFrom;
import de.peeeq.wurstscript.ast.StmtForIn;
import de.peeeq.wurstscript.ast.StmtForRange;
import de.peeeq.wurstscript.ast.StmtForRangeDown;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.utils.Utils;

public class UninitializedVars {
	
	static void checkFunc(Collection<NameDef> locals, WStatements statements) {
		if (Utils.isJassCode(statements)) {
			// JassCode needs no safety -> use wurst
			return;
		}
		Set<LocalVarDef> uninitializedVars = Sets.newHashSet();
		for (NameDef n : locals) {
			if (n instanceof LocalVarDef) {
				LocalVarDef localVarDef = (LocalVarDef) n;
				if (localVarDef.attrTyp() instanceof PScriptTypeArray) {
					
				} else {
					uninitializedVars.add((LocalVarDef) n);
				}
			}
		}

		uninitializedVars = checkStatements(statements, uninitializedVars);
	}
	
	private static void checkExpr(OptExpr optExpr, final Set<LocalVarDef> uninitializedVars) {
		optExpr.accept(new OptExpr.DefaultVisitor() {
			@Override
			public void visit(ExprVarAccess exprVarAccess) {
				if (uninitializedVars.contains(exprVarAccess.attrNameDef())) {
					attr.addError(exprVarAccess.getSource(), "Variable " + exprVarAccess.getVarName() + 
							" is not initialized.");
				}
			}
		});
	}
	
	private static Set<LocalVarDef> checkStatements(WStatements statements, final Set<LocalVarDef> p_uninitializedVars) {
		final Set<LocalVarDef> uninitializedVars = Sets.newHashSet();
		uninitializedVars.addAll(p_uninitializedVars);
		for (WStatement s : statements) {
			s.match(new WStatement.MatcherVoid() {

				@Override
				public void case_StmtWhile(StmtWhile stmtWhile) {
					checkExpr(stmtWhile.getCond(), uninitializedVars);
					checkStatements(stmtWhile.getBody(), uninitializedVars);
				}

				@Override
				public void case_StmtSet(StmtSet stmtSet) {
					NameRef left = stmtSet.getUpdatedExpr();
					checkExpr(stmtSet.getRight(), uninitializedVars);
					if (left instanceof ExprVarAccess) {
						ExprVarAccess exprVarAccess = (ExprVarAccess) left;
						if (stmtSet.getOpAssign() instanceof OpUpdateAssign) {
							checkExpr(left, uninitializedVars);
						}
						uninitializedVars.remove(exprVarAccess.attrNameDef());
					} else {
						checkExpr(left, uninitializedVars);
					}
				}

				@Override
				public void case_StmtReturn(StmtReturn stmtReturn) {
					checkExpr(stmtReturn.getReturnedObj(), uninitializedVars);
				}

				@Override
				public void case_StmtLoop(StmtLoop stmtLoop) {
					Set<LocalVarDef> unLoop = checkStatements(stmtLoop.getBody(), uninitializedVars);
					// TODO add parameter to only return things before exitwhen
				}

				@Override
				public void case_StmtIf(StmtIf stmtIf) {
					checkExpr(stmtIf.getCond(), uninitializedVars);
					
					Set<LocalVarDef> unThen = checkStatements(stmtIf.getThenBlock(), uninitializedVars);
					Set<LocalVarDef> unElse = checkStatements(stmtIf.getElseBlock(), uninitializedVars);
					// the uninitialized vars after the if is the union of both uninitialized vars (then + else)
					uninitializedVars.clear();
					uninitializedVars.addAll(unThen);
					uninitializedVars.addAll(unElse);
				}

				@Override
				public void case_StmtForRange(StmtForRange stmtForRange) {
					uninitializedVars.remove(stmtForRange.getLoopVar());
					checkStatements(stmtForRange.getBody(), uninitializedVars);
				}

				@Override
				public void case_StmtExitwhen(StmtExitwhen stmtExitwhen) {
					checkExpr(stmtExitwhen.getCond(), uninitializedVars);
				}

				@Override
				public void case_StmtErr(StmtErr stmtErr) {
				}

				@Override
				public void case_StmtDestroy(StmtDestroy stmtDestroy) {
					checkExpr(stmtDestroy.getDestroyedObj(), uninitializedVars);
				}

				@Override
				public void case_LocalVarDef(LocalVarDef localVarDef) {
					if (localVarDef.getInitialExpr() instanceof Expr) {
						uninitializedVars.remove(localVarDef);
						checkExpr(localVarDef.getInitialExpr(), uninitializedVars);
					}
				}

				@Override
				public void case_ExprNewObject(ExprNewObject exprNewObject) {
					checkExpr(exprNewObject, uninitializedVars);
				}

				@Override
				public void case_ExprMemberMethod(ExprMemberMethod exprMemberMethod) {
					checkExpr(exprMemberMethod, uninitializedVars);
				}

				@Override
				public void case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
					checkExpr(exprFunctionCall, uninitializedVars);
				}

				@Override
				public void case_StmtForIn(StmtForIn s) {
					uninitializedVars.remove(s.getLoopVar());
					checkStatements(s.getBody(), uninitializedVars);
				}

				@Override
				public void case_StmtForRangeDown(StmtForRangeDown s) {
					uninitializedVars.remove(s.getLoopVar());
					checkStatements(s.getBody(), uninitializedVars);
				}

				@Override
				public void case_StmtForFrom(StmtForFrom s) {
					uninitializedVars.remove(s.getLoopVar());
					checkStatements(s.getBody(), uninitializedVars);
				}
			});
		}
		return uninitializedVars;
	}
}
