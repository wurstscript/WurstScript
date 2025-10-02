package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;

public class LocalMerger implements OptimizerPass {
    private int totalLocalsMerged = 0;

    @Override
    public int optimize(ImTranslator trans) {
        ImProg prog = trans.getImProg();
        totalLocalsMerged = 0;
        for (ImFunction func : de.peeeq.wurstscript.translation.imtranslation.ImHelper.calculateFunctionsOfProg(prog)) {
            if (!func.isNative() && !func.isBj()) {
                optimizeFunc(func);
            }
        }
        return totalLocalsMerged;
    }

    @Override
    public String getName() { return "Local variables merged"; }

    void optimizeFunc(ImFunction func) {
        Map<ImStmt, Set<ImVar>> livenessInfo = calculateLiveness(func);
        eliminateDeadCode(livenessInfo);
        mergeLocals(livenessInfo, func);
    }

    private boolean canMerge(ImType a, ImType b) { return a.equalsType(b); }

    private void mergeLocals(Map<ImStmt, Set<ImVar>> livenessInfo, ImFunction func) {
        Map<ImVar, Set<ImVar>> interference = calculateInferenceGraph(livenessInfo);

        PriorityQueue<ImVar> queue = new PriorityQueue<>(
            (x, y) -> interference.get(y).size() - interference.get(x).size()
        );
        queue.addAll(interference.keySet());

        List<ImVar> params = new ArrayList<>(func.getParameters());
        if (func.hasFlag(de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG) && !params.isEmpty()) {
            params.remove(params.size() - 1);
        }
        queue.removeAll(func.getParameters());

        List<ImVar> colors = new ArrayList<>(params);
        Map<ImVar, ImVar> merges = new LinkedHashMap<>();

        while (!queue.isEmpty()) {
            ImVar v = queue.poll();
            boolean merged = false;

            for (ImVar color : colors) {
                if (!canMerge(color.getType(), v.getType())) continue;

                boolean conflict = false;
                for (ImVar neigh : interference.get(v)) {
                    if (merges.getOrDefault(neigh, neigh) == color) { conflict = true; break; }
                }
                if (!conflict) { merges.put(v, color); merged = true; break; }
            }
            if (!merged) colors.add(v);
        }

        applyMerges(func, merges);
        int removed = removeUnusedLocals(func);
        totalLocalsMerged += removed;
    }

    private static void applyMerges(ImFunction func, Map<ImVar, ImVar> merges) {
        if (merges.isEmpty()) return;

        func.accept(new ImFunction.DefaultVisitor() {
            @Override public void visit(ImVarAccess va) {
                super.visit(va);
                ImVar m = merges.get(va.getVar());
                if (m != null) va.setVar(m);
            }
            @Override public void visit(ImSet set) {
                super.visit(set);
                if (set.getLeft() instanceof ImVarAccess) {
                    ImVar m = merges.get(((ImVarAccess) set.getLeft()).getVar());
                    if (m != null) {
                        ImVarAccess newAccess = JassIm.ImVarAccess(m);
                        set.getLeft().replaceBy(newAccess);
                    }
                }
            }
            @Override public void visit(ImVarargLoop varargLoop) {
                super.visit(varargLoop);
                ImVar m = merges.get(varargLoop.getLoopVar());
                if (m != null) varargLoop.setLoopVar(m);
            }
        });
    }

    private static int removeUnusedLocals(ImFunction f) {
        final java.util.Set<ImVar> used = new java.util.HashSet<>();
        used.addAll(f.getParameters());
        f.getBody().accept(new Element.DefaultVisitor() {
            @Override public void visit(ImVarAccess va) { super.visit(va); used.add(va.getVar()); }
            @Override public void visit(ImMemberAccess ma) { super.visit(ma); used.add(ma.getVar()); }
            @Override public void visit(ImVarArrayAccess vaa) { super.visit(vaa); used.add(vaa.getVar()); }
        });
        List<ImVar> locals = new ArrayList<>(f.getLocals());
        int before = locals.size();
        List<ImVar> kept = new ArrayList<>(locals.size());
        for (ImVar v : locals) if (used.contains(v)) kept.add(v);
        if (kept.size() != locals.size()) { f.getLocals().clear(); f.getLocals().addAll(kept); }
        return before - kept.size();
    }

