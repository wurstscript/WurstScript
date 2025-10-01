package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.datastructures.NodeWorklist;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

import static de.peeeq.wurstscript.WurstOperator.*;

public class ConstantAndCopyPropagation implements OptimizerPass {
    private int totalPropagated = 0;

    public int optimize(ImTranslator trans) {
        ImProg prog = trans.getImProg();

        totalPropagated = 0;
        for (ImFunction func : ImHelper.calculateFunctionsOfProg(prog)) {
            if (!func.isNative() && !func.isBj()) {
                optimizeFunc(func);
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
        final @Nullable ImConst constantValue;
        final @Nullable ImTupleExpr constantTuple;

        public Value(ImVar copyVar) {
            this.copyVar = copyVar;
            this.constantValue = null;
            this.constantTuple = null;
            if(copyVar.isGlobal()) {
                throw new IllegalArgumentException("copyVar must not be a Global.");
            }
        }

        public Value(ImConst constantValue) {
            this.copyVar = null;
            this.constantValue = constantValue;
            this.constantTuple = null;
        }

        public Value(ImTupleExpr tupleExpr) {
            this.copyVar = null;
            this.constantValue = null;
            this.constantTuple = tupleExpr;

            for(ImExpr e : tupleExpr.getExprs()) {
                if(tryValue(e) == null) {
                    throw new IllegalArgumentException("tupleExpr must only contain constant values.");
                }
            }
        }

        public static Value tryValue(ImExpr e) {
            try {
                if (e instanceof ImVarAccess) {
                    return new Value(((ImVarAccess) e).getVar());
                }
                if (e instanceof ImConst) {
                    return new Value((ImConst) e);
                }
                if (e instanceof ImTupleExpr) {
                    return new Value((ImTupleExpr) e);
                }
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Value) {
                return equalValue((Value) obj);
            }
            return false;
        }

        public boolean equalValue(Value other) {
            if (copyVar != null && other.copyVar != null) {
                return copyVar == other.copyVar;
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
            if (copyVar != null) {
                return "copy of " + copyVar;
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

    void optimizeFunc(ImFunction func) {
        ControlFlowGraph cfg = new ControlFlowGraph(func.getBody());
        Map<Node, Knowledge> knowledge = calculateKnowledge(cfg);
        rewriteCode(cfg, knowledge);
    }

    private void rewriteCode(ControlFlowGraph cfg, Map<Node, Knowledge> knowledge) {
        for (Node node : cfg.getNodes()) {
            ImStmt stmt = node.getStmt();
            if (stmt == null) {
                continue;
            }
            Knowledge kn = knowledge.get(node);
            stmt.accept(new ImStmt.DefaultVisitor() {

                @Override
                public void visit(ImSet imSet) {
                    ImLExpr e = imSet.getLeft();
                    if (e instanceof ImMemberAccess) {
                        ImMemberAccess ma = (ImMemberAccess) e;
                        ma.accept(this);
                    } else if (e instanceof ImVarArrayAccess) {
                        ImVarArrayAccess vaa = (ImVarArrayAccess) e;
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

    private Map<Node, Knowledge> calculateKnowledge(ControlFlowGraph cfg) {
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
            analyzeComponent(scc, knowledge);
        }

        return knowledge;
    }

    /**
     * Runs a local worklist algorithm on a single SCC until it reaches a fixed-point.
     */
    private void analyzeComponent(List<Node> scc, Map<Node, Knowledge> knowledge) {
        NodeWorklist worklist = new NodeWorklist(scc);
        java.util.HashSet<Node> sccNodeSet = new java.util.HashSet<>(scc);

        while (!worklist.isEmpty()) {
            Node n = worklist.poll();
            Knowledge kn = knowledge.get(n);

            // --- MERGE PREDECESSORS ---
            HashMap<ImVar, Value> newKnowledge = HashMap.empty();
            if (!n.getPredecessors().isEmpty()) {
                Node pred1 = n.getPredecessors().get(0);
                newKnowledge = knowledge.get(pred1).varKnowledgeOut;

                if (n.getPredecessors().size() > 1) {
                    for (Tuple2<ImVar, Value> e : newKnowledge) {
                        ImVar var = e._1();
                        Value val = e._2();
                        boolean allSame = true;
                        for (int i = 1; i < n.getPredecessors().size(); i++) {
                            Node predi = n.getPredecessors().get(i);
                            Value predi_val = knowledge.get(predi).varKnowledgeOut.get(var).getOrNull();
                            if (predi_val == null || !predi_val.equalValue(val)) {
                                allSame = false;
                                break;
                            }
                        }
                        if (!allSame) {
                            newKnowledge = newKnowledge.remove(var);
                        }
                    }
                }
            }

            // --- APPLY TRANSFER FUNCTION ---
            HashMap<ImVar, Value> newOut = newKnowledge;
            ImStmt stmt = n.getStmt();
            if (stmt instanceof ImSet) {
                ImSet imSet = (ImSet) stmt;
                if (imSet.getLeft() instanceof ImVarAccess) {
                    ImVar var = ((ImVarAccess) imSet.getLeft()).getVar();
                    if (var != null && !var.isGlobal()) {
                        ImExpr right = imSet.getRight();

                        // Check if this is a no-op like 'set x = x'
                        if (right instanceof ImVarAccess && ((ImVarAccess) right).getVar() == var) {
                            // Self-assignment: no-op, don't change knowledge
                        } else {
                            Value newValue = null;

                            // Try constant folding first
                            ImExpr foldedExpr = tryConstantFold(right, newOut);
                            if (foldedExpr != null && foldedExpr != right) {
                                // We successfully folded to a constant
                                newValue = Value.tryValue(foldedExpr);
                                if (newValue != null) {
                                    // Replace the RHS with the folded constant in the AST
                                    right.replaceBy(foldedExpr);
                                }
                            }

                            // If no folding happened, try regular value propagation
                            if (newValue == null) {
                                if (right instanceof ImConst) {
                                    newValue = Value.tryValue(right);
                                } else if (right instanceof ImVarAccess) {
                                    ImVar varRight = ((ImVarAccess) right).getVar();
                                    if(newOut.containsKey(varRight)) {
                                        newValue = newOut.get(varRight).getOrNull();
                                    } else {
                                        newValue = Value.tryValue(right);
                                    }
                                } else if(right instanceof ImTupleExpr) {
                                    newValue = Value.tryValue(right);
                                }
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

            // --- PROPAGATE CHANGES ---
            if (!kn.varKnowledgeOut.equals(newOut)) {
                kn.varKnowledge = newKnowledge;
                kn.varKnowledgeOut = newOut;

                for (Node succ : n.getSuccessors()) {
                    if (sccNodeSet.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }

    /**
     * Try to constant-fold an expression using known values.
     * Returns the folded constant expression, or null if folding is not possible.
     */
    private @Nullable ImExpr tryConstantFold(ImExpr expr, HashMap<ImVar, Value> knowledge) {
        // Binary operations
        if (expr instanceof ImOperatorCall) {
            ImOperatorCall op = (ImOperatorCall) expr;
            if (op.getArguments().size() == 2) {
                ImExpr left = op.getArguments().get(0);
                ImExpr right = op.getArguments().get(1);

                // Resolve variables to their constant values
                ImConst leftConst = resolveToConstant(left, knowledge);
                ImConst rightConst = resolveToConstant(right, knowledge);

                if (leftConst != null && rightConst != null) {
                    return foldBinaryOp(op.getOp(), leftConst, rightConst);
                }
            }
        }

        // Unary operations
        if (expr instanceof ImOperatorCall) {
            ImOperatorCall op = (ImOperatorCall) expr;
            if (op.getArguments().size() == 1) {
                ImExpr arg = op.getArguments().get(0);
                ImConst argConst = resolveToConstant(arg, knowledge);
                if (argConst != null) {
                    return foldUnaryOp(op.getOp(), argConst);
                }
            }
        }

        return null;
    }

    private @Nullable ImConst resolveToConstant(ImExpr expr, HashMap<ImVar, Value> knowledge) {
        if (expr instanceof ImConst) {
            return (ImConst) expr;
        }
        if (expr instanceof ImVarAccess) {
            ImVar var = ((ImVarAccess) expr).getVar();
            Value val = knowledge.get(var).getOrNull();
            if (val != null && val.constantValue != null) {
                return val.constantValue;
            }
        }
        return null;
    }

    private @Nullable ImExpr foldBinaryOp(WurstOperator op, ImConst left, ImConst right) {
        try {
            if (left instanceof ImIntVal && right instanceof ImIntVal) {
                int l = ((ImIntVal) left).getValI();
                int r = ((ImIntVal) right).getValI();

                switch (op) {
                    case PLUS: return JassIm.ImIntVal(l + r);
                    case MINUS: return JassIm.ImIntVal(l - r);
                    case MULT: return JassIm.ImIntVal(l * r);
                    case DIV_INT: if (r != 0) return JassIm.ImIntVal(l / r); break;
                    case MOD_INT: if (r != 0) return JassIm.ImIntVal(l % r); break;
                    // IMPORTANT: Return ImBoolVal for comparisons, not ImIntVal!
                    case EQ: return JassIm.ImBoolVal(l == r);
                    case NOTEQ: return JassIm.ImBoolVal(l != r);
                    case LESS: return JassIm.ImBoolVal(l < r);
                    case LESS_EQ: return JassIm.ImBoolVal(l <= r);
                    case GREATER: return JassIm.ImBoolVal(l > r);
                    case GREATER_EQ: return JassIm.ImBoolVal(l >= r);
                    // Bitwise/logical operations
                    case AND: return JassIm.ImIntVal(l & r);
                    case OR: return JassIm.ImIntVal(l | r);
                }
            } else if (left instanceof ImRealVal && right instanceof ImRealVal) {
                double l = Double.parseDouble(((ImRealVal) left).getValR());
                double r = Double.parseDouble(((ImRealVal) right).getValR());

                switch (op) {
                    case PLUS: return JassIm.ImRealVal(String.valueOf(l + r));
                    case MINUS: return JassIm.ImRealVal(String.valueOf(l - r));
                    case MULT: return JassIm.ImRealVal(String.valueOf(l * r));
                    case DIV_REAL: if (r != 0.0) return JassIm.ImRealVal(String.valueOf(l / r)); break;
                    // IMPORTANT: Return ImBoolVal for comparisons!
                    case EQ: return JassIm.ImBoolVal(l == r);
                    case NOTEQ: return JassIm.ImBoolVal(l != r);
                    case LESS: return JassIm.ImBoolVal(l < r);
                    case LESS_EQ: return JassIm.ImBoolVal(l <= r);
                    case GREATER: return JassIm.ImBoolVal(l > r);
                    case GREATER_EQ: return JassIm.ImBoolVal(l >= r);
                }
            } else if (left instanceof ImBoolVal && right instanceof ImBoolVal) {
                // Handle boolean operations
                boolean l = ((ImBoolVal) left).getValB();
                boolean r = ((ImBoolVal) right).getValB();

                switch (op) {
                    case EQ: return JassIm.ImBoolVal(l == r);
                    case NOTEQ: return JassIm.ImBoolVal(l != r);
                    case AND: return JassIm.ImBoolVal(l && r);
                    case OR: return JassIm.ImBoolVal(l || r);
                }
            } else if (left instanceof ImIntVal && right instanceof ImRealVal) {
                // int op real -> real
                double l = ((ImIntVal) left).getValI();
                double r = Double.parseDouble(((ImRealVal) right).getValR());

                switch (op) {
                    case PLUS: return JassIm.ImRealVal(String.valueOf(l + r));
                    case MINUS: return JassIm.ImRealVal(String.valueOf(l - r));
                    case MULT: return JassIm.ImRealVal(String.valueOf(l * r));
                    case DIV_REAL: if (r != 0.0) return JassIm.ImRealVal(String.valueOf(l / r)); break;
                    // Comparisons return bool
                    case EQ: return JassIm.ImBoolVal(l == r);
                    case NOTEQ: return JassIm.ImBoolVal(l != r);
                    case LESS: return JassIm.ImBoolVal(l < r);
                    case LESS_EQ: return JassIm.ImBoolVal(l <= r);
                    case GREATER: return JassIm.ImBoolVal(l > r);
                    case GREATER_EQ: return JassIm.ImBoolVal(l >= r);
                }
            } else if (left instanceof ImRealVal && right instanceof ImIntVal) {
                // real op int -> real
                double l = Double.parseDouble(((ImRealVal) left).getValR());
                double r = ((ImIntVal) right).getValI();

                switch (op) {
                    case PLUS: return JassIm.ImRealVal(String.valueOf(l + r));
                    case MINUS: return JassIm.ImRealVal(String.valueOf(l - r));
                    case MULT: return JassIm.ImRealVal(String.valueOf(l * r));
                    case DIV_REAL: if (r != 0.0) return JassIm.ImRealVal(String.valueOf(l / r)); break;
                    // Comparisons return bool
                    case EQ: return JassIm.ImBoolVal(l == r);
                    case NOTEQ: return JassIm.ImBoolVal(l != r);
                    case LESS: return JassIm.ImBoolVal(l < r);
                    case LESS_EQ: return JassIm.ImBoolVal(l <= r);
                    case GREATER: return JassIm.ImBoolVal(l > r);
                    case GREATER_EQ: return JassIm.ImBoolVal(l >= r);
                }
            }
        } catch (Exception e) {
            // Folding failed, return null
        }
        return null;
    }

    private @Nullable ImExpr foldUnaryOp(WurstOperator op, ImConst arg) {
        try {
            if (arg instanceof ImIntVal) {
                int val = ((ImIntVal) arg).getValI();
                switch (op) {
                    case UNARY_MINUS: return JassIm.ImIntVal(-val);
                    case NOT: return JassIm.ImBoolVal(val == 0); // Return ImBoolVal!
                }
            } else if (arg instanceof ImRealVal) {
                double val = Double.parseDouble(((ImRealVal) arg).getValR());
                switch (op) {
                    case UNARY_MINUS: return JassIm.ImRealVal(String.valueOf(-val));
                }
            } else if (arg instanceof ImBoolVal) {
                boolean val = ((ImBoolVal) arg).getValB();
                switch (op) {
                    case NOT: return JassIm.ImBoolVal(!val);
                }
            }
        } catch (Exception e) {
            // Folding failed
        }
        return null;
    }
}
