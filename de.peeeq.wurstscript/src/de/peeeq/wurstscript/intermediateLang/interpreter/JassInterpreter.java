package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.peeeq.wurstscript.intermediateLang.ILStatement;
import de.peeeq.wurstscript.intermediateLang.ILarraySetVar;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstAddable;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstNum;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.ILexitwhen;
import de.peeeq.wurstscript.intermediateLang.ILfunction;
import de.peeeq.wurstscript.intermediateLang.ILfunctionCall;
import de.peeeq.wurstscript.intermediateLang.ILif;
import de.peeeq.wurstscript.intermediateLang.ILloop;
import de.peeeq.wurstscript.intermediateLang.ILprog;
import de.peeeq.wurstscript.intermediateLang.ILreturn;
import de.peeeq.wurstscript.intermediateLang.ILsetBinary;
import de.peeeq.wurstscript.intermediateLang.ILsetBinaryCL;
import de.peeeq.wurstscript.intermediateLang.ILsetBinaryCR;
import de.peeeq.wurstscript.intermediateLang.ILsetVar;
import de.peeeq.wurstscript.intermediateLang.ILsetVarArray;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.intermediateLang.IlbuildinFunctionCall;
import de.peeeq.wurstscript.intermediateLang.Iloperator;
import de.peeeq.wurstscript.intermediateLang.IlsetConst;
import de.peeeq.wurstscript.intermediateLang.IlsetUnary;
import de.peeeq.wurstscript.intermediateLang.translator.ILerror;
import de.peeeq.wurstscript.jassAst.JassArrayVar;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprBinary;
import de.peeeq.wurstscript.jassAst.JassExprBoolVal;
import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprIntVal;
import de.peeeq.wurstscript.jassAst.JassExprNull;
import de.peeeq.wurstscript.jassAst.JassExprRealVal;
import de.peeeq.wurstscript.jassAst.JassExprStringVal;
import de.peeeq.wurstscript.jassAst.JassExprUnary;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassOpAnd;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassOpDiv;
import de.peeeq.wurstscript.jassAst.JassOpEquals;
import de.peeeq.wurstscript.jassAst.JassOpGreater;
import de.peeeq.wurstscript.jassAst.JassOpGreaterEq;
import de.peeeq.wurstscript.jassAst.JassOpLess;
import de.peeeq.wurstscript.jassAst.JassOpLessEq;
import de.peeeq.wurstscript.jassAst.JassOpMinus;
import de.peeeq.wurstscript.jassAst.JassOpMult;
import de.peeeq.wurstscript.jassAst.JassOpNot;
import de.peeeq.wurstscript.jassAst.JassOpOr;
import de.peeeq.wurstscript.jassAst.JassOpPlus;
import de.peeeq.wurstscript.jassAst.JassOpUnary;
import de.peeeq.wurstscript.jassAst.JassOpUnequals;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.jassAst.JassStmtExitwhen;
import de.peeeq.wurstscript.jassAst.JassStmtIf;
import de.peeeq.wurstscript.jassAst.JassStmtLoop;
import de.peeeq.wurstscript.jassAst.JassStmtReturn;
import de.peeeq.wurstscript.jassAst.JassStmtReturnVoid;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassStmtSetArray;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeHandle;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.utils.Utils;

public class JassInterpreter {

	private JassProg prog;
	private static ExitwhenException staticExitwhenException = new ExitwhenException();
	private static ReturnException staticReturnException = new ReturnException(null);
	private Map<String, ILconst> globalVarMap;
	private boolean trace = false;

	public void LoadProgram(JassProg prog) {
		this.prog = prog;
		globalVarMap = new HashMap<String, ILconst>();

		List<JassVar> globals = prog.getGlobals();
		// globals initialisieren
		for (JassVar v : globals) {
			ILconst value = null;
			if (v instanceof JassArrayVar) {
				value = new JassArray(v.getType());
			} else {
				value = getDefaultValue(v.getType());
			}
			globalVarMap.put(v.getName(), value );
		}

	}

	public static ILconst getDefaultValue(String type) {
		if (type.equals("integer")) {
			return new ILconstInt(0);
		}else if (type.equals("boolean")) {
			return ILconstBool.FALSE;
		}else if (type.equals("real")) {
			return new ILconstReal(0.0f);
		}else {
			return new ILconstNull();
		}
	}
	
