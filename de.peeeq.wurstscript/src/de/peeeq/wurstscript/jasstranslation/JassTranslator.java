package de.peeeq.wurstscript.jasstranslation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static de.peeeq.wurstscript.jassAst.JassAst.*;


import org.testng.collections.Maps;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprAssignable;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.NoTypeExpr;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OpAnd;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpDivInt;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OpGreater;
import de.peeeq.wurstscript.ast.OpGreaterEq;
import de.peeeq.wurstscript.ast.OpLess;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpMult;
import de.peeeq.wurstscript.ast.OpNot;
import de.peeeq.wurstscript.ast.OpOr;
import de.peeeq.wurstscript.ast.OpPlus;
import de.peeeq.wurstscript.ast.OpUnary;
import de.peeeq.wurstscript.ast.OpUnequals;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.StmtExitwhen;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.intermediateLang.ILStatement;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILif;
import de.peeeq.wurstscript.intermediateLang.ILsetBinaryCR;
import de.peeeq.wurstscript.intermediateLang.ILsetVar;
import de.peeeq.wurstscript.intermediateLang.ILsetVarArray;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.intermediateLang.Iloperator;
import de.peeeq.wurstscript.jassAst.JassArrayVar;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassOpUnary;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PScriptTypeHandle;
import de.peeeq.wurstscript.types.PScriptTypeInfer;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeNull;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeError;
import de.peeeq.wurstscript.types.PsrciptFuncType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.NotNullList;

public class JassTranslator {

	private JassManager manager;
	private CompilationUnit wurstProg;
	private JassProg prog;
	private JassVars globals;
	private JassFunctions functions;
	private List<GlobalInit> globalInitializers = Lists.newLinkedList();
	private Map<WPackage, JassFunction> initFunctions = Maps.newHashMap();

	public JassTranslator(CompilationUnit wurstProgram) {
		this.manager = new JassManager(this);
		this.wurstProg = wurstProgram;
	}
	
	public JassProg translate() {
		globals = JassVars();
		functions = JassFunctions();
		prog = JassProg(globals, functions);
		
		for (TopLevelDeclaration t : wurstProg) {
			translateTopLevelDeclaration(t);
		}
		
		// TODO create main function
		
		// TODO init globals
		
		// TODO call initializers
		
		// TODO sort functions
		
		return prog;
	}

	private void translateTopLevelDeclaration(TopLevelDeclaration t) {
		t.match(new TopLevelDeclaration.MatcherVoid() {
			
			@Override
			public void case_WPackage(WPackage wPackage) {
				translatePackage(wPackage);
			}
			
			@Override
			public void case_NativeType(NativeType nativeType) {
				translateNativeType(nativeType);
			}
			
			@Override
			public void case_NativeFunc(NativeFunc nativeFunc) {
				translateNativeFunc(nativeFunc);
			}
			
			@Override
			public void case_JassGlobalBlock(JassGlobalBlock jassGlobalBlock) {
				translateJassGlobalsBlock(jassGlobalBlock);
			}
			
			@Override
			public void case_FuncDef(FuncDef funcDef) {
				translateFuncDef(funcDef, false);
			}
		});
	}

	protected void translateFuncDef(FuncDef funcDef, boolean isMethod) {
		JassFunction f = manager.getJassFunctionFor(funcDef);
		f.setReturnType(translateType(funcDef.getSignature().getTyp()));
		if (isMethod) {
			// methods have an additional implicit parameter
			f.getParams().add(JassSimpleVar("integer", "this"));
		}
		for (WParameter param : funcDef.getSignature().getParameters()) {
			f.getParams().add(translateParam(param));
		}
		f.setBody(translateStatements(f, funcDef.getBody()));
	}

	private JassStatements translateStatements(JassFunction f, WStatements statements) {
		JassStatements result = JassStatements();
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
				JassStatements body = translateStatements(f, stmtLoop.getBody());
				result.add(JassStmtLoop(body ));
			}

