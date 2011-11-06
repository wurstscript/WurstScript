package de.peeeq.wurstscript.jasstranslation;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprAssignable;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.NoTypeExpr;
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
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
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

	public JassTranslator(CompilationUnit wurstProgram) {
		this.manager = new JassManager();
		this.wurstProg = wurstProgram;
	}
	
	public JassProg translate() {
		globals = JassAst.JassVars();
		functions = JassAst.JassFunctions();
		prog = JassAst.JassProg(globals, functions);
		
		for (TopLevelDeclaration t : wurstProg) {
			translateTopLevelDeclaration(t);
		}
		
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
				translateFuncDef(funcDef);
			}
		});
	}

	protected void translateFuncDef(FuncDef funcDef) {
		JassFunction f = manager.getJassFunctionFor(funcDef);
		f.setReturnType(translateType(funcDef.getSignature().getTyp()));
		for (WParameter param : funcDef.getSignature().getParameters()) {
			f.getParams().add(translateParam(param));
		}
		f.setBody(translateStatements(f, funcDef.getBody()));
	}

	private JassStatements translateStatements(JassFunction f, WStatements statements) {
		JassStatements result = JassAst.JassStatements();
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
				FunctionDefinition calledFunc = exprMemberMethod.attrFuncDef();
				JassFunction calledJassFunc = manager.getJassFunctionFor(calledFunc);
				String functionName = calledJassFunc.getName();
				JassExprlist arguments = translateArguments(exprMemberMethod.getArgs());
				arguments.addFront(translateExpr(exprMemberMethod.getLeft()));
				JassStatement stmtCall = JassAst.JassStmtCall(functionName, arguments);
				result.add(stmtCall);
			}


			@Override
			public void case_StmtLoop(StmtLoop stmtLoop) {
				JassStatements body = translateStatements(f, stmtLoop.getBody());
				result.add(JassAst.JassStmtLoop(body ));
			}

			@Override
			public void case_StmtSet(StmtSet stmtSet) {
				final JassExpr right = translateExpr(stmtSet.getRight());
				stmtSet.getLeft().match(new ExprAssignable.MatcherVoid() {

					@Override
					public void case_ExprMemberVar(ExprMemberVar exprMemberVar) {
						VarDef leftVar = exprMemberVar.attrVarDef();
						JassVar leftJassVar = manager.getJassVarFor(leftVar);
						JassExpr index = translateExpr(exprMemberVar.getLeft());
						result.add(JassAst.JassStmtSetArray(leftJassVar.getName(), index, right));
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
						result.add(JassAst.JassStmtSet(leftJassVar.getName(), right));
					}

					@Override
					public void case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
						VarDef leftVar = exprVarArrayAccess.attrVarDef();
						JassVar leftJassVar = manager.getJassVarFor(leftVar);
						JassExpr index;
						if (exprVarArrayAccess.getIndexes().size() == 1) {
							index = translateExpr(exprVarArrayAccess.getIndexes().get(0));
						} else {
							throw new Error("multiple indexes not supported yet");
						}
						result.add(JassAst.JassStmtSetArray(leftJassVar.getName(), index, right));
					}
				});
				
				
			}

			@Override
			public void case_StmtIf(StmtIf stmtIf) {
				JassExpr cond = translateExpr(stmtIf.getCond());
				JassStatements thenBlock = translateStatements(f, stmtIf.getThenBlock());
				JassStatements elseBlock = translateStatements(f, stmtIf.getElseBlock());
				result.add(JassAst.JassStmtIf(cond, thenBlock, elseBlock));
			}

			@Override
			public void case_StmtDestroy(StmtDestroy stmtDestroy) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}

			@Override
			public void case_StmtWhile(StmtWhile stmtWhile) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}

			@Override
			public void case_ExprNewObject(ExprNewObject exprNewObject) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}

			@Override
			public void case_StmtReturn(StmtReturn stmtReturn) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}


			@Override
			public void case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}

			@Override
			public void case_StmtExitwhen(StmtExitwhen stmtExitwhen) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}

			@Override
			public void case_StmtErr(StmtErr stmtErr) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}

			@Override
			public void case_LocalVarDef(LocalVarDef localVarDef) {
				// TODO Auto-generated method stub
				throw new Error("Not implemented yet.");
			}

		
		});
		return result;
	}

	protected JassExpr translateExpr(Expr left) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	protected JassExprlist translateArguments(Arguments args) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	private JassSimpleVar translateParam(WParameter param) {
		return JassAst.JassSimpleVar(translateType(param.getTyp()), param.getName());
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
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	protected void translateNativeFunc(NativeFunc nativeFunc) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	protected void translateNativeType(NativeType nativeType) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	protected void translatePackage(WPackage wPackage) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	/**
	 * translate a vardef to a jassvar without adding the jassvariable to anything
	 * @param f
	 * @return
	 */
	public static JassVar translateVarDef(VarDef f) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}
	
}
