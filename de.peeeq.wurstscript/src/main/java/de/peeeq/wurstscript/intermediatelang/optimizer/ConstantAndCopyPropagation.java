package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.datastructures.NodeWorklist;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.LuaMultipleResults;
import de.peeeq.wurstscript.types.TypesHelper;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

/**
 * Replaces a read of a local by the constant or the other local it is known to hold, and propagates nullness
 * established by branches. It needs no locality analysis:
 * a read which is replaced reads the same value on every client, and no computation moves, so a value which depends
 * on the local player is propagated like any other.
 */
public class ConstantAndCopyPropagation implements OptimizerPass {
    private int totalPropagated = 0;

    @Override
    public int optimize(ImTranslator trans) {
        ImProg prog = trans.getImProg();

        totalPropagated = 0;
        for (ImFunction func : ImHelper.calculateFunctionsOfProg(prog)) {
            if (!func.isNative() && !func.isBj()) {
                optimizeFunc(func, prog);
            }
        }
        return totalPropagated;
    }

    @Override
    public String getName() {
        return "Constant and Copy Propagated";
    }

    static class Value {
        final @Nullable ImVar copyVar;
        /** With a copyVar which holds a call's results (LuaMultipleResults): the component copied, otherwise -1. */
        final int component;
        final @Nullable ImConst constantValue;
        final @Nullable ImTupleExpr constantTuple;
        final boolean nonNull;

        /** A branch proves non-nullness without proving which value the local holds. */
        private Value() {
            copyVar = null;
            component = -1;
            constantValue = null;
            constantTuple = null;
            nonNull = true;
        }

        private static final Value NON_NULL = new Value();

        public Value(ImVar copyVar) {
            this(copyVar, -1);
        }

        /** A copy of component {@code component} of the results local {@code results}. */
        public Value(ImVar results, int component) {
            this.copyVar = results;
            this.component = component;
            this.constantValue = null;
            this.constantTuple = null;
            this.nonNull = false;
            if(results.isGlobal()) {
                throw new IllegalArgumentException("copyVar must not be a Global.");
            }
        }

        public Value(ImConst constantValue) {
            this.copyVar = null;
            this.component = -1;
            this.constantValue = constantValue;
            this.constantTuple = null;
            this.nonNull = false;
        }

        public Value(ImTupleExpr tupleExpr) {
            this.copyVar = null;
            this.component = -1;
            this.constantValue = null;
            this.constantTuple = tupleExpr;
            this.nonNull = false;

            for(ImExpr e : tupleExpr.getExprs()) {
                if(tryValue(e) == null) {
                    throw new IllegalArgumentException("tupleExpr must only contain constant values.");
                }
            }
        }

        public static Value tryValue(ImExpr e) {
            try {
                if (e instanceof ImVarAccess imVarAccess) {
                    return new Value(imVarAccess.getVar());
                }
                if (e instanceof ImConst imConst) {
                    return new Value(imConst);
                }
                if (e instanceof ImTupleExpr imTupleExpr) {
                    return new Value(imTupleExpr);
                }
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Value value) {
                return equalValue(value);
            }
            return false;
        }

