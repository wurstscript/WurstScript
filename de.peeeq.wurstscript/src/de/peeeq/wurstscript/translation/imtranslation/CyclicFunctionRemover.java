package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.datastructures.GraphInterpreter.TopsortResult;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.utils.Utils;

/**
 * Removes cyclic functions from a program
 * by putting cyclic functions into one big function
 */
public class CyclicFunctionRemover {


	private ImProg prog;
	private ImTranslator tr;
	private ImFuncGraph graph;

	public CyclicFunctionRemover(ImTranslator tr, ImProg prog) {
		this.tr = tr;
		this.prog = prog;
		this.graph = new ImFuncGraph();
	}

	public void work() {
		for (;;) {
			TopsortResult<ImFunction> r = graph.topSort(prog.getFunctions());
			if (r.isCycle()) {
//				System.out.println("Found cycle: ");
//				for (ImFunction f : r.getResult()) {
//					System.out.println("	" + f.getName());
//				}
				removeCycle(r.getResult());
			} else {
				// finished
				return;
			}
		}
	}

	private void removeCycle(List<ImFunction> funcs) {
		List<ImVar> newParameters = Lists.newArrayList();
		Map<ImVar, ImVar> oldToNewVar = Maps.newHashMap();

		calculateNewParameters(funcs, newParameters, oldToNewVar);

		AstElement trace = funcs.get(0).getTrace();

		ImVar choiceVar = JassIm.ImVar(trace, WurstTypeInt.instance().imTranslateType(), "funcChoice", false);

		List<FunctionFlag> flags = Lists.newArrayList();
		ImFunction newFunc = JassIm.ImFunction(trace, makeName(funcs), JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), flags);
		prog.getFunctions().add(newFunc);
		newFunc.getParameters().add(choiceVar);
		newFunc.getParameters().addAll(newParameters);

		ImStmts stmts = newFunc.getBody();

		for (int i=0; i<funcs.size(); i++) {
			ImFunction f = funcs.get(i);
			ImStmts thenBlock = JassIm.ImStmts();

			// add body
			thenBlock.addAll(f.getBody().removeAll());
			// addLocals
			newFunc.getLocals().addAll(f.getLocals().removeAll());

			replaceVars(thenBlock, oldToNewVar);

			if (!(f.getReturnType() instanceof ImVoid)) {
				replaceReturn(thenBlock, f.getReturnType());
			}

			ImStmts elseBlock = JassIm.ImStmts();
			stmts.add(JassIm.ImIf(trace, 
					JassIm.ImOperatorCall(WurstOperator.EQ, 
							JassIm.ImExprs(JassIm.ImVarAccess(choiceVar), JassIm.ImIntVal(i))), 
							thenBlock, 
							elseBlock));
			stmts = elseBlock;
		}

		replaceCalls(funcs, newFunc, oldToNewVar, prog);

		for (ImFunction e : Lists.newArrayList(tr.getCalledFunctions().keys())) {
			Collection<ImFunction> called = tr.getCalledFunctions().get(e);
			called.removeAll(funcs);
		}