    private Map<ImVar, Set<ImVar>> calculateInferenceGraph(Map<ImStmt, Set<ImVar>> livenessInfo) {
        Map<ImVar, Set<ImVar>> g = new LinkedHashMap<>();
        for (Map.Entry<ImStmt, Set<ImVar>> e : livenessInfo.entrySet()) {
            Set<ImVar> live = e.getValue();
            for (ImVar v1 : live) {
                Set<ImVar> set = g.getOrDefault(v1, HashSet.empty());
                set = set.addAll(live.filter(v2 -> canMerge(v1.getType(), v2.getType())));
                g.put(v1, set);
            }
        }
        return g;
    }

    private void eliminateDeadCode(Map<ImStmt, Set<ImVar>> livenessInfo) {
        for (ImStmt s : livenessInfo.keySet()) {
            if (!(s instanceof ImSet)) continue;

            ImSet set = (ImSet) s;
            ImLExpr lhs = set.getLeft();

            if (lhs instanceof ImVarAccess && set.getRight() instanceof ImVarAccess) {
                if (((ImVarAccess) lhs).getVar() == ((ImVarAccess) set.getRight()).getVar()) {
                    s.replaceBy(ImHelper.nullExpr());
                    continue;
                }
            }

            ImVar v = null;
            if (lhs instanceof ImVarAccess) {
                v = ((ImVarAccess) lhs).getVar();
            } else if (lhs instanceof ImTupleSelection) {
                v = TypesHelper.getSimpleAndPureTupleVar((ImTupleSelection) lhs);
            }

            if (v == null || v.isGlobal()) continue;

            if (!livenessInfo.get(s).contains(v)) {
                final List<ImExpr> raw = new ArrayList<>();
                collectLhsSideEffects(lhs, raw);
                if (hasSideEffects(set.getRight())) raw.add(set.getRight());

                if (raw.isEmpty()) {
                    AstEdits.deleteStmt(s);  // remove the dead assignment entirely
                } else {
                    ImStmts block = JassIm.ImStmts();
                    for (ImExpr e : raw) {
                        // wrap expression as a statement; add a *copy* to avoid re-parenting conflicts
                        block.add(ImHelper.statementExprVoid(e.copy()));
                    }
                    AstEdits.replaceStmtWithMany(s, block); // removes 's', then inserts the new stmts
                }
            }
        }
    }

    private static void collectLhsSideEffects(ImLExpr lhs, List<ImExpr> out) {
        if (lhs instanceof ImVarArrayAccess a) {
            for (ImExpr idx : a.getIndexes()) if (hasSideEffects(idx)) out.add(idx);
        } else if (lhs instanceof ImMemberAccess m) {
            if (hasSideEffects(m.getReceiver())) out.add(m.getReceiver());
            for (ImExpr idx : m.getIndexes()) if (hasSideEffects(idx)) out.add(idx);
        } else if (lhs instanceof ImTupleSelection ts) {
            Element t = ts.getTupleExpr();
            if (hasSideEffects(t)) out.add((ImExpr) t);
        }
    }


    private static boolean hasSideEffects(Element e) {
        if (e instanceof ImFunctionCall || e instanceof ImMethodCall) return true;
        for (int i = 0; i < e.size(); i++) if (hasSideEffects(e.get(i))) return true;
        return false;
    }

