package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.collect.Lists;
import de.peeeq.datastructures.IntTuple;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassIm.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
        LocalState r = ILInterpreter.runFunc(globalState, f, trace, args);
        return r.getReturnVal();
    }

    public static ILconst eval(ImIntVal e, ProgramState globalState, LocalState localState) {
        return new ILconstInt(e.getValI());
    }

    public static ILconst eval(ImNull e, ProgramState globalState, LocalState localState) {
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
        ILconstTuple t = (ILconstTuple) e.getTupleExpr().evaluate(globalState, localState);
        if (e.getTupleIndex() >= t.values().size()) {
            throw new InterpreterException(globalState, "Trying to get element " + e.getTupleIndex() + " of tuple value " + t);
        }
        return t.getValue(e.getTupleIndex());

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
            return notNull(localState.getVal(var), var.getType(), "Local variable " + var.getName() + " is null.", true);
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
        ILconstInt index = (ILconstInt) e.getIndex().evaluate(globalState, localState);
        if (e.getVar().isGlobal()) {
            return notNull(globalState.getArrayVal(e.getVar(), index.getVal()), e.getVar().getType(), "Variable " + e.getVar().getName() + " is null.", false);
        } else {
            return notNull(localState.getArrayVal(e.getVar(), index.getVal()), e.getVar().getType(), "Variable " + e.getVar().getName() + " is null.", false);
        }
    }

    public static @Nullable ILconst eval(ImMethodCall mc,
                                         ProgramState globalState, LocalState localState) {
        ILconstInt receiver = (ILconstInt) mc.getReceiver().evaluate(globalState, localState);

        globalState.assertAllocated(receiver.getVal(), mc.attrTrace());


        ArrayList<ImExpr> args = Lists.newArrayList(mc.getArguments());
        args.add(0, JassIm.ImIntVal(receiver.getVal()));


        ImMethod mostPrecise = mc.getMethod();

        // find correct implementation:
        for (ImMethod m : mc.getMethod().getSubMethods()) {

            if (m.attrClass().isSubclassOf(mostPrecise.attrClass())) {
                if (globalState.isInstanceOf(receiver.getVal(), m.attrClass(), mc.attrTrace())) {
                    // found more precise method
                    mostPrecise = m;
                }
            }
        }
        // execute most precise method
        return evaluateFunc(globalState, localState, mostPrecise.getImplementation(), args, mc);
    }

    public static ILconst eval(ImMemberAccess ma, ProgramState globalState, LocalState localState) {
        ILconstInt receiver = (ILconstInt) ma.getReceiver().evaluate(globalState, localState);
        if (receiver.getVal() == 0) {
            throw new RuntimeException("Null pointer dereference");
        }
        return notNull(globalState.getArrayVal(ma.getVar(), receiver.getVal()), ma.getVar().getType(), "Variable " + ma.getVar().getName() + " is null.", false);
    }

    public static ILconst eval(ImAlloc imAlloc, ProgramState globalState,
                               LocalState localState) {
        return new ILconstInt(globalState.allocate(imAlloc.getClazz(), imAlloc.attrTrace()));
    }

    public static ILconst eval(ImDealloc imDealloc, ProgramState globalState,
                               LocalState localState) {
        ILconstInt obj = (ILconstInt) imDealloc.getObj().evaluate(globalState, localState);
        globalState.deallocate(obj.getVal(), imDealloc.getClazz(), imDealloc.attrTrace());
        return ILconstNull.instance();
    }

    public static ILconst eval(ImInstanceof e, ProgramState globalState,
                               LocalState localState) {
        ILconstInt obj = (ILconstInt) e.getObj().evaluate(globalState, localState);
        return ILconstBool.instance(globalState.isInstanceOf(obj.getVal(), e.getClazz(), e.attrTrace()));
    }

    public static ILconst eval(ImTypeIdOfClass e,
                               ProgramState globalState, LocalState localState) {
        return new ILconstInt(e.getClazz().attrTypeId());
    }

    public static ILconst eval(ImTypeIdOfObj e,
                               ProgramState globalState, LocalState localState) {
        ILconstInt obj = (ILconstInt) e.getObj().evaluate(globalState, localState);
        return new ILconstInt(globalState.getTypeId(obj.getVal(), e.attrTrace()));
    }

    public static ILconst eval(ImVarArrayMultiAccess s,
                               ProgramState globalState, LocalState localState) {
        ImVar v = s.getVar();
        int[] indices = {
                ((ILconstInt) s.getIndex1().evaluate(globalState, localState)).getVal(),
                ((ILconstInt) s.getIndex2().evaluate(globalState, localState)).getVal()
        };

        IntTuple indicesT = IntTuple.of(indices);
        ILconstMultiArray ar;
        if (v.isGlobal()) {
            ar = (ILconstMultiArray) globalState.getArrayVal(v, indicesT.head());
            if (ar == null) {
                ar = new ILconstMultiArray();
                globalState.setArrayVal(v, indicesT.head(), ar);
            }
        } else {
            ar = (ILconstMultiArray) localState.getArrayVal(v, indicesT.head());
            if (ar == null) {
                ar = new ILconstMultiArray();
                globalState.setArrayVal(v, indicesT.head(), ar);
            }
        }
        return ar.get(indicesT.tail());
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
}