        public boolean equalValue(Value other) {
            if (nonNull || other.nonNull) {
                return nonNull == other.nonNull;
            } else if (copyVar != null && other.copyVar != null) {
                return copyVar == other.copyVar && component == other.component;
            } else if (constantValue != null && other.constantValue != null) {
                return constantValue.equalValue(other.constantValue);
            } else if (constantTuple != null && other.constantTuple != null) {
                ImTupleExpr a = constantTuple;
                ImTupleExpr b = other.constantTuple;
                if(!a.attrTyp().equalsType(b.attrTyp())) {
                    return false;
                }
                for(int i = 0; i < a.getExprs().size() ;++i) {
                    Value aV = tryValue(a.getExprs().get(i));
                    Value bV = tryValue(b.getExprs().get(i));
                    if(aV == null || bV == null || !aV.equalValue(bV)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            if (nonNull) {
                return "non-null";
            } else if (copyVar != null) {
                return "copy of " + copyVar + (component < 0 ? "" : "." + component);
            } else if (constantValue != null) {
                return "constant " + constantValue;
            } else {
                return "tuple of " + constantTuple;
            }
        }
    }

    static class Knowledge {
        HashMap<ImVar, Value> varKnowledge = HashMap.empty();
        HashMap<ImVar, Value> varKnowledgeOut = HashMap.empty();

        @Override
        public String toString() {
            return "[in =" + varKnowledge + ", out=" + varKnowledgeOut + "]";
        }
    }

    void optimizeFunc(ImFunction func, ImProg prog) {
        ControlFlowGraph cfg = new ControlFlowGraph(func.getBody());
        Map<Node, Knowledge> knowledge = calculateKnowledge(cfg, prog);
        rewriteCode(cfg, knowledge, prog);
    }

    private void rewriteCode(ControlFlowGraph cfg, Map<Node, Knowledge> knowledge, ImProg prog) {
        for (Node node : cfg.getNodes()) {
            ImStmt stmt = node.getStmt();
            if (stmt == null) {
                continue;
            }
            Knowledge kn = knowledge.get(node);
            stmt.accept(new ImStmt.DefaultVisitor() {

                @Override
                public void visit(ImOperatorCall call) {
                    if ((call.getOp() == WurstOperator.EQ || call.getOp() == WurstOperator.NOTEQ)
                        && call.getArguments().size() == 2) {
                        ImExpr left = call.getArguments().get(0);
                        ImExpr right = call.getArguments().get(1);
                        Boolean nonNull = left instanceof ImNull ? nonNull(right, kn.varKnowledge, prog)
                            : right instanceof ImNull ? nonNull(left, kn.varKnowledge, prog) : null;
                        if (nonNull != null) {
                            call.replaceBy(JassIm.ImBoolVal(nonNull == (call.getOp() == WurstOperator.NOTEQ)));
                            totalPropagated++;
                            return;
                        }
                    }
                    super.visit(call);
                }

                @Override
                public void visit(ImSet imSet) {
                    ImLExpr e = imSet.getLeft();
                    if (e instanceof ImMemberAccess ma) {
                        ma.accept(this);
                    } else if (e instanceof ImVarArrayAccess vaa) {
                        for (ImExpr ie : vaa.getIndexes()) {
                            ie.accept(this);
                        }
                    }
                    imSet.getRight().accept(this);
                }

                public void visit(ImVarAccess va) {
                    if (va.isUsedAsLValue()) {
                        return;
                    }

                    Value val = kn.varKnowledge.get(va.getVar()).getOrNull();
                    if (val == null) return;

                    if (val.constantValue != null) {
                        va.replaceBy(val.constantValue.copy());
                        totalPropagated++;
                    } else if (val.component >= 0) {
                        va.replaceBy(JassIm.ImTupleSelection(JassIm.ImVarAccess(val.copyVar), val.component));
                        totalPropagated++;
                    } else if (val.copyVar != null) {
                        ImVar old = va.getVar();
                        ImVar target = val.copyVar;
                        if (old != target) {
                            va.setVar(target);
                            totalPropagated++;
                            visit(va);
                        }
                    } else if (val.constantTuple != null) {
                        boolean changed = false;
                        if(va.getParent() instanceof ImTupleSelection) {
                            ImTupleSelection ts = (ImTupleSelection) va.getParent();
                            Element t = ts;
                            ImExpr constT = val.constantTuple;
                            while(t instanceof ImTupleSelection) {
                                ts = (ImTupleSelection) t;
                                constT = ((ImTupleExpr) constT).getExprs().get(ts.getTupleIndex());
                                t = ts.getParent();
                            }
                            boolean replace = true;
                            if(constT instanceof ImTupleExpr) {
                                ImTupleExpr te = (ImTupleExpr) constT;
                                if (te.getExprs().size() != 1 || te.getExprs().get(0) instanceof ImTupleSelection) {
                                    replace = false;
                                }
                            }
                            if(replace) {
                                ts.replaceBy(constT.copy());
                                changed = true;
                            }
                        } else {
                            if(val.constantTuple.getExprs().size() == 1 && !(val.constantTuple.getExprs().get(0) instanceof ImTupleSelection)) {
                                va.replaceBy(val.constantTuple.copy());
                                changed = true;
                            }
                        }
                        if (changed) {
                            totalPropagated++;
                        }
                    }
                }
            });
        }
    }

    private Map<Node, Knowledge> calculateKnowledge(ControlFlowGraph cfg, ImProg prog) {
        Map<Node, Knowledge> knowledge = new java.util.HashMap<>();
        List<Node> allNodes = cfg.getNodes();
        if (allNodes.isEmpty()) {
            return knowledge;
        }

        // Initialize knowledge for all nodes
        for (Node n : allNodes) {
            knowledge.put(n, new Knowledge());
        }

        // Decompose the CFG into Strongly Connected Components
        GraphInterpreter<Node> graphInterpreter = new GraphInterpreter<>() {
            @Override
            protected Collection<Node> getIncidentNodes(Node t) {
                return t.getSuccessors();
            }
        };
        List<List<Node>> sccs = graphInterpreter.findStronglyConnectedComponents(allNodes);

        // The SCC algorithm outputs components in reverse topological order, so we reverse them
        Collections.reverse(sccs);

        // Analyze each SCC in topological order
        for (List<Node> scc : sccs) {
            analyzeComponent(scc, knowledge, cfg, prog);
        }

        return knowledge;
    }

    /**
     * Runs a local worklist algorithm on a single SCC until it reaches a fixed-point.
     */
    private void analyzeComponent(List<Node> scc, Map<Node, Knowledge> knowledge, ControlFlowGraph cfg, ImProg prog) {
        NodeWorklist worklist = new NodeWorklist(scc);
        java.util.HashSet<Node> sccNodeSet = new java.util.HashSet<>(scc);

        while (!worklist.isEmpty()) {
            Node n = worklist.poll();
            Knowledge kn = knowledge.get(n);

            // --- MERGE PREDECESSORS ---
            HashMap<ImVar, Value> newKnowledge = HashMap.empty();
            if (!n.getPredecessors().isEmpty()) {
                Node pred1 = n.getPredecessors().get(0);
                newKnowledge = edgeKnowledge(pred1, n, knowledge, cfg);

                for (int i = 1; i < n.getPredecessors().size(); i++) {
                    HashMap<ImVar, Value> other = edgeKnowledge(n.getPredecessors().get(i), n, knowledge, cfg);
                    for (Tuple2<ImVar, Value> e : newKnowledge) {
                        ImVar var = e._1();
                        Value val = e._2();
                        Value otherValue = other.get(var).getOrNull();
                        if (otherValue == null || !otherValue.equalValue(val)) {
                            newKnowledge = newKnowledge.remove(var);
                        }
                    }
                }
            }

            // --- APPLY TRANSFER FUNCTION ---
            HashMap<ImVar, Value> newOut = newKnowledge;
            ImStmt stmt = n.getStmt();
            if (stmt instanceof ImSet imSet) {
                if (imSet.getLeft() instanceof ImVarAccess) {
                    ImVar var = ((ImVarAccess) imSet.getLeft()).getVar();
                    if (var != null && !var.isGlobal()) {
                        ImExpr right = imSet.getRight();

                        // Check if this is a no-op like 'set x = x'
                        if (right instanceof ImVarAccess imVarAccess && imVarAccess.getVar() == var) {
                            // Self-assignment: no-op, don't change knowledge
                        } else {
                            Value newValue = null;

                            // Constant folding is intentionally centralized in SimpleRewrites.
                            // This pass performs propagation only to keep fold semantics in one place.
                            if (TypesHelper.isRealType(var.getType()) && TypesHelper.isIntType(right.attrTyp())) {
                                // Jass converts an integer stored into a real, so the variable holds a real.
                                // Its integer source in place of a read would make `r / n` an integer division.
                                // A literal can be converted here; a copy of an integer variable cannot.
                                newValue = right instanceof ImIntVal intVal
                                    ? new Value(JassIm.ImRealVal(intVal.getValI() + "."))
                                    : null;
                            } else if (right instanceof ImConst) {
                                newValue = Value.tryValue(right);
                            } else if (right instanceof ImVarAccess imVarAccess) {
                                ImVar varRight = imVarAccess.getVar();
                                if(newOut.containsKey(varRight)) {
                                    newValue = newOut.get(varRight).getOrNull();
                                } else {
                                    newValue = ImHelper.isNonNullHandleConstant(varRight, prog)
                                        ? Value.NON_NULL : Value.tryValue(right);
                                }
                            } else if(right instanceof ImTupleExpr) {
                                newValue = Value.tryValue(right);
                            } else if (right instanceof ImTupleSelection selection
                                && selection.getTupleExpr() instanceof ImVarAccess results
                                && LuaMultipleResults.isResultsLocal(results.getVar())) {
                                // a component of a call's results, which only another call writes
                                newValue = new Value(results.getVar(), selection.getTupleIndex());
                            }

                            if (newValue == null) {
                                // invalidate old value
                                newOut = newOut.remove(var);
                            } else {
                                newOut = newOut.put(var, newValue);
                            }

                            // OPTIMIZED: invalidate copies of the lhs
                            // Only iterate if we actually have entries in the map
                            if (!newOut.isEmpty()) {
                                // Collect keys to remove to avoid modification during iteration
                                List<ImVar> toRemove = null;
                                for (Tuple2<ImVar, Value> p : newOut) {
                                    Value v = p._2();
                                    if (v.copyVar == var) {
                                        if (toRemove == null) {
                                            toRemove = new ArrayList<>(2); // Usually very few
                                        }
                                        toRemove.add(p._1());
                                    }
                                }
                                if (toRemove != null) {
                                    for (ImVar removeVar : toRemove) {
                                        newOut = newOut.remove(removeVar);
                                    }
                                }
                            }
                        }
                    }
                } else if(imSet.getLeft() instanceof ImTupleSelection) {
                    ImVar var = TypesHelper.getSimpleAndPureTupleVar((ImTupleSelection) imSet.getLeft());
                    if(var != null) {
                        Value rightVal = Value.tryValue(imSet.getRight());
                        Value existingValue = newOut.get(var).getOrNull();
                        if (rightVal != null && existingValue != null && existingValue.constantTuple != null) {
                            ImTupleExpr te = existingValue.constantTuple.copy();
                            ImExpr knownTuple = te;
                            Element left = imSet.getLeft();
                            while (left instanceof ImTupleSelection) {
                                left = ((ImTupleSelection) left).getTupleExpr();
                            }
                            while (left != imSet.getLeft()) {
                                left = left.getParent();
                                knownTuple = ((ImTupleExpr) knownTuple).getExprs().get(((ImTupleSelection) left).getTupleIndex());
                            }
                            knownTuple.replaceBy(imSet.getRight().copy());
                            newOut = newOut.put(var, new Value(te));
                        } else {
                            newOut = newOut.remove(var);
                        }

                        // OPTIMIZED: invalidate copies of the lhs
                        if (!newOut.isEmpty()) {
                            List<ImVar> toRemove = null;
                            for (Tuple2<ImVar, Value> p : newOut) {
                                Value v = p._2();
                                if (v.copyVar == var) {
                                    if (toRemove == null) {
                                        toRemove = new ArrayList<>(2);
                                    }
                                    toRemove.add(p._1());
                                }
                            }
                            if (toRemove != null) {
                                for (ImVar removeVar : toRemove) {
                                    newOut = newOut.remove(removeVar);
                                }
                            }
                        }
                    }
                }
            }

            // Kept from every evaluation, not only when what goes out changes: the reads of the statement itself are
            // rewritten with it. A statement which drops everything that reaches it (p = f(p), with p known to be a
            // copy) sends out what it sent before, so this was never kept for it and no read in it was rewritten.
            kn.varKnowledge = newKnowledge;

            // --- PROPAGATE CHANGES ---
            if (!kn.varKnowledgeOut.equals(newOut)) {
                kn.varKnowledgeOut = newOut;

                for (Node succ : n.getSuccessors()) {
                    if (sccNodeSet.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }

    private HashMap<ImVar, Value> edgeKnowledge(Node from, Node to, Map<Node, Knowledge> knowledge,
                                               ControlFlowGraph cfg) {
        HashMap<ImVar, Value> values = knowledge.get(from).varKnowledgeOut;
        Boolean branch = cfg.branchValue(from, to);
        return branch == null ? values : assume((ImExpr) from.getStmt(), branch, values);
    }

    private HashMap<ImVar, Value> assume(ImExpr condition, boolean truth, HashMap<ImVar, Value> values) {
        if (!(condition instanceof ImOperatorCall call)) {
            return values;
        }
        ImExprs args = call.getArguments();
        if (call.getOp() == WurstOperator.NOT) {
            return assume(args.get(0), !truth, values);
        }
        if ((call.getOp() == WurstOperator.AND && truth) || (call.getOp() == WurstOperator.OR && !truth)) {
            for (ImExpr arg : args) {
                values = assume(arg, truth, values);
            }
        } else if (call.getOp() == WurstOperator.EQ || call.getOp() == WurstOperator.NOTEQ) {
            ImExpr left = args.get(0);
            ImExpr right = args.get(1);
            ImExpr tested = left instanceof ImNull ? right : right instanceof ImNull ? left : null;
            if (tested instanceof ImVarAccess access && !access.getVar().isGlobal()) {
                boolean nonNull = truth == (call.getOp() == WurstOperator.NOTEQ);
                Value value = nonNull ? Value.NON_NULL : new Value((ImConst) (left instanceof ImNull ? left : right));
                values = values.put(access.getVar(), value);
            }
        }
        return values;
    }

    private @Nullable Boolean nonNull(ImExpr expr, HashMap<ImVar, Value> values, ImProg prog) {
        if (expr instanceof ImNull) {
            return false;
        }
        if (!(expr instanceof ImVarAccess access)) {
            return null;
        }
        ImVar var = access.getVar();
        for (int i = 0; i <= values.size(); i++) {
            if (ImHelper.isNonNullHandleConstant(var, prog)) {
                return true;
            }
            Value value = values.get(var).getOrNull();
            if (value == null) {
                return null;
            }
            if (value.nonNull) {
                return true;
            }
            if (value.constantValue instanceof ImNull) {
                return false;
            }
            if (value.copyVar == null || value.component >= 0) {
                return null;
            }
            var = value.copyVar;
        }
        return null;
    }
}
