package de.peeeq.wurstscript.intermediatelang.optimizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.*;

/**
 * merges local variable, if they have disjoint live-spans
 * <p>
 * the input must be a flattened program
 */
public class LocalMerger implements OptimizerPass {
    private int totalLocalsMerged = 0;

    @Override
    public int optimize(ImTranslator trans) {
        ImProg prog = trans.getImProg();
        totalLocalsMerged = 0;
        for (ImFunction func : prog.getFunctions()) {
            if (!func.isNative() && !func.isBj()) {
                optimizeFunc(func);
            }
        }
        return totalLocalsMerged;
    }


    @Override
    public String getName() {
        return "Local variables merged";
    }

    private void optimizeFunc(ImFunction func) {
        Map<ImStmt, Set<ImVar>> livenessInfo = calculateLiveness(func);
        eliminateDeadCode(livenessInfo);
        mergeLocals(livenessInfo, func);
    }

    private void mergeLocals(Map<ImStmt, Set<ImVar>> livenessInfo, ImFunction func) {
        Multimap<ImVar, ImVar> inferenceGraph = calculateInferenceGraph(livenessInfo);

        // priority queue, sorted by number of inferring vars
        PriorityQueue<ImVar> vars = new PriorityQueue<>((ImVar a, ImVar b) ->
                inferenceGraph.get(b).size() - inferenceGraph.get(a).size());
        vars.addAll(inferenceGraph.keySet());
        // do not merge parameters (this would not work)
        vars.removeAll(func.getParameters());

        // variables which represent their own 'color', initially these are the parameters
        List<ImVar> assigned = new ArrayList<>(func.getParameters());

        Map<ImVar, ImVar> merges = new HashMap<>();

        nextVar:
        while (!vars.isEmpty()) {
            ImVar v = vars.poll();
//			System.out.println("v = " + v + " // " + inferenceGraph.get(v));

            // check if there is some other variable which is already assigned, has the same type and does not interfere
            nextAssigned:
            for (ImVar other : assigned) {
                if (other.getType().equalsType(v.getType())) {
                    for (ImVar inferingVar : inferenceGraph.get(v)) {
                        if (merges.getOrDefault(inferingVar, inferingVar) == other) {
                            // variable already used by infering var, try next color
                            continue nextAssigned;
                        }
                    }
                    // found a color to merge
                    merges.put(v, other);
                    continue nextVar;
                }
            }
            assigned.add(v);
        }

        totalLocalsMerged += merges.size();

//		System.out.println("merges = " + merges);
        func.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                ImVar v = va.getVar();
                if (merges.containsKey(v)) {
                    va.setVar(merges.get(v));
                }
            }

