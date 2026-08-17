package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstTypeInt;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

/**
 * Removes cyclic functions from a program
 * by putting cyclic functions into one big function
 */
public class CyclicFunctionRemover {


    private final ImProg prog;
    private final TimeTaker timeTaker;
    private final ImTranslator tr;
    private final ImFuncGraph graph;

    public CyclicFunctionRemover(ImTranslator tr, ImProg prog, TimeTaker timeTaker) {
        this.tr = tr;
        this.prog = prog;
        this.timeTaker = timeTaker;
        this.graph = new ImFuncGraph();
    }

    public void work() {
        tr.calculateCallRelationsAndReadVariables();
        List<List<ImFunction>> components = timeTaker.measure("finding cycles",
            () -> graph.findStronglyConnectedComponents(prog.getFunctions())
        );
        timeTaker.measure("removing cycles", () -> removeCycles(components));
    }

    private void removeCycles(List<List<ImFunction>> components) {
        for (List<ImFunction> component : components) {
            if (component.size() > 1) {
                // keep list for order; set for O(1) membership
                Set<ImFunction> funcSet = Collections.newSetFromMap(new IdentityHashMap<>());
                funcSet.addAll(component);
                removeCycle(component, funcSet);
            }
        }
    }

    private void removeCycle(List<ImFunction> funcs, Set<ImFunction> funcSet) {
        List<ImVar> newParameters = Lists.newArrayList();
        Map<ImVar, ImVar> oldToNewVar = new IdentityHashMap<>();

        calculateNewParameters(funcs, newParameters, oldToNewVar);

        de.peeeq.wurstscript.ast.Element trace = funcs.get(0).getTrace();

        ImVar choiceVar = JassIm.ImVar(trace, WurstTypeInt.instance().imTranslateType(tr), "funcChoice", false);

        List<FunctionFlag> flags = Lists.newArrayList();

        ImFunction newFunc = JassIm.ImFunction(trace, makeName(funcs), JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), flags);
        prog.getFunctions().add(newFunc);
        newFunc.getParameters().add(choiceVar);
        newFunc.getParameters().addAll(newParameters);
        checkMergedSignatureFits(funcs, newFunc);

        ImStmts stmts = newFunc.getBody();

        for (int i = 0; i < funcs.size(); i++) {
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

        Map<ImFunction, Integer> funcToIndex = new IdentityHashMap<>();
        for (int i = 0; i < funcs.size(); i++) {
            funcToIndex.put(funcs.get(i), i);
        }
        Map<ImFunction, ImFunction> proxyByOriginal = new IdentityHashMap<>();
        // Rewrite only affected roots:
        // - merged cycle body (contains moved bodies from all old funcs)
        // - callers that directly call any removed function
        // - global inits / other program-level roots that may contain ImFuncRef
        Set<Element> rewriteRoots = new LinkedHashSet<>();
        rewriteRoots.add(newFunc.getBody());
        rewriteRoots.add(prog);
        for (List<ImSet> initStmts : prog.getGlobalInits().values()) {
            rewriteRoots.addAll(initStmts);
        }
        for (ImFunction caller : new ArrayList<>(tr.getCalledFunctions().keySet())) {
            Collection<ImFunction> called = tr.getCalledFunctions().get(caller);
            for (ImFunction c : called) {
                if (funcSet.contains(c)) {
                    rewriteRoots.add(caller.getBody());
                    break;
                }
            }
        }
        for (Element root : rewriteRoots) {
            replaceCalls(funcSet, funcToIndex, newFunc, oldToNewVar, proxyByOriginal, root);
        }

        // Iterate over a snapshot: removing values may remove keys from the live keySet.
        for (ImFunction caller : new ArrayList<>(tr.getCalledFunctions().keySet())) {
            tr.getCalledFunctions().get(caller).removeAll(funcSet);
        }

        // remove the old funcs
        prog.getFunctions().removeAll(funcs);
//		System.out.println("----------------------------------");
//		System.out.println(prog.toString());
//		System.out.println("----------------------------------");
    }

