package de.peeeq.wurstscript.intermediateLang.translator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import katja.common.NE;
import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.ArgumentsPos;
import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.CompilationUnitPos;
import de.peeeq.wurstscript.ast.ConstructorDefPos;
import de.peeeq.wurstscript.ast.ExprAssignablePos;
import de.peeeq.wurstscript.ast.ExprBinaryPos;
import de.peeeq.wurstscript.ast.ExprBoolValPos;
import de.peeeq.wurstscript.ast.ExprFuncRefPos;
import de.peeeq.wurstscript.ast.ExprFunctionCallPos;
import de.peeeq.wurstscript.ast.ExprIntValPos;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarPos;
import de.peeeq.wurstscript.ast.ExprMemberMethodPos;
import de.peeeq.wurstscript.ast.ExprMemberVarPos;
import de.peeeq.wurstscript.ast.ExprNewObjectPos;
import de.peeeq.wurstscript.ast.ExprNullPos;
import de.peeeq.wurstscript.ast.ExprPos;
import de.peeeq.wurstscript.ast.ExprRealValPos;
import de.peeeq.wurstscript.ast.ExprStringValPos;
import de.peeeq.wurstscript.ast.ExprThisPos;
import de.peeeq.wurstscript.ast.ExprUnaryPos;
import de.peeeq.wurstscript.ast.ExprVarAccessPos;
import de.peeeq.wurstscript.ast.ExprVarArrayAccessPos;
import de.peeeq.wurstscript.ast.FuncDefPos;
import de.peeeq.wurstscript.ast.FunctionDefinitionPos;
import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.IndexesPos;
import de.peeeq.wurstscript.ast.InitBlockPos;
import de.peeeq.wurstscript.ast.JassGlobalBlockPos;
import de.peeeq.wurstscript.ast.LocalVarDefPos;
import de.peeeq.wurstscript.ast.NativeFuncPos;
import de.peeeq.wurstscript.ast.NativeTypePos;
import de.peeeq.wurstscript.ast.OpAndPos;
import de.peeeq.wurstscript.ast.OpDivIntPos;
import de.peeeq.wurstscript.ast.OpDivRealPos;
import de.peeeq.wurstscript.ast.OpEqualsPos;
import de.peeeq.wurstscript.ast.OpGreaterEqPos;
import de.peeeq.wurstscript.ast.OpGreaterPos;
import de.peeeq.wurstscript.ast.OpLessEqPos;
import de.peeeq.wurstscript.ast.OpLessPos;
import de.peeeq.wurstscript.ast.OpMinusPos;
import de.peeeq.wurstscript.ast.OpModIntPos;
import de.peeeq.wurstscript.ast.OpModRealPos;
import de.peeeq.wurstscript.ast.OpMultPos;
import de.peeeq.wurstscript.ast.OpNotPos;
import de.peeeq.wurstscript.ast.OpOrPos;
import de.peeeq.wurstscript.ast.OpPlusPos;
import de.peeeq.wurstscript.ast.OpPos;
import de.peeeq.wurstscript.ast.OpUnequalsPos;
import de.peeeq.wurstscript.ast.OptTypeExprPos;
import de.peeeq.wurstscript.ast.PackageOrGlobalPos;
import de.peeeq.wurstscript.ast.StmtDecRefCountPos;
import de.peeeq.wurstscript.ast.StmtDestroyPos;
import de.peeeq.wurstscript.ast.StmtErrPos;
import de.peeeq.wurstscript.ast.StmtExitwhenPos;
import de.peeeq.wurstscript.ast.StmtIfPos;
import de.peeeq.wurstscript.ast.StmtIncRefCountPos;
import de.peeeq.wurstscript.ast.StmtLoopPos;
import de.peeeq.wurstscript.ast.StmtReturnPos;
import de.peeeq.wurstscript.ast.StmtSetPos;
import de.peeeq.wurstscript.ast.StmtWhilePos;
import de.peeeq.wurstscript.ast.TopLevelDeclarationPos;
import de.peeeq.wurstscript.ast.TypeExprPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;
import de.peeeq.wurstscript.ast.WParameterPos;
import de.peeeq.wurstscript.ast.WStatementPos;
import de.peeeq.wurstscript.ast.WStatementsPos;
import de.peeeq.wurstscript.attributes.Attributes;
import de.peeeq.wurstscript.intermediateLang.ILStatement;
import de.peeeq.wurstscript.intermediateLang.ILarraySetVar;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstNum;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.ILexitwhen;
import de.peeeq.wurstscript.intermediateLang.ILfunction;
import de.peeeq.wurstscript.intermediateLang.ILfunctionCall;
import de.peeeq.wurstscript.intermediateLang.ILif;
import de.peeeq.wurstscript.intermediateLang.ILloop;
import de.peeeq.wurstscript.intermediateLang.ILprog;
import de.peeeq.wurstscript.intermediateLang.ILreturn;
import de.peeeq.wurstscript.intermediateLang.ILreturnVoid;
import de.peeeq.wurstscript.intermediateLang.ILsetBinary;
import de.peeeq.wurstscript.intermediateLang.ILsetVar;
import de.peeeq.wurstscript.intermediateLang.ILsetVarArray;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.intermediateLang.IlbuildinFunctionCall;
import de.peeeq.wurstscript.intermediateLang.Iloperator;
import de.peeeq.wurstscript.intermediateLang.IlsetConst;
import de.peeeq.wurstscript.intermediateLang.IlsetUnary;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;
/**
 * translates an AST into the intermediate language
 */
