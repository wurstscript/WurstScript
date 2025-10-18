package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.VarargArray;
import de.peeeq.wurstscript.intermediatelang.ILaddress;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassinterpreter.ExitwhenException;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;

public class RunStatement {

    public static void run(ImExpr e, ProgramState globalState, LocalState localState) {
        e.evaluate(globalState, localState);
    }

    public static void run(ImExitwhen s, ProgramState globalState, LocalState localState) {
        ILconstBool c = (ILconstBool) s.getCondition().evaluate(globalState, localState);
        if (c.getVal()) {
            throw ExitwhenException.instance();
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
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterpreterException(globalState, "Execution interrupted");
                }
                s.getBody().runStatements(globalState, localState);
            }
        } catch (ExitwhenException e) {
            // end of loop
        }

    }

    public static void run(ImReturn s, ProgramState globalState, LocalState localState) {
        ILconst r = null;
        if (s.getReturnValue() instanceof ImExpr) {
            ImExpr e = (ImExpr) s.getReturnValue();
            r = e.evaluate(globalState, localState);
        }
        throw new ReturnException(r);
    }

    public static void run(ImSet s, ProgramState globalState, LocalState localState) {
        ImLExpr left = s.getLeft();
        ILaddress v = left.evaluateLvalue(globalState, localState);
        ILconst right = s.getRight().evaluate(globalState, localState);
        v.set(right);
    }


    public static void run(ImStmts stmts, ProgramState globalState, LocalState localState) {
        for (ImStmt s : stmts) {
            globalState.setLastStatement(s);
            s.runStatement(globalState, localState);
        }
    }


    public static void run(ImVarargLoop loop, ProgramState globalState, LocalState localState) {
        ImFunction func = loop.getNearestFunc();
        ImVar varargParam = func.getParameters().get(func.getParameters().size() - 1);
        VarargArray val = (VarargArray) localState.getVal(varargParam);
        for (int i = 0; i < val.size(); i++) {
            localState.setVal(loop.getLoopVar(), val.get(i));
            loop.getBody().runStatements(globalState, localState);
        }
    }
}