	public ILconst executeFunction(String name, ILconst... arguments) {
		if (trace) {
			System.out.println("#trace: " + name + "( "  + Utils.join(arguments, ", ") + ")");
		}



		JassFunction func = searchFunction(name);
		List<JassVar> locals = func.getLocals();


		List<JassStatement> body = func.getBody();

		Map<String, ILconst> localVarMap = new HashMap<String, ILconst>();
		// locals initialisieren
		for (JassVar v : locals) {
			ILconst value = null;
			if (v instanceof JassArrayVar) {
				value = new JassArray(v.getType());
			}
			localVarMap.put(v.getName(), value );
		}

		
		if (func.getParams().size() != arguments.length) {
			throw new Error("Wrong number of parameters: " + func.getParams().size() + " != " + arguments.length);
		}
		int i = 0;
		for (JassSimpleVar v : func.getParams()) {
			localVarMap.put(v.getName(), arguments[i] );
			i++;
		}
		
		try {
			this.executeStatements(localVarMap, body);
		} catch (ReturnException e) {
			if (trace) {
				System.out.println("#trace: end function " + name + " returns " + e.getVal());
			}
			return e.getVal();
		}

		if (trace) {
			System.out.println("#trace: end function " + name);
		}
		return null; 
	}

	private void executeStatements(Map<String, ILconst> localVarMap, List<JassStatement> body) {
		for (JassStatement s : body) {
			executeStatement(localVarMap, s);
		}
	}


	private void executeStatement(final Map<String, ILconst> localVarMap, JassStatement s) {
		s.match(new JassStatement.MatcherVoid() {
			
			@Override
			public void case_JassStmtSetArray(JassStmtSetArray jassStmtSetArray) {
				ILconst right = executeExpr(localVarMap, jassStmtSetArray.getRight());
				ILconstInt index = (ILconstInt) executeExpr(localVarMap, jassStmtSetArray.getIndex());
				JassArray v = (JassArray) getVarValue(localVarMap, jassStmtSetArray.getLeft());
				v.set(index.getVal(), right);
			}
			
			@Override
			public void case_JassStmtSet(JassStmtSet jassStmtSet) {
				ILconst right = executeExpr(localVarMap, jassStmtSet.getRight());
				setVarValue(localVarMap, jassStmtSet.getLeft(), right);
			}
			
			@Override
			public void case_JassStmtReturnVoid(JassStmtReturnVoid jassStmtReturnVoid) {
				throw staticReturnException.setVal(null);
			}
			
			@Override
			public void case_JassStmtReturn(JassStmtReturn jassStmtReturn) {
				ILconst c = executeExpr(localVarMap, jassStmtReturn.getReturnValue());
				throw staticReturnException.setVal(c);
			}
			
			@Override
			public void case_JassStmtLoop(JassStmtLoop jassStmtLoop) {
				try {
					while (true) {
						executeStatements(localVarMap, jassStmtLoop.getBody());
					}
				} catch (ExitwhenException e) {
					// end loop
				}
			}
			
			@Override
			public void case_JassStmtIf(JassStmtIf jassStmtIf) {
				ILconstBool cond = (ILconstBool) executeExpr(localVarMap, jassStmtIf.getCond());
				if (cond.getVal()) {
					executeStatements(localVarMap, jassStmtIf.getThenBlock());
				} else {
					executeStatements(localVarMap, jassStmtIf.getElseBlock());
				}
			}
			
			@Override
			public void case_JassStmtExitwhen(JassStmtExitwhen jassStmtExitwhen) {
				ILconstBool cond = (ILconstBool) executeExpr(localVarMap, jassStmtExitwhen.getCond());
				if (cond.getVal()) {
					throw staticExitwhenException;
				}
			}
			
			@Override
			public void case_JassStmtCall(JassStmtCall jassStmtCall) {
				ILconst[] args = new ILconst[jassStmtCall.getArguments().size()];
				int i = 0;
				for (JassExpr arg : jassStmtCall.getArguments()) {
					args[i] = executeExpr(localVarMap, arg);
					i++;
				}
				executeFunction(jassStmtCall.getFunctionName(), args);
			}
		});
		
		
//		if (s instanceof ILif) {
//			translateStatementIf(localVarMap, (ILif)s);
//		} else if (s instanceof ILsetVar) {
//			translateIlcopy( localVarMap, (ILsetVar)s);
//		} else if (s instanceof IlsetConst) {
//			translateIlsetConst( localVarMap, (IlsetConst)s);
//		} else if (s instanceof ILsetBinary) {
//			translateIlbinary( localVarMap, (ILsetBinary)s);
//		} else if (s instanceof IlsetUnary) {
//			translateIlunary(localVarMap, (IlsetUnary) s);
//		} else if (s instanceof ILreturn) {
//			translateReturn(localVarMap, (ILreturn) s);
//		} else if (s instanceof ILloop) {
//			translateLoop(localVarMap, (ILloop) s);
//		} else if (s instanceof ILexitwhen) {
//			translateExitwhen(localVarMap, (ILexitwhen) s);
//		} else if (s instanceof ILfunctionCall) {
//			translateFunctionCall(localVarMap, (ILfunctionCall) s);
//		} else if (s instanceof IlbuildinFunctionCall) {
//			translateBuildinFunction(localVarMap, (IlbuildinFunctionCall) s);
//		} else if (s instanceof ILsetBinaryCR) {
//			translateILsetBinaryCR(localVarMap, (ILsetBinaryCR) s);
//		} else if (s instanceof ILsetBinaryCL) {
//			translateILsetBinaryCL(localVarMap, (ILsetBinaryCL) s);
//		} else if (s instanceof ILarraySetVar) {
//			translateILarraySetVar(localVarMap, (ILarraySetVar) s);
//		} else if (s instanceof ILsetVarArray) {
//			translateILsetVarArray(localVarMap, (ILsetVarArray) s);
//		} else if (s instanceof ILerror) {
//			throw new Error("IL execution error: " + lookupVarValue(localVarMap, ((ILerror) s).msg));
//		} else {
//
//			throw new Error("not implemented " + s);
//		}
	}

	

