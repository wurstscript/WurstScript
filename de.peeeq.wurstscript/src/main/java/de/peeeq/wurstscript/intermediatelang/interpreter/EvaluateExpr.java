package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class EvaluateExpr {

    public static ILconst eval(ImBoolVal e, ProgramState globalState, LocalState localState) {
        return ILconstBool.instance(e.getValB());
    }

    public static ILconst eval(ImFuncRef e, ProgramState globalState, LocalState localState) {
        return new ILconstFuncRef(e.getFunc());
    }

    public static @Nullable ILconst eval(ImFunctionCall e, ProgramState globalState, LocalState localState) {
        ImFunction f = e.getFunc();
        ImExprs arguments = e.getArguments();
        return evaluateFunc(globalState, localState, f, arguments, e);
    }

    public static @Nullable ILconst evaluateFunc(ProgramState globalState,
                                                 LocalState localState, ImFunction f, List<ImExpr> args2, Element trace) {
        ILconst[] args = new ILconst[args2.size()];
        for (int i = 0; i < args2.size(); i++) {
            args[i] = args2.get(i).evaluate(globalState, localState);
        }
        return evaluateFunc(globalState, f, trace, args);
    }

    @Nullable
    private static ILconst evaluateFunc(ProgramState globalState, ImFunction f, Element trace, ILconst[] args) {
        LocalState r = ILInterpreter.runFunc(globalState, f, trace, args);
        return r.getReturnVal();
    }

    public static ILconst eval(ImIntVal e, ProgramState globalState, LocalState localState) {
        return new ILconstInt(e.getValI());
    }

    public static ILconst eval(ImNull e, ProgramState globalState, LocalState localState) {
        if (e.getType() instanceof ImAnyType
            || e.getType() instanceof ImClassType
            || e.getType() instanceof ImTypeVarRef
            || TypesHelper.isIntType(e.getType())) {
            return ILconstInt.create(0);
        }
        return ILconstNull.instance();
    }

    public static ILconst eval(ImOperatorCall e, final ProgramState globalState, final LocalState localState) {
        final ImExprs arguments = e.getArguments();
        WurstOperator op = e.getOp();
        if (arguments.size() == 2 && op.isBinaryOp()) {
            return op.evaluateBinaryOperator(arguments.get(0).evaluate(globalState, localState), () -> arguments.get(1).evaluate(globalState, localState));
        } else if (arguments.size() == 1 && op.isUnaryOp()) {
            return op.evaluateUnaryOperator(arguments.get(0).evaluate(globalState, localState));
        } else {
            throw new Error();
        }
    }

    public static ILconst eval(ImRealVal e, ProgramState globalState, LocalState localState) {
        return new ILconstReal(e.getValR());
    }

    public static ILconst eval(ImStatementExpr e, ProgramState globalState, LocalState localState) {
        e.getStatements().runStatements(globalState, localState);
        return e.getExpr().evaluate(globalState, localState);
    }

    public static ILaddress evaluateLvalue(ImStatementExpr e, ProgramState globalState, LocalState localState) {
        e.getStatements().runStatements(globalState, localState);
        return ((ImLExpr) e.getExpr()).evaluateLvalue(globalState, localState);
    }

    public static ILconst eval(ImStringVal e, ProgramState globalState, LocalState localState) {
        return new ILconstString(e.getValS());
    }

    public static ILconst eval(ImTupleExpr e, ProgramState globalState, LocalState localState) {
        ILconst[] values = new ILconst[e.getExprs().size()];
        for (int i = 0; i < e.getExprs().size(); i++) {
            values[i] = e.getExprs().get(i).evaluate(globalState, localState);
        }
        return new ILconstTuple(values);
    }

    public static ILconst eval(ImTupleSelection e, ProgramState globalState, LocalState localState) {
        ILconst tupleE = e.getTupleExpr().evaluate(globalState, localState);
        if (tupleE instanceof ILconstTuple) {
            ILconstTuple t = (ILconstTuple) tupleE;
            if (e.getTupleIndex() >= t.values().size()) {
                throw new InterpreterException(globalState, "Trying to get element " + e.getTupleIndex() + " of tuple value " + t);
            }
            return t.getValue(e.getTupleIndex());
        } else {
            throw new InterpreterException(globalState, "Tuple " + e + " evaluated to " + tupleE);
        }

    }

    public static ILconst eval(ImVarAccess e, ProgramState globalState, LocalState localState) {
        ImVar var = e.getVar();
        if (var.isGlobal()) {
            if (isMagicCompiletimeConstant(var)) {
                return ILconstBool.instance(globalState.isCompiletime());
            }

            ILconst r = globalState.getVal(var);
            if (r == null) {
                List<ImExpr> initExpr = globalState.getProg().getGlobalInits().get(var);
                if (initExpr != null) {
                    r = initExpr.get(0).evaluate(globalState, localState);
                } else {
                    throw new InterpreterException(globalState, "Variable " + var.getName() + " is not initialized.");
                }
                globalState.setVal(var, r);
            }
            return r;
        } else {
            return notNull(localState.getVal(var), var.getType(), "Local variable " + var + " is null.", true);
        }
    }

    private static boolean isMagicCompiletimeConstant(ImVar var) {
        if (var.getTrace() instanceof VarDef) {
            VarDef varDef = (VarDef) var.getTrace();
            if (varDef.getName().equals("compiletime")) {
                PackageOrGlobal nearestPackage = varDef.attrNearestPackage();
                if (nearestPackage instanceof WPackage) {
                    WPackage p = (WPackage) nearestPackage;
                    if (p.getName().equals("MagicFunctions")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static ILconst notNull(@Nullable ILconst val, ImType imType, String msg, boolean failOnErr) {
        if (val == null) {
            if (failOnErr) {
                throw new InterpreterException(msg);
            } else {
                WLogger.warning(msg);
                return imType.defaultValue();
            }
        }
        return val;
    }

    public static ILconst eval(ImVarArrayAccess e, ProgramState globalState, LocalState localState) {
        List<Integer> indexes = e.getIndexes().stream()
            .map(ie -> ((ILconstInt) ie.evaluate(globalState, localState)).getVal())
            .collect(Collectors.toList());

        if (e.getVar().isGlobal()) {
            return notNull(globalState.getArrayVal(e.getVar(), indexes), e.getVar().getType(), "Variable " + e.getVar().getName() + " is null.", false);
        } else {
            return notNull(localState.getArrayVal(e.getVar(), indexes), e.getVar().getType(), "Variable " + e.getVar().getName() + " is null.", false);
        }
    }

    public static @Nullable ILconst eval(ImMethodCall mc,
                                         ProgramState globalState, LocalState localState) {
        ILconstObject receiver = globalState.toObject(mc.getReceiver().evaluate(globalState, localState));

        globalState.assertAllocated(receiver, mc.attrTrace());


        List<ImExpr> args = mc.getArguments();


        ImMethod mostPrecise = mc.getMethod();

        // find correct implementation:
        for (ImMethod m : mc.getMethod().getSubMethods()) {

            if (m.attrClass().isSubclassOf(mostPrecise.attrClass())) {
                if (globalState.isInstanceOf(receiver, m.attrClass(), mc.attrTrace())) {
                    // found more precise method
                    mostPrecise = m;
                }
            }
        }
        // execute most precise method
        ILconst[] eargs = new ILconst[args.size() + 1];
        eargs[0] = receiver;
        for (int i = 0; i < args.size(); i++) {
            eargs[i + 1] = args.get(i).evaluate(globalState, localState);
        }
        return evaluateFunc(globalState, mostPrecise.getImplementation(), mc, eargs);
    }

    public static ILconst eval(ImMemberAccess ma, ProgramState globalState, LocalState localState) {
        ILconstObject receiver = globalState.toObject(ma.getReceiver().evaluate(globalState, localState));
        if (receiver == null) {
            throw new InterpreterException(ma.getTrace(), "Null pointer dereference");
        }
        List<Integer> indexes = ma.getIndexes().stream()
            .map(i -> ((ILconstInt) i.evaluate(globalState, localState)).getVal())
            .collect(Collectors.toList());
        return receiver.get(ma.getVar(), indexes).orElseGet(() -> ma.attrTyp().defaultValue());
    }

    public static ILconst eval(ImAlloc imAlloc, ProgramState globalState,
                               LocalState localState) {
        return globalState.allocate(imAlloc.getClazz(), imAlloc.attrTrace());
    }

    public static ILconst eval(ImDealloc imDealloc, ProgramState globalState,
                               LocalState localState) {
        ILconstObject obj = globalState.toObject(imDealloc.getObj().evaluate(globalState, localState));
        globalState.deallocate(obj, imDealloc.getClazz().getClassDef(), imDealloc.attrTrace());
        return ILconstNull.instance();
    }

    public static ILconst eval(ImInstanceof e, ProgramState globalState,
                               LocalState localState) {
        ILconstObject obj = globalState.toObject(e.getObj().evaluate(globalState, localState));
        return ILconstBool.instance(globalState.isInstanceOf(obj, e.getClazz().getClassDef(), e.attrTrace()));
    }

    public static ILconst eval(ImTypeIdOfClass e,
                               ProgramState globalState, LocalState localState) {
        return new ILconstInt(e.getClazz().getClassDef().attrTypeId());
    }

    public static ILconst eval(ImTypeIdOfObj e,
                               ProgramState globalState, LocalState localState) {
        ILconstObject obj = globalState.toObject(e.getObj().evaluate(globalState, localState));
        return new ILconstInt(globalState.getTypeId(obj, e.attrTrace()));
    }


    public static ILconst eval(ImGetStackTrace e, ProgramState globalState,
                               LocalState localState) {
        StringBuilder sb = new StringBuilder();
        globalState.getStackFrames().appendTo(sb);
        return new ILconstString(sb.toString());
    }

    public static ILconst eval(ImCompiletimeExpr expr, ProgramState globalState, LocalState localState) {
        // make sure that compiletime expression is only evaluated once
        ILconst res = expr.evaluationResult().get();
        if (res == null) {
            res = expr.getExpr().evaluate(globalState, localState);
            expr.evaluationResult().set(res);
        }
        return res;
    }

    public static AtomicReference<ILconst> compiletimeEvaluationResult(ImCompiletimeExpr imCompiletimeExpr) {
        return new AtomicReference<>();
    }

    public static ILaddress evaluateLvalue(ImVarAccess va, ProgramState globalState, LocalState localState) {
        ImVar v = va.getVar();
        State state;
        state = v.isGlobal() ? globalState : localState;
        return new ILaddress() {
            @Override
            public void set(ILconst value) {
                state.setVal(v, value);
            }

            @Override
            public ILconst get() {
                return state.getVal(v);
            }
        };
    }


    public static ILaddress evaluateLvalue(ImVarArrayAccess va, ProgramState globalState, LocalState localState) {
        ImVar v = va.getVar();
        State state;
        state = v.isGlobal() ? globalState : localState;
        List<Integer> indexes = va.getIndexes().stream()
            .map(ie -> ((ILconstInt) ie.evaluate(globalState, localState)).getVal())
            .collect(Collectors.toList());
        return new ILaddress() {
            @Override
            public void set(ILconst value) {
                state.setArrayVal(v, indexes, value);
            }

            @Override
            public ILconst get() {
                return state.getArrayVal(v, indexes);
            }
        };
    }

    public static ILaddress evaluateLvalue(ImTupleSelection ts, ProgramState globalState, LocalState localState) {
        ImExpr tupleExpr = ts.getTupleExpr();
        int tupleIndex = ts.getTupleIndex();
        if (tupleExpr instanceof ImLExpr) {
            ILaddress addr = ((ImLExpr) tupleExpr).evaluateLvalue(globalState, localState);
            return new ILaddress() {
                @Override
                public void set(ILconst value) {
                    ILconst val = addr.get();
                    ILconstTuple tuple = (ILconstTuple) val;
                    ILconstTuple updated = tuple.updated(tupleIndex, value);
                    addr.set(updated);
                }

                @Override
                public ILconst get() {
                    ILconstTuple tuple = (ILconstTuple) addr.get();
                    return tuple.getValue(tupleIndex);
                }
            };
        } else {
            ILconstTuple tupleValue = (ILconstTuple) tupleExpr.evaluate(globalState, localState);
            return new ILaddress() {
                @Override
                public void set(ILconst value) {
                    throw new InterpreterException(ts.attrTrace(), "Not a valid L-value in tuple-selection");
                }

                @Override
                public ILconst get() {
                    return tupleValue.getValue(tupleIndex);
                }
            };
        }
    }

    public static ILaddress evaluateLvalue(ImMemberAccess va, ProgramState globalState, LocalState localState) {
        ImVar v = va.getVar();
        ILconstObject receiver = globalState.toObject(va.getReceiver().evaluate(globalState, localState));
        List<Integer> indexes =
            va.getIndexes().stream()
                .map(ie -> ((ILconstInt) ie.evaluate(globalState, localState)).getVal())
                .collect(Collectors.toList());
        return new ILaddress() {
            @Override
            public void set(ILconst value) {
                receiver.set(v, indexes, value);
            }

            @Override
            public ILconst get() {
                return receiver.get(v, indexes)
                    .orElseGet(() -> va.attrTyp().defaultValue());
            }
        };
    }


    public static ILaddress evaluateLvalue(ImTupleExpr e, ProgramState globalState, LocalState localState) {
        List<ILaddress> addresses = new ArrayList<>();
        for (ImExpr lexpr : e.getExprs()) {
            ILaddress addr = ((ImLExpr) lexpr).evaluateLvalue(globalState, localState);
            addresses.add(addr);
        }
        return new ILaddress() {
            @Override
            public void set(ILconst value) {
                if (value instanceof ILconstTuple) {
                    ILconstTuple te = (ILconstTuple) value;
                    for (int i = 0; i < addresses.size(); i++) {
                        addresses.get(i).set(te.getValue(i));
                    }
                }
            }

            @Override
            public ILconst get() {
                return new ILconstTuple(addresses.stream()
                    .map(ILaddress::get)
                    .toArray(ILconst[]::new));
            }
        };
    }


    public static ILconst eval(ImTypeVarDispatch e, ProgramState globalState, LocalState localState) {
        // TODO store type arguments in localState with the required dispatch functions
        throw new InterpreterException(e.attrTrace(), "Cannot evaluate " + e);
    }

    public static ILconst eval(ImCast imCast, ProgramState globalState, LocalState localState) {
        ILconst res = imCast.getExpr().evaluate(globalState, localState);
        if (TypesHelper.isIntType(imCast.getToType())) {
            if (res instanceof ILconstObject) {
                return ILconstInt.create(((ILconstObject) res).getObjectId());
            }
            if (res instanceof IlConstHandle) {
                int id = globalState.getHandleMap().size() + 1;
                globalState.getHandleMap().put(id, (IlConstHandle) res);
                return ILconstInt.create(id);
            }
        }
        if (res instanceof ILconstInt) {
            if (imCast.getToType() instanceof ImClassType) {
                return globalState.getObjectByIndex(((ILconstInt) res).getVal());
            }
            if (imCast.getToType() instanceof IlConstHandle) {
                return globalState.getHandleByIndex(((ILconstInt) res).getVal());
            }
        }
        return res;
    }
}
