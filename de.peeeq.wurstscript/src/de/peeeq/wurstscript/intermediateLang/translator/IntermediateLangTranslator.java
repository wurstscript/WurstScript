package de.peeeq.wurstscript.intermediateLang.translator;

import java.util.LinkedList;
import java.util.List;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.Ast;
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
import de.peeeq.wurstscript.ast.Indexes;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.Op;
import de.peeeq.wurstscript.ast.OpAnd;
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
import de.peeeq.wurstscript.ast.OpUnequals;
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
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.attr;
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
import de.peeeq.wurstscript.intermediateLang.ILsetBinaryCR;
import de.peeeq.wurstscript.intermediateLang.ILsetVar;
import de.peeeq.wurstscript.intermediateLang.ILsetVarArray;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.intermediateLang.IlbuildinFunctionCall;
import de.peeeq.wurstscript.intermediateLang.Iloperator;
import de.peeeq.wurstscript.intermediateLang.IlsetConst;
import de.peeeq.wurstscript.intermediateLang.IlsetUnary;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.TopsortCycleException;
import de.peeeq.wurstscript.utils.Utils;
/**
 * translates an AST into the intermediate language
 */
public class IntermediateLangTranslator {

	private CompilationUnit cu;
	private ILprog prog = new ILprog();
	private NameManagement names;
	private GlobalInits globalInits = new GlobalInits();


	public IntermediateLangTranslator(CompilationUnit cu) {
		this.cu = cu;
		names = new NameManagement();
	}

	public ILprog translate() {

		for (TopLevelDeclaration tl : cu) {
			tl.match(new TopLevelDeclaration.MatcherVoid() {
				@Override
				public void case_JassGlobalBlock(JassGlobalBlock term)  {
					translateGlobalBlock(term);
				}

				@Override
				public void case_FuncDef(FuncDef term)  {
					translateFuncDef(null, term);
				}

				@Override
				public void case_NativeType(NativeType term)  {
				}


				@Override
				public void case_WPackage(WPackage term)  {
					translatePackage(term);
				}

				@Override
				public void case_NativeFunc(NativeFunc term)  {
				}
			});

		}



		ILfunction globalInitFunction = createGlobalInitFunction();
		ILfunction initalizerFunction = createPackageInitializerFunction();
		
		ILfunction mainFunction = getMainFunction();
		mainFunction.addBeforeBody(new ILfunctionCall(null, initalizerFunction.getName()));
		prog.addCallDependency(mainFunction, initalizerFunction);
		mainFunction.addBeforeBody(new ILfunctionCall(null, globalInitFunction.getName()));
		prog.addCallDependency(mainFunction, globalInitFunction);
		
		// TODO main function, call init function from main function
		try {
			prog.sortFunctions();
		} catch (TopsortCycleException e) {
			List<ILfunction> cycle = (List<ILfunction>) e.activeItems;
			attr.addError(cycle.get(0).getSource() , "There are mutually recursive calls between the following functions: " + 
					Utils.join(cycle, ", "));
		}


		return prog;

	}

	private ILfunction createPackageInitializerFunction() {
		ILfunction initFunc = new ILfunction("wurst_packageinits", Ast.WPos("generated", 0, 0));
		prog.addFunction(initFunc);
		initFunc.initParams();
		initFunc.setReturnType(PScriptTypeVoid.instance());
		List<ILStatement> statements = new NotNullList<ILStatement>();
		for (ILfunction i : prog.getInitFunctions()) {
			statements.add(new ILfunctionCall(null, i.getName()));
			prog.addCallDependency(initFunc, i);
		}
		
		initFunc.addBody(statements);
		return initFunc ;
	}

	private ILfunction getMainFunction() {
		for (ILfunction f : prog.getFunctions()) {
			if (f.getName().equals("main")) {
				return f;
			}
		}
		// create main function if it does not exist
		ILfunction mainFunction = new ILfunction("main", Ast.WPos("generated", 0, 0));
		prog.addFunction(mainFunction);
		mainFunction.initParams();
		mainFunction.setReturnType(PScriptTypeVoid.instance());
		return mainFunction;
	}