	private ILconst executeExpr(final Map<String, ILconst> localVarMap, JassExpr expr) {
		return expr.match(new JassExpr.Matcher<ILconst>() {

			@Override
			public ILconst case_JassExprVarArrayAccess(JassExprVarArrayAccess e) {
				JassArray ar =  (JassArray) getVarValue(localVarMap, e.getVarName());
				ILconstInt index = (ILconstInt) executeExpr(localVarMap, e.getIndex());
				return ar.get(index.getVal());
			}

			@Override
			public ILconst case_JassExprRealVal(JassExprRealVal e) {
				return new ILconstReal(e.getVal());
			}

			@Override
			public ILconst case_JassExprUnary(JassExprUnary e) {
				final ILconst right = executeExpr(localVarMap, e.getRight());
				return e.getOp().match(new JassOpUnary.Matcher<ILconst>() {

					@Override
					public ILconst case_JassOpNot(JassOpNot jassOpNot) {
						ILconstBool b = (ILconstBool) right;
						return b.negate();
					}

					@Override
					public ILconst case_JassOpMinus(JassOpMinus jassOpMinus) {
						return ((ILconstNum) right).negate();
					}
					
				});
			}

			@Override
			public ILconst case_JassExprFuncRef(JassExprFuncRef e) {
				return new ILconstFuncRef(e.getFuncName());
			}

			@Override
			public ILconst case_JassExprBoolVal(JassExprBoolVal e) {
				return ILconstBool.instance(e.getVal());
			}

			@Override
			public ILconst case_JassExprBinary(final JassExprBinary e) {
				return e.getOp().match(new JassOpBinary.Matcher<ILconst>() {

					ILconst getLeft() {
						return  executeExpr(localVarMap, e.getLeft());
					}
					
					ILconstNum getLeftNum() {
						return (ILconstNum) getLeft();
					}
					
					ILconstAddable getLeftAddable() {
						return (ILconstAddable) getLeft();
					}
					
					ILconstBool getLeftBool() {
						return (ILconstBool) getLeft();
					}
					
					ILconst getRight() {
						return  executeExpr(localVarMap, e.getRight());
					}
					
					ILconstNum getRightNum() {
						return (ILconstNum) getRight();
					}
					
					ILconstAddable getRightAddable() {
						return (ILconstAddable) getRight();
					}
					
					ILconstBool getRightBool() {
						return (ILconstBool) getRight();
					}
					
					
					@Override
					public ILconst case_JassOpDiv(JassOpDiv jassOpDiv) {
						return getLeftNum().div(getRightNum());
					}

					@Override
					public ILconst case_JassOpLess(JassOpLess jassOpLess) {
						return getLeftNum().less(getRightNum());
					}

					@Override
					public ILconst case_JassOpAnd(JassOpAnd jassOpAnd) {
						if (getLeftBool().getVal()) {
							return getRightBool();
						} else {
							return ILconstBool.FALSE;
						}
						
					}

					@Override
					public ILconst case_JassOpUnequals(JassOpUnequals jassOpUnequals) {
						return ILconstBool.instance(! getLeft().isEqualTo(getRight()));
					}

					@Override
					public ILconst case_JassOpGreaterEq(JassOpGreaterEq jassOpGreaterEq) {
						return getLeftNum().greaterEq(getRightNum());
					}

					@Override
					public ILconst case_JassOpMinus(JassOpMinus jassOpMinus) {
						return getLeftNum().sub(getRightNum());
					}

					@Override
					public ILconst case_JassOpMult(JassOpMult jassOpMult) {
						return getLeftNum().mul(getRightNum());
					}

					@Override
					public ILconst case_JassOpGreater(JassOpGreater jassOpGreater) {
						return getLeftNum().greater(getRightNum());
					}

					@Override
					public ILconst case_JassOpPlus(JassOpPlus jassOpPlus) {
						return getLeftAddable().add(getRightAddable());
					}

					@Override
					public ILconst case_JassOpLessEq(JassOpLessEq jassOpLessEq) {
						return getLeftNum().lessEq(getRightNum());
					}

					@Override
					public ILconst case_JassOpOr(JassOpOr jassOpOr) {
						if (getLeftBool().getVal()) {
							return ILconstBool.TRUE;
						} else {
							return getRightBool();
						}
					}

					@Override
					public ILconst case_JassOpEquals(JassOpEquals jassOpEquals) {
						return ILconstBool.instance( getLeft().isEqualTo(getRight()));
					}
					
				});
			}

			@Override
			public ILconst case_JassExprStringVal(JassExprStringVal e) {
				return new ILconstString(e.getVal());
			}

			@Override
			public ILconst case_JassExprIntVal(JassExprIntVal e) {
				return ILconstInt.create(e.getVal());
			}

			@Override
			public ILconst case_JassExprFunctionCall(JassExprFunctionCall e) {
				ILconst[] args = new ILconst[e.getArguments().size()];
				int i = 0;
				for (JassExpr arg : e.getArguments()) {
					args[i] = executeExpr(localVarMap, arg);
					i++;
				}
				return executeFunction(e.getFuncName(), args);
			}

			@Override
			public ILconst case_JassExprVarAccess(JassExprVarAccess e) {
				return getVarValue(localVarMap, e.getVarName());
			}

			@Override
			public ILconst case_JassExprNull(JassExprNull e) {
				return new ILconstNull();
			}
			
		});
	}




	private void setVarValue(Map<String, ILconst> localVarMap, String varName, ILconst s ) {
		if (isLocal(localVarMap, varName)) {
			localVarMap.put(varName, s);
		} else if (isGlobal( varName )) {
			globalVarMap.put(varName, s);
		} else {
			throw new Error("var " + varName + " is neither local nor global?");
		}
	}


	private boolean isLocal(Map<String, ILconst> localVarMap, String varName) {
		return localVarMap.containsKey(varName);

	}

	private boolean isGlobal( String varName) {
		return globalVarMap.containsKey(varName);

	}

	private ILconst getVarValue(Map<String, ILconst> localVarMap, String name) {
		ILconst value = localVarMap.get(name);
		if (value == null) {
			value = globalVarMap.get(name);
			if (value == null) {
				throw new Error("Variable " + name + " not found.");
			}
		}
		return value;
	}

	private JassFunction searchFunction(String name) {
		for (JassFunction f : prog.getFunctions()) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		throw new Error("Function " + name + " not found.");
	}

	public void trace(boolean b) {
		trace = b;
	}

}
