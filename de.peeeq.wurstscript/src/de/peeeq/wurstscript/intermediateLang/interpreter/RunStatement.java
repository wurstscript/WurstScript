package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.ILconstTuple;
import de.peeeq.wurstscript.jassIm.ImError;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
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
			r  = e.evaluate(globalState, localState);
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
			ILconstTuple newVal = oldVal.updated(s.getTupleIndex(), right);
			globalState.setArrayVal(v, index.getVal(), newVal); 
		} else {
			ILconstTuple oldVal = (ILconstTuple) localState.getArrayVal(v, index.getVal());
			ILconstTuple newVal = oldVal.updated(s.getTupleIndex(), right);
			localState.setArrayVal(v, index.getVal(), newVal);
		}
	}

	public static void run(ImSetTuple s, ProgramState globalState, LocalState localState) {
		ImVar v = s.getLeft();
		ILconst right = s.getRight().evaluate(globalState, localState);
		if (v.isGlobal()) {
			ILconstTuple oldVal = (ILconstTuple) globalState.getVal(v);
			ILconstTuple newVal = oldVal.updated(s.getTupleIndex(), right);
			globalState.setVal(v, newVal); 
		} else {
			ILconstTuple oldVal = (ILconstTuple) localState.getVal(v);
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

	public static void run(ImError s, ProgramState globalState,
			LocalState localState) {
		ILconstString msg = (ILconstString) s.getMessage().evaluate(globalState, localState);
		throw new DebugPrintError(msg.getVal());
	}

}