			@Override
			public void case_StmtSet(StmtSet stmtSet) {
				final ExprTranslationResult right = translateExpr(f, stmtSet.getRight());
				stmtSet.getLeft().match(new ExprAssignable.MatcherVoid() {

					@Override
					public void case_ExprMemberVar(ExprMemberVar exprMemberVar) {
						VarDef leftVar = exprMemberVar.attrVarDef();
						JassVar leftJassVar = manager.getJassVarFor(leftVar);
						ExprTranslationResult index = translateExpr(f, exprMemberVar.getLeft());
						
						result.addAll(index.getStatements());
						result.addAll(right.getStatements());						
						result.add(JassStmtSetArray(leftJassVar.getName(), index.getExpr(), right.getExpr()));
					}

					@Override
					public void case_ExprMemberArrayVar(ExprMemberArrayVar exprMemberArrayVar) {
						// TODO
						throw new Error("Array member vars not supported yet.");
					}

					@Override
					public void case_ExprVarAccess(ExprVarAccess exprVarAccess) {
						VarDef leftVar = exprVarAccess.attrVarDef();
						JassVar leftJassVar = manager.getJassVarFor(leftVar);
						result.addAll(right.getStatements());
						result.add(JassStmtSet(leftJassVar.getName(), right.getExpr()));
					}

					@Override
					public void case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
						VarDef leftVar = exprVarArrayAccess.attrVarDef();
						JassVar leftJassVar = manager.getJassVarFor(leftVar);
						ExprTranslationResult index;
						if (exprVarArrayAccess.getIndexes().size() == 1) {
							index = translateExpr(f, exprVarArrayAccess.getIndexes().get(0));
						} else {
							throw new Error("multiple indexes not supported yet");
						}
						result.addAll(index.getStatements());
						result.addAll(right.getStatements());
						result.add(JassStmtSetArray(leftJassVar.getName(), index.getExpr(), right.getExpr()));
					}
				});
				
				
			}

			@Override
			public void case_StmtIf(StmtIf stmtIf) {
				ExprTranslationResult cond = translateExpr(f, stmtIf.getCond());
				JassStatements thenBlock = translateStatements(f, stmtIf.getThenBlock());
				JassStatements elseBlock = translateStatements(f, stmtIf.getElseBlock());
				result.addAll(cond.getStatements());
				result.add(JassStmtIf(cond.getExpr(), thenBlock, elseBlock));
			}

			@Override
			public void case_StmtDestroy(StmtDestroy stmtDestroy) {
				PscriptType typ = stmtDestroy.getObj().attrTyp();
				if (typ instanceof PscriptTypeClass) {
					PscriptTypeClass classType = (PscriptTypeClass) typ;
					ClassDef classDef = classType.getClassDef();
					JassFunction destroyMethod = manager.getJassDestroyFunctionFor(classDef); 
					ExprTranslationResult toDestroy = translateExpr(f, stmtDestroy.getObj());
					result.addAll(toDestroy.getStatements());
					result.add(JassStmtCall(destroyMethod.getName(), JassExprlist(toDestroy.getExpr())));
					
				} else {
					throw new Error("cannot destroy object of type " + typ);
				}
			}

			@Override
			public void case_StmtWhile(StmtWhile stmtWhile) {
				ExprTranslationResult cond = translateExpr(f, stmtWhile.getCond());
				
				JassStatements body = JassStatements();
				// ==> exitwhen not cond
				body.addAll(cond.getStatements());
				body.add(JassStmtExitwhen(JassExprUnary(JassOpNot(), cond.getExpr())));
				
				translateStatements(f, stmtWhile.getBody());
				
				
				
				result.add(JassStmtLoop(body));
			}

			@Override
			public void case_ExprNewObject(ExprNewObject exprNewObject) {
				case_Expr(exprNewObject);
			}

			private void case_Expr(Expr expr) {
				ExprTranslationResult e = translateExpr(f, expr);
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
					ExprTranslationResult e = translateExpr(f, expr);
					result.addAll(e.getStatements());
					result.add(JassStmtReturn(e.getExpr()));
				} else {
					result.add(JassStmtReturnVoid());
				}
			}


			@Override
			public void case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
				case_Expr(exprFunctionCall);
			}

			@Override
			public void case_StmtExitwhen(StmtExitwhen stmtExitwhen) {
				ExprTranslationResult e = translateExpr(f, stmtExitwhen.getCond());
				result.addAll(e.getStatements());
				result.add(JassStmtExitwhen(e.getExpr()));
			}

			@Override
			public void case_StmtErr(StmtErr stmtErr) {
				throw new Error("Source contains errors : " + stmtErr);
			}

			@Override
			public void case_LocalVarDef(LocalVarDef localVarDef) {
				if (localVarDef.getInitialExpr() instanceof Expr) {
					Expr expr = (Expr) localVarDef.getInitialExpr();
					ExprTranslationResult e = translateExpr(f, expr);
					result.addAll(e.getStatements());
					result.add(JassStmtSet(localVarDef.getName(), e.getExpr()));
				}
				// no initial expr -> we do not have to translate this
			}

		
		});
		return result;
	}

	protected ExprTranslationResult translateExpr(final JassFunction f, Expr expr) {
		return expr.match(new Expr.Matcher<ExprTranslationResult>() {

			@Override
			public ExprTranslationResult case_ExprNewObject(ExprNewObject exprNewObject) {
				ConstructorDef constructorFunc = exprNewObject.attrConstructorDef();
				JassFunction constructorJassFunc = manager.getJassConstructorFor(constructorFunc);
				
				JassExprlist arguments = JassExprlist(); 
				ExprListTranslationResult args = translateArguments(f, exprNewObject.getArgs());
				List<JassStatement> statements = Lists.newLinkedList();
				statements.addAll(args.getStatements());
				arguments.addAll(args.getExprs());
				JassExpr e = JassExprFunctionCall(constructorJassFunc.getName(), arguments);
				return new ExprTranslationResult(statements, e);
			}

			@Override
			public ExprTranslationResult case_ExprRealVal(ExprRealVal exprRealVal) {
				return new ExprTranslationResult(JassExprRealVal(exprRealVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprUnary(ExprUnary exprUnary) {
				ExprTranslationResult right = translateExpr(f, exprUnary.getRight());
				return new ExprTranslationResult(
						right.getStatements(), 
						JassExprUnary(translateOpUnary(exprUnary.getOp()), right.getExpr()));
			}

			@Override
			public ExprTranslationResult case_ExprThis(ExprThis exprThis) {
				return new ExprTranslationResult(JassExprVarAccess("this"));
			}

			@Override
			public ExprTranslationResult case_ExprCast(ExprCast exprCast) {
				// a cast has no effect:
				return translateExpr(f, exprCast.getExpr());
			}

			@Override
			public ExprTranslationResult case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
				FunctionDefinition calledFunc = exprFunctionCall.attrFuncDef();
				JassFunction calledJassFunc = manager.getJassFunctionFor(calledFunc);
				String functionName = calledJassFunc.getName();
				
				ExprListTranslationResult args = translateArguments(f, exprFunctionCall.getArgs());
				JassExprlist arguments = JassExprlist();
				arguments.addAll(args.getExprs());
				return new ExprTranslationResult(
						args.getStatements(),
						JassExprFunctionCall(functionName, arguments )
						);
			}

			@Override
			public ExprTranslationResult case_ExprMemberMethod(ExprMemberMethod exprMemberMethod) {
				FunctionDefinition calledFunc = exprMemberMethod.attrFuncDef();
				JassFunction calledJassFunc = manager.getJassFunctionFor(calledFunc);
				String functionName = calledJassFunc.getName();
				JassExprlist arguments = JassExprlist();
				// translate right:
				ExprTranslationResult e = translateExpr(f, exprMemberMethod.getLeft());
				List<JassStatement> statements = Lists.newLinkedList();
				statements .addAll(e.getStatements());
				arguments.addFront(e.getExpr());
				
				// translate arguments:			
				ExprListTranslationResult args = translateArguments(f, exprMemberMethod.getArgs());
				statements.addAll(args.getStatements());
				arguments.addAll(args.getExprs());
				
				
				JassExprFunctionCall ex = JassExprFunctionCall(functionName, arguments);
				return new ExprTranslationResult(statements, ex);
			}

			@Override
			public ExprTranslationResult case_ExprMemberArrayVar(ExprMemberArrayVar exprMemberArrayVar) {
				VarDef varDef = exprMemberArrayVar.attrVarDef();
				JassVar jassVarDef = manager.getJassVarFor(varDef);
				String varName = jassVarDef.getName();
				
				ExprTranslationResult left = translateExpr(f, exprMemberArrayVar.getLeft());
				return new ExprTranslationResult(left.getStatements(), JassExprVarArrayAccess(varName, left.getExpr()));
			}

			@Override
			public ExprTranslationResult case_ExprStringVal(ExprStringVal exprStringVal) {
				return new ExprTranslationResult(JassExprStringVal(exprStringVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprVarAccess(ExprVarAccess exprVarAccess) {
				VarDef varDef = exprVarAccess.attrVarDef();
				JassVar jassVar = manager.getJassVarFor(varDef);
				String varName = jassVar.getName();
				if (varDef.attrIsClassMember()) {
					// access to a field
					JassExpr index = JassExprVarAccess("this");
					return new ExprTranslationResult(JassExprVarArrayAccess(varName, index));
				} else {
					// access to a normal variable
					return new ExprTranslationResult(JassExprVarAccess(varName));
				}
			}

			@Override
			public ExprTranslationResult case_ExprBinary(ExprBinary exprBinary) {
				ExprTranslationResult left = translateExpr(f, exprBinary.getLeft());
				ExprTranslationResult right = translateExpr(f, exprBinary.getRight());
				
				JassExpr leftExpr;
				JassExpr rightExpr;
				
				List<JassStatement> statements = Lists.newLinkedList();
				statements.addAll(left.getStatements());
				
				
				
				// if the right hand side of the expression uses statements we have to make sure that
				// the left hand side is executed first:
				if (right.getStatements().size() > 0) {
					String type = translateType(exprBinary.getLeft().attrTyp());
					JassVar tempVar = getNewTempVar(f, type);
					statements.add(JassStmtSet(tempVar.getName(), left.getExpr()));
					leftExpr = JassExprVarAccess(tempVar.getName());
					
					
					// boolean operators (and, or) have to be treated differently because the evalutation
					// of the right hand side depends on the result of the left hand side.
					if (exprBinary.getOp() instanceof OpAnd) {
						JassStatements thenBlock = JassStatements();
						JassStatements elseBlock  = JassStatements();;
						elseBlock.addAll(right.getStatements());
						elseBlock.add(JassStmtSet(tempVar.getName(), right.getExpr()));
						statements.add(
								JassStmtIf(
										JassExprVarAccess(tempVar.getName()), 
										thenBlock, elseBlock));
						return new ExprTranslationResult(statements, JassExprVarAccess(tempVar.getName()));
					} else if (exprBinary.getOp() instanceof OpAnd) {
						JassStatements thenBlock = JassStatements();
						JassStatements elseBlock  = JassStatements();;
						thenBlock.addAll(right.getStatements());
						thenBlock.add(JassStmtSet(tempVar.getName(), right.getExpr()));
						statements.add(
								JassStmtIf(
										JassExprVarAccess(tempVar.getName()), 
										thenBlock, elseBlock));
						return new ExprTranslationResult(statements, JassExprVarAccess(tempVar.getName()));
					} else {
						rightExpr = right.getExpr();
					}
				} else {
					leftExpr = left.getExpr();
					rightExpr = right.getExpr();
				}
				
				
				JassExpr e = JassExprBinary(leftExpr, translateOp(exprBinary.getOp()), rightExpr);
				return new ExprTranslationResult(statements, e);
			}

			@Override
			public ExprTranslationResult case_ExprBoolVal(ExprBoolVal exprBoolVal) {
				return new ExprTranslationResult(JassExprBoolVal(exprBoolVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprMemberVar(ExprMemberVar exprMemberVar) {
				VarDef varDef = exprMemberVar.attrVarDef();
				JassVar jassVar = manager.getJassVarFor(varDef);
				String varName = jassVar.getName();

				ExprTranslationResult left = translateExpr(f, exprMemberVar.getLeft());
				
				JassExpr e = JassExprVarArrayAccess(varName, left.getExpr());
				return new ExprTranslationResult(left.getStatements(), e);
			}

			@Override
			public ExprTranslationResult case_ExprNull(ExprNull exprNull) {
				return new ExprTranslationResult(JassExprNull());
			}

			@Override
			public ExprTranslationResult case_ExprIntVal(ExprIntVal exprIntVal) {
				return new ExprTranslationResult(JassExprIntVal(exprIntVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprFuncRef(ExprFuncRef exprFuncRef) {
				FunctionDefinition funcDef = exprFuncRef.attrFuncDef();
				JassFunction jassFunc = manager.getJassFunctionFor(funcDef);
				String funcName = jassFunc.getName();
				return new ExprTranslationResult(JassExprFuncRef(funcName));
			}

			@Override
			public ExprTranslationResult case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
				VarDef varDef = exprVarArrayAccess.attrVarDef();
				JassVar jassVar = manager.getJassVarFor(varDef);
				String varName = jassVar.getName();

				ExprTranslationResult left;
				if (exprVarArrayAccess.getIndexes().size() == 1) {
					left = translateExpr(f, exprVarArrayAccess.getIndexes().get(0)); 
				} else {
					throw new Error("only one array index is supported currently");
				}
				JassExpr e = JassExprVarArrayAccess(varName, left.getExpr());
				return new ExprTranslationResult(left.getStatements(), e);
			}
		});
	}

	protected JassOpBinary translateOp(OpBinary op) {
		return op.match(new OpBinary.Matcher<JassOpBinary>() {

			@Override
			public JassOpBinary case_OpDivInt(OpDivInt opDivInt) {
				return JassOpDiv();
			}

			@Override
			public JassOpBinary case_OpLessEq(OpLessEq opLessEq) {
				return JassOpLessEq();
			}

			@Override
			public JassOpBinary case_OpEquals(OpEquals opEquals) {
				return JassOpEquals();
			}

			@Override
			public JassOpBinary case_OpModReal(OpModReal opModReal) {
				throw new Error("modulo operator cannot be translated");
			}

			@Override
			public JassOpBinary case_OpModInt(OpModInt opModInt) {
				throw new Error("modulo operator cannot be translated");
			}

			@Override
			public JassOpBinary case_OpUnequals(OpUnequals opUnequals) {
				return JassOpUnequals();
			}

			@Override
			public JassOpBinary case_OpDivReal(OpDivReal opDivReal) {
				return JassOpDiv();
			}

			@Override
			public JassOpBinary case_OpAnd(OpAnd opAnd) {
				return JassOpAnd();
			}

			@Override
			public JassOpBinary case_OpGreater(OpGreater opGreater) {
				return JassOpGreater();
			}

			@Override
			public JassOpBinary case_OpPlus(OpPlus opPlus) {
				return JassOpPlus();
			}

			@Override
			public JassOpBinary case_OpMult(OpMult opMult) {
				return JassOpMult();
			}

			@Override
			public JassOpBinary case_OpGreaterEq(OpGreaterEq opGreaterEq) {
				return JassOpGreaterEq();
			}

			@Override
			public JassOpBinary case_OpMinus(OpMinus opMinus) {
				return JassOpMinus();
			}

			@Override
			public JassOpBinary case_OpLess(OpLess opLess) {
				return JassOpLess();
			}

			@Override
			public JassOpBinary case_OpOr(OpOr opOr) {
				return JassOpOr();
			}
		});
	}

	protected JassVar getNewTempVar(JassFunction f, String type) {
		int maxTempIndex = 0;
		for (JassVar v : f.getLocals()) {
			if (v.getName().startsWith("temp")) {
				maxTempIndex = Integer.parseInt(v.getName().substring(4));
			}
		}
		maxTempIndex++;
		return JassSimpleVar(type, "temp" + maxTempIndex);
	}

	protected JassOpUnary translateOpUnary(OpUnary op) {
		return op.match(new OpUnary.Matcher<JassOpUnary>() {

			@Override
			public JassOpUnary case_OpNot(OpNot opNot) {
				return JassOpNot();
			}

			@Override
			public JassOpUnary case_OpMinus(OpMinus opMinus) {
				return JassOpMinus();
			}
		});
	}

	/**
	 * translate a list of expressions, makes sure that the arguments are evaluated in correct order
	 * so if we hava a list of expressions (a,b,c) and c requires additional statements then a and b
	 * will use statements too
	 * @param f 
	 */
	protected ExprListTranslationResult translateArguments(JassFunction f, Arguments args) {
		List<ExprTranslationResult> translations = Lists.newLinkedList();
		List<String> types = Lists.newLinkedList();
		int lastTranslationWithStatements = 0;
		int i = 0;
		for (Expr arg : args) {
			types.add(translateType(arg.attrTyp()));
			ExprTranslationResult translation = translateExpr(f, arg);
			if (translation.getStatements().size() > 0) {
				lastTranslationWithStatements = i;
			}
			translations.add(translation);
			i++;
		}
		
		
		
		List<JassStatement> statements = Lists.newLinkedList();
		List<JassExpr> exprs = Lists.newLinkedList();
		
		i = 0;
		for (ExprTranslationResult arg : translations) {
			statements.addAll(arg.getStatements());
			if (i < lastTranslationWithStatements) {
				JassVar tempVar = getNewTempVar(f, types.get(i));
				statements.add(JassStmtSet(tempVar.getName(), arg.getExpr()));
				exprs.add(JassExprVarAccess(tempVar.getName()));
			} else {
				exprs.add(arg.getExpr());
			}
		}
		return new ExprListTranslationResult(statements, exprs);
	}

	private JassSimpleVar translateParam(WParameter param) {
		return JassSimpleVar(translateType(param.getTyp()), param.getName());
	}

	private String translateType(OptTypeExpr typ) {
		return translateType(typ.attrTyp());
		
	}

	private String translateType(PscriptType t) {
		if (t instanceof PscriptNativeType) {
			return t.getName();
		} else if (t instanceof PScriptTypeArray) {
			return translateType(((PScriptTypeArray) t).getBaseType());
		} else if (t instanceof PScriptTypeBool) {
			return "boolean";
		} else if (t instanceof PscriptTypeClass) {
			return "integer";
		} else if (t instanceof PScriptTypeCode) {
			return "code";
		} else if (t instanceof PscriptTypeError) {
			throw new Error("Error type in program...");
		} else if (t instanceof PScriptTypeHandle) {
			PScriptTypeHandle tt = (PScriptTypeHandle) t;
			return tt.getName();
		} else if (t instanceof PScriptTypeInt) {
			return "integer";
		} else if (t instanceof PScriptTypeReal) {
			return "real";
		} else if (t instanceof PScriptTypeString) {
			return "string";
		} else if (t instanceof PScriptTypeVoid) {
			return "nothing";
		}		
		throw new Error("Cannot translate type: " + t);
	}

	protected void translateJassGlobalsBlock(JassGlobalBlock jassGlobalBlock) {
		for (GlobalVarDef v : jassGlobalBlock) {
			translateGlobalVariable(v);			
		}
	}

	private void translateGlobalVariable(GlobalVarDef v) {
		JassVar jassVar = manager.getJassVarFor(v);
		prog.getGlobals().add(jassVar);
		if (v.getInitialExpr() instanceof Expr) {
			Expr expr = (Expr) v.getInitialExpr();
			globalInitializers.add(new GlobalInit(jassVar, expr));
		}
	}

	protected void translateNativeFunc(NativeFunc nativeFunc) {
		// nothing to translate
	}

	protected void translateNativeType(NativeType nativeType) {
		// nothing to translate
	}

	protected void translatePackage(WPackage wPackage) {
		for (WEntity elem : wPackage.getElements()) {
			elem.match(new WEntity.MatcherVoid() {
				
				@Override
				public void case_NativeType(NativeType nativeType) {
					translateNativeType(nativeType);
				}
				
				@Override
				public void case_NativeFunc(NativeFunc nativeFunc) {
					translateNativeFunc(nativeFunc);
				}
				
				@Override
				public void case_InitBlock(InitBlock initBlock) {
					translateInitBlock(initBlock);
				}
				
				@Override
				public void case_GlobalVarDef(GlobalVarDef globalVarDef) {
					translateGlobalVariable(globalVarDef);
				}
				
				@Override
				public void case_FuncDef(FuncDef funcDef) {
					translateFuncDef(funcDef, false);
				}
				
				@Override
				public void case_ClassDef(ClassDef classDef) {
					translateClassDef(classDef);
				}
			});
		}
	}

	protected void translateInitBlock(InitBlock initBlock) {
		JassFunction jassFunction = manager.getJassInitFunctionFor(initBlock);
		jassFunction.setBody(translateStatements(jassFunction, initBlock.getBody()));
		
		initFunctions.put((WPackage) initBlock.attrNearestPackage(), jassFunction);
	}

	protected void translateClassDef(final ClassDef classDef) {
		String baseName = ((ClassDef) classDef.attrNearestPackage()).getName() + "_" + classDef.getName() + "_";
		
		
		// create default class variables
		final JassArrayVar nextFree = JassArrayVar("integer", manager.getUniqueName(baseName + "nextFree"));
		globals.add(nextFree);
		final JassSimpleVar firstFree = JassSimpleVar("integer", manager.getUniqueName(baseName + "firstFree"));
		globals.add(firstFree);
		final JassSimpleVar maxIndex = JassSimpleVar("integer", manager.getUniqueName(baseName + "maxIndex"));
		globals.add(maxIndex);
		
		
		
		for (ClassSlot member : classDef.getSlots()) {
			member.match(new ClassSlot.MatcherVoid() {
				
				@Override
				public void case_OnDestroyDef(OnDestroyDef onDestroyDef) {
					JassFunction f = manager.getJassDestroyFunctionFor(classDef);
					f.getBody().addAll(translateStatements(f, onDestroyDef.getBody()));
				}
				
				@Override
				public void case_GlobalVarDef(GlobalVarDef globalVarDef) {
					translateVarDef(globalVarDef);
				}
				
				@Override
				public void case_FuncDef(FuncDef funcDef) {
					translateFuncDef(funcDef, true);
				}
				
				@Override
				public void case_ConstructorDef(ConstructorDef constructorDef) {
					translateConstructorDef(classDef, constructorDef, nextFree, firstFree, maxIndex);
				}
			});
		}
		
		finishDestroyMethod(classDef, nextFree, firstFree, maxIndex);
	}

	private void finishDestroyMethod(ClassDef classDef, JassArrayVar nextFree, JassSimpleVar firstFree, JassSimpleVar maxIndex) {
		JassFunction f = manager.getJassDestroyFunctionFor(classDef);
		

		f.getBody().add(
		//	if nextFree[this] < 0
			JassStmtIf(
				JassExprBinary(
						JassExprVarArrayAccess(nextFree.getName(), JassExprVarAccess("this")), 
						JassOpLess(),
						JassExprIntVal(0)),
		// then
				JassStatements(
		//	show error
					JassStmtCall("BJDebugMsg", JassExprlist(JassExprStringVal("Double free of " + classDef.getName())))						
				),
		//else 
				JassStatements(
		//	nextFree[this] = firstFree
						JassStmtSetArray(nextFree.getName(), JassExprVarAccess("this"), JassExprVarAccess(firstFree.getName())),
		//	firstFree = this
						JassStmtSet(firstFree.getName(), JassExprVarAccess("this"))
			)));
		//endif
	}

	protected void translateConstructorDef(ClassDef classDef, ConstructorDef constructorDef, JassArrayVar nextFree, JassSimpleVar firstFree, JassSimpleVar maxIndex) {
		JassFunction f = manager.getJassConstructorFor(constructorDef);
		
		f.setReturnType("integer");
		
		for (WParameter param : constructorDef.getParams()) {
			f.getParams().add(translateParam(param));
		}
		
		
		// if has free indexes (firstFree > 0)
		f.getBody().add(JassStmtIf(
				JassExprBinary(
						JassExprVarAccess(firstFree.getName()), JassOpGreater(), JassExprIntVal(0)),
		// then
				JassStatements(
		// 		this = firstFree
						JassStmtSet("this", JassExprVarAccess(firstFree.getName())),
		//		firstFree = nextFree[this]
						JassStmtSet(firstFree.getName(), JassExprVarArrayAccess(nextFree.getName(), JassExprVarAccess("this")))
				),
		// else
				JassStatements(
		// 		maxIndex = maxIndex + 1
						JassStmtSet(maxIndex.getName(), 
								JassExprBinary(JassExprVarAccess(maxIndex.getName()), JassOpPlus(), JassExprIntVal(1))),
		// 		this = maxIndex
						JassStmtSet("this", JassExprVarAccess(maxIndex.getName()))
		// endif
				)));
		// nextFree[this] = -1
		f.getBody().add(JassStmtSetArray(nextFree.getName(), JassExprVarAccess("this"), JassExprIntVal(-1)));
		
		// init members:
		for (ClassSlot member : classDef.getSlots()) {
			if (member instanceof GlobalVarDef) {
				GlobalVarDef var = (GlobalVarDef) member;
				if (var.getInitialExpr() instanceof Expr) {
					Expr initial = (Expr) var.getInitialExpr();
					ExprTranslationResult e = translateExpr(f, initial);
					f.getBody().addAll(e.getStatements());
					f.getBody().add(JassStmtSet(var.getName(), e.getExpr()));
				}
			}
		}
		
		
		// custom code:
		f.getBody().addAll(translateStatements(f, constructorDef.getBody()));
		
		// return this:
		f.getBody().add(JassStmtReturn(JassExprVarAccess("this")));
	}

	/**
	 * translate a vardef to a jassvar without adding the jassvariable to anything
	 * @param f
	 * @return
	 */
	public JassVar translateVarDef(VarDef v) {
		return v.match(new VarDef.Matcher<JassVar>() {

			@Override
			public JassVar case_WParameter(WParameter wParameter) {
				return JassSimpleVar(translateType(wParameter.attrTyp()), wParameter.getName());
			}

			@Override
			public JassVar case_GlobalVarDef(GlobalVarDef globalVarDef) {
				PscriptType typ = globalVarDef.attrTyp();
				String name = globalVarDef.getName();
				if (globalVarDef.attrNearestClassDef() != null) {
					name = globalVarDef.attrNearestClassDef().getName() + "_" + name;
				}
				if (globalVarDef.attrNearestPackage() instanceof WPackage) {
					name = ((WPackage) globalVarDef.attrNearestPackage()).getName() + "_" + name;
				}	
				name = manager.getUniqueName(globalVarDef, name);
				if (globalVarDef.attrIsClassMember()) {
					return JassArrayVar(translateType(typ), name);	
				} else {
					if (typ instanceof PScriptTypeArray) {
						PScriptTypeArray arrayTyp = (PScriptTypeArray) typ;
						return JassArrayVar(translateType(arrayTyp.getBaseType()), name);					
					} else {
						return JassSimpleVar(translateType(typ), name);
					}
				}
			}

			@Override
			public JassVar case_LocalVarDef(LocalVarDef localVarDef) {
				PscriptType typ = localVarDef.attrTyp();
				if (typ instanceof PScriptTypeArray) {
					PScriptTypeArray arrayTyp = (PScriptTypeArray) typ;
					return JassArrayVar(translateType(arrayTyp.getBaseType()), localVarDef.getName());					
				} else {
					return JassSimpleVar(translateType(typ), localVarDef.getName());
				}
			}
		});
	}
	
}