    private void replaceVars(Element e, Map<ImVar, ImVar> oldToNewVar) {
        // process children
        for (int i = 0; i < e.size(); i++) {
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


    private void replaceCalls(Set<ImFunction> funcSet, Map<ImFunction, Integer> funcToIndex, ImFunction newFunc,
                              Map<ImVar, ImVar> oldToNewVar, Map<ImFunction, ImFunction> proxyByOriginal, Element e) {
        ArrayDeque<Element> stack = new ArrayDeque<>();
        stack.push(e);
        while (!stack.isEmpty()) {
            Element current = stack.pop();
            if (current instanceof ImFuncRef) {
                replaceImFuncRef(funcSet, funcToIndex, newFunc, oldToNewVar, proxyByOriginal, (ImFuncRef) current);
            } else if (current instanceof ImFunctionCall) {
                replaceImFunctionCall(funcSet, funcToIndex, newFunc, oldToNewVar, (ImFunctionCall) current);
            }
            for (int i = current.size() - 1; i >= 0; i--) {
                stack.push(current.get(i));
            }
        }
    }

    private void replaceImFuncRef(Set<ImFunction> funcSet, Map<ImFunction, Integer> funcToIndex, ImFunction newFunc,
                                  Map<ImVar, ImVar> oldToNewVar, Map<ImFunction, ImFunction> proxyByOriginal,
                                  ImFuncRef e) {
        ImFuncRef fr = e;
        ImFunction f = fr.getFunc();
        if (funcSet.contains(f)) {
            ImFunction proxyFunc = proxyByOriginal.get(f);
            if (proxyFunc == null) {
                proxyFunc = JassIm.ImFunction(f.attrTrace(), f.getName() + "_proxy", JassIm.ImTypeVars(), f.getParameters().copy(), f.getReturnType().copy(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
                prog.getFunctions().add(proxyFunc);
                proxyByOriginal.put(f, proxyFunc);

                ImExprs arguments = JassIm.ImExprs();
                for (ImVar p : proxyFunc.getParameters()) {
                    arguments.add(JassIm.ImVarAccess(p));
                }

                ImFunctionCall call = JassIm.ImFunctionCall(fr.attrTrace(), f, JassIm.ImTypeArguments(), arguments, true, CallType.NORMAL);

                if (f.getReturnType() instanceof ImVoid) {
                    proxyFunc.getBody().add(call);
                } else {
                    proxyFunc.getBody().add(JassIm.ImReturn(proxyFunc.getTrace(), call));
                }
                // rewrite the proxy call once per function:
                replaceCalls(funcSet, funcToIndex, newFunc, oldToNewVar, proxyByOriginal, call);
            }
            // change the funcref to use the shared proxy
            fr.setFunc(proxyFunc);
        }
    }

    private void replaceImFunctionCall(Set<ImFunction> funcSet, Map<ImFunction, Integer> funcToIndex, ImFunction newFunc, Map<ImVar, ImVar> oldToNewVar, ImFunctionCall e) {
        ImFunctionCall fc = e;
        ImFunction oldFunc = fc.getFunc();
        if (funcSet.contains(oldFunc)) {

            ImExprs arguments = JassIm.ImExprs();

            // first argument is the choice index
            arguments.add(JassIm.ImIntVal(funcToIndex.get(oldFunc)));

            // now for the actual arguments: each one goes to the slot its parameter was given, and
            // every slot this function does not use gets a default. Walking the two in step instead
            // would require the slots to be handed out in parameter order, which is a constraint on
            // how tightly they can be shared and not one this rewriting needs.
            List<ImExpr> oldArgs = fc.getArguments().removeAll();
            Map<ImVar, ImExpr> argumentBySlot = new IdentityHashMap<>();
            for (int i = 0; i < oldArgs.size() && i < oldFunc.getParameters().size(); i++) {
                argumentBySlot.put(oldToNewVar.get(oldFunc.getParameters().get(i)), oldArgs.get(i));
            }
            for (int i = 1; i < newFunc.getParameters().size(); i++) {
                ImVar p = newFunc.getParameters().get(i);
                ImExpr argument = argumentBySlot.get(p);
                arguments.add(argument != null ? argument : tr.getDefaultValueForJassType(p.getType()));
            }


            ImFunctionCall newCall = JassIm.ImFunctionCall(fc.getTrace(), newFunc, JassIm.ImTypeArguments(), arguments, true, CallType.NORMAL);

            Element ret;
            if (oldFunc.getReturnType() instanceof ImVoid) {
                ret = newCall;
            } else {
                // if there is a return value, use the temporary return value
                ret = JassIm.ImStatementExpr(JassIm.ImStmts(newCall), JassIm.ImVarAccess(getTempReturnVar(oldFunc.getReturnType())));
            }
            fc.replaceBy(ret);

        }
    }

    private void replaceReturn(Element e, ImType returnType) {
        // process children
        for (int i = 0; i < e.size(); i++) {
            replaceReturn(e.get(i), returnType);
        }


        if (e instanceof ImReturn) {
            ImReturn r = (ImReturn) e;

            ImExprOpt returnValue = r.getReturnValue();
            returnValue.setParent(null);
            ImStmts stmts = JassIm.ImStmts(
                    JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(getTempReturnVar(returnType)), (ImExpr) returnValue),
                    JassIm.ImReturn(r.getTrace(), JassIm.ImNoExpr())
            );
            r.replaceBy(ImHelper.statementExprVoid(stmts));
        }

    }

    private final Map<String, ImVar> tempReturnVars = Maps.newLinkedHashMap();

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

    /**
     * Builds the parameters the merged function takes: one slot per parameter which needs to exist at
     * the same time as another, shared by every function in the cycle which can use it.
     * <p>
     * Two parameters of the same function are live at once and so can never share a slot, which is the
     * only constraint here. This used to be enforced by searching for a slot from a position which only
     * moved forwards, which enforces rather more than that: a parameter matching a slot late in the
     * union puts every parameter after it past everything already allocated, so it allocates again. The
     * cost is per function and accumulates along the cycle, and three functions taking the same
     * parameters in different orders already came out at fifteen slots where nine were needed.
     * <p>
     * That is how a merged function ends up over the Jass parameter limit without any function in the
     * source being anywhere near it. Remembering which slots this function has already claimed says the
     * same thing about liveness without the ordering, and needs one slot per type per position at which
     * some function in the cycle uses that type, which is the fewest this scheme can use.
     */
    private void calculateNewParameters(List<ImFunction> funcs,
                                        List<ImVar> newParameters, Map<ImVar, ImVar> oldToNewVar) {
        for (ImFunction f : funcs) {
            Set<ImVar> claimedByThisFunction = Collections.newSetFromMap(new IdentityHashMap<>());
            for (ImVar v : f.getParameters()) {
                ImVar slot = firstFreeSlotOfType(newParameters, claimedByThisFunction, v);
                if (slot == null) {
                    slot = JassIm.ImVar(v.getTrace(), v.getType().copy(), v.getName(), false);
                    newParameters.add(slot);
                }
                claimedByThisFunction.add(slot);
                oldToNewVar.put(v, slot);
            }
        }
    }

    /** The first slot this function has not claimed which holds the same Jass type, if there is one. */
    private @Nullable ImVar firstFreeSlotOfType(List<ImVar> newParameters, Set<ImVar> claimed, ImVar v) {
        for (ImVar candidate : newParameters) {
            if (!claimed.contains(candidate)
                    && candidate.getType().translateType().equals(v.getType().translateType())) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Refuses a merged function the game could not load, rather than emitting one.
     * <p>
     * With slots shared as tightly as they can be this needs a cycle whose functions genuinely need
     * more than 31 Jass parameters live at once, which no reachable source has produced - but the
     * failure it replaces is silent, and a script the game rejects at load is the worst way to find out.
     * Passing the excess through an array indexed by recursion depth would lift the limit, and is worth
     * doing only once something hits this.
     */
    private void checkMergedSignatureFits(List<ImFunction> funcs, ImFunction newFunc) {
        int jassParameterCount = newFunc.getParameters().stream()
            .mapToInt(p -> ImHelper.flattenedJassArity(p.getType()))
            .sum();
        if (jassParameterCount > ImHelper.JASS_MAX_PARAMETERS) {
            StringBuilder names = new StringBuilder();
            for (ImFunction f : funcs) {
                names.append(names.length() == 0 ? "" : ", ").append(f.getName());
            }
            throw new CompileError(newFunc.getTrace(), "These functions call each other in a cycle: "
                + names + ". Jass cannot declare a function before it is defined, so they are compiled"
                + " into one function taking the parameters of all of them, which comes to "
                + jassParameterCount + " Jass parameters and the maximum is "
                + ImHelper.JASS_MAX_PARAMETERS + " (a tuple counts as one per component). Break the"
                + " cycle, or pass fewer values through it.");
        }
    }

    class ImFuncGraph extends GraphInterpreter<ImFunction> {

        @Override
        protected Collection<ImFunction> getIncidentNodes(ImFunction f) {
            return tr.getCalledFunctions().get(f);
        }

    }


}
