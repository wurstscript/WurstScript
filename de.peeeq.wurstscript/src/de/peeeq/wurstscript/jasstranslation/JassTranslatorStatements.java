package de.peeeq.wurstscript.jasstranslation;

import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprNull;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprUnary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarArrayAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprlist;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreater;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMinus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMult;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpNot;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpPlus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStatements;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtExitwhen;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtIf;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtLoop;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturn;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturnVoid;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSet;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSetArray;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprAssignable;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.OpAssign;
import de.peeeq.wurstscript.ast.OpAssignment;
import de.peeeq.wurstscript.ast.OpMinusAssign;
import de.peeeq.wurstscript.ast.OpMultAssign;
import de.peeeq.wurstscript.ast.OpPlusAssign;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.StmtExitwhen;
import de.peeeq.wurstscript.ast.StmtForRange;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprIntVal;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModuleInstanciation;
import de.peeeq.wurstscript.utils.Utils;

public class JassTranslatorStatements {

	private JassTranslator translator;
	private JassManager manager;

	public JassTranslatorStatements(JassTranslator t) {
		this.translator = t;
		this.manager = t.manager;
	}
	
	
	List<JassStatement> translateStatements(JassFunction f, WStatements statements) {
		List<JassStatement> result = Lists.newLinkedList();
		for (WStatement s : statements) {
			result.addAll(translateStatement(f, s));
		}
		return result;
	}

