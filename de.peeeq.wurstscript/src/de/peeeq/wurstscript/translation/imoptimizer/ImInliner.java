package de.peeeq.wurstscript.translation.imoptimizer;

import static de.peeeq.wurstscript.jassIm.JassIm.ImStatementExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class ImInliner {
	
	private ImTranslator translator;
	private ImProg prog;
	private Set<ImFunction> inlinableFunctions = Sets.newHashSet();
	private Map<ImFunction, Integer> callCounts = Maps.newHashMap();
	private Map<ImFunction, Integer> funcSizes = Maps.newHashMap();
	private Set<ImFunction> done = Sets.newHashSet();
	
	public ImInliner(ImTranslator translator) {
		this.translator = translator;
		this.prog = translator.getImProg();
	}
	
	public void doInlining() {
		prog.flatten(translator);
		collectInlinableFunctions();
		rateInitalizableFunctions();
		inlineFunctions();
	}

	private void inlineFunctions() {
		
		for(ImFunction f : prog.getFunctions())  {
			inlineFunctions(f);
		}
	}

	private void inlineFunctions(ImFunction f) {
		if (done.contains(f)) {
			return;
		}
		done.add(f);
		// first inline functions called from this function
		for (ImFunction called : translator.getCalledFunctions().get(f)) {
			inlineFunctions(called);
		}
		boolean[] changed = new boolean[] {false};
		inlineFunctions(f, f, 0, f.getBody(), changed);
		if (changed[0]) {
			funcSizes.put(f, estimateSize(f));
		}
	}

	private boolean inlineFunctions(ImFunction f, JassImElement parent, int parentI, JassImElement e, boolean[] changed) {
		if (e instanceof ImFunctionCall) {
			ImFunctionCall call = (ImFunctionCall) e;
			ImFunction called = call.getFunc();
			if (f != called && shouldInline(called)) {
				inlineCall(f, parent, parentI, call);
//				translator.removeCallRelation(f, called); // XXX is it safe to remove this call relation?
				changed[0] = true;
				return true;
			}
		}
		for (int i=0; i<e.size(); i++) {
			JassImElement child = e.get(i);
			
			boolean recur = inlineFunctions(f, e, i, child, changed);
			if (recur) {
				// check inlined again
				i--;
			}
		}
		return false;
	}

	private void inlineCall(ImFunction f, JassImElement parent, int parentI, ImFunctionCall call) {
		ImFunction called = call.getFunc();
		if (called == f) {
			throw new Error("cannot inline self.");
		}
		List<ImStmt> stmts = Lists.newArrayList();
		// save arguments to temp vars:
		List<ImExpr> args = call.getArguments().removeAll();
		Map<ImVar, ImVar> varSubtitutions = Maps.newHashMap();
		for (int pi =0; pi < called.getParameters().size(); pi++) {
			ImVar param = called.getParameters().get(pi);
			ImExpr arg = args.get(pi);
			ImVar tempVar = JassIm.ImVar(param.getType(), param.getName(), false);
			f.getLocals().add(tempVar);
			varSubtitutions.put(param, tempVar);
			// set temp var
			stmts.add(JassIm.ImSet(arg.attrTrace(), tempVar, arg));
		}
		// add locals
		for (ImVar l : called.getLocals()) {
			ImVar newL = JassIm.ImVar(l.getType(), l.getName(), false);
			f.getLocals().add(newL);
			varSubtitutions.put(l, newL);
		}
		// add body and replace params with tempvars
		for (int i=0; i<called.getBody().size(); i++) {
			ImStmt s = (ImStmt) called.getBody().get(i).copy();
			ImHelper.replaceVar(s, varSubtitutions);
			stmts.add(s);
		}
		// handle return
		ImExpr newExpr = null;
		if (stmts.size() > 0) {
			ImStmt lastStmt = stmts.get(stmts.size()-1);
			if (lastStmt instanceof ImReturn) {
				ImReturn ret = (ImReturn) lastStmt;
				stmts.remove(stmts.size()-1);
				ImExprOpt valOpt = ret.getReturnValue();
				if (valOpt instanceof ImExpr) {
					ImExpr val = (ImExpr) valOpt.copy();
					ImHelper.replaceVar(val, varSubtitutions);
					newExpr = ImStatementExpr(ImStmts(stmts), val);
				}
			}
		}
		if (newExpr == null) {
			newExpr = JassIm.ImStatementExpr(ImStmts(stmts), JassIm.ImNull());
		}
		parent.set(parentI, newExpr);
		
	}

	private void rateInitalizableFunctions() {
		for (ImFunction f : translator.getCalledFunctions().values()) {
			incCallCount(f);
		}
		for (ImFunction f : inlinableFunctions) {
			int size = estimateSize(f);
			funcSizes.put(f, size);
		}
	}

	private double getRating(ImFunction f) {
		if (f.isNative() || !inlinableFunctions.contains(f)) {
			return Double.MAX_VALUE;
		}
		
		double callCount = getCallCount(f);
		double size = getFuncSize(f);
		double rating = size * callCount;
		return rating;
	}

	private int getFuncSize(ImFunction f) {
		Integer size = funcSizes.get(f);
		if (size != null) {
			return size;
		} else {
			return Integer.MAX_VALUE;
		}
	}
	
	private boolean shouldInline(ImFunction f) {
		if (f.isNative()) {
			return false;
		}
		System.out.println("Should I inline function " + f.getName() + "?");
		System.out.println("	ininable: " + inlinableFunctions.contains(f));
		System.out.println("	rating: " + getRating(f));
		return  inlinableFunctions.contains(f) 
				&& getRating(f) < 50;
	}
	
	private int estimateSize(ImFunction f) {
		int[] r = new int[]{0};
		estimateSize(f.getBody(), r);
		return r[0];
	}

	private void estimateSize(JassImElement e, int[] r) {
		for (int i=0; i<e.size(); i++) {
			r[0]++;
			estimateSize(e.get(i), r);
		}
	}

	private void incCallCount(ImFunction f) {
		int count = getCallCount(f);
		count++;
		callCounts.put(f, count);
	}

	private int getCallCount(ImFunction f) {
		Integer r = callCounts.get(f);
		if (r == null) {
			return 0;
		}
		return r;
	}

	private void collectInlinableFunctions() {
		for (ImFunction f : prog.getFunctions()) {
			if (f.hasFlag(FunctionFlag.IS_COMPILETIME_NATIVE) || f.hasFlag(FunctionFlag.IS_NATIVE)) {
				// do not inline natives
				continue;
			}
			if (maxOneReturn(f)) {
				inlinableFunctions.add(f);
			}
		}
	}

	private boolean maxOneReturn(ImFunction f) {
		return maxOneReturn(f.getBody());
	}

	private boolean maxOneReturn(ImStmts body) {
		if (body.size() == 0) {
			return true;
		}
		for (int i = 0; i<body.size()-1; i++) {
			if (hasReturn(body.get(i))) {
				return false; 
			}
		}
		if (body.get(body.size()-1) instanceof ImReturn) {
			return true;
		} else if (hasReturn(body.get(body.size()-1))) {
			return false;
		} else {
			return true;
		}
	}

	private boolean hasReturn(final ImStmt s) {
		final boolean[] r = new boolean[]{false};		
		s.accept(new ImStmt.DefaultVisitor() {
			public void visit(ImReturn rs) {
				r[0] = true;
			}
		});
		return r[0];
	}

}
