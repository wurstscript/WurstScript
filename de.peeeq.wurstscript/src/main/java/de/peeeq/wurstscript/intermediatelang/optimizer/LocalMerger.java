package de.peeeq.wurstscript.intermediatelang.optimizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import de.peeeq.datastructures.Worklist;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;
import de.peeeq.wurstscript.translation.imtranslation.ImPrinter;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

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

    void optimizeFunc(ImFunction func) {
        Map<ImStmt, Set<ImVar>> livenessInfo = calculateLiveness(func);
        eliminateDeadCode(livenessInfo);
        mergeLocals(livenessInfo, func);
    }

    private void mergeLocals(Map<ImStmt, Set<ImVar>> livenessInfo, ImFunction func) {
        Map<ImVar, Set<ImVar>> inferenceGraph = calculateInferenceGraph(livenessInfo);

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

    /**
     * for each variable: the set of variables which share some lifetime-range
     */
    private Map<ImVar, Set<ImVar>> calculateInferenceGraph(Map<ImStmt, Set<ImVar>> livenessInfo) {
        Map<ImVar, Set<ImVar>> inferenceGraph = new HashMap<>();
        java.util.Set<ImStmt> keys = livenessInfo.keySet();
        int i  = 0;
        for (ImStmt s : keys) {
            i++;
            Set<ImVar> live = livenessInfo.get(s);
            for (ImVar v1 : live) {
                Set<ImVar> inferenceSet = inferenceGraph.getOrDefault(v1, HashSet.empty());
                inferenceSet = inferenceSet.addAll(live.filter(v2 -> v1.getType().equalsType(v2.getType())));
                inferenceGraph.put(v1, inferenceSet);
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


    public Map<ImStmt, Set<ImVar>> calculateLiveness(ImFunction func) {
        ControlFlowGraph cfg = new ControlFlowGraph(func.getBody());
        Map<Node, Set<ImVar>> in = new HashMap<>();
        Map<Node, Set<ImVar>> out = new HashMap<>();

        Worklist<Node> todo = new Worklist<>();

        Map<Node, Integer> index = new HashMap<>();

        // init in and out with empty sets
        for (Node node : cfg.getNodes()) {
            in.put(node, HashSet.empty());
            out.put(node, HashSet.empty());
            todo.addFirst(node);
            index.put(node, 1+ index.size());
        }

        // calculate def- and use- sets for each node
        Map<Node, Set<ImVar>> def = calculateDefs(cfg.getNodes());
        Map<Node, Set<ImVar>> use = calculateUses(cfg.getNodes());
        while (!todo.isEmpty()) {
            Node node = todo.poll();

            // out[n] = union s in succ[n]: in[s]
            Set<ImVar> newOut = node.getSuccessors()
                .stream()
                .map(in::get)
                .reduce(HashSet.empty(), Set::union);

            // in[n] = use[n] + (out[n] - def[n])
            Set<ImVar> newIn = newOut;
            newIn = newIn.diff(def.get(node));
            newIn = newIn.union(use.get(node));


            if (!newIn.equals(in.get(node))) {
                in.put(node, newIn);
                // if in changes, then all predecessors have to be recalculated
                for (Node pred : node.getPredecessors()) {
                    todo.addLast(pred);
                }
            }
            if (!newOut.equals(out.get(node))) {
                out.put(node, newOut);
            }
        }

        Map<ImStmt, Set<ImVar>> result = new HashMap<>();
        for (Node node : cfg.getNodes()) {
            ImStmt stmt = node.getStmt();
            if (stmt != null) {
                result.put(stmt, out.get(node));
            }
        }
        return result;
    }

    private Map<Node, Set<ImVar>> calculateUses(List<Node> nodes) {
        Map<Node, Set<ImVar>> result = new HashMap<>();
        for (Node node : nodes) {
            List<ImVar> uses = new ArrayList<>();
            ImStmt stmt = node.getStmt();
            if (stmt != null) {
                stmt.accept(new ImStmt.DefaultVisitor() {
                    @Override
                    public void visit(ImVarAccess va) {
                        super.visit(va);
                        if (!va.getVar().isGlobal()) {
                            uses.add(va.getVar());
                        }
                    }

                    @Override
                    public void visit(ImSet set) {
                        set.getRight().accept(this);
                        Element.DefaultVisitor outerThis = this;
                        set.getLeft().match(new ImLExpr.MatcherVoid() {
                            @Override
                            public void case_ImTupleSelection(ImTupleSelection e) {
                                ((ImLExpr) (e.getTupleExpr())).match(this);
                            }

                            @Override
                            public void case_ImVarAccess(ImVarAccess e) {
                            }

                            @Override
                            public void case_ImVarArrayAccess(ImVarArrayAccess e) {
                                e.getIndexes().accept(outerThis);
                            }

                            @Override
                            public void case_ImMemberAccess(ImMemberAccess e) {
                                e.getReceiver().accept(outerThis);
                                e.getIndexes().accept(outerThis);
                            }

                            @Override
                            public void case_ImStatementExpr(ImStatementExpr e) {
                                e.getStatements().accept(outerThis);
                                ((ImLExpr) e.getExpr()).match(this);
                            }

                            @Override
                            public void case_ImTupleExpr(ImTupleExpr e) {
                                for (ImExpr expr : e.getExprs()) {
                                    ((ImLExpr) expr).match(this);
                                }
                            }
                        });
                    }
                });
            }
            result.put(node, HashSet.ofAll(uses));
        }
        return result;
    }

    private Map<Node, Set<ImVar>> calculateDefs(List<Node> nodes) {
        Map<Node, Set<ImVar>>result = new HashMap<>();
        for (Node node : nodes) {
            result.put(node, HashSet.empty());
            ImStmt stmt = node.getStmt();
            if (stmt instanceof ImSet) {
                ImSet imSet = (ImSet) stmt;
                if (imSet.getLeft() instanceof ImVarAccess) {
                    ImVar v = ((ImVarAccess) imSet.getLeft()).getVar();
                    if (!v.isGlobal()) {
                        result.put(node, HashSet.of(v));
                    }
                }
            }
        }
        return result;
    }


}
