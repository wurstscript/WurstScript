package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstTypeInt;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
        tr.calculateCallRelationsAndUsedVariables();
        AtomicReference<List<List<ImFunction>>> components = new AtomicReference<>();
        timeTaker.measure("finding cycles",
            () -> components.set(graph.findStronglyConnectedComponents(prog.getFunctions()))
        );
        timeTaker.measure("removing cycles", () -> removeCycles(components));
    }

    private void removeCycles(AtomicReference<List<List<ImFunction>>> componentsRef) {
        for (List<ImFunction> component : componentsRef.get()) {
            if (component.size() > 1) {
                // keep list for order; set for O(1) membership
                Set<ImFunction> funcSet = new HashSet<>(component);
                removeCycle(component, funcSet);
            }
        }
    }

    private void removeCycle(List<ImFunction> funcs, Set<ImFunction> funcSet) {
        List<ImVar> newParameters = Lists.newArrayList();
        Map<ImVar, ImVar> oldToNewVar = Maps.newLinkedHashMap();

        calculateNewParameters(funcs, newParameters, oldToNewVar);

        de.peeeq.wurstscript.ast.Element trace = funcs.get(0).getTrace();

        ImVar choiceVar = JassIm.ImVar(trace, WurstTypeInt.instance().imTranslateType(tr), "funcChoice", false);

        List<FunctionFlag> flags = Lists.newArrayList();

        ImFunction newFunc = JassIm.ImFunction(trace, makeName(funcs), JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), flags);
        prog.getFunctions().add(newFunc);
        newFunc.getParameters().add(choiceVar);
        newFunc.getParameters().addAll(newParameters);

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

        Map<ImFunction, Integer> funcToIndex = new HashMap<>();
        for (int i = 0; i < funcs.size(); i++) {
            funcToIndex.put(funcs.get(i), i);
        }
        replaceCalls(funcSet, funcToIndex, newFunc, oldToNewVar, prog);

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


    private void replaceCalls(Set<ImFunction> funcSet, Map<ImFunction, Integer> funcToIndex, ImFunction newFunc, Map<ImVar, ImVar> oldToNewVar, Element e) {
        List<Element> relevant = new ArrayList<>();
        e.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFuncRef imFuncRef) {
                super.visit(imFuncRef);
                relevant.add(imFuncRef);
            }

            @Override
            public void visit(ImFunctionCall imFunctionCall) {
                super.visit(imFunctionCall);
                relevant.add(imFunctionCall);
            }
        });
        for (Element relevantElem : relevant) {
            if (relevantElem instanceof ImFuncRef) {
                replaceImFuncRef(funcSet, funcToIndex, newFunc, oldToNewVar, (ImFuncRef) relevantElem);
            } else if (relevantElem instanceof ImFunctionCall) {
                replaceImFunctionCall(funcSet, funcToIndex, newFunc, oldToNewVar, (ImFunctionCall) relevantElem);
            }
        }
    }

    private void replaceImFuncRef(Set<ImFunction> funcSet, Map<ImFunction, Integer> funcToIndex, ImFunction newFunc, Map<ImVar, ImVar> oldToNewVar, ImFuncRef e) {
        ImFuncRef fr = e;
        ImFunction f = fr.getFunc();
        if (funcSet.contains(f)) {

            ImFunction proxyFunc = JassIm.ImFunction(f.attrTrace(), f.getName() + "_proxy", JassIm.ImTypeVars(), f.getParameters().copy(), f.getReturnType().copy(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
            prog.getFunctions().add(proxyFunc);

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
            // rewrite the proxy call:
            replaceCalls(funcSet, funcToIndex, newFunc, oldToNewVar, call);
            // change the funcref to use the proxy
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

            // now for the actual arguments
            List<ImExpr> oldArgs = fc.getArguments().removeAll();
            int pos = 0;
            for (int i = 1; i < newFunc.getParameters().size(); i++) {
                ImVar p = newFunc.getParameters().get(i);
                if (pos < oldArgs.size() && oldToNewVar.get(oldFunc.getParameters().get(pos)) == p) {
                    arguments.add(oldArgs.get(pos));
                    pos++;
                } else {
                    // use default value
                    arguments.add(tr.getDefaultValueForJassType(p.getType()));
                }
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

    private void calculateNewParameters(List<ImFunction> funcs,
                                        List<ImVar> newParameters, Map<ImVar, ImVar> oldToNewVar) {
        for (ImFunction f : funcs) {
            int pos = 0;
            withNextParameter:
            for (ImVar v : f.getParameters()) {
                // first check if we can reuse a parameter from the newParameters
                for (int i = pos; i < newParameters.size(); i++) {
                    if (newParameters.get(i).getType().translateType().equals(v.getType().translateType())) {
                        // found a var we can reuse
                        oldToNewVar.put(v, newParameters.get(i));
                        pos = i + 1;
                        continue withNextParameter;
                    }
                }
                // otherwise, we have to create a new var:
                ImVar newVar = JassIm.ImVar(v.getTrace(), v.getType().copy(), v.getName(), false);
                oldToNewVar.put(v, newVar);
                newParameters.add(newVar);
                pos = newParameters.size() + 1;
            }
        }
    }

    class ImFuncGraph extends GraphInterpreter<ImFunction> {

        @Override
        protected Collection<ImFunction> getIncidentNodes(ImFunction f) {
            return tr.getCalledFunctions().get(f);
        }

    }


}