    /**
     * Calculates liveness for each statement using a fixed-point iteration
     * over the strongly connected components of the control flow graph.
     */
    public Map<ImStmt, Set<ImVar>> calculateLiveness(ImFunction func) {
        // 1. Build Control Flow Graph
        ControlFlowGraph cfg = new ControlFlowGraph(func.getBody());
        final List<Node> nodes = cfg.getNodes();
        final int N = nodes.size();

        // Map nodes to indices for quick array access
        final Object2IntOpenHashMap<Node> idx = new Object2IntOpenHashMap<>(N);
        idx.defaultReturnValue(-1);
        for (int i = 0; i < N; i++) idx.put(nodes.get(i), i);

        // 2. Calculate USE and DEF sets for each node
        @SuppressWarnings("unchecked") final ObjectOpenHashSet<ImVar>[] use = new ObjectOpenHashSet[N];
        @SuppressWarnings("unchecked") final ObjectOpenHashSet<ImVar>[] def = new ObjectOpenHashSet[N];

        for (int i = 0; i < N; i++) {
            Node node = nodes.get(i);
            use[i] = new ObjectOpenHashSet<>();
            def[i] = new ObjectOpenHashSet<>();

            ImStmt stmt = node.getStmt();
            if (stmt == null) continue;

            final int ii = i;
            stmt.accept(new ImStmt.DefaultVisitor() {
                @Override public void visit(ImVarAccess va) {
                    super.visit(va);
                    ImVar v = va.getVar();
                    if (!v.isGlobal()) use[ii].add(v);
                }
                @Override public void visit(ImSet set) {
                    set.getRight().accept(this);
                    Element.DefaultVisitor me = this;
                    set.getLeft().match(new ImLExpr.MatcherVoid() {
                        @Override public void case_ImTupleSelection(ImTupleSelection e) { ((ImLExpr) e.getTupleExpr()).match(this); }
                        @Override public void case_ImVarAccess(ImVarAccess e) {}
                        @Override public void case_ImVarArrayAccess(ImVarArrayAccess e) { e.getIndexes().accept(me); }
                        @Override public void case_ImMemberAccess(ImMemberAccess e) { e.getReceiver().accept(me); e.getIndexes().accept(me); }
                        @Override public void case_ImStatementExpr(ImStatementExpr e) { e.getStatements().accept(me); ((ImLExpr) e.getExpr()).match(this); }
                        @Override public void case_ImTupleExpr(ImTupleExpr e) { for (ImExpr ex : e.getExprs()) ((ImLExpr) ex).match(this); }
                    });
                }
            });

            if (stmt instanceof ImSet) {
                ImSet set = (ImSet) stmt;
                if (set.getLeft() instanceof ImVarAccess) {
                    ImVar v = ((ImVarAccess) set.getLeft()).getVar();
                    if (!v.isGlobal()) def[i].add(v);
                }
            }
        }

        // 3. Find SCCs on the REVERSED graph for backward analysis
        GraphInterpreter<Node> reverseCfgInterpreter = new GraphInterpreter<>() {
            @Override
            protected Collection<Node> getIncidentNodes(Node t) {
                // For backward analysis, we traverse predecessors
                return t.getPredecessors();
            }
        };
        // Use the path-based strong component algorithm [1] on the reversed CFG.
        // It returns SCCs in reverse topological order of the graph it is given.
        List<List<Node>> sccs = reverseCfgInterpreter.findStronglyConnectedComponents(nodes);
        // For a backward analysis, we need to process SCCs in reverse topological order of the original CFG.
        // The algorithm on the reversed graph gives a topological sort of the original graph's SCCs.
        // Therefore, we reverse the list to get the required processing order.
        Collections.reverse(sccs);

        // 4. Initialize IN and OUT sets for the data-flow analysis
        @SuppressWarnings("unchecked") final ObjectOpenHashSet<ImVar>[] in  = new ObjectOpenHashSet[N];
        @SuppressWarnings("unchecked") final ObjectOpenHashSet<ImVar>[] out = new ObjectOpenHashSet[N];
        for (int i = 0; i < N; i++) { in[i] = new ObjectOpenHashSet<>(); out[i] = new ObjectOpenHashSet<>(); }

        // 5. Iterate over SCCs in reverse topological order
        for (List<Node> scc : sccs) {
            if (scc.isEmpty()) continue;

            // Iterate within this SCC until a fixed point is reached for all its nodes.
            boolean changedInScc = true;
            while (changedInScc) {
                changedInScc = false;
                for (Node u_node : scc) {
                    int u_idx = idx.getInt(u_node);

                    // Recalculate OUT[u] from the IN sets of its successors.
                    // Any successor not in the current SCC has already been processed and its IN set is stable.
                    final ObjectOpenHashSet<ImVar> newOut = new ObjectOpenHashSet<>();
                    for (Node succ : u_node.getSuccessors()) {
                        int v_idx = idx.getInt(succ);
                        if (v_idx != -1) {
                            newOut.addAll(in[v_idx]);
                        }
                    }
                    out[u_idx] = newOut;

                    // Recalculate IN[u] using the data-flow equation: in[u] = use[u] U (out[u] - def[u])
                    final ObjectOpenHashSet<ImVar> oldIn = in[u_idx];
                    final ObjectOpenHashSet<ImVar> newIn = new ObjectOpenHashSet<>();
                    newIn.addAll(newOut);
                    newIn.removeAll(def[u_idx]);
                    newIn.addAll(use[u_idx]);

                    // If IN[u] changed, update it and flag that we need another iteration for this SCC.
                    if (!newIn.equals(oldIn)) {
                        in[u_idx] = newIn;
                        changedInScc = true;
                    }
                }
            }
        }

        // 6. Collect results into the final map format
        final java.util.LinkedHashMap<ImStmt, Set<ImVar>> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < N; i++) {
            ImStmt stmt = nodes.get(i).getStmt();
            if (stmt != null) {
                result.put(stmt, io.vavr.collection.HashSet.ofAll(out[i]));
            }
        }
        return result;
    }
}