public class IntermediateLangTranslator {

	private CompilationUnitPos cu;
	private ILprog prog = new ILprog();
	private Attributes attr;
	private NameManagement names;
	
	
	public IntermediateLangTranslator(CompilationUnitPos cu, Attributes attr) {
		this.cu = cu;
		this.attr = attr;
		names = new NameManagement(attr);
	}

	public ILprog translate() {

		for (TopLevelDeclarationPos tl : cu) {
			tl.Switch(new TopLevelDeclarationPos.Switch<Void, NE>() {
				@Override
				public Void CaseJassGlobalBlockPos(JassGlobalBlockPos term) throws NE {
					translateGlobalBlock(term);
					return null;
				}

				@Override
				public Void CaseFuncDefPos(FuncDefPos term) throws NE {
					translateFuncDef(null, term);
					return null;
				}

				@Override
				public Void CaseNativeTypePos(NativeTypePos term) throws NE {
					return null;
				}


				@Override
				public Void CaseWPackagePos(WPackagePos term) throws NE {
					translatePackage(term);
					return null;
				}

				@Override
				public Void CaseNativeFuncPos(NativeFuncPos term) throws NE {
					return null;
				}
			});
			
		}

		return prog;

	}

	protected void translateGlobalBlock(JassGlobalBlockPos globalBlock) {
		for (GlobalVarDefPos g : globalBlock) {
			translateGlobalVarDef(null, g);
		}
	}

	private void translatePackage(final WPackagePos p) {
		for (WEntityPos e : p.elements()) {
			e.Switch(new WEntityPos.Switch<Void, NE>() {

				@Override
				public Void CaseNativeTypePos(NativeTypePos term) throws NE {
					// nothing to do here
					return null;
				}

				@Override
				public Void CaseClassDefPos(ClassDefPos term) throws NE {
					transltateClassDef(p, term);
					return null;
				}

				@Override
				public Void CaseFuncDefPos(FuncDefPos term) throws NE {
					translateFuncDef(p, term);
					return null;
				}

				@Override
				public Void CaseGlobalVarDefPos(GlobalVarDefPos term) throws NE {
					translateGlobalVarDef(p, term);
					return null;
				}

				@Override
				public Void CaseInitBlockPos(InitBlockPos term) throws NE {
					translateInitBlock(p, term);
					return null;
				}

				@Override
				public Void CaseNativeFuncPos(NativeFuncPos term) throws NE {
					// nothing to do here
					return null;
				}
			});
		}
	}