	private ILfunction createGlobalInitFunction() {
		List<GlobalInit> inits;
		ILfunction initFunc = names.getGlobalInitFunction();
		prog.addFunction(initFunc);
		try {
			inits = globalInits.getSortedGlobalInits();

			
			initFunc.setReturnType(PScriptTypeVoid.instance());
			initFunc.initParams();
			List<ILStatement> body = new NotNullList<ILStatement>();

			for (GlobalInit g : inits) {
				body.addAll(translateExpr(initFunc, g.v, g.init));
			}

			initFunc.addBody(body);
			prog.addFunction(initFunc);
		} catch (TopsortCycleException e) {
			@SuppressWarnings("unchecked")
			List<GlobalInit> cycle = (List<GlobalInit>) e.activeItems;
			attr.addError(cycle.get(0).init.getSource(), "Cycle in global dependencies: " + Utils.join(cycle, " -> "));
		}
		return initFunc;
	}

	protected void translateGlobalBlock(JassGlobalBlock globalBlock) {
		for (GlobalVarDef g : globalBlock) {
			translateGlobalVarDef(null, g);
		}
	}

	private void translatePackage(final WPackage p) {
		for (WEntity e : p.getElements()) {
			e.match(new WEntity.MatcherVoid() {

				@Override
				public void case_NativeType(NativeType term)  {
					// nothing to do here
				}

				@Override
				public void case_ClassDef(ClassDef term)  {
					transltateClassDef(p, term);
				}

				@Override
				public void case_FuncDef(FuncDef term)  {
					translateFuncDef(p, term);
				}

				@Override
				public void case_GlobalVarDef(GlobalVarDef term)  {
					translateGlobalVarDef(p, term);
				}

				@Override
				public void case_InitBlock(InitBlock term)  {
					translateInitBlock(p, term);
				}

				@Override
				public void case_NativeFunc(NativeFunc term)  {
					// nothing to do here
				}
			});
		}
	}

	protected void translateInitBlock(WPackage p, InitBlock term) {
		ILfunction initFunc = names.getInitBlockFunction(p, term);
		initFunc.initParams();
		initFunc.setReturnType(PScriptTypeVoid.instance());
		prog.addInitializer(initFunc);
		translateFunctionBody(initFunc, term.getBody());

	}

	protected void translateFuncDef(WPackage p, FuncDef term) {
		ILfunction func = names.getFunction(term);
		prog.addFunction(func);
			func.setReturnType(term.getSignature().getTyp().attrTyp());
		func.initParams();
		for (WParameter param : term.getSignature().getParameters()) {
			func.addParam(new ILvar(param.getName(), param.getTyp().attrTyp()));
		}

		OptTypeExpr retTyp = term.getSignature().getTyp();
		func.setReturnType(retTyp.attrTyp());
		translateFunctionBody(func, term.getBody());
	}







	private void translateFunctionBody(ILfunction func, WStatements body) {
		func.addBody(translateStatements(func, body));

	}

