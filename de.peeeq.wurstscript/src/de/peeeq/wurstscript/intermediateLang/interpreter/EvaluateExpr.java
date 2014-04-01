package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
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
import de.peeeq.wurstscript.parser.WPos;

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
		return evaluateFunc(globalState, localState, f, arguments, e.attrTrace().attrSource());
	}

	public static ILconst evaluateFunc(ProgramState globalState,
			LocalState localState, ImFunction f, List<ImExpr> args2, WPos trace) {
		ILconst[] args = new ILconst[args2.size()];
		for (int i=0; i < args2.size(); i++) {
			args[i] = args2.get(i).evaluate(globalState, localState);
		}
		globalState.pushStackframe(f, args, trace);
		LocalState r = ILInterpreter.runFunc(globalState, f, args);
		globalState.popStackframe();
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
				ImExpr initExpr = globalState.getProg().getGlobalInits().get(var);
				if (initExpr != null) {
					r = initExpr.evaluate(globalState, localState);
				} else {
					throw new InterpreterException(globalState, "Variable " + var.getName() + " is not initalized.");
				}
				globalState.setVal(var, r);
			}
			return r;
		} else {
			return notNull(localState.getVal(var), var.getType(), "Local variable " + var.getName() + " is null.", true);
		}
	}

	private static ILconst notNull(ILconst val, ImType imType, String msg, boolean failOnErr) {
		if (val == null) {
			if (failOnErr) {
				throw new InterpreterException(msg);
			} else {
				System.err.println(msg);
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

	public static ILconst eval(ImMethodCall mc,
			ProgramState globalState, LocalState localState) {
		ILconstInt receiver = (ILconstInt)mc.getReceiver().evaluate(globalState, localState);

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
		return evaluateFunc(globalState, localState, mostPrecise.getImplementation(), args, mc.attrTrace().attrSource());
	}

	public static ILconst eval(ImMemberAccess ma, ProgramState globalState, LocalState localState) {
		ILconstInt receiver = (ILconstInt)ma.getReceiver().evaluate(globalState, localState);
		if (receiver.getVal() == 0) {
			throw new RuntimeException("Null pointer derefenced at ...");
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
	
}