	protected void translateInitBlock(WPackagePos p, InitBlockPos term) {
		ILfunction initFunc = names.getInitBlockFunction(p, term);
		prog.addInitializer(initFunc);
		translateFunctionBody(initFunc, term.body());

	}

	protected void translateFuncDef(WPackagePos p, FuncDefPos term) {
		ILfunction func = names.getFunction(term);
		prog.addFunction(func);
		if (term.signature().typ() instanceof TypeExprPos) {
			func.setReturnType(attr.typeExprType.get((TypeExprPos) term.signature().typ()));
		} else {
			func.setReturnType(PScriptTypeVoid.instance());
		}
		func.initParams();
		for (WParameterPos param : term.signature().parameters()) {
			func.addParam(new ILvar(param.name().term(), attr.typeExprType.get(param.typ())));
		}
		
		OptTypeExprPos retTyp = term.signature().typ();
		if (retTyp instanceof TypeExprPos) {
			func.setReturnType(attr.typeExprType.get((TypeExprPos) retTyp));
		} else {
			func.setReturnType(PScriptTypeVoid.instance());
		}
		translateFunctionBody(func, term.body());
	}

	
	
	
	
	

	private void translateFunctionBody(ILfunction func, WStatementsPos body) {
		func.addBody(translateStatements(func, body));

	}

