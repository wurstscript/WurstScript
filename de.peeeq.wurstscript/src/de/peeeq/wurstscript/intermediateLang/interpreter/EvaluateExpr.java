package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.ILconstTuple;
import de.peeeq.wurstscript.jassIm.ImAlloc;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImConst;
import de.peeeq.wurstscript.jassIm.ImDealloc;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImInstanceof;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImMemberAccess;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImMethodCall;
import de.peeeq.wurstscript.jassIm.ImNull;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypeIdOfClass;
import de.peeeq.wurstscript.jassIm.ImTypeIdOfObj;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.JassIm;

public class EvaluateExpr {

	public static ILconst eval(ImBoolVal e, ProgramState globalState, LocalState localState) {
		return ILconstBool.instance(e.getValB());
	}

	public static ILconst eval(ImFuncRef e, ProgramState globalState, LocalState localState) {
		return new ILconstFuncRef(e.getFunc());
	}

	public static ILconst eval(ImFunctionCall e, ProgramState globalState, LocalState localState) {
		ImFunction f = e.getFunc();
		ImExprs arguments = e.getArguments();
		return evaluateFunc(globalState, localState, f, arguments);
	}

	public static ILconst evaluateFunc(ProgramState globalState,
			LocalState localState, ImFunction f, List<ImExpr> args2) {
		ILconst[] args = new ILconst[args2.size()];
		for (int i=0; i < args2.size(); i++) {
			args[i] = args2.get(i).evaluate(globalState, localState);
		}
		LocalState r = ILInterpreter.runFunc(globalState, f, args);
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
			return op.evaluateBinaryOperator(arguments.get(0).evaluate(globalState, localState), new Supplier<ILconst>() {
				
				@Override
				public ILconst get() {
					return arguments.get(1).evaluate(globalState, localState);
				}
			});
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
		for (int i=0; i< e.getExprs().size(); i++) {
			values[i] = e.getExprs().get(i).evaluate(globalState, localState); 
		}
		return new ILconstTuple(values);
	}

	public static ILconst eval(ImTupleSelection e, ProgramState globalState, LocalState localState) {
		ILconstTuple t = (ILconstTuple) e.getTupleExpr().evaluate(globalState, localState);
		return t.getValue(e.getTupleIndex());

	}

	public static de.peeeq.wurstscript.intermediateLang.ILconst eval(ImVarAccess e, ProgramState globalState, LocalState localState) {
		ImVar var = e.getVar();
		if (var.isGlobal()) {
			ILconst r = globalState.getVal(var);
			if (r == null) {
				ImExpr initExpr = e.attrProg().getGlobalInits().get(var);
				if (initExpr != null) {
					r = initExpr.evaluate(globalState, localState);
				} else {
					throw new Error("Variable " + var.getName() + " is not initalized.");
				}
				globalState.setVal(var, r);
			}
			return r;
		} else {
			return notNull(localState.getVal(var), var.getType(), "Local variable " + var.getName() + " is null.");
		}
	}

	private static ILconst notNull(ILconst val, ImType imType, String msg) {
		if (val == null) {
			System.err.println(msg);
			return imType.defaultValue();
		}
		return val;
	}

	public static ILconst eval(ImVarArrayAccess e, ProgramState globalState, LocalState localState) {
		ILconstInt index = (ILconstInt) e.getIndex().evaluate(globalState, localState);
		if (e.getVar().isGlobal()) {
			return notNull(globalState.getArrayVal(e.getVar(), index.getVal()), e.getVar().getType(), "Variable " + e.getVar().getName() + " is null.");
		} else {
			return notNull(localState.getArrayVal(e.getVar(), index.getVal()), e.getVar().getType(), "Variable " + e.getVar().getName() + " is null.");
		}
	}

	public static ILconst eval(ImMethodCall mc,
			ProgramState globalState, LocalState localState) {
		ImClass c = mc.getMethod().attrClass();
		ImVar typeIdVar = null; // TODO c.attrManagementVars().typeId;
		ILconstInt receiver = (ILconstInt)mc.getReceiver().evaluate(globalState, localState);
		if (receiver.getVal() == 0) {
			throw new RuntimeException("Null pointer derefenced at " + mc.getTrace());
		}
		ImIntVal typeId = (ImIntVal) globalState.getArrayVal(typeIdVar, receiver.getVal());
		ArrayList<ImExpr> args = Lists.newArrayList(mc.getArguments());
		args.add(0, JassIm.ImIntVal(receiver.getVal()));
		// find correct implementation:
		for (ImMethod m : mc.getMethod().getSubMethods()) {
			if (typeId.getValI() == m.attrClass().attrTypeId()) {
				return evaluateFunc(globalState, localState, m.getImplementation(), args);
			}
		}
		// default implementation
		return evaluateFunc(globalState, localState, mc.getMethod().getImplementation(), args);
	}

	public static ILconst eval(ImMemberAccess ma, ProgramState globalState, LocalState localState) {
		ILconstInt receiver = (ILconstInt)ma.getReceiver().evaluate(globalState, localState);
		if (receiver.getVal() == 0) {
			throw new RuntimeException("Null pointer derefenced at ...");
		}
		return notNull(globalState.getArrayVal(ma.getVar(), receiver.getVal()), ma.getVar().getType(), "Variable " + ma.getVar().getName() + " is null.");
	}

	public static ILconst eval(ImAlloc imAlloc, ProgramState globalState,
			LocalState localState) {
		throw new Error("not implemented"); // TODO
	}

	public static ILconst eval(ImDealloc imDealloc, ProgramState globalState,
			LocalState localState) {
		throw new Error("not implemented"); // TODO
	}

	public static ILconst eval(ImInstanceof e, ProgramState globalState,
			LocalState localState) {
		throw new Error("not implemented"); // TODO
	}

	public static ILconst eval(ImTypeIdOfClass imTypeIdOfClass,
			ProgramState globalState, LocalState localState) {
		throw new Error("not implemented"); // TODO
	}

	public static ILconst eval(ImTypeIdOfObj imTypeIdOfObj,
			ProgramState globalState, LocalState localState) {
		throw new Error("not implemented"); // TODO
	}
	
}
