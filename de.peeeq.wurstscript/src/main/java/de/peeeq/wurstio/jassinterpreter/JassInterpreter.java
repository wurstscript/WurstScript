package de.peeeq.wurstio.jassinterpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.TimerMockHandler;
import de.peeeq.wurstscript.jassAst.*;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassinterpreter.ExitwhenException;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JassInterpreter implements AbstractInterpreter {

    private JassProg prog;
    private static ReturnException staticReturnException = new ReturnException(null);
    private Map<String, ILconst> globalVarMap;
    private boolean trace = false;
    private Map<String, ExecutableJassFunction> functionCache = new HashMap<>();
    private final TimerMockHandler timerMockHandler = new TimerMockHandler();

    public void loadProgram(JassProg prog) {
        this.prog = prog;
        globalVarMap = Maps.newLinkedHashMap();

        List<JassVar> globals = prog.getGlobals();
        // globals initialisieren
        for (JassVar v : globals) {
            ILconst value = null;
            if (v instanceof JassArrayVar) {
                value = new JassArray(v.getType());
            } else {
                // --- not initialized
            }
            globalVarMap.put(v.getName(), value);
        }

    }

    public static ILconst getDefaultValue(String type) {
        switch (type) {
            case "integer":
                return new ILconstInt(0);
            case "boolean":
                return ILconstBool.FALSE;
            case "real":
                return new ILconstReal(0.0f);
            default:
                return ILconstNull.instance();
        }
    }

    public ILconst executeFunction(String name, ILconst... arguments) {
        if (trace) {
            WLogger.trace(name + "( " + Utils.join(arguments, ", ") + ")");
        }

        ExecutableJassFunction func = searchFunction(name);
        return func.execute(this, arguments);

    }

    @Nullable
    ILconst executeJassFunction(JassFunction func, ILconst... arguments) {
        List<JassVar> locals = func.getLocals();


        List<JassStatement> body = func.getBody();

        Map<String, ILconst> localVarMap = Maps.newLinkedHashMap();
        // locals initialisieren
        for (JassVar v : locals) {
            ILconst value = null;
            if (v instanceof JassArrayVar) {
                value = new JassArray(v.getType());
            }
            localVarMap.put(v.getName(), value);
        }


        if (func.getParams().size() != arguments.length) {
            throw new InterpreterException("Wrong number of parameters: " + func.getParams().size() + " != " + arguments.length);
        }
        int i = 0;
        for (JassSimpleVar v : func.getParams()) {
            localVarMap.put(v.getName(), arguments[i]);
            i++;
        }

        try {
            this.executeStatements(localVarMap, body);
        } catch (ReturnException e) {
            if (trace) {
                WLogger.trace("end function " + func.getName() + " returns " + e.getVal());
            }
            return e.getVal();
        }

        if (trace) {
            WLogger.trace("end function " + func.getName());
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
                    throw ExitwhenException.instance();
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
                executeFunction(jassStmtCall.getFuncName(), args);
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
//			throw new InterpreterException("IL execution error: " + lookupVarValue(localVarMap, ((ILerror) s).msg));
//		} else {
//
//			throw new InterpreterException("not implemented " + s);
//		}
    }


    private ILconst executeExpr(final Map<String, ILconst> localVarMap, JassExpr expr) {
        return expr.match(new JassExpr.Matcher<ILconst>() {

            @Override
            public ILconst case_JassExprVarArrayAccess(JassExprVarArrayAccess e) {
                JassArray ar = (JassArray) getVarValue(localVarMap, e.getVarName());
                ILconstInt index = (ILconstInt) executeExpr(localVarMap, e.getIndex());
                return ar.get(index.getVal());
            }

            @Override
            public ILconst case_JassExprRealVal(JassExprRealVal e) {
                return new ILconstReal(e.getValR());
            }

            @Override
            public ILconst case_JassExprUnary(JassExprUnary e) {
                final ILconst right = executeExpr(localVarMap, e.getRight());
                return e.getOpU().match(new JassOpUnary.Matcher<ILconst>() {

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
                return ILconstBool.instance(e.getValB());
            }

            @Override
            public ILconst case_JassExprBinary(final JassExprBinary e) {
                return e.getOp().match(new JassOpBinary.Matcher<ILconst>() {

                    ILconst getLeft() {
                        return executeExpr(localVarMap, e.getLeftExpr());
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
                        return executeExpr(localVarMap, e.getRight());
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
                        return ILconstBool.instance(!getLeft().isEqualTo(getRight()));
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
                        return ILconstBool.instance(getLeft().isEqualTo(getRight()));
                    }

                });
            }

            @Override
            public ILconst case_JassExprStringVal(JassExprStringVal e) {
                return new ILconstString(e.getValS());
            }

            @Override
            public ILconst case_JassExprIntVal(JassExprIntVal e) {
                return ILconstInt.create(Integer.parseInt(e.getValI()));
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
                return ILconstNull.instance();
            }

        });
    }


    private void setVarValue(Map<String, ILconst> localVarMap, String varName, ILconst s) {
        if (isLocal(localVarMap, varName)) {
            localVarMap.put(varName, s);
        } else if (isGlobal(varName)) {
            globalVarMap.put(varName, s);
        } else {
            throw new InterpreterException("var " + varName + " is neither local nor global?");
        }
    }


    private boolean isLocal(Map<String, ILconst> localVarMap, String varName) {
        return localVarMap.containsKey(varName);

    }

    private boolean isGlobal(String varName) {
        return globalVarMap.containsKey(varName);

    }

    private ILconst getVarValue(Map<String, ILconst> localVarMap, String name) {
        ILconst value = localVarMap.get(name);
        if (value == null) {
            value = globalVarMap.get(name);
            if (value == null) {
                throw new InterpreterException("Variable " + name + " not found.");
            }
        }
        return value;
    }

    private ExecutableJassFunction searchFunction(String fname) {
        return functionCache.computeIfAbsent(fname, name -> {
            for (JassFunction f : prog.getFunctions()) {
                if (f.getName().equals(name)) {
                    if (!f.getIsCompiletimeNative()) {
                        return new UserDefinedJassFunction(f);
                    }
                }
            }
            return searchNativeJassFunction(name);
        });
    }

    private ExecutableJassFunction searchNativeJassFunction(String name) {
        if (name.equals("ExecuteFunc")) {
            return executeFuncNative();
        }
        ReflectionNativeProvider nf = new ReflectionNativeProvider(this);
        ExecutableJassFunction functionPair = nf.getFunctionPair(name);
        return functionPair != null ? functionPair : new UnknownJassFunction(name);
    }

    private ExecutableJassFunction executeFuncNative() {
        return (jassInterpreter, arguments) -> {
            ILconstString funcName = (ILconstString) arguments[0];
            jassInterpreter.executeFunction(funcName.getVal());
            return ILconstBool.TRUE;
        };
    }

    public void trace(boolean b) {
        trace = b;
    }

    @Override
    public void runFuncRef(ILconstFuncRef f, @Nullable Element trace) {
        if (f == null) {
            throw new RuntimeException("Function was null in " + trace);
        }
        ExecutableJassFunction func = searchFunction(f.getFuncName());
        func.execute(this);
    }

    @Override
    public TimerMockHandler getTimerMockHandler() {
        return timerMockHandler;
    }

    public void runProgram() {
        for (JassVar var : prog.getGlobals()) {
            if (var instanceof JassInitializedVar) {
                JassInitializedVar iVar = (JassInitializedVar) var;
                globalVarMap.put(iVar.getName(), executeExpr(Collections.emptyMap(), iVar.getVal()));
            }
        }
        executeFunction("main");
        timerMockHandler.completeTimers();
    }


    @Override
    public void completeTimers() {
        timerMockHandler.completeTimers();
    }

    @Override
    public ImProg getImProg() {
        throw new UnsupportedOperationException("Not supported in Jass interpreter.");
    }

    @Override
    public int getInstanceCount(int val) {
        throw new UnsupportedOperationException("Not supported in Jass interpreter.");
    }

    @Override
    public int getMaxInstanceCount(int val) {
        throw new UnsupportedOperationException("Not supported in Jass interpreter.");
    }
}
