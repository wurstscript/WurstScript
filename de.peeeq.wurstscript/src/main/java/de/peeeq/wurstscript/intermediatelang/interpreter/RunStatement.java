package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.datastructures.IntTuple;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.JassArray;
import de.peeeq.wurstio.jassinterpreter.VarargArray;
import de.peeeq.wurstscript.intermediatelang.*;
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
        ImVar v = s.getLeft();
        ILconst right = s.getRight().evaluate(globalState, localState);
        if (v.isGlobal()) {
            globalState.setVal(v, right);
        } else {
            localState.setVal(v, right);
        }
    }

    public static void run(ImSetArray s, ProgramState globalState, LocalState localState) {
        ImVar v = s.getLeft();
        ILconstInt index = (ILconstInt) s.getIndex().evaluate(globalState, localState);
        ILconst right = s.getRight().evaluate(globalState, localState);
        if (v.isGlobal()) {
            globalState.setArrayVal(v, index.getVal(), right);
        } else {
            localState.setArrayVal(v, index.getVal(), right);
        }
    }

    public static void run(ImSetArrayTuple s, ProgramState globalState, LocalState localState) {
        ImVar v = s.getLeft();
        ILconstInt index = (ILconstInt) s.getIndex().evaluate(globalState, localState);
        ILconst right = s.getRight().evaluate(globalState, localState);
        if (v.isGlobal()) {
            ILconstTuple oldVal = (ILconstTuple) globalState.getArrayVal(v, index.getVal());
            if (oldVal == null) {
                throw new Error("Tuple not initialized");
            }
            ILconstTuple newVal = oldVal.updated(s.getTupleIndex(), right);
            globalState.setArrayVal(v, index.getVal(), newVal);
        } else {
            ILconstTuple oldVal = (ILconstTuple) localState.getArrayVal(v, index.getVal());
            if (oldVal == null) {
                throw new Error("Tuple not initialized");
            }
            ILconstTuple newVal = oldVal.updated(s.getTupleIndex(), right);
            localState.setArrayVal(v, index.getVal(), newVal);
        }
    }

    public static void run(ImSetArrayMulti s, ProgramState globalState, LocalState localState) {
        ImVar v = s.getLeft();
        int[] indices = new int[s.getIndices().size()];

        for (int i = 0; i < indices.length; i++) {
            ILconstInt index = (ILconstInt) s.getIndices().get(i).evaluate(globalState, localState);
            indices[i] = index.getVal();
        }
        IntTuple indicesT = IntTuple.of(indices);
        ILconst right = s.getRight().evaluate(globalState, localState);
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
        ar.set(indicesT.tail(), right);
    }

    public static void run(ImSetTuple s, ProgramState globalState, LocalState localState) {
        ImVar v = s.getLeft();
        ILconst right = s.getRight().evaluate(globalState, localState);
        if (v.isGlobal()) {
            ILconstTuple oldVal = (ILconstTuple) globalState.getVal(v);
            if (oldVal == null) {
                throw new Error("Tuple not initialized");
            }
            ILconstTuple newVal = oldVal.updated(s.getTupleIndex(), right);
            globalState.setVal(v, newVal);
        } else {
            ILconstTuple oldVal = (ILconstTuple) localState.getVal(v);
            if (oldVal == null) {
                throw new Error("Tuple not initialized");
            }
            ILconstTuple newVal = oldVal.updated(s.getTupleIndex(), right);
            localState.setVal(v, newVal);
        }

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