	private List<ILStatement> translateStatements(ILfunction func, WStatements statements) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		for (WStatement s : statements) {
			result.addAll(translateStatement(func, s));
		}
		return result;
	}

	private boolean isMemberVar(VarDef varDef) {
		if (varDef.getParent().getParent() instanceof ClassDef) {
			return true;
		}
		return false;
	}

	private List<ILStatement> translateStatement(final ILfunction func, WStatement s) {
		final List<ILStatement> result = new NotNullList<ILStatement>();
		return s.match(new WStatement.Matcher<List<ILStatement>>() {

			@Override
			public List<ILStatement> case_ExprMemberMethod(ExprMemberMethod term)  {
				return translateExprMemberMethod(func, null, term);
			}

			@Override
			public List<ILStatement> case_ExprFunctionCall(ExprFunctionCall term)  {
				return translateFunctionCall(func, null, term, term.getArgs());				
			}

			@Override
			public List<ILStatement> case_ExprNewObject(ExprNewObject term)  {
				return translateExprNewPos(func, null, term);
			}

			@Override
			public List<ILStatement> case_StmtIf(StmtIf term)  {
				ILvar cond = names.getNewLocalVar(func, PScriptTypeBool.instance(), "ifCondition");
				// translate condition
				result.addAll(translateExpr(func, cond, term.getCond()));
				List<ILStatement> thenBlock = translateStatements(func, term.getThenBlock());
				List<ILStatement> elseBlock = translateStatements(func, term.getElseBlock());
				result.add(new ILif(cond, thenBlock, elseBlock));
				return result;
			}

			@Override
			public List<ILStatement> case_StmtWhile(StmtWhile term)  {
				ILvar whileCondition = names.getNewLocalVar(func, PScriptTypeBool.instance(), "whileCondition");
				ILvar negatedWhileCondition = names.getNewLocalVar(func, PScriptTypeBool.instance(), "exitwhenCondition");

				List<ILStatement> body = new NotNullList<ILStatement>();
				// calculate while condition
				body.addAll(translateExpr(func, whileCondition, term.getCond()));
				// exitwhen not whileCondition
				body.add(new IlsetUnary(negatedWhileCondition, Iloperator.NOT, whileCondition));
				body.add(new ILexitwhen(negatedWhileCondition));
				body.addAll(translateStatements(func, term.getBody()));
				result.add(new ILloop(body));
				return result;
			}

			@Override
			public List<ILStatement> case_LocalVarDef(LocalVarDef term)  {
				ILvar v = names.getILvarForVarDef(term);
				func.getLocals().add(v);

				if (term.getInitialExpr() instanceof Expr) {
					// translate initial expr
					result.addAll(translateExpr(func, v, (Expr) term.getInitialExpr()));
				}
				return result;
			}


			@Override
			public List<ILStatement> case_StmtSet(final StmtSet term)  {

				return term.getLeft().match(new ExprAssignable.Matcher<List<ILStatement>>() {

					@Override
					public List<ILStatement> case_ExprMemberVar(ExprMemberVar left)  {
						VarDef memberVar = left.attrVarDef();

						ILvar arVar = names.getILvarForVarDef(memberVar);

						// evaluate receiver object
						ILvar receiver = names.getNewLocalVar(func, PScriptTypeInt.instance(), "receiver");
						result.addAll(translateExpr(func, receiver, left.getLeft()));

						PscriptType typ = term.getRight().attrTyp();
						// evaluate right side
						ILvar tempResult = names.getNewLocalVar(func, typ, "temp");
						result.addAll(translateExpr(func, tempResult, term.getRight()));

						// arVar[receiver] = tempResult
						result.add(new ILarraySetVar(arVar, receiver, tempResult));
						return result;
					}

					@Override
					public List<ILStatement> case_ExprMemberArrayVar(ExprMemberArrayVar term)  {
						// TODO class array members
						throw new Error("class array members not implemented");
					}

					@Override
					public List<ILStatement> case_ExprVarAccess(ExprVarAccess left)  {
						VarDef varDef = left.attrVarDef();
						ILvar v = names.getILvarForVarDef(varDef);
						if (isMemberVar(varDef)) {


							PscriptType typ = term.getRight().attrTyp();
							// evaluate right side
							ILvar tempResult = names.getNewLocalVar(func, typ, "temp");
							result.addAll(translateExpr(func, tempResult, term.getRight()));

							ILvar thisVar = new ILvar("this", PScriptTypeInt.instance());
							// v[this] = tempResult
							result.add(new ILarraySetVar(v, thisVar , tempResult));
							return result;
						} else { // normal variable

							result.addAll(translateExpr(func, v, term.getRight()));
						}
						return result;
					}

					@Override
					public List<ILStatement> case_ExprVarArrayAccess(ExprVarArrayAccess arAccess)  {
						VarDef varDef = arAccess.attrVarDef();
						PscriptType type = varDef.attrTyp();
						if (type instanceof PScriptTypeArray) {
							PScriptTypeArray typeA = (PScriptTypeArray) type;
							ILvar indexResult = names.getNewLocalVar(func, PScriptTypeInt.instance(), "index");
							result.addAll(calculateIndexes(func, typeA, indexResult, arAccess.getIndexes()));

							ILvar tempResult = names.getNewLocalVar(func, typeA.getBaseType(), "temp");
							result.addAll(translateExpr(func, tempResult, term.getRight()));

							ILvar arrayVar = names.getILvarForVarDef(varDef);
							result.add(new ILarraySetVar(arrayVar , indexResult, tempResult));
							return result;
						} else {
							attr.addError(arAccess.getSource(), "Variable " + varDef.getName() + " is not an array.");
							return new NotNullList<ILStatement>();
						}
					}
				});
			}

			@Override
			public List<ILStatement> case_StmtReturn(StmtReturn term)  {
				if (term.getObj() instanceof Expr) {
					Expr returnValue = (Expr) term.getObj();
					PscriptType type = returnValue.attrTyp();
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
			public List<ILStatement> case_StmtDestroy(StmtDestroy term)  {
				PscriptType type = term.getObj().attrTyp();
				if (type instanceof PscriptTypeClass) {
					PscriptTypeClass classtype = (PscriptTypeClass) type;
					ClassDef classDef = classtype.getClassDef();

					ILfunction destroyFunc = names.getDestroyFunction(classDef);

					ILvar toDestroy = names.getNewLocalVar(func, classtype, "toDestroy");
					result.addAll(translateExpr(func, toDestroy , term.getObj()));
					result.add(new ILfunctionCall(null, destroyFunc.getName(), Utils.array(classtype), Utils.array(toDestroy)));

				} else {
					attr.addError(term.getObj().getSource(), "Cannot destroy objects of type " + type);
				}
				return result;
			}


			@Override
			public List<ILStatement> case_StmtErr(StmtErr term)  {
				throw new Error("not implemented");
			}

			@Override
			public List<ILStatement> case_StmtLoop(StmtLoop term)  {
				List<ILStatement> loopBody = translateStatements(func, term.getBody());
				result.add(new ILloop(loopBody));
				return result;
			}

			@Override
			public List<ILStatement> case_StmtExitwhen(StmtExitwhen term)  {
				ILvar exitWhenVar = names.getNewLocalVar(func, PScriptTypeBool.instance(), "exitwhen_condition");
				result.addAll(translateExpr(func, exitWhenVar, term.getCond()));
				result.add(new ILexitwhen(exitWhenVar ));
				return result;
			}
		});
	}

	protected List<ILStatement> translateExprMemberMethod(ILfunction func, ILvar resultVar, ExprMemberMethod term) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		Expr left = term.getLeft();
		FunctionDefinition calledFuncDef = term.attrFuncDef();
		ILfunction calledFunc = names.getFunction(calledFuncDef);

		// translate left expr
		ILvar tempLeft = names.getNewLocalVar(func, PScriptTypeInt.instance(), "receiver");

		result.addAll(translateExpr(func, tempLeft, left));

		PscriptType[] argumentTypes = calledFunc.getParamTypes();
		ILvar[] argumentVars = new ILvar[argumentTypes.length];
		argumentVars[0] = tempLeft; // first argument is the implicit parameter
		for (int i=1; i<argumentVars.length; i++) {
			argumentVars[i] = names.getNewLocalVar(func, argumentTypes[i], "param");
			result.addAll(translateExpr(func, argumentVars[i], term.getArgs().get(i-1)));
		}
		result.add(new ILfunctionCall(resultVar, calledFunc.getName(), argumentTypes , argumentVars));
		return result;
	}

	protected List<ILStatement> translateFunctionCall(ILfunction func, final ILvar resultVar, ExprFunctionCall term, Arguments args) {
		final List<ILStatement> result = new NotNullList<ILStatement>();
		final FunctionDefinition calledFunc = term.attrFuncDef();

		// add call dependency
		if (! (calledFunc instanceof NativeFunc)) {
			prog.addCallDependency(func, names.getFunction(calledFunc));
		}


		// translate Arguments:
		int argCount = term.getArgs().size();

		final PscriptType[] argumentTypes = new PscriptType[argCount];
		final ILvar[] argumentVars = new ILvar[argCount];


		for (int i = 0; i < argCount; i++) {
			Expr arg = term.getArgs().get(i);
			WParameter param = calledFunc.getSignature().getParameters().get(i);
			argumentTypes[i] = param.attrTyp();

			argumentVars[i] = names.getNewLocalVar(func, argumentTypes[i], calledFunc.getSignature().getName() + "_param" + i);
			result.addAll(translateExpr(func, argumentVars[i], arg));
		}
		// make the call:
		calledFunc.match(new FunctionDefinition.MatcherVoid() {

			@Override
			public void case_FuncDef(FuncDef term)  {
				result.add(new ILfunctionCall(resultVar, names.getFunction(calledFunc).getName(), argumentTypes, argumentVars));
				
			}

			@Override
			public void case_NativeFunc(NativeFunc term)  {
				result.add(new IlbuildinFunctionCall(resultVar, term.getSignature().getName(), argumentVars));
				
			}
		});

		return result;
	}





	protected List<ILStatement> translateExpr(final ILfunction func, final ILvar resultVar, Expr expr) {
		final List<ILStatement> result = new NotNullList<ILStatement>();
		return expr.match(new Expr.Matcher<List<ILStatement>>() {

			@Override
			public List<ILStatement> case_ExprIntVal(ExprIntVal term)  {
				result.add(new IlsetConst(resultVar, new ILconstInt(term.getVal())));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprRealVal(ExprRealVal term)  {
				result.add(new IlsetConst(resultVar, new ILconstNum(term.getVal())));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprStringVal(ExprStringVal term)  {
				result.add(new IlsetConst(resultVar, new ILconstString(term.getVal())));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprBoolVal(ExprBoolVal term)  {
				result.add(new IlsetConst(resultVar, new ILconstBool(term.getVal())));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprFuncRef(ExprFuncRef term)  {
				FunctionDefinition f = term.attrFuncDef();
				ILfunction ilfunc = names.getFunction(f);
				result.add(new IlsetConst(resultVar, new ILconstFuncRef(ilfunc)));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprVarAccess(ExprVarAccess term)  {

				VarDef varDef = term.attrVarDef();
				ILvar var = names.getILvarForVarDef(varDef);
				if (isMemberVar(varDef)) {
					ILvar thisVar = new ILvar("this", PScriptTypeInt.instance());
					result.add(new ILsetVarArray(resultVar, var, thisVar ));
				} else { // normal var:
					result.add(new ILsetVar(resultVar, var));
				}
				return result;
			}

			@Override
			public List<ILStatement> case_ExprVarArrayAccess(ExprVarArrayAccess arAccess)  {
				VarDef varDef = arAccess.attrVarDef();
				PscriptType type = varDef.attrTyp();
				if (type instanceof PScriptTypeArray) {
					PScriptTypeArray typeA = (PScriptTypeArray) type;
					ILvar indexResult = names.getNewLocalVar(func, PScriptTypeInt.instance(), "index");
					result.addAll(calculateIndexes(func, typeA, indexResult, arAccess.getIndexes()));


					ILvar arVar = names.getILvarForVarDef(varDef);
					result.add(new ILsetVarArray(resultVar, arVar, indexResult));
					return result;
				} else {
					attr.addError(arAccess.getSource(), "Variable " + varDef.getName() + " is not an array.");
					return new NotNullList<ILStatement>();
				}
			}

			@Override
			public List<ILStatement> case_ExprThis(ExprThis term)  {
				ILvar var = getThisVariableForMethod(func);
				result.add(new ILsetVar(resultVar, var));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprBinary(ExprBinary term)  {

				if (term.getOp() instanceof OpAnd) {
					result.addAll(translateExpr(func, resultVar, term.getLeft()));
					result.add(new ILif(resultVar, translateExpr(func, resultVar, term.getRight()), new LinkedList<ILStatement>()));
				} else if (term.getOp() instanceof OpOr) {
					result.addAll(translateExpr(func, resultVar, term.getLeft()));
					result.add(new ILif(resultVar, new LinkedList<ILStatement>(), translateExpr(func, resultVar, term.getRight())));
				} else {
					// evaluate left expr:
					PscriptType leftType = term.getLeft().attrTyp();
					ILvar leftVar = names.getNewLocalVar(func, leftType, "leftOperand");
					List<ILStatement> leftExpr = translateExpr(func, leftVar, term.getLeft());
					// evaluate right expr:
					PscriptType rightType = term.getLeft().attrTyp();
					ILvar rightVar = names.getNewLocalVar(func, rightType, "rightOperand");
					List<ILStatement> rightExpr = translateExpr(func, rightVar, term.getRight());

					result.addAll(leftExpr);
					result.addAll(rightExpr);
					Iloperator op;
					if (Utils.isJassCode(term)) {
						op = translateOpJass(term.getOp(), leftType, rightType);
					} else {
						op = translateOp(term.getOp());
					}

					result.add(new ILsetBinary(resultVar, leftVar, op, rightVar));
				}
				return result;
			}

			@Override
			public List<ILStatement> case_ExprUnary(final ExprUnary term)  {
				PscriptType type = term.getRight().attrTyp();
				ILvar tempVar = names.getNewLocalVar(func, type, "temp");
				result.addAll(translateExpr(func, tempVar, term.getRight()));
				result.add(new IlsetUnary(resultVar, translateOp(term.getOp()), tempVar));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprMemberVar(ExprMemberVar term)  {
				VarDef varDef = term.attrVarDef();
				ILvar varDefVar = names.getILvarForClassMemberDef((GlobalVarDef) varDef);

				ILvar index = names.getNewLocalVar(func, PScriptTypeInt.instance(), "index");
				result.addAll(translateExpr(func, index, term.getLeft()));
				result.add(new ILsetVarArray(resultVar, varDefVar, index));

				return result;
			}

			@Override
			public List<ILStatement> case_ExprMemberArrayVar(ExprMemberArrayVar term)  {
				// TODO member arrayvar expr
				throw new Error("Array member vars not implemented yet.");
			}

			@Override
			public List<ILStatement> case_ExprMemberMethod(ExprMemberMethod term)  {
				return translateExprMemberMethod(func, resultVar, term);
			}

			@Override
			public List<ILStatement> case_ExprFunctionCall(ExprFunctionCall term)  {
				return translateFunctionCall(func, resultVar, term, term.getArgs());
			}

			@Override
			public List<ILStatement> case_ExprNewObject(ExprNewObject term)  {
				return translateExprNewPos(func, resultVar, term);
			}

			@Override
			public List<ILStatement> case_ExprNull(ExprNull term)  {
				result.add(new IlsetConst(resultVar, new ILconstNull()));
				return result;
			}

			@Override
			public List<ILStatement> case_ExprCast(ExprCast term)  {
				// a cast expression does not do anyting
				return translateExpr(func, resultVar, term.getExpr());
			}

		});
	}


	protected List<ILStatement> translateExprNewPos(ILfunction func, ILvar resultVar, ExprNewObject term) {
		List<ILStatement> result = new NotNullList<ILStatement>();

		ConstructorDef constr = term.attrConstructorDef();
		
		
		ILfunction constrFunc = names.getConstructorFunction(constr);

		// translate arguments

		PscriptType[] argumentTypes = constrFunc.getParamTypes();
		ILvar[] argumentVars = new ILvar[argumentTypes.length];
		for (int i=0; i<argumentTypes.length; i++) {
			argumentVars[i] = names.getNewLocalVar(func, argumentTypes[i], "contr_arg");
			result.addAll(translateExpr(func, argumentVars[i], term.getArgs().get(i)));
		}
		result.add(new ILfunctionCall(resultVar, constrFunc.getName(), argumentTypes, argumentVars));
		return result ;
	}

	protected Iloperator translateOpJass(Op op, PscriptType leftType, PscriptType rightType) {
		if (op instanceof OpDivReal) {
			if (leftType instanceof PScriptTypeInt && rightType instanceof PScriptTypeInt) {
				return Iloperator.DIV_INT;
			}
		}
		return translateOp(op);
	}

	protected Iloperator translateOp(Op op) {
		return op.match(new Op.Matcher<Iloperator>() {

			@Override
			public Iloperator case_OpOr(OpOr term)  {
				return Iloperator.OR;
			}

			@Override
			public Iloperator case_OpAnd(OpAnd term)  {
				return Iloperator.AND;
			}

			@Override
			public Iloperator case_OpEquals(OpEquals term)  {
				return Iloperator.EQUALITY;
			}

			@Override
			public Iloperator case_OpUnequals(OpUnequals term)  {
				return Iloperator.UNEQUALITY;
			}

			@Override
			public Iloperator case_OpLessEq(OpLessEq term)  {
				return Iloperator.LESS_EQ;
			}

			@Override
			public Iloperator case_OpLess(OpLess term)  {
				return Iloperator.LESS;
			}

			@Override
			public Iloperator case_OpGreaterEq(OpGreaterEq term)  {
				return Iloperator.GREATER_EQ;
			}

			@Override
			public Iloperator case_OpGreater(OpGreater term)  {
				return Iloperator.GREATER;
			}

			@Override
			public Iloperator case_OpPlus(OpPlus term)  {
				return Iloperator.PLUS;
			}

			@Override
			public Iloperator case_OpMinus(OpMinus term)  {
				return Iloperator.MINUS;
			}

			@Override
			public Iloperator case_OpMult(OpMult term)  {
				return Iloperator.MULT;
			}

			@Override
			public Iloperator case_OpDivReal(OpDivReal term)  {
				return Iloperator.DIV_REAL;
			}

			@Override
			public Iloperator case_OpModReal(OpModReal term)  {
				return Iloperator.MOD_REAL;
			}

			@Override
			public Iloperator case_OpModInt(OpModInt term)  {
				return Iloperator.MOD_INT;
			}

			@Override
			public Iloperator case_OpDivInt(OpDivInt term)  {
				return Iloperator.DIV_INT;
			}

			@Override
			public Iloperator case_OpNot(OpNot term)  {
				return Iloperator.NOT;
			}
		});
	}

	protected ILvar getThisVariableForMethod(ILfunction func) {
		return new ILvar("this", PScriptTypeInt.instance());
	}

	protected void translateGlobalVarDef(WPackage p, GlobalVarDef term) {
		ILvar v = names.getILvarForVarDef(term);
		prog.addGlobalVar(v);
		// TODO inital value + global dependencies
		if (term.getInitialExpr() instanceof Expr) {
			Expr initialExpr = (Expr) term.getInitialExpr();

			
			
			List<ExprVarAccess> varRefs = Utils.collect(ExprVarAccess.class, initialExpr);
			for (ExprVarAccess varRef : varRefs) {
				ILvar dependsOn = names.getILvarForVarDef(varRef.attrVarDef());
				addGlobalInitializationDependency(v, dependsOn);
			}


			addGlobalInit(v, initialExpr);
		}
	}



	private void addGlobalInit(ILvar v, Expr initialExpr) {
		globalInits.add(v, initialExpr);
	}

	private void addGlobalInitializationDependency(ILvar v, ILvar dependsOn) {
		globalInits.addDependency(v, dependsOn);
	}

	protected void transltateClassDef(final WPackage pack, final ClassDef classDef) {
		String packageName = pack.getName();
		String className = classDef.getName();
		String prefix = packageName + "_" + className + "_";

		final List<GlobalVarDef> classVars = new NotNullList<GlobalVarDef>();
		final List<ConstructorDef> constructors = new NotNullList<ConstructorDef>();
		final ILfunction destroyFunc = names.getDestroyFunction(classDef);
		prog.addFunction(destroyFunc);
		destroyFunc.initParams();
		destroyFunc.addParam(new ILvar("this", PScriptTypeInt.instance()));
		destroyFunc.setReturnType(PScriptTypeVoid.instance());


		for (ClassSlot elem : classDef.getSlots()) {
			elem.match(new ClassSlot.MatcherVoid() {

				@Override
				public void case_GlobalVarDef(GlobalVarDef varDef)  {
					ILvar v = names.getILvarForClassMemberDef(varDef);
					prog.addGlobalVar(v);
					classVars.add(varDef);
					
				}

				@Override
				public void case_FuncDef(FuncDef term)  {
					ILfunction func = names.getFunction(term);
					func.setReturnType(term.getSignature().getTyp().attrTyp());
					func.initParams();
					// add the implicit parameter "this"
					func.addParam(names.getThis(term));
					// translate other parameters:
					for (WParameter p : term.getSignature().getParameters()) {
						func.addParam(new ILvar(p.getName(), p.getTyp().attrTyp()));
					}
					func.addBody(translateStatements(func, term.getBody()));
					prog.addFunction(func);
					
				}

				@Override
				public void case_ConstructorDef(ConstructorDef term)  {
					constructors.add(term);
					
				}

				@Override
				public void case_OnDestroyDef(OnDestroyDef term)  {
					destroyFunc.addBody(translateStatements(destroyFunc, term.getBody()));			
					
				}
			});
		}

		// create global variables needed for management
		ILvar nextFree = new ILvar(names.getNewName(prefix + "nextFree"), new PScriptTypeArray(PScriptTypeInt.instance(), Utils.array(1)));
		prog.addGlobalVar(nextFree);
		ILvar firstFree = new ILvar(names.getNewName(prefix + "firstFree"), PScriptTypeInt.instance());
		prog.addGlobalVar(firstFree);
		ILvar maxIndex = new ILvar(names.getNewName(prefix + "maxIndex"), PScriptTypeInt.instance());
		prog.addGlobalVar(maxIndex);


		// create constructors
		for (ConstructorDef constr : constructors) {
			translateConstructor(nextFree, firstFree, maxIndex, classVars, constr);
		}

		// finish destroy function
		finishDestroyFunc(nextFree, firstFree, maxIndex, destroyFunc);

	}

	private void translateConstructor(ILvar nextFree, ILvar firstFree, ILvar maxIndex, List<GlobalVarDef> classVars, ConstructorDef constr) {
		ILfunction func = names.getConstructorFunction(constr);
		prog.addFunction(func);

		func.initParams();
		for (WParameter p : constr.getParams()) {
			ILvar param = names.getILvarForVarDef(p);
			func.addParam(param);
		}
		func.setReturnType(PScriptTypeInt.instance());
		ILvar thisVar = new ILvar("this", PScriptTypeInt.instance());
		func.addLocalVar(thisVar);


		List<ILStatement> statements = new NotNullList<ILStatement>();
		// if has free indexes (firstFree > 0)
		ILvar hasFree = names.getNewLocalVar(func, PScriptTypeBool.instance(), "hasFree");
		statements.add(new ILsetBinaryCR(hasFree, firstFree, Iloperator.GREATER, new ILconstInt(0)));
		List<ILStatement> thenBlock = new NotNullList<ILStatement>();
		// then
		// 		this = firstFree 
		thenBlock.add(new ILsetVar(thisVar, firstFree));
		//		firstFree = nextFree[this]
		thenBlock.add(new ILsetVarArray(firstFree, nextFree, thisVar));
		// else
		List<ILStatement> elseBlock = new NotNullList<ILStatement>();
		// 		maxIndex = maxIndex + 1
		elseBlock.add(new ILsetBinaryCR(maxIndex, maxIndex, Iloperator.PLUS, new ILconstInt(1)));
		// 		this = maxIndex
		elseBlock.add(new ILsetVar(thisVar, maxIndex));
		statements.add(new ILif(hasFree, thenBlock, elseBlock));
		// endif
		// nextFree[this] = -1
		ILvar minusOne = names.getNewLocalVar(func, PScriptTypeInt.instance(), "minusOne");
		statements.add(new IlsetConst(minusOne , new ILconstInt(-1)));
		statements.add(new ILarraySetVar(nextFree, thisVar, minusOne));

		// initialize member vars
		for (GlobalVarDef classVar : classVars) {
			PscriptType varType = classVar.attrTyp();
			ILvar tempVar = names.getNewLocalVar(func, varType, "temp");
			if (classVar.getInitialExpr() instanceof Expr) {
				Expr initial = (Expr) classVar.getInitialExpr();
				statements.addAll(translateExpr(func, tempVar , initial));
			} else {
				statements.add(new IlsetConst(tempVar, NativeTypes.getDefaultValue(varType)));
			}
			statements.add(new ILarraySetVar(names.getILvarForClassMemberDef(classVar), thisVar, tempVar));
		}
		func.addBody(statements );

		// translate body
		func.addBody(translateStatements(func, constr.getBody()));
		// return this
		func.addBody(Utils.list(new ILreturn(thisVar)));
	}

	private void finishDestroyFunc(ILvar nextFree, ILvar firstFree, ILvar maxIndex, ILfunction destroyFunc) {

		/* the following code would look like this in nice syntax:
		 if (nextFree[this] < 0) {
		  error
		 } else {
		 	nextFree[this] = firstFree
		 	firstFree = this
		 }
		 */
		List<ILStatement> statements = new NotNullList<ILStatement>();
		ILvar varThis = new ILvar("this", PScriptTypeInt.instance());

		ILvar doubleFree = names.getNewLocalVar(destroyFunc, PScriptTypeBool.instance(), "doubleFree");
		ILvar left = names.getNewLocalVar(destroyFunc, PScriptTypeInt.instance(), "lef");
		statements.add(new ILsetVarArray(left, nextFree, varThis));
		statements.add(new ILsetBinaryCR(doubleFree, left, Iloperator.GREATER_EQ, new ILconstInt(0)));
		List<ILStatement> thenBlock = new NotNullList<ILStatement>();
		ILvar msg = names.getNewLocalVar(destroyFunc, PScriptTypeString.instance(), "msg");
		thenBlock.add(new IlsetConst(msg, new ILconstString("double free")));
		thenBlock.add(new ILerror(msg));		
		List<ILStatement> elseBlock = new NotNullList<ILStatement>();
		elseBlock.add(new ILarraySetVar(nextFree, varThis, firstFree));
		elseBlock.add(new ILsetVar(firstFree, varThis));
		statements.add(new ILif(doubleFree, thenBlock, elseBlock));

		destroyFunc.addBody(statements);
	}

	/**
	 * calculate the index for an array
	 * @param func current function
	 * @param type type of the array (stores information about dimensions and about where arraysizes can be found)
	 * @param indexResult the variable where to store the final one dimensional index to
	 * @param indexesPos the indexes
	 * @return
	 */
	protected List<ILStatement> calculateIndexes(ILfunction func, PScriptTypeArray type, ILvar indexResult, Indexes indexesPos) {
		if (indexesPos.size() > 1) {
			throw new Error("Multidimensional arrays are not supported yet.");
		}

		LinkedList<ILStatement> result = new LinkedList<ILStatement>();
		result.addAll(translateExpr(func, indexResult, indexesPos.get(0)));			
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
