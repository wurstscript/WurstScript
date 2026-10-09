package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.Flatten;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import io.vavr.collection.Set;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;

public class LocalMerger implements LocalPlayerAwareOptimizerPass {
    private int totalLocalsMerged = 0;
    private LocalPlayerContextAnalyzer localPlayerContextAnalyzer;
    private ImTranslator translator;

    @Override
    public int optimize(ImTranslator trans, LocalPlayerContextAnalyzer analyzer) {
        ImProg prog = trans.getImProg();
        translator = trans;
        localPlayerContextAnalyzer = analyzer;
        totalLocalsMerged = 0;
        optimizeFunctions(prog.getFunctions());
        List<ImClass> classes = prog.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            optimizeFunctions(classes.get(i).getFunctions());
        }
        return totalLocalsMerged;
    }

    private void optimizeFunctions(List<ImFunction> functions) {
        for (int i = 0; i < functions.size(); i++) {
            ImFunction func = functions.get(i);
            if (!func.isNative() && !func.isBj()) {
                optimizeFunc(func);
            }
        }
    }

    @Override
    public String getName() { return "Local variables merged"; }

    void optimizeFunc(ImFunction func) {
        LivenessAnalysis liveness = analyzeLiveness(func);
        // The liveness is that of the code which a path reaches: a local which only code no path reaches reads (after
        // a loop which nothing leaves, say) is live nowhere, and eliminateDeadCode removes the assignments to it. That
        // code goes too, so that no read is left with no assignment before it, which pjass rejects.
        List<ImStmt> unreachable = liveness.cfg.unreachableStatements();
        if (!unreachable.isEmpty()) {
            for (ImStmt s : unreachable) {
                AstEdits.deleteStmt(s);
            }
            liveness = analyzeLiveness(func);
        }
        Map<ImStmt, Set<ImVar>> livenessInfo = liveness.liveOut;
        eliminateDeadCode(livenessInfo);
        mergeLocals(livenessInfo, liveness.liveAtEntry, func);
    }

    void optimizeFunc(ImFunction func, LocalPlayerContextAnalyzer analyzer) {
        localPlayerContextAnalyzer = analyzer;
        optimizeFunc(func);
    }

    /**
     * Entry point for callers which optimise one function outside {@link #optimize}: the translator
     * identifies the Lua operator intrinsics whose unused calls may be dropped. Without one, every
     * call is kept as a side effect.
     */
    public void optimizeFunc(ImFunction func, LocalPlayerContextAnalyzer analyzer, ImTranslator trans) {
        translator = trans;
        optimizeFunc(func, analyzer);
    }

    private boolean canMerge(ImType a, ImType b) { return a.equalsType(b); }

    private void mergeLocals(Map<ImStmt, Set<ImVar>> livenessInfo, Set<ImVar> liveAtEntry,
        ImFunction func) {
        Map<ImVar, java.util.Set<ImVar>> interference =
            calculateInterferenceGraph(livenessInfo, liveAtEntry, func);

        Map<ImVar, Integer> declarationOrder = new IdentityHashMap<>();
        int nextOrder = 0;
        for (ImVar parameter : func.getParameters()) {
            declarationOrder.put(parameter, nextOrder++);
        }
        for (ImVar local : func.getLocals()) {
            declarationOrder.put(local, nextOrder++);
        }

        PriorityQueue<ImVar> queue = new PriorityQueue<>(
            Comparator.<ImVar>comparingInt(v -> interference.get(v).size()).reversed()
                .thenComparingInt(declarationOrder::get)
        );
        queue.addAll(interference.keySet());

        List<ImVar> colors = new ArrayList<>(func.getParameters());
        if (func.hasFlag(de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG) && !colors.isEmpty()) {
            colors.remove(colors.size() - 1);
        }
        queue.removeAll(func.getParameters());

        Map<ImVar, ImVar> merges = new LinkedHashMap<>();

        while (!queue.isEmpty()) {
            ImVar v = queue.poll();
            boolean merged = false;

            for (int colorIndex = 0; colorIndex < colors.size(); colorIndex++) {
                ImVar color = colors.get(colorIndex);
                if (!canMerge(color.getType(), v.getType())) {
                    continue;
                }
                if (localPlayerContextAnalyzer != null
                    && localPlayerContextAnalyzer.isLocalPlayerDependent(v)
                    != localPlayerContextAnalyzer.isLocalPlayerDependent(color)) {
                    continue;
                }

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
                List<ImVarargLoopVar> loopVars = varargLoop.getLoopVars();
                for (int i = 0; i < loopVars.size(); i++) {
                    ImVarargLoopVar loopVar = loopVars.get(i);
                    ImVar m = merges.get(loopVar.getVar());
                    if (m != null) loopVar.setVar(m);
                }
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
            @Override public void visit(ImVarargLoop loop) {
                super.visit(loop);
                for (int i = 0; i < loop.getLoopVars().size(); i++) {
                    used.add(loop.getLoopVars().get(i).getVar());
                }
            }
        });
        List<ImVar> locals = f.getLocals();
        int before = locals.size();
        List<ImVar> kept = new ArrayList<>(locals.size());
        for (int i = 0; i < locals.size(); i++) {
            ImVar v = locals.get(i);
            if (used.contains(v)) {
                kept.add(v);
            }
        }
        if (kept.size() != locals.size()) { f.getLocals().clear(); f.getLocals().addAll(kept); }
        return before - kept.size();
    }

    private Map<ImVar, java.util.Set<ImVar>> calculateInterferenceGraph(
        Map<ImStmt, Set<ImVar>> livenessInfo, Set<ImVar> liveAtEntry, ImFunction func) {
        Map<ImVar, java.util.Set<ImVar>> graph = new LinkedHashMap<>();
        for (ImVar parameter : func.getParameters()) {
            graph.put(parameter, new ObjectOpenHashSet<>());
        }
        for (ImVar local : func.getLocals()) {
            graph.put(local, new ObjectOpenHashSet<>());
        }

        // A definition interferes with every compatible value that remains live after it.
        // Building only those edges is equivalent to cliquing every live set, while avoiding
        // the old O(statements * liveValues^2) behavior on large inlined functions.
        for (Map.Entry<ImStmt, Set<ImVar>> entry : livenessInfo.entrySet()) {
            List<ImVar> defined = definedLocals(entry.getKey());
            if (defined.isEmpty()) {
                continue;
            }
            for (int i = 0; i < defined.size(); i++) {
                ImVar definition = defined.get(i);
                java.util.Set<ImVar> neighbors = graph.computeIfAbsent(definition, ignored -> new ObjectOpenHashSet<>());
                for (ImVar live : entry.getValue()) {
                    if (live == definition || !canMerge(definition.getType(), live.getType())) {
                        continue;
                    }
                    neighbors.add(live);
                    graph.computeIfAbsent(live, ignored -> new ObjectOpenHashSet<>()).add(definition);
                }
                // Vararg tuple components are assigned at the same loop boundary. They must
                // occupy distinct slots even when neither component is live before the loop.
                for (int j = i + 1; j < defined.size(); j++) {
                    ImVar other = defined.get(j);
                    if (canMerge(definition.getType(), other.getType())) {
                        neighbors.add(other);
                        graph.computeIfAbsent(other, ignored -> new ObjectOpenHashSet<>()).add(definition);
                    }
                }
            }
        }

        // A local live at entry is read before every control-flow path has assigned it. Its
        // target-default value must remain distinct from every incoming parameter and from the
        // other entry-live locals, even if a later assignment eventually defines it.
        List<ImVar> entryDefinitions = new ArrayList<>(func.getParameters());
        for (ImVar local : func.getLocals()) {
            if (liveAtEntry.contains(local)) {
                entryDefinitions.add(local);
            }
        }
        for (int i = 0; i < entryDefinitions.size(); i++) {
            ImVar definition = entryDefinitions.get(i);
            java.util.Set<ImVar> neighbors = graph.get(definition);
            for (int j = i + 1; j < entryDefinitions.size(); j++) {
                ImVar other = entryDefinitions.get(j);
                if (canMerge(definition.getType(), other.getType())) {
                    neighbors.add(other);
                    graph.get(other).add(definition);
                }
            }
        }
        return graph;
    }

    private static List<ImVar> definedLocals(ImStmt stmt) {
        if (stmt instanceof ImVarargLoop loop) {
            List<ImVar> result = new ArrayList<>(loop.getLoopVars().size());
            for (ImVarargLoopVar loopVar : loop.getLoopVars()) {
                result.add(loopVar.getVar());
            }
            return result;
        }
        if (!(stmt instanceof ImSet set)) {
            return Collections.emptyList();
        }
        ImLExpr left = set.getLeft();
        if (left instanceof ImVarAccess access && !access.getVar().isGlobal()) {
            return Collections.singletonList(access.getVar());
        }
        if (left instanceof ImTupleSelection selection) {
            ImVar var = TypesHelper.getSimpleAndPureTupleVar(selection);
            if (var != null && !var.isGlobal()) {
                return Collections.singletonList(var);
            }
        }
        return Collections.emptyList();
    }

    private void eliminateDeadCode(Map<ImStmt, Set<ImVar>> livenessInfo) {
        for (ImStmt s : livenessInfo.keySet()) {
            if (!(s instanceof ImSet set)) continue;

            ImLExpr lhs = set.getLeft();

            if (lhs instanceof ImVarAccess imVarAccess && set.getRight() instanceof ImVarAccess) {
                if (imVarAccess.getVar() == ((ImVarAccess) set.getRight()).getVar()) {
                    s.replaceBy(ImHelper.nullExpr());
                    continue;
                }
            }

            ImVar v = null;
            if (lhs instanceof ImVarAccess imVarAccess) {
                v = imVarAccess.getVar();
            } else if (lhs instanceof ImTupleSelection imTupleSelection) {
                v = TypesHelper.getSimpleAndPureTupleVar(imTupleSelection);
            }

            if (v == null || v.isGlobal()) continue;

            if (!livenessInfo.get(s).contains(v) && !Flatten.mayStopTheThread(set.getRight())) {
                // (an assignment of a division which may stop the thread is the statement which evaluates it)
                final List<ImExpr> raw = new ArrayList<>();
                collectLhsSideEffects(lhs, raw);
                if (hasSideEffects(set.getRight())) raw.add(set.getRight());

                if (raw.isEmpty()) {
                    AstEdits.deleteStmt(s);  // remove the dead assignment entirely
                } else {
                    ImStmts block = JassIm.ImStmts();
                    for (int i = 0; i < raw.size(); i++) {
                        ImExpr e = raw.get(i);
                        // wrap expression as a statement; add a *copy* to avoid re-parenting conflicts
                        block.add(ImHelper.statementExprVoid(e.copy()));
                    }
                    AstEdits.replaceStmtWithMany(s, block); // removes 's', then inserts the new stmts
                }
            }
        }
    }

    private void collectLhsSideEffects(ImLExpr lhs, List<ImExpr> out) {
        if (lhs instanceof ImVarArrayAccess a) {
            ImExprs indexes = a.getIndexes();
            for (int i = 0; i < indexes.size(); i++) {
                ImExpr idx = indexes.get(i);
                if (hasSideEffects(idx)) {
                    out.add(idx);
                }
            }
        } else if (lhs instanceof ImMemberAccess m) {
            if (hasSideEffects(m.getReceiver())) out.add(m.getReceiver());
            ImExprs indexes = m.getIndexes();
            for (int i = 0; i < indexes.size(); i++) {
                ImExpr idx = indexes.get(i);
                if (hasSideEffects(idx)) {
                    out.add(idx);
                }
            }
        } else if (lhs instanceof ImTupleSelection ts) {
            Element t = ts.getTupleExpr();
            if (hasSideEffects(t)) out.add((ImExpr) t);
        }
    }


    private boolean hasSideEffects(Element e) {
        if (e instanceof ImMethodCall) return true;
        if (e instanceof ImFunctionCall call
            && (translator == null || !translator.isTrapFreeLuaIntrinsicCall(call))) return true;
        if (e instanceof ImExpr expr && Flatten.mayStopTheThread(expr)) return true;
        for (int i = 0; i < e.size(); i++) if (hasSideEffects(e.get(i))) return true;
        return false;
    }

    /**
     * Calculates liveness for each statement using a fixed-point iteration
     * over the strongly connected components of the control flow graph.
     */
    public Map<ImStmt, Set<ImVar>> calculateLiveness(ImFunction func) {
        return analyzeLiveness(func).liveOut;
    }

    private LivenessAnalysis analyzeLiveness(ImFunction func) {
        // 1. Build Control Flow Graph
        ControlFlowGraph cfg = new ControlFlowGraph(func.getBody());
        final List<Node> nodes = cfg.getNodes();
        final int N = nodes.size();

        // Map nodes to indices for quick array access
        final Object2IntOpenHashMap<Node> idx = new Object2IntOpenHashMap<>(N);
        idx.defaultReturnValue(-1);
        for (int i = 0; i < N; i++) idx.put(nodes.get(i), i);

        // The sets below hold numbers of locals, as sorted arrays without repeats. Most statements
        // neither read nor assign a local, so most sets are shared rather than copied.
        final VariableNumbering variables = new VariableNumbering();

        // 2. Calculate USE and DEF sets for each node
        final int[][] use = new int[N][];
        final int[][] def = new int[N][];

        for (int i = 0; i < N; i++) {
            Node node = nodes.get(i);
            final IntArrayList used = new IntArrayList();
            final IntArrayList defined = new IntArrayList();
            use[i] = NO_VARIABLES;
            def[i] = NO_VARIABLES;

            ImStmt stmt = node.getStmt();
            if (stmt == null) continue;

            if (stmt instanceof ImVarargLoop loop) {
                for (ImVarargLoopVar loopVar : loop.getLoopVars()) {
                    if (!loopVar.getVar().isGlobal()) {
                        defined.add(variables.numberOf(loopVar.getVar()));
                    }
                }
                def[i] = sortedWithoutRepeats(defined);
                // The loop body has its own CFG nodes. Visiting it here would incorrectly
                // classify all body reads as uses at the loop header.
                continue;
            }

            stmt.accept(new ImStmt.DefaultVisitor() {
                @Override public void visit(ImVarAccess va) {
                    super.visit(va);
                    ImVar v = va.getVar();
                    if (!v.isGlobal()) used.add(variables.numberOf(v));
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
                        @Override public void case_ImTupleExpr(ImTupleExpr e) {
                            ImExprs exprs = e.getExprs();
                            for (int i = 0; i < exprs.size(); i++) {
                                ((ImLExpr) exprs.get(i)).match(this);
                            }
                        }
                    });
                }
            });

            if (stmt instanceof ImSet set) {
                if (set.getLeft() instanceof ImVarAccess) {
                    ImVar v = ((ImVarAccess) set.getLeft()).getVar();
                    if (!v.isGlobal()) defined.add(variables.numberOf(v));
                }
            }
            use[i] = sortedWithoutRepeats(used);
            def[i] = sortedWithoutRepeats(defined);
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
        final int[][] in = new int[N][];
        final int[][] out = new int[N][];
        Arrays.fill(in, NO_VARIABLES);
        Arrays.fill(out, NO_VARIABLES);

        // 5. Iterate over SCCs in reverse topological order
        for (int sccIndex = 0; sccIndex < sccs.size(); sccIndex++) {
            List<Node> scc = sccs.get(sccIndex);
            if (scc.isEmpty()) continue;

            // Iterate within this SCC until a fixed point is reached for all its nodes.
            boolean changedInScc = true;
            while (changedInScc) {
                changedInScc = false;
                for (int uIndex = 0; uIndex < scc.size(); uIndex++) {
                    Node u_node = scc.get(uIndex);
                    int u_idx = idx.getInt(u_node);

                    // Recalculate OUT[u] from the IN sets of its successors.
                    // Any successor not in the current SCC has already been processed and its IN set is stable.
                    int[] newOut = NO_VARIABLES;
                    for (Node succ : u_node.getSuccessors()) {
                        int v_idx = idx.getInt(succ);
                        if (v_idx != -1) {
                            newOut = union(newOut, in[v_idx]);
                        }
                    }
                    out[u_idx] = newOut;

                    // Recalculate IN[u] using the data-flow equation: in[u] = use[u] U (out[u] - def[u])
                    final int[] oldIn = in[u_idx];
                    final int[] newIn = union(difference(newOut, def[u_idx]), use[u_idx]);

                    // If IN[u] changed, update it and flag that we need another iteration for this SCC.
                    if (newIn != oldIn && !Arrays.equals(newIn, oldIn)) {
                        in[u_idx] = newIn;
                        changedInScc = true;
                    }
                }
            }
        }

        // 6. Collect results into the final map format. Statements that share a set share its copy.
        final java.util.LinkedHashMap<ImStmt, Set<ImVar>> result = new java.util.LinkedHashMap<>();
        final IdentityHashMap<int[], Set<ImVar>> converted = new IdentityHashMap<>();
        for (int i = 0; i < N; i++) {
            ImStmt stmt = nodes.get(i).getStmt();
            if (stmt != null) {
                result.put(stmt, variables.toSet(out[i], converted));
            }
        }
        Set<ImVar> liveAtEntry = N == 0
            ? io.vavr.collection.HashSet.empty()
            : variables.toSet(in[0], converted);
        return new LivenessAnalysis(cfg, result, liveAtEntry);
    }

    private static final int[] NO_VARIABLES = new int[0];

    /** Numbers the locals met by one liveness analysis, from 0. */
    private static final class VariableNumbering {
        private final Object2IntOpenHashMap<ImVar> numbers = new Object2IntOpenHashMap<>();
        private final List<ImVar> variables = new ArrayList<>();

        private VariableNumbering() {
            numbers.defaultReturnValue(-1);
        }

        private int numberOf(ImVar variable) {
            int number = numbers.getInt(variable);
            if (number < 0) {
                number = variables.size();
                variables.add(variable);
                numbers.put(variable, number);
            }
            return number;
        }

        private Set<ImVar> toSet(int[] numbersInSet, IdentityHashMap<int[], Set<ImVar>> converted) {
            if (numbersInSet.length == 0) {
                return io.vavr.collection.HashSet.empty();
            }
            Set<ImVar> existing = converted.get(numbersInSet);
            if (existing == null) {
                List<ImVar> members = new ArrayList<>(numbersInSet.length);
                for (int number : numbersInSet) {
                    members.add(variables.get(number));
                }
                existing = io.vavr.collection.HashSet.ofAll(members);
                converted.put(numbersInSet, existing);
            }
            return existing;
        }
    }

    private static int[] sortedWithoutRepeats(IntArrayList numbers) {
        if (numbers.isEmpty()) {
            return NO_VARIABLES;
        }
        int[] sorted = numbers.toIntArray();
        Arrays.sort(sorted);
        int length = 1;
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] != sorted[length - 1]) {
                sorted[length++] = sorted[i];
            }
        }
        return length == sorted.length ? sorted : Arrays.copyOf(sorted, length);
    }

    /** The union of two sorted sets, which is one of them when it holds the other. */
    private static int[] union(int[] a, int[] b) {
        if (a == b || b.length == 0) {
            return a;
        }
        if (a.length == 0) {
            return b;
        }
        int[] merged = new int[a.length + b.length];
        int i = 0;
        int j = 0;
        int length = 0;
        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) {
                merged[length++] = a[i++];
            } else if (a[i] > b[j]) {
                merged[length++] = b[j++];
            } else {
                merged[length++] = a[i++];
                j++;
            }
        }
        while (i < a.length) {
            merged[length++] = a[i++];
        }
        while (j < b.length) {
            merged[length++] = b[j++];
        }
        if (length == a.length) {
            return a;
        }
        if (length == b.length) {
            return b;
        }
        return Arrays.copyOf(merged, length);
    }

    /** The members of a sorted set which a second one does not hold; the set itself when it holds none of them. */
    private static int[] difference(int[] a, int[] removed) {
        if (a.length == 0 || removed.length == 0) {
            return a;
        }
        int[] kept = null;
        int length = 0;
        int j = 0;
        for (int i = 0; i < a.length; i++) {
            while (j < removed.length && removed[j] < a[i]) {
                j++;
            }
            boolean isRemoved = j < removed.length && removed[j] == a[i];
            if (isRemoved && kept == null) {
                kept = new int[a.length - 1];
                System.arraycopy(a, 0, kept, 0, i);
                length = i;
            } else if (!isRemoved && kept != null) {
                kept[length++] = a[i];
            }
        }
        if (kept == null) {
            return a;
        }
        return length == 0 ? NO_VARIABLES : Arrays.copyOf(kept, length);
    }

    private static final class LivenessAnalysis {
        private final ControlFlowGraph cfg;
        private final Map<ImStmt, Set<ImVar>> liveOut;
        private final Set<ImVar> liveAtEntry;

        private LivenessAnalysis(ControlFlowGraph cfg, Map<ImStmt, Set<ImVar>> liveOut, Set<ImVar> liveAtEntry) {
            this.cfg = cfg;
            this.liveOut = liveOut;
            this.liveAtEntry = liveAtEntry;
        }
    }
}
