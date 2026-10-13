package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.utils.CompilationCancellation;
import com.google.common.base.Preconditions;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.VarargArray;
import de.peeeq.wurstscript.intermediatelang.ILaddress;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassinterpreter.ExitwhenException;

public class RunStatement {

    public static void run(ImExpr e, ProgramState globalState, LocalState localState) {
        e.evaluate(globalState, localState);
    }

    public static void run(ImExitwhen s, ProgramState globalState, LocalState localState) {
        ILconstBool c = (ILconstBool) s.getCondition().evaluate(globalState, localState);
        if (c.getVal()) {
            localState.exitLoop();
        }
    }

    public static void run(ImIf s, ProgramState globalState, LocalState localState) {
        ILconstBool c = (ILconstBool) s.getCondition().evaluate(globalState, localState);
        if (c.getVal()) {
            s.getThenBlock().runStatements(globalState, localState);
        } else {
            s.getElseBlock().runStatements(globalState, localState);
        }

    }

    public static void run(ImLoop s, ProgramState globalState, LocalState localState) {
        try {
            while (true) {
                CompilationCancellation.check();
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterpreterException(globalState, "Execution interrupted");
                }
                s.getBody().runStatements(globalState, localState);
                if (localState.isLeaving()) {
                    // an exitwhen ends this loop; a return stays set and ends the call
                    localState.consumeExit();
                    break;
                }
            }
        } catch (ExitwhenException e) {
            // end of loop: an exitwhen inside a statement expression, see EvaluateExpr.leaveByException
        }

    }

    public static void run(ImReturn s, ProgramState globalState, LocalState localState) {
        ILconst r = null;
        if (s.getReturnValue() instanceof ImExpr) {
            ImExpr e = (ImExpr) s.getReturnValue();
            r = e.evaluate(globalState, localState);
        }
        localState.returnWith(r);
    }

    public static void run(ImSet s, ProgramState globalState, LocalState localState) {
        ImLExpr left = s.getLeft();
        if (left instanceof ImVarAccess va) {
            // a variable needs no address: finding its state evaluates nothing
            ImVar var = va.getVar();
            ILconst right = s.getRight().evaluate(globalState, localState);
            (var.isGlobal() ? globalState : localState).setVal(var, right);
            return;
        }
        ILaddress v = left.evaluateLvalue(globalState, localState);
        ILconst right = s.getRight().evaluate(globalState, localState);
        v.set(right);
    }


    public static void run(ImStmts stmts, ProgramState globalState, LocalState localState) {
        for (ImStmt s : stmts) {
            CompilationCancellation.check();
            globalState.setLastStatement(s);
            s.runStatement(globalState, localState);
            if (localState.isLeaving()) {
                return;
            }
        }
    }


    public static void run(ImVarargLoop loop, ProgramState globalState, LocalState localState) {
        Preconditions.checkState(loop.getLoopVars().size() == 1,
            "Expected one vararg loop variable in the interpreter.");
        ImFunction func = loop.getNearestFunc();
        ImVar varargParam = func.getParameters().get(func.getParameters().size() - 1);
        VarargArray val = (VarargArray) localState.getVal(varargParam);
        for (int i = 0; i < val.size(); i++) {
            localState.setVal(loop.getLoopVars().get(0).getVar(), val.get(i));
            loop.getBody().runStatements(globalState, localState);
            if (localState.isLeaving()) {
                // an exitwhen ends the enclosing loop, as a return ends the call
                break;
            }
        }
    }
}