		// remove the old funcs
		prog.getFunctions().removeAll(funcs);
//		System.out.println("----------------------------------");
//		System.out.println(prog.toString());
//		System.out.println("----------------------------------");
	}

	private void replaceVars(JassImElement e, Map<ImVar, ImVar> oldToNewVar) {
		// process children
		for (int i=0; i<e.size(); i++) {
			replaceVars(e.get(i), oldToNewVar);
		}
		
		if (e instanceof ImVarAccess) {
			ImVarAccess va = (ImVarAccess) e;
			ImVar newVar = oldToNewVar.get(va.getVar());
			if (newVar != null) {
				va.setVar(newVar);
			}
		}
	}

	private void replaceCalls(List<ImFunction> funcs, ImFunction newFunc, Map<ImVar, ImVar> oldToNewVar, JassImElement e) {
		// process children
		for (int i=0; i<e.size(); i++) {
			replaceCalls(funcs, newFunc, oldToNewVar, e.get(i));
		}


		if (e instanceof ImFuncRef) {
			ImFuncRef fr = (ImFuncRef) e;
			if (funcs.contains(fr.getFunc())) {
				throw new CompileError(fr.attrTrace().attrSource(), "Function references should never be in cycles.");
			}
		} else if (e instanceof ImFunctionCall) {
			ImFunctionCall fc = (ImFunctionCall) e;
			ImFunction oldFunc = fc.getFunc();
			if (funcs.contains(oldFunc)) {

				ImExprs arguments = JassIm.ImExprs();

				// first argument is the choice index
				arguments.add(JassIm.ImIntVal(funcs.indexOf(oldFunc)));

				// now for the actual arguments
				List<ImExpr> oldArgs = fc.getArguments().removeAll();
				int pos = 0;
				for (int i=1; i<newFunc.getParameters().size(); i++) {
					ImVar p = newFunc.getParameters().get(i);
					if (pos < oldArgs.size() && oldToNewVar.get(oldFunc.getParameters().get(pos)) == p) {
						arguments.add(oldArgs.get(pos));
						pos++;
					} else {
						// use default value
						arguments.add(tr.getDefaultValueForJassType(p.getType()));
					}
				}


				ImFunctionCall newCall = JassIm.ImFunctionCall(fc.getTrace(), newFunc, arguments, true, CallType.NORMAL);

				JassImElement ret;
				if (oldFunc.getReturnType() instanceof ImVoid) {
					ret = newCall;
				} else {
					// if there is a return value, use the temporary return value
					ret = JassIm.ImStatementExpr(JassIm.ImStmts(newCall), JassIm.ImVarAccess(getTempReturnVar(oldFunc.getReturnType())));
				}
				fc.replaceWith(ret);

			}
		}


	}

	private void replaceReturn(JassImElement e, ImType returnType) {
		// process children
		for (int i=0; i<e.size(); i++) {
			replaceReturn(e.get(i), returnType);
		}


		if (e instanceof ImReturn) {
			ImReturn r = (ImReturn) e;

			ImExprOpt returnValue = r.getReturnValue();
			returnValue.setParent(null);
			ImStmts stmts = JassIm.ImStmts(
					JassIm.ImSet(r.getTrace(), getTempReturnVar(returnType), (ImExpr) returnValue),
					JassIm.ImReturn(r.getTrace(), JassIm.ImNoExpr())
					);
			r.replaceWith(JassIm.ImStatementExpr(stmts, JassIm.ImNull()));
		}

	}

	private Map<String, ImVar> tempReturnVars = Maps.newHashMap();

	private ImVar getTempReturnVar(ImType t) {
		String typeName = t.translateType();
		ImVar r = tempReturnVars.get(typeName);
		if (r == null) {
			r = JassIm.ImVar(t.attrTrace(), t, "tempReturn_" + typeName, false);
			prog.getGlobals().add(r);
			tempReturnVars.put(typeName, r);
		}
		return r;
	}

	private String makeName(List<ImFunction> funcs) {
		return "cyc_" + funcs.get(0).getName();
	}

	private void calculateNewParameters(List<ImFunction> funcs,
			List<ImVar> newParameters, Map<ImVar, ImVar> oldToNewVar) {
		for (ImFunction f : funcs) {
			int pos = 0;
			withNextParameter: for (ImVar v : f.getParameters()) {
				// first check if we can reuse a parameter from the newParameters
				for (int i=pos; i<newParameters.size(); i++) {
					if (newParameters.get(i).getType().translateType().equals(v.getType().translateType())) {
						// found a var we can reuse
						oldToNewVar.put(v, newParameters.get(i));
						pos = i+1;
						continue withNextParameter;
					}
				}
				// otherwise, we have to create a new var:
				ImVar newVar = JassIm.ImVar(v.getTrace(), (ImType) v.getType().copy(), v.getName(), false);
				oldToNewVar.put(v, newVar);
				newParameters.add(newVar);
				pos = newParameters.size()+1;
			}
		}
	}

	private <T> void incCount(Map<T, Integer> map, T t) {
		if (map.containsKey(t)) {
			map.put(t, map.get(t)+1);
		} else {
			map.put(t, 1);
		}
	}

	class ImFuncGraph extends GraphInterpreter<ImFunction> {

		@Override
		protected Collection<ImFunction> getIncidentNodes(ImFunction f) {
			return tr.getCalledFunctions().get(f);
		}

	}


}


