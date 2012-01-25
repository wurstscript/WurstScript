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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithIndexes;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InstanceDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NoExpr;
import de.peeeq.wurstscript.ast.OpAssign;
import de.peeeq.wurstscript.ast.OpAssignment;
import de.peeeq.wurstscript.ast.OpMinusAssign;
import de.peeeq.wurstscript.ast.OpMultAssign;
import de.peeeq.wurstscript.ast.OpPlusAssign;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.StmtExitwhen;
import de.peeeq.wurstscript.ast.StmtForIn;
import de.peeeq.wurstscript.ast.StmtForRange;
import de.peeeq.wurstscript.ast.StmtForRangeDown;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprAtomic;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprIntVal;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassOpGreater;
import de.peeeq.wurstscript.jassAst.JassOpLess;
import de.peeeq.wurstscript.jassAst.JassOpMinus;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeInterface;
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
				Expr newValue = stmtSet.getRight();
				NameRef updatedExpr = stmtSet.getUpdatedExpr();
				OpAssignment opAssign = stmtSet.getOpAssign();
				final JassOpBinary binaryOp = opAssign.match(new OpAssignment.Matcher<JassOpBinary>() {

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


				
				translateAssignment(result, f, updatedExpr, binaryOp, newValue);


			}


			

			@Override
			public void case_StmtIf(StmtIf stmtIf) {
				ExprTranslationResult cond = translator.translateExpr(f, stmtIf.getCond());
				List<JassStatement> thenBlock = translateStatements(f, stmtIf.getThenBlock());
				List<JassStatement> elseBlock = translateStatements(f, stmtIf.getElseBlock());
				result.addAll(cond.getStatements());
				result.add(JassStmtIf(cond.getExprSingle(), JassStatements(thenBlock), JassStatements(elseBlock)));
			}

			@Override
			public void case_StmtDestroy(StmtDestroy stmtDestroy) {
				PscriptType typ = stmtDestroy.getDestroyedObj().attrTyp();
				if (typ instanceof PscriptTypeClass) {
					PscriptTypeClass classType = (PscriptTypeClass) typ;
					ClassDef classDef = classType.getClassDef();
					callDestroyFunc(classDef, stmtDestroy.getDestroyedObj());
				} else if (typ instanceof PscriptTypeModuleInstanciation) {
					ClassDef classDef = translator.getClassDef((PscriptTypeModuleInstanciation) typ);
					callDestroyFunc(classDef, stmtDestroy.getDestroyedObj());
				} else {
					throw new Error("cannot destroy object of type " + typ);
				}
			}

			private void callDestroyFunc(ClassDef classDef, Expr e) {
				JassFunction destroyMethod = manager.getJassDestroyFunctionFor(classDef); 
				translator.calledFunctions.put(f, destroyMethod);
				ExprTranslationResult toDestroy = translator.translateExpr(f, e);
				result.addAll(toDestroy.getStatements());
				result.add(JassStmtCall(destroyMethod.getName(), JassExprlist(toDestroy.getExprSingle())));
				// TODO destroy interfaces?
			}


			@Override
			public void case_StmtWhile(StmtWhile stmtWhile) {
				ExprTranslationResult cond = translator.translateExpr(f, stmtWhile.getCond());

				JassStatements body = JassStatements();
				// ==> exitwhen not cond
				body.addAll(cond.getStatements());
				body.add(JassStmtExitwhen(JassExprUnary(JassOpNot(), cond.getExprSingle())));

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
				for (JassExpr e1 : e.getExpressions()) {
					if (e1 instanceof JassExprFunctionCall) {
						JassExprFunctionCall call = (JassExprFunctionCall) e1;
						result.add(JassStmtCall(call.getFuncName(), call.getArguments().copy()));
					} else {
						// we can ignore any other case because we will not need the result of the expression
					}
				}
			}


			@Override
			public void case_StmtReturn(StmtReturn stmtReturn) {
				if (stmtReturn.getReturnedObj() instanceof Expr) {
					Expr expr = (Expr) stmtReturn.getReturnedObj();
					ExprTranslationResult e = translator.translateExpr(f, expr);
					result.addAll(e.getStatements());



					Set<LocalVarDef> usedLocalVars = translator.getUsedLocalVarsInExpr(expr);
					List<JassVar> usedLocalJassVars = Lists.newArrayList();
					
					for (LocalVarDef v : usedLocalVars) {
						usedLocalJassVars.addAll(manager.getJassVarsFor(v));
					}
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
					
					PscriptType retTyp = stmtReturn.attrNearestFuncDef().getReturnTyp().attrTyp();
					String[] returnedTypes = translator.translateType(retTyp);
					
					e = inferMissingTypes(expr, retTyp, expr.attrTyp(), e);
					
					if (returnedTypes.length != e.exprCount()) {
						throw new CompileError(stmtReturn.getSource(), "length does not match: " 
								+ returnedTypes.length + " != " + e.exprCount());
					}
					
					if (returnedTypes.length > 1) {
						JassVar tempVar = manager.getTempReturnVar(returnedTypes[0], translator.prog);
						result.add(JassStmtSet(tempVar.getName(), e.getExpressions().get(0)));
						for (int i=1; i< returnedTypes.length; i++) {
							JassVar tempVar2 = manager.getTupleReturnVar(returnedTypes[i], i);
							result.add(JassStmtSet(tempVar2.getName(), e.getExpressions().get(i)));
						}
						result.addAll(nullSetters);
						result.add(JassStmtReturn(JassExprVarAccess(tempVar.getName())));
					} else if (useTempVar) {
						
						JassVar tempVar = manager.getTempReturnVar(returnedTypes[0], translator.prog);
						result.add(JassStmtSet(tempVar.getName(), e.getExprSingle()));  
						result.addAll(nullSetters);
						result.add(JassStmtReturn(JassExprVarAccess(tempVar.getName())));
					} else {
						result.addAll(nullSetters);
						result.add(JassStmtReturn(e.getExprSingle())); // TODO
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
				result.add(JassStmtExitwhen(e.getExprSingle()));
			}

			@Override
			public void case_StmtErr(StmtErr stmtErr) {
				throw new Error("Source contains errors : " + stmtErr);
			}

			@Override
			public void case_LocalVarDef(LocalVarDef localVarDef) {
				f.getLocals().addAll(manager.getJassVarsFor(localVarDef));
				
				if (localVarDef.getInitialExpr() instanceof Expr) {
					Expr initalExpr = (Expr) localVarDef.getInitialExpr();
					ExprTranslationResult newValue = translator.translateExpr(f, initalExpr);
					//translateAssignmentNoIndex(result, f, localVarDef, null, newValue);
					translateAssignment(result, f, localVarDef, null, null, initalExpr);
				}
			}


			@Override
			public void case_StmtForRange(StmtForRange s) {
				case_StmtForRange(s.getLoopVar(), s.getFrom(), s.getTo(), s.getStep(), s.getBody(), JassOpPlus(), JassOpGreater());
			}

			@Override
			public void case_StmtForRangeDown(StmtForRangeDown s) {
				case_StmtForRange(s.getLoopVar(), s.getFrom(), s.getTo(), s.getStep(), s.getBody(), JassOpMinus(), JassAst.JassOpLess());
			}
			
			
			private void case_StmtForRange(LocalVarDef loopVar, Expr from, Expr to, Expr step, WStatements body,
					JassOpBinary opInc, JassOpBinary op) {
				JassVar jassLoopVar = manager.getJassVarsFor(loopVar).get(0); 
				f.getLocals().add(jassLoopVar);

				ExprTranslationResult fromExpr = translator.translateExpr(f, from);
				result.add(JassStmtSet(jassLoopVar.getName(), fromExpr.getExprSingle()));
				
				JassExpr toExpr = addCacheVariableSmart(f, result, to, fromExpr);
				JassExpr stepExpr = addCacheVariableSmart(f, result, step, fromExpr);
				
				JassStatements jassBody = JassStatements();
				jassBody.add(JassStmtExitwhen(JassExprBinary(JassExprVarAccess(jassLoopVar.getName()), op, toExpr)));
				jassBody.addAll(translateStatements(f, body));
				jassBody.add(JassStmtSet(jassLoopVar.getName(), JassExprBinary(JassExprVarAccess(jassLoopVar.getName()), opInc, stepExpr)));
				result.add(JassStmtLoop(jassBody));
			}


			@Override
			public void case_StmtForIn(StmtForIn stmtForIn) {
				throw new CompileError(stmtForIn.attrSource(), "Syntactic sugar");
			}


		});
		return result;
	}
	
	


	/**
	 * stores the value of expression e in a temporary variable if  
	 * @return the expression e if e was constant or the VarAccess to the cacheVariable
	 */
	private JassExpr addCacheVariableSmart(final JassFunction f, final List<JassStatement> result,
			Expr e, ExprTranslationResult fromExpr) {
		ExprTranslationResult er = translator.translateExpr(f, e);
		result.addAll(fromExpr.getStatements());
		JassExpr r;
		if (er.getStatements().size() == 0 && er.getExprSingle() instanceof JassExprAtomic) {
			r = er.getExprSingle(); 
		} else {
			JassVar loopEndVar = translator.getNewTempVar(f, "integer");
			result.addAll(er.getStatements());
			result.add(JassStmtSet(loopEndVar.getName(), er.getExprSingle()));
			r = JassExprVarAccess(loopEndVar.getName());
		}
		return r;
	}
	
	
	
	private void translateAssignment(final List<JassStatement> result, final JassFunction f, NameRef updatedExpr,
			final JassOpBinary binaryOp, Expr newValue) throws CompileError {
		
		
		
		// calculate the index
		ExprTranslationResult index = calculateIndex(f, updatedExpr);
		
		NameDef nameDef = updatedExpr.attrNameDef();
		if (!(nameDef instanceof VarDef)) {
			throw new CompileError(updatedExpr.getSource(), "Cannot assign to " + Utils.printElement(nameDef));
		}
		VarDef varDef = (VarDef) nameDef;
		translateAssignment(result, f, varDef, index, binaryOp, newValue);
	}
	
	private void translateAssignment(final List<JassStatement> result, final JassFunction f, VarDef updatedVar
			,ExprTranslationResult index, final JassOpBinary binaryOp, Expr newValue) throws CompileError {

		
		PscriptType varTyp = updatedVar.attrTyp();
		PscriptType rightTyp = newValue.attrTyp();
		
		
		
		ExprTranslationResult right = translator.translateExpr(f, newValue);
		
		right = inferMissingTypes(newValue, varTyp, rightTyp, right);
		
		
		translateAssignment2(result, f, updatedVar, index, binaryOp, right);
	}


	private ExprTranslationResult inferMissingTypes(AstElement where, PscriptType neededTyp, PscriptType actualTyp,	ExprTranslationResult expr) {
		if (neededTyp instanceof PscriptTypeInterface && actualTyp instanceof PscriptTypeClass) {
			PscriptTypeInterface neededTyp2 = (PscriptTypeInterface)neededTyp;
			PscriptTypeClass actualTyp2 = (PscriptTypeClass) actualTyp;
			// in this special case we have to manually add the type based on the static type information that we have
			int instanceId = translator.getInstanceId(where, neededTyp2, actualTyp2);
			expr = expr.plus(JassExprIntVal(instanceId));
		}
		return expr;
	}


	


	private ExprTranslationResult calculateIndex(final JassFunction f, NameRef updatedExpr) throws CompileError {
		if (updatedExpr.attrImplicitParameter() instanceof Expr) {
			if (updatedExpr instanceof AstElementWithIndexes) {
				throw new CompileError(updatedExpr.getSource(), "Not supported: array members");
			} else {
				return translator.translateExpr(f, (Expr) updatedExpr.attrImplicitParameter());
			}
		} else { // no implicit parameter
			if (updatedExpr instanceof AstElementWithIndexes) {
				AstElementWithIndexes withIndexes = (AstElementWithIndexes) updatedExpr;
				return translator.translateExpr(f, withIndexes.getIndexes().get(0));
			}
		}
		return null;
	}


	void translateAssignment2(final List<JassStatement> result, final JassFunction f, VarDef varDef, ExprTranslationResult index, final JassOpBinary binaryOp, final ExprTranslationResult right) {
		
		if (index != null) {
			translateAssignmentWithIndex(result, f, varDef, index, binaryOp, right);
		} else { // we have no index
			translateAssignmentNoIndex(result, f, varDef, binaryOp, right);
		}
	}


	private void translateAssignmentWithIndex(final List<JassStatement> result, final JassFunction f, VarDef varDef,
			ExprTranslationResult index, final JassOpBinary binaryOp, final ExprTranslationResult right) {
		
		List<JassVar> left = manager.getJassVarsFor(varDef);
		result.addAll(index.getStatements());
		result.addAll(right.getStatements());
		translateAssignmentWithIndex2(result, f, left, index.getExprSingle(), binaryOp, right.getExpressions());
	}


	private void translateAssignmentNoIndex(final List<JassStatement> result, JassFunction f, VarDef varDef
			,final JassOpBinary binaryOp, final ExprTranslationResult right) {
		List<JassVar> left = manager.getJassVarsFor(varDef);
		result.addAll(right.getStatements());
		translateAssignmentNoIndex2(result, f, left, binaryOp, right.getExpressions());
	}


	public void translateAssignmentNoIndex2(List<JassStatement> result, JassFunction f, List<JassVar> left
			, JassOpBinary binaryOp, List<JassExpr> right) {
		if (left.size() > right.size()) {
			throw new Error("error in assignment to "+  left.get(0).getName()+ " : " + left.size() + " != " + right.size());
		}
		
		for (int i=0; i<left.size(); i++) {
			String varName = left.get(i).getName();
			if (binaryOp == null) {
				result.add(JassStmtSet(varName, right.get(i)));
			} else {
				result.add(JassStmtSet(varName, 
						JassExprBinary(JassExprVarAccess(varName), binaryOp, right.get(i))));
			}
		}
		
		
	}
	
	public void translateAssignmentWithIndex2(List<JassStatement> result, JassFunction f, List<JassVar> left, JassExpr index
			, JassOpBinary binaryOp, List<JassExpr> right) {
		if (left.size() != right.size()) {
			throw new Error(left.size() + " != " + right.size());
		}
		
		if (left.size() > 1 || binaryOp != null) {
			index = smartStoreToTemp(result, f, index);
		}
		
		for (int i=0; i<left.size(); i++) {
			String varName = left.get(i).getName();
			if (binaryOp == null) {
				result.add(JassStmtSetArray(varName, (JassExpr) index.copy(), right.get(i)));
			} else {
				result.add(JassStmtSetArray(varName, (JassExpr) index.copy(), 
						JassExprBinary(JassExprVarArrayAccess(varName, (JassExpr) index.copy()), binaryOp, right.get(i))));
			}
		}
		
		
	}


	private JassExpr smartStoreToTemp(List<JassStatement> result, JassFunction f, JassExpr index) {
		if (!(index instanceof JassExprAtomic)) {
			// save index to tempvar
			JassVar tempVar = translator.getNewTempVar(f, "integer");
			result.add(JassStmtSet(tempVar.getName(), index));
			index = JassExprVarAccess(tempVar.getName());
		}
		return index;
	}
	
}
