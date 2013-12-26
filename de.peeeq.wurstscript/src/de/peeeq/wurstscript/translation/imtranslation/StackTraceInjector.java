package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.ImError;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Utils;

/**
 * Takes a program and inserts stack traces at error messages 
 */
public class StackTraceInjector {
	
	private static final String WURST_STACK_TRACE = "wurstStackTrace";
	private ImProg prog;

	public StackTraceInjector(ImProg prog) {
		this.prog = prog;
	}
	
	public void transform() {
		final Multimap<ImFunction, ImError> errorPrints = LinkedListMultimap.create();
		final Multimap<ImFunction, ImFunctionCall> calls = LinkedListMultimap.create();
		final Multimap<ImFunction, ImFunction> callRelation = LinkedListMultimap.create();
		final List<ImFuncRef> funcRefs = Lists.newArrayList();
		prog.accept(new ImProg.DefaultVisitor() {
			@Override
			public void visit(ImError imError) {
				errorPrints.put(imError.getNearestFunc(), imError);
				
			}
			
			@Override
			public void visit(ImFunctionCall c) {
				calls.put(c.getFunc(), c);
				ImFunction caller = c.getNearestFunc();
				callRelation.put(caller, c.getFunc());
			}
			
			@Override
			public void visit(ImFuncRef imFuncRef) {
				funcRefs.add(imFuncRef);
			}
		});
		
		Multimap<ImFunction, ImFunction> callRelationTr = Utils.transientClosure(callRelation);

		// find affected functions
		Set<ImFunction> affectedFuncs = Sets.newHashSet(errorPrints.keySet());
		for (Entry<ImFunction, ImFunction> e : callRelationTr.entries()) {
			if (errorPrints.containsKey(e.getValue())) {
				affectedFuncs.add(e.getKey());
			}
		}
	
		addStackTraceParams(affectedFuncs);
		passStacktraceParams(calls, affectedFuncs);
		rewriteFuncRefs(funcRefs, affectedFuncs);
		rewriteErrorStatements(errorPrints);
	}

	private void addStackTraceParams(Set<ImFunction> affectedFuncs) {
		// add parameter to affected functions
		for (ImFunction f : affectedFuncs) {
			if (isMainOrConfig(f)) {
				continue;
			}
			f.getParameters().add(JassIm.ImVar(WurstTypeString.instance().imTranslateType(), WURST_STACK_TRACE, false));
		}
	}

	private boolean isMainOrConfig(ImFunction f) {
		return f.getName().equals("main") || f.getName().equals("config");
	}

	private void passStacktraceParams(
			final Multimap<ImFunction, ImFunctionCall> calls,
			Set<ImFunction> affectedFuncs) {
		// pass the stacktrace parameter at all cals
		for (ImFunction f : affectedFuncs) {
			for (ImFunctionCall call : calls.get(f)) {
				ImFunction caller = call.getNearestFunc();
				ImExpr stExpr;
				if (isMainOrConfig(caller)) {
					stExpr = str("   " + f.getName());
				} else {
					ImVar stackTraceVar = getStackTraceVar(caller); 
					String callPos = "\n   " + call.attrTrace().attrSource().printShort();
					stExpr = JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(
							str(callPos),
							JassIm.ImVarAccess(stackTraceVar)
							));
				}
				call.getArguments().add(stExpr);
			}
		}
	}

	private void rewriteFuncRefs(final List<ImFuncRef> funcRefs, Set<ImFunction> affectedFuncs) {
		// rewrite funcrefs
		for (ImFuncRef fr : funcRefs) {
			ImFunction f = fr.getFunc();
			if (!affectedFuncs.contains(f)) {
				continue;
			}
			
			ImFunction bridgeFunc = JassIm.ImFunction(f.getTrace(), "bridge_" + f.getName(), 
					f.getParameters().copy(), (ImType) f.getReturnType().copy(), JassIm.ImVars(), JassIm.ImStmts(), f.getFlags());
			prog.getFunctions().add(bridgeFunc);
			
			//remove statcktrace var from params
			bridgeFunc.getParameters().remove(getStackTraceVar(bridgeFunc));
			
			ImStmt stmt;
			ImExprs args = JassIm.ImExprs(str("\n   " + fr.attrTrace().attrSource().printShort()));
			ImFunctionCall call = JassIm.ImFunctionCall(fr.attrTrace(), f, args, true, CallType.NORMAL);
			if (bridgeFunc.getReturnType() instanceof ImVoid) {
				stmt = call; 
			} else {
				stmt = JassIm.ImReturn(fr.attrTrace(), call);
			}
			bridgeFunc.getBody().add(stmt);
			
			fr.setFunc(bridgeFunc);
		}
	}

	private void rewriteErrorStatements(
			final Multimap<ImFunction, ImError> errorPrints) {
		//  rewrite error statements
		for (Entry<ImFunction, ImError> e: errorPrints.entries()) {
			ImFunction f = e.getKey();
			ImError s = e.getValue();
			ImExpr message = s.getMessage();
			message.setParent(null);
			
			
			s.setMessage(JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(
					message,
					JassIm.ImVarAccess(getStackTraceVar(f))
					)));
		}
	}

	private ImVar getStackTraceVar(ImFunction f) {
		if (!f.getParameters().isEmpty()) {
			ImVar v = f.getParameters().get(f.getParameters().size()-1);
			if (v.getName().equals(WURST_STACK_TRACE)) {
				return v;
			}
		}
		throw new Error("no stacktrace var found in: " + f.getName());
	}

	private ImExpr str(String s) {
		return JassIm.ImStringVal(s);
	}

}