	private List<JassStatement> translateStatement(final JassFunction f, final WStatement s) {
		final List<JassStatement> result = Lists.newLinkedList();
		s.match(new WStatement.MatcherVoid() {

			@Override
			public void case_ExprMemberMethod(ExprMemberMethod exprMemberMethod) {
				case_Expr(exprMemberMethod);

			}


			@Override
			public void case_StmtLoop(StmtLoop stmtLoop) {
				List<JassStatement> body = translateStatements(f, stmtLoop.getBody());
				result.add(JassStmtLoop(JassStatements(body)));
			}

			@Override
			public void case_StmtSet(StmtSet stmtSet) {
				final ExprTranslationResult right = translator.translateExpr(f, stmtSet.getRight());
				final JassOpBinary binaryOp = stmtSet.getOp().match(new OpAssignment.Matcher<JassOpBinary>() {

					@Override
					public JassOpBinary case_OpMinusAssign(OpMinusAssign opMinusAssign) {
						return JassOpMinus();
					}

					@Override
					public JassOpBinary case_OpPlusAssign(OpPlusAssign opPlusAssign) {
						return JassOpPlus();
					}

					@Override
					public JassOpBinary case_OpMultAssign(OpMultAssign opMultAssign) {
						return JassOpMult();
					}

					@Override
					public JassOpBinary case_OpAssign(OpAssign opAssign) {
						return null;
					}
				});


				stmtSet.getLeft().match(new ExprAssignable.MatcherVoid() {

					@Override
					public void case_ExprMemberVar(ExprMemberVar exprMemberVar) {
						VarDef leftVar = (VarDef) exprMemberVar.attrNameDef();
						String leftJassVar = translator.manager.getJassVarNameFor(leftVar);
						ExprTranslationResult index = translator.translateExpr(f, exprMemberVar.getLeft());

						withIndex(leftJassVar, index);
					}

					private void withIndex(String leftJassVar, ExprTranslationResult index) {
						result.addAll(index.getStatements());
						result.addAll(right.getStatements());
						JassExpr indexExpr;

						if (binaryOp == null) {
							result.add(JassStmtSetArray(leftJassVar, index.getExpr(), right.getExpr())); 
						} else {

							if (index.getExpr() instanceof ExprIntVal 
									|| index.getExpr() instanceof ExprVarAccess) {
								indexExpr = index.getExpr();
							} else {
								JassVar tempIndex = translator.getNewTempVar(f, "integer");
								result.add(JassStmtSet(tempIndex.getName(), index.getExpr()));
								indexExpr = JassExprVarAccess(tempIndex.getName());
							}

							result.add(JassStmtSetArray(leftJassVar, indexExpr, 
									JassExprBinary(JassExprVarArrayAccess(leftJassVar, indexExpr), binaryOp, right.getExpr())));
						}
					}

					private void withoutIndex(String leftJassVar) {
						result.addAll(right.getStatements());
						if (binaryOp == null) {
							result.add(JassStmtSet(leftJassVar, right.getExpr())); 
						} else {
							result.add(JassStmtSet(leftJassVar, 
									JassExprBinary(JassExprVarAccess(leftJassVar), binaryOp, right.getExpr())));
						}
					}

					@Override
					public void case_ExprMemberArrayVar(ExprMemberArrayVar exprMemberArrayVar) {
						throw new Error("Array member vars not supported yet.");
					}

					@Override
					public void case_ExprVarAccess(ExprVarAccess exprVarAccess) {
						VarDef leftVar = (VarDef) exprVarAccess.attrNameDef();
						String leftJassVar = manager.getJassVarNameFor(leftVar);
						result.addAll(right.getStatements());
						if (leftVar.attrIsDynamicClassMember()) {
							withIndex(leftJassVar, new ExprTranslationResult(JassExprVarAccess("this")));
						} else {
							withoutIndex(leftJassVar);
						}
					}



					@Override
					public void case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
						VarDef leftVar = (VarDef) exprVarArrayAccess.attrNameDef(); // TODO cast not always possible
						String leftJassVar = manager.getJassVarNameFor(leftVar);
						ExprTranslationResult index;
						if (exprVarArrayAccess.getIndexes().size() == 1) {
							index = translator.translateExpr(f, exprVarArrayAccess.getIndexes().get(0));
						} else {
							throw new Error("multiple indexes not supported yet");
						}
						withIndex(leftJassVar, index);
					}
				});



			}

			@Override
			public void case_StmtIf(StmtIf stmtIf) {
				ExprTranslationResult cond = translator.translateExpr(f, stmtIf.getCond());
				List<JassStatement> thenBlock = translateStatements(f, stmtIf.getThenBlock());
				List<JassStatement> elseBlock = translateStatements(f, stmtIf.getElseBlock());
				result.addAll(cond.getStatements());
				result.add(JassStmtIf(cond.getExpr(), JassStatements(thenBlock), JassStatements(elseBlock)));
			}

			@Override
			public void case_StmtDestroy(StmtDestroy stmtDestroy) {
				PscriptType typ = stmtDestroy.getObj().attrTyp();
				if (typ instanceof PscriptTypeClass) {
					PscriptTypeClass classType = (PscriptTypeClass) typ;
					ClassDef classDef = classType.getClassDef();
					callDestroyFunc(classDef, stmtDestroy.getObj());
				} else if (typ instanceof PscriptTypeModuleInstanciation) {
					ClassDef classDef = translator.getClassDef((PscriptTypeModuleInstanciation) typ);
					callDestroyFunc(classDef, stmtDestroy.getObj());
				} else {
					throw new Error("cannot destroy object of type " + typ);
				}
			}

			private void callDestroyFunc(ClassDef classDef, Expr e) {
				JassFunction destroyMethod = manager.getJassDestroyFunctionFor(classDef); 
				translator.calledFunctions.put(f, destroyMethod);
				ExprTranslationResult toDestroy = translator.translateExpr(f, e);
				result.addAll(toDestroy.getStatements());
				result.add(JassStmtCall(destroyMethod.getName(), JassExprlist(toDestroy.getExpr())));
			}


			@Override
			public void case_StmtWhile(StmtWhile stmtWhile) {
				ExprTranslationResult cond = translator.translateExpr(f, stmtWhile.getCond());

				JassStatements body = JassStatements();
				// ==> exitwhen not cond
				body.addAll(cond.getStatements());
				body.add(JassStmtExitwhen(JassExprUnary(JassOpNot(), cond.getExpr())));

				body.addAll(translateStatements(f, stmtWhile.getBody()));



				result.add(JassStmtLoop(body));
			}

			@Override
			public void case_ExprNewObject(ExprNewObject exprNewObject) {
				case_Expr(exprNewObject);
			}

			private void case_Expr(Expr expr) {
				ExprTranslationResult e = translator.translateExpr(f, expr);
				result.addAll(e.getStatements());
				if (e.getExpr() instanceof JassExprFunctionCall) {
					JassExprFunctionCall call = (JassExprFunctionCall) e.getExpr();
					result.add(JassStmtCall(call.getFuncName(), call.getArguments().copy()));
				} else {
					// we can ignore any other case because we will not need the result of the expression
				}
			}


			@Override
			public void case_StmtReturn(StmtReturn stmtReturn) {
				if (stmtReturn.getObj() instanceof Expr) {
					Expr expr = (Expr) stmtReturn.getObj();
					ExprTranslationResult e = translator.translateExpr(f, expr);
					result.addAll(e.getStatements());



					Set<LocalVarDef> usedLocalVars = translator.getUsedLocalVarsInExpr(expr);
					List<JassVar> usedLocalJassVars = Utils.map(usedLocalVars, new Function<LocalVarDef, JassVar>() {

						@Override
						public JassVar apply(LocalVarDef input) {
							return manager.getJassVarFor(input, translator.translateType(input.attrTyp()), false);
						}
					});
					List<JassStatement> nullSetters = Lists.newLinkedList(); 
					boolean useTempVar = false;
					for (JassVar l : f.getLocals()) {
						if (translator.handleSubTypes.contains(l.getType())) {
							// this is a handle type -> null it
							nullSetters.add(JassStmtSet(l.getName(), JassExprNull()));

							// check if this var is used in the return statement...
							if (usedLocalJassVars.contains(l)) {
								useTempVar = true;
							}
						}
					}
					if (useTempVar) {
						String returnTyp = translator.translateType(expr.attrTyp());
						JassVar tempVar = manager.getTempReturnVar(returnTyp, translator.prog);
						result.add(JassStmtSet(tempVar.getName(), e.getExpr()));
						result.addAll(nullSetters);
						result.add(JassStmtReturn(JassExprVarAccess(tempVar.getName())));
					} else {
						result.addAll(nullSetters);
						result.add(JassStmtReturn(e.getExpr()));
					}
				} else {
					// set handle variables to null
					for (JassVar l : f.getLocals()) {
						if (translator.handleSubTypes.contains(l.getType())) {
							result.add(JassStmtSet(l.getName(), JassExprNull()));
						}
					}
					result.add(JassStmtReturnVoid());
				}
			}


			@Override
			public void case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
				case_Expr(exprFunctionCall);
			}

			@Override
			public void case_StmtExitwhen(StmtExitwhen stmtExitwhen) {
				ExprTranslationResult e = translator.translateExpr(f, stmtExitwhen.getCond());
				result.addAll(e.getStatements());
				result.add(JassStmtExitwhen(e.getExpr()));
			}

			@Override
			public void case_StmtErr(StmtErr stmtErr) {
				throw new Error("Source contains errors : " + stmtErr);
			}

			@Override
			public void case_LocalVarDef(LocalVarDef localVarDef) {
				String type = translator.translateType(localVarDef.attrTyp());
				JassVar v = manager.getJassVarFor(localVarDef, type , translator.isArray(localVarDef), true); 
				//getLocalVar(f, localVarDef.getName(), localVarDef.attrTyp());
				f.getLocals().add(v);
				if (localVarDef.getInitialExpr() instanceof Expr) {
					Expr expr = (Expr) localVarDef.getInitialExpr();
					ExprTranslationResult e = translator.translateExpr(f, expr);
					result.addAll(e.getStatements());
					result.add(JassStmtSet(v.getName(), e.getExpr()));
				}
			}


			@Override
			public void case_StmtForRange(StmtForRange stmtForRange) {
				JassVar loopVar = manager.getJassVarFor(stmtForRange.getLoopVar(), translator.translateType(stmtForRange.getLoopVar().getTyp()), false);
				f.getLocals().add(loopVar);

				ExprTranslationResult fromExpr = translator.translateExpr(f, stmtForRange.getFrom());
				ExprTranslationResult toExpr = translator.translateExpr(f, stmtForRange.getTo());
				result.addAll(fromExpr.getStatements());
				JassExpr toExpr2;
				if (toExpr.getStatements().size() == 0 && toExpr.getExpr() instanceof JassExprIntVal) {
					toExpr2 = toExpr.getExpr(); 
				} else {
					JassVar loopEndVar = translator.getNewTempVar(f, "integer");
					result.addAll(toExpr.getStatements());
					result.add(JassStmtSet(loopEndVar.getName(), toExpr.getExpr()));
					toExpr2 = JassExprVarAccess(loopEndVar.getName());
				}
				result.add(JassStmtSet(loopVar.getName(), fromExpr.getExpr()));
				JassStatements body = JassStatements();
				body.add(JassStmtExitwhen(JassExprBinary(JassExprVarAccess(loopVar.getName()), JassOpGreater(), toExpr2)));
				body.addAll(translateStatements(f, stmtForRange.getBody()));
				body.add(JassStmtSet(loopVar.getName(), JassExprBinary(JassExprVarAccess(loopVar.getName()), JassOpPlus(), JassExprIntVal(1))));
				result.add(JassStmtLoop(body));

			}


		});
		return result;
	}
	
}