            @Override
            public void visit(ImSet set) {
                super.visit(set);
                if (set.getLeft() instanceof ImVarAccess) {
                    ImVar v = ((ImVarAccess) set.getLeft()).getVar();
                    if (merges.containsKey(v)) {
                        set.setLeft(JassIm.ImVarAccess(merges.get(v)));
                    }
                }
            }
        });
    }

    private Multimap<ImVar, ImVar> calculateInferenceGraph(Map<ImStmt, Set<ImVar>> livenessInfo) {
        Multimap<ImVar, ImVar> inferenceGraph = HashMultimap.create();
        for (ImStmt s : livenessInfo.keySet()) {
            Collection<ImVar> live = livenessInfo.get(s);
            for (ImVar v1 : live) {
                for (ImVar v2 : live) {
                    if (v1.getType().equalsType(v2.getType())) {
                        inferenceGraph.put(v1, v2);
                    }
                }
            }
        }
        return inferenceGraph;
    }

    private void eliminateDeadCode(Map<ImStmt, Set<ImVar>> livenessInfo) {
        for (ImStmt s : livenessInfo.keySet()) {
            if (s instanceof ImSet) {
                ImSet imSet = (ImSet) s;
                if (!(imSet.getLeft() instanceof ImVarAccess)) {
                    continue;
                }
                ImVarAccess va = (ImVarAccess) imSet.getLeft();
                ImVar v = va.getVar();
                if (v.isGlobal()) {
                    continue;
                }

                if (!livenessInfo.get(s).contains(v)) {
                    // write to a variable which is not live
                    // --> only keep side effects
                    ImExpr right = imSet.getRight();
                    right.setParent(null);
                    s.replaceBy(right);
                }
            }
        }
    }


    private Map<ImStmt, Set<ImVar>> calculateLiveness(ImFunction func) {
        ControlFlowGraph cfg = new ControlFlowGraph(func.getBody());
        Map<Node, Set<ImVar>> in = new HashMap<>();
        Map<Node, Set<ImVar>> out = new HashMap<>();

        Deque<Node> todo = new ArrayDeque<>();

        // init in and out with empty sets
        for (Node node : cfg.getNodes()) {
            in.put(node, Collections.emptySet());
            out.put(node, Collections.emptySet());
            todo.addFirst(node);
        }
        // calculate def- and use- sets for each node
        Multimap<Node, ImVar> def = calculateDefs(cfg.getNodes());
        Multimap<Node, ImVar> use = calculateUses(cfg.getNodes());
//        boolean changes = true;
//        int iterations = 0;
        while (!todo.isEmpty()) {
            Node node = todo.poll();
//            iterations++;
            // in[n] = use[n] + (out[n] - def[n])
            Set<ImVar> newIn = new HashSet<>(out.get(node));
            newIn.removeAll(def.get(node));
            newIn.addAll(use.get(node));

            // out[n] = union s in succ[n]: in[s]
            Set<ImVar> newOut = new HashSet<>();
            for (Node s : node.getSuccessors()) {
                newOut.addAll(in.get(s));
            }

            if (!newIn.equals(in.get(node))) {
                in.put(node, newIn);
                // if in changes, then all predecessors have to be recalculated
                for (Node pred : node.getPredecessors()) {
                    todo.addLast(pred);
                }
            }
            if (!newOut.equals(out.get(node))) {
                out.put(node, newOut);
                // if out changes, then this node has to be recalculated
                todo.addLast(node);
            }
        }
//		System.out.println("result after " + iterations + " iterations in func " + func.getName());
//		System.out.println("//#########################################");
//		System.out.println("// liveness for " + func.getName());
//		for (Node node : nodes) {
//			System.out.println(" // " + in.get(node));
//			System.out.println(node);
//			System.out.println(" // " + out.get(node));
//		}

        Map<ImStmt, Set<ImVar>> result = new HashMap<>();
        for (Node node : cfg.getNodes()) {
            ImStmt stmt = node.getStmt();
            if (stmt != null) {
                result.put(stmt, ImmutableSet.copyOf(out.get(node)));
            }
        }
        return result;
    }

    private Multimap<Node, ImVar> calculateUses(List<Node> nodes) {
        Multimap<Node, ImVar> result = HashMultimap.create();
        for (Node node : nodes) {
            ImStmt stmt = node.getStmt();
            if (stmt != null) {
                stmt.accept(new ImStmt.DefaultVisitor() {
                    @Override
                    public void visit(ImVarAccess va) {
                        super.visit(va);
                        if (!va.getVar().isGlobal()) {
                            result.put(node, va.getVar());
                        }
                    }
                });
            }
        }
        return result;
    }

    private Multimap<Node, ImVar> calculateDefs(List<Node> nodes) {
        Multimap<Node, ImVar> result = HashMultimap.create();
        for (Node node : nodes) {
            ImStmt stmt = node.getStmt();
            if (stmt instanceof ImSet) {
                ImSet imSet = (ImSet) stmt;
                if (imSet.getLeft() instanceof ImVarAccess) {
                    ImVar v = ((ImVarAccess) imSet.getLeft()).getVar();
                    if (!v.isGlobal()) {
                        result.put(node, v);
                    }
                }
            }
        }
        return result;
    }


}