	private List<ILStatement> translateStatements(ILfunction func, WStatementsPos statements) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		for (WStatementPos s : statements) {
			result.addAll(translateStatement(func, s));
		}
		return result;
	}

	private List<ILStatement> translateStatement(final ILfunction func, WStatementPos s) {
		final List<ILStatement> result = new NotNullList<ILStatement>();
		return s.Switch(new WStatementPos.Switch<List<ILStatement>, NE>() {

			@Override
			public List<ILStatement> CaseExprMemberMethodPos(ExprMemberMethodPos term) throws NE {
				return translateExprMemberMethodPos(func, null, term);
			}

			@Override
			public List<ILStatement> CaseExprFunctionCallPos(ExprFunctionCallPos term) throws NE {
				return translateFunctionCall(func, null, term, term.args());				
			}

			@Override
			public List<ILStatement> CaseExprNewObjectPos(ExprNewObjectPos term) throws NE {
				// get constructor for this
				
				
				// TODO implement new expr
				throw new Error("not implemented");
			}

			@Override
			public List<ILStatement> CaseStmtIfPos(StmtIfPos term) throws NE {
				ILvar cond = names.getNewLocalVar(func, PScriptTypeBool.instance(), "ifCondition");
				// translate condition
				result.addAll(translateExpr(func, cond, term.cond()));
				List<ILStatement> thenBlock = translateStatements(func, term.thenBlock());
				List<ILStatement> elseBlock = translateStatements(func, term.elseBlock());
				result.add(new ILif(cond, thenBlock, elseBlock));
				return result;
			}

			@Override
			public List<ILStatement> CaseStmtWhilePos(StmtWhilePos term) throws NE {
				ILvar whileCondition = names.getNewLocalVar(func, PScriptTypeBool.instance(), "whileCondition");
				ILvar negatedWhileCondition = names.getNewLocalVar(func, PScriptTypeBool.instance(), "exitwhenCondition");

				List<ILStatement> body = new NotNullList<ILStatement>();
				// calculate while condition
				body.addAll(translateExpr(func, whileCondition, term.cond()));
				// exitwhen not whileCondition
				body.add(new IlsetUnary(negatedWhileCondition, Iloperator.NOT, whileCondition));
				body.add(new ILexitwhen(negatedWhileCondition));
				body.addAll(translateStatements(func, term.body()));
				result.add(new ILloop(body));
				return result;
			}

			@Override
			public List<ILStatement> CaseLocalVarDefPos(LocalVarDefPos term) throws NE {
				ILvar v = names.getILvarForVarDef(term);
				func.getLocals().add(v);

				if (term.initialExpr() instanceof ExprPos) {
					// translate initial expr
					result.addAll(translateExpr(func, v, (ExprPos) term.initialExpr()));
				}
				return result;
			}


			@Override
			public List<ILStatement> CaseStmtSetPos(final StmtSetPos term) throws NE {

				return term.left().Switch(new ExprAssignablePos.Switch<List<ILStatement>, NE>() {

					@Override
					public List<ILStatement> CaseExprMemberVarPos(ExprMemberVarPos left) throws NE {
						VarDefPos memberVar = attr.varDef.get(left);

						ILvar arVar = names.getILvarForVarDef(memberVar);

						// evaluate receiver object
						ILvar receiver = names.getNewLocalVar(func, PScriptTypeInt.instance(), "receiver");
						result.addAll(translateExpr(func, receiver, left.left()));

						PscriptType typ = attr.exprType.get(term.right());
						// evaluate right side
						ILvar tempResult = names.getNewLocalVar(func, typ, "temp");
						result.addAll(translateExpr(func, tempResult, term.right()));

						// arVar[receiver] = tempResult
						result.add(new ILarraySetVar(arVar, receiver, tempResult));
						return result;
					}

					@Override
					public List<ILStatement> CaseExprMemberArrayVarPos(ExprMemberArrayVarPos term) throws NE {
						// TODO class array members
						throw new Error("class array members not implemented");
					}

					@Override
					public List<ILStatement> CaseExprVarAccessPos(ExprVarAccessPos left) throws NE {
						VarDefPos varDef = attr.varDef.get(left);
						ILvar v = names.getILvarForVarDef(varDef);

						result.addAll(translateExpr(func, v, term.right()));
						return result;
					}

					@Override
					public List<ILStatement> CaseExprVarArrayAccessPos(ExprVarArrayAccessPos arAccess) throws NE {
						VarDefPos varDef = attr.varDef.get(arAccess);
						PscriptType type = attr.varDefType.get(varDef);
						if (type instanceof PScriptTypeArray) {
							PScriptTypeArray typeA = (PScriptTypeArray) type;
							ILvar indexResult = names.getNewLocalVar(func, PScriptTypeInt.instance(), "index");
							result.addAll(calculateIndexes(func, typeA, indexResult, arAccess.indexes()));

							ILvar tempResult = names.getNewLocalVar(func, typeA.getBaseType(), "temp");
							result.addAll(translateExpr(func, tempResult, term.right()));

							ILvar arrayVar = names.getILvarForVarDef(varDef);
							result.add(new ILarraySetVar(arrayVar , indexResult, tempResult));
							return result;
						} else {
							attr.addError(arAccess.source(), "Variable " + varDef.name().term() + " is not an array.");
							return new NotNullList<ILStatement>();
						}
					}
				});
			}

			@Override
			public List<ILStatement> CaseStmtReturnPos(StmtReturnPos term) throws NE {
				if (term.obj() instanceof ExprPos) {
					ExprPos returnValue = (ExprPos) term.obj();
					PscriptType type = attr.exprType.get(returnValue);
					ILvar returnVar = names.getNewLocalVar(func, type, "tempReturn");
					result.addAll(translateExpr(func, returnVar, returnValue));
					result.add(new ILreturn(returnVar));
				} else {
					// return void
					result.add(new ILreturnVoid());
				}
				return result;
				
			}

			@Override
			public List<ILStatement> CaseStmtDestroyPos(StmtDestroyPos term) throws NE {
				PscriptType type = attr.exprType.get(term.obj());
				if (type instanceof PscriptTypeClass) {
					PscriptTypeClass classtype = (PscriptTypeClass) type;
					ClassDefPos classDef = classtype.getClassDef();
					
					ILfunction func = names.getDestroyFunction(classDef);
					
					ILvar toDestroy = names.getNewLocalVar(func, classtype, "toDestroy");
					result.addAll(translateExpr(func, toDestroy , term.obj()));
					result.add(new ILfunctionCall(null, func.getName(), Utils.array(classtype), Utils.array(toDestroy)));
					
				} else {
					attr.addError(term.obj().source(), "Cannot destroy objects of type " + type);
				}
				return result;
			}

			@Override
			public List<ILStatement> CaseStmtIncRefCountPos(StmtIncRefCountPos term) throws NE {
				throw new Error("ref counting not implemented");
			}

			@Override
			public List<ILStatement> CaseStmtDecRefCountPos(StmtDecRefCountPos term) throws NE {
				throw new Error("ref counting not implemented");
			}

			@Override
			public List<ILStatement> CaseStmtErrPos(StmtErrPos term) throws NE {
				throw new Error("not implemented");
			}

			@Override
			public List<ILStatement> CaseStmtLoopPos(StmtLoopPos term) throws NE {
				List<ILStatement> loopBody = translateStatements(func, term.body());
				result.add(new ILloop(loopBody));
				return result;
			}

			@Override
			public List<ILStatement> CaseStmtExitwhenPos(StmtExitwhenPos term) throws NE {
				ILvar exitWhenVar = names.getNewLocalVar(func, PScriptTypeBool.instance(), "exitwhen_condition");
				result.addAll(translateExpr(func, exitWhenVar, term.cond()));
				result.add(new ILexitwhen(exitWhenVar ));
				return result;
			}
		});
	}

	protected List<ILStatement> translateExprMemberMethodPos(ILfunction func, ILvar resultVar, ExprMemberMethodPos term) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		ExprPos left = term.left();
		FunctionDefinitionPos calledFuncDef = attr.funcDef.get(term);
		ILfunction calledFunc = names.getFunction(calledFuncDef);
		
		// translate left expr
		ILvar tempLeft = names.getNewLocalVar(func, PScriptTypeInt.instance(), "receiver");
		
		result.addAll(translateExpr(func, tempLeft, left));
		
		PscriptType[] argumentTypes = calledFunc.getParamTypes();
		ILvar[] argumentVars = new ILvar[argumentTypes.length];
		argumentVars[0] = tempLeft; // first argument is the implicit parameter
		for (int i=1; i<argumentVars.length; i++) {
			argumentVars[i] = names.getNewLocalVar(func, argumentTypes[i], "param");
			result.addAll(translateExpr(func, argumentVars[i], term.args().get(i-1)));
		}
		result.add(new ILfunctionCall(resultVar, calledFunc.getName(), argumentTypes , argumentVars));
		return result;
	}

	protected List<ILStatement> translateFunctionCall(ILfunction func, final ILvar resultVar, ExprFunctionCallPos term, ArgumentsPos args) {
		final List<ILStatement> result = new NotNullList<ILStatement>();
		final FunctionDefinitionPos calledFunc = attr.funcDef.get(term);
		
		// add call dependency
		prog.addCallDependency(func, names.getFunction(calledFunc));
		
		
		// translate Arguments:
		int argCount = term.args().size();

		final PscriptType[] argumentTypes = new PscriptType[argCount];
		final ILvar[] argumentVars = new ILvar[argCount];

		
		for (int i = 0; i < argCount; i++) {
			ExprPos arg = term.args().get(i);
			WParameterPos param = calledFunc.signature().parameters().get(i);
			argumentTypes[i] = attr.varDefType.get(param);

			argumentVars[i] = names.getNewLocalVar(func, argumentTypes[i], calledFunc.signature().name().term() + "_param" + i);
			result.addAll(translateExpr(func, argumentVars[i], arg));
		}
		// make the call:
		calledFunc.Switch(new FunctionDefinitionPos.Switch<Void, NE>() {

			@Override
			public Void CaseFuncDefPos(FuncDefPos term) throws NE {
				result.add(new ILfunctionCall(resultVar, names.getFunction(calledFunc).getName(), argumentTypes, argumentVars));
				return null;
			}

			@Override
			public Void CaseNativeFuncPos(NativeFuncPos term) throws NE {
				result.add(new IlbuildinFunctionCall(resultVar, term.signature().name().term(), argumentVars));
				return null;
			}
		});
		
		return result;
	}

	

	

	protected List<ILStatement> translateExpr(final ILfunction func, final ILvar resultVar, ExprPos expr) {
		final List<ILStatement> result = new NotNullList<ILStatement>();
		return expr.Switch(new ExprPos.Switch<List<ILStatement>, NE>() {

			@Override
			public List<ILStatement> CaseExprIntValPos(ExprIntValPos term) throws NE {
				result.add(new IlsetConst(resultVar, new ILconstInt(term.val().term())));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprRealValPos(ExprRealValPos term) throws NE {
				result.add(new IlsetConst(resultVar, new ILconstNum(term.val().term())));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprStringValPos(ExprStringValPos term) throws NE {
				result.add(new IlsetConst(resultVar, new ILconstString(term.val().term())));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprBoolValPos(ExprBoolValPos term) throws NE {
				result.add(new IlsetConst(resultVar, new ILconstBool(term.val().term())));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprFuncRefPos(ExprFuncRefPos term) throws NE {
				FunctionDefinitionPos f = attr.funcDef.get(term);
				ILfunction ilfunc = names.getFunction(f);
				result.add(new IlsetConst(resultVar, new ILconstFuncRef(ilfunc)));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprVarAccessPos(ExprVarAccessPos term) throws NE {
				VarDefPos varDef = attr.varDef.get(term);
				ILvar var = names.getILvarForVarDef(varDef);
				result.add(new ILsetVar(resultVar, var));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprVarArrayAccessPos(ExprVarArrayAccessPos arAccess) throws NE {
				VarDefPos varDef = attr.varDef.get(arAccess);
				PscriptType type = attr.varDefType.get(varDef);
				if (type instanceof PScriptTypeArray) {
					PScriptTypeArray typeA = (PScriptTypeArray) type;
					ILvar indexResult = names.getNewLocalVar(func, PScriptTypeInt.instance(), "index");
					result.addAll(calculateIndexes(func, typeA, indexResult, arAccess.indexes()));
					
					
					ILvar arVar = names.getILvarForVarDef(varDef);
					result.add(new ILsetVarArray(resultVar, arVar, indexResult));
					return result;
				} else {
					attr.addError(arAccess.source(), "Variable " + varDef.name().term() + " is not an array.");
					return new NotNullList<ILStatement>();
				}
			}

			@Override
			public List<ILStatement> CaseExprThisPos(ExprThisPos term) throws NE {
				ILvar var = getThisVariableForMethod(func);
				result.add(new ILsetVar(resultVar, var));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprBinaryPos(ExprBinaryPos term) throws NE {

				if (term.op() instanceof OpAndPos) {
					result.addAll(translateExpr(func, resultVar, term.left()));
					result.add(new ILif(resultVar, translateExpr(func, resultVar, term.right()), new LinkedList<ILStatement>()));
				} else if (term.op() instanceof OpOrPos) {
					result.addAll(translateExpr(func, resultVar, term.left()));
					result.add(new ILif(resultVar, new LinkedList<ILStatement>(), translateExpr(func, resultVar, term.right())));
				} else {
					// evaluate left expr:
					PscriptType leftType = attr.exprType.get(term.left());
					ILvar leftVar = names.getNewLocalVar(func, leftType, "leftOperand");
					List<ILStatement> leftExpr = translateExpr(func, leftVar, term.left());
					// evaluate right expr:
					PscriptType rightType = attr.exprType.get(term.left());
					ILvar rightVar = names.getNewLocalVar(func, rightType, "rightOperand");
					List<ILStatement> rightExpr = translateExpr(func, rightVar, term.right());

					result.addAll(leftExpr);
					result.addAll(rightExpr);
					Iloperator op;
					if (Utils.isJassCode(term)) {
						op = translateOpJass(term.op(), leftType, rightType);
					} else {
						op = translateOp(term.op());
					}
					
					result.add(new ILsetBinary(resultVar, leftVar, op, rightVar));
				}
				return result;
			}

			@Override
			public List<ILStatement> CaseExprUnaryPos(final ExprUnaryPos term) throws NE {
				PscriptType type = attr.exprType.get(term.right());
				ILvar tempVar = names.getNewLocalVar(func, type, "temp");
				result.addAll(translateExpr(func, tempVar, term.right()));
				result.add(new IlsetUnary(resultVar, translateOp(term.op()), tempVar));
				return result;
			}

			@Override
			public List<ILStatement> CaseExprMemberVarPos(ExprMemberVarPos term) throws NE {
				// TODO member var expr
				throw new Error("Not implemented yet.");
			}

			@Override
			public List<ILStatement> CaseExprMemberArrayVarPos(ExprMemberArrayVarPos term) throws NE {
				// TODO member arrayvar expr
				throw new Error("Not implemented yet.");
			}

			@Override
			public List<ILStatement> CaseExprMemberMethodPos(ExprMemberMethodPos term) throws NE {
				return translateExprMemberMethodPos(func, resultVar, term);
			}

			@Override
			public List<ILStatement> CaseExprFunctionCallPos(ExprFunctionCallPos term) throws NE {
				return translateFunctionCall(func, resultVar, term, term.args());
			}

			@Override
			public List<ILStatement> CaseExprNewObjectPos(ExprNewObjectPos term) throws NE {
				return translateExprNewPos(func, resultVar, term);
			}

			@Override
			public List<ILStatement> CaseExprNullPos(ExprNullPos term) throws NE {
				result.add(new IlsetConst(resultVar, new ILconstNull()));
				return result;
			}

		});
	}

	
	protected List<ILStatement> translateExprNewPos(ILfunction func, ILvar resultVar, ExprNewObjectPos term) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		
		ConstructorDefPos constr = attr.constrDef.get(term);
		
		ILfunction constrFunc = names.getConstructorFunction(constr);
		
		// translate arguments
		
		PscriptType[] argumentTypes = constrFunc.getParamTypes();
		ILvar[] argumentVars = new ILvar[argumentTypes.length];
		for (int i=0; i<argumentTypes.length; i++) {
			argumentVars[i] = names.getNewLocalVar(func, argumentTypes[i], "contr_arg");
			result.addAll(translateExpr(func, argumentVars[i], term.args().get(i)));
		}
		result.add(new ILfunctionCall(resultVar, constrFunc.getName(), argumentTypes, argumentVars));
		return result ;
	}

	protected Iloperator translateOpJass(OpPos op, PscriptType leftType, PscriptType rightType) {
		if (op instanceof OpDivRealPos) {
			if (leftType instanceof PScriptTypeInt && rightType instanceof PScriptTypeInt) {
				return Iloperator.DIV_INT;
			}
		}
		return translateOp(op);
	}
	
	protected Iloperator translateOp(OpPos op) {
		return op.Switch(new OpPos.Switch<Iloperator, NE>() {

			@Override
			public Iloperator CaseOpOrPos(OpOrPos term) throws NE {
				return Iloperator.OR;
			}

			@Override
			public Iloperator CaseOpAndPos(OpAndPos term) throws NE {
				return Iloperator.AND;
			}

			@Override
			public Iloperator CaseOpEqualsPos(OpEqualsPos term) throws NE {
				return Iloperator.EQUALITY;
			}

			@Override
			public Iloperator CaseOpUnequalsPos(OpUnequalsPos term) throws NE {
				return Iloperator.UNEQUALITY;
			}

			@Override
			public Iloperator CaseOpLessEqPos(OpLessEqPos term) throws NE {
				return Iloperator.LESS_EQ;
			}

			@Override
			public Iloperator CaseOpLessPos(OpLessPos term) throws NE {
				return Iloperator.LESS;
			}

			@Override
			public Iloperator CaseOpGreaterEqPos(OpGreaterEqPos term) throws NE {
				return Iloperator.GREATER_EQ;
			}

			@Override
			public Iloperator CaseOpGreaterPos(OpGreaterPos term) throws NE {
				return Iloperator.GREATER;
			}

			@Override
			public Iloperator CaseOpPlusPos(OpPlusPos term) throws NE {
				return Iloperator.PLUS;
			}

			@Override
			public Iloperator CaseOpMinusPos(OpMinusPos term) throws NE {
				return Iloperator.MINUS;
			}

			@Override
			public Iloperator CaseOpMultPos(OpMultPos term) throws NE {
				return Iloperator.MULT;
			}

			@Override
			public Iloperator CaseOpDivRealPos(OpDivRealPos term) throws NE {
				return Iloperator.DIV_REAL;
			}

			@Override
			public Iloperator CaseOpModRealPos(OpModRealPos term) throws NE {
				return Iloperator.MOD_REAL;
			}

			@Override
			public Iloperator CaseOpModIntPos(OpModIntPos term) throws NE {
				return Iloperator.MOD_INT;
			}

			@Override
			public Iloperator CaseOpDivIntPos(OpDivIntPos term) throws NE {
				return Iloperator.DIV_INT;
			}

			@Override
			public Iloperator CaseOpNotPos(OpNotPos term) throws NE {
				return Iloperator.NOT;
			}
		});
	}

	protected ILvar getThisVariableForMethod(ILfunction func) {
		return new ILvar("this", PScriptTypeInt.instance());
	}

	protected void translateGlobalVarDef(WPackagePos p, GlobalVarDefPos term) {
		ILvar v = names.getILvarForVarDef(term);
		prog.addGlobalVar(v);
		// TODO inital value + global dependencies
		if (term.initialExpr() instanceof ExprPos) {
			ExprPos initialExpr = (ExprPos) term.initialExpr();
			
			List<ExprVarAccessPos> varRefs = Utils.collect(ExprVarAccessPos.class, initialExpr);
			for (ExprVarAccessPos varRef : varRefs) {
				ILvar dependsOn = names.getILvarForVarDef(attr.varDef.get(varRef));
				addGlobalInitializationDependency(v, dependsOn);
			}
			
			
			addGlobalInit(v, initialExpr);
		}
	}

	

	private void addGlobalInit(ILvar v, ExprPos initialExpr) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	private void addGlobalInitializationDependency(ILvar v, ILvar dependsOn) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	protected void transltateClassDef(WPackagePos p, ClassDefPos term) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}
	
	/**
	 * calculate the index for an array
	 * @param func current function
	 * @param type type of the array (stores information about dimensions and about where arraysizes can be found)
	 * @param indexResult the variable where to store the final one dimensional index to
	 * @param indexesPos the indexes
	 * @return
	 */
	protected List<ILStatement> calculateIndexes(ILfunction func, PScriptTypeArray type, ILvar indexResult, IndexesPos indexesPos) {
		if (indexesPos.size() > 1) {
			throw new Error("Multidimensional arrays are not supported yet.");
		}
		
		LinkedList<ILStatement> result = new LinkedList<ILStatement>();
		result.addAll(translateExpr(func, indexResult, indexesPos.first()));			
		return result;	
		
		
//		LinkedList<ILStatement> result = new LinkedList<ILStatement>();
//		ILvar[] indexVar = new ILvar[indexesPos.size()];
//		ILvar[] indexVarM = new ILvar[indexesPos.size()];
//		
//		// calculate indizes
//		for (int i = 0; i < indexesPos.size(); i++) {
//			indexVar[i] = getNewLocalVar(func, PScriptTypeInt.instance(), "index");
//			indexVarM[i] = getNewLocalVar(func, PScriptTypeInt.instance(), "indexM");
//			
//			int rightSize = 1;
//			for (int j = i+1; j < indexesPos.size(); j++) {
//				rightSize *= type.getSize(j);
//			}
//			
//			//result.addAll(translateExpr(indizes.get(i), indexVar[i]));
//			result.addAll(translateExpr(func, indexVar[i], indexesPos.get(i)));
//			result.add(new ILsetBinaryCR(indexVarM[i], indexVar[i], Iloperator.MULT, new ILconstInt(rightSize)));
//		}
//		
//		
//		result.add(new ILsetVar(indexResult, indexVarM[0]));
//		
//		// calculate the sum of the indizes2:
//		for (int i=1; i < indexesPos.size(); i++) {
//			result.add(new ILsetBinary(indexResult, indexResult, Iloperator.PLUS, indexVarM[i]));
//		}
//		return result;
	}

}
