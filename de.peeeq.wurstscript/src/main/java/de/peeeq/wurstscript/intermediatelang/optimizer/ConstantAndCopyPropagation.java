package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.datastructures.Worklist;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

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
        // one of the two is null
        final @Nullable
        ImVar copyVar;
        final @Nullable
        ImConst constantValue;
        ImTupleExpr constantTuple;

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
            
            for(ImExpr e :  tupleExpr.getExprs()) {
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
                    if(!aV.equalValue(bV)) {
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

                @Override
                public void visit(ImVarAccess va) {
                    if (va.isUsedAsLValue()) {
                        return;
                    }
                    Value val = kn.varKnowledge.get(va.getVar()).getOrNull();
                    if (val == null) {
                        return;
                    }
                    if (val.constantValue != null) {
                        va.replaceBy(val.constantValue.copy());
                        totalPropagated++;
                    } else if (val.copyVar != null) {
                        va.setVar(val.copyVar);
                        // recursive call, because maybe it is possible to also replace the new var
                        visit(va);
                    } else if (val.constantTuple != null) {
                        va.replaceBy(val.constantTuple.copy());
                        totalPropagated++;
                    }
                }
            });

        }

    }

    private Map<Node, Knowledge> calculateKnowledge(ControlFlowGraph cfg) {
        Map<Node, Knowledge> knowledge = new java.util.HashMap<>();

        // initialize with empty knowledge:
        for (Node n : cfg.getNodes()) {
            knowledge.put(n, new Knowledge());
        }

        Worklist<Node> todo = new Worklist<>(cfg.getNodes());

        while (!todo.isEmpty()) {
            Node n = todo.poll();

            Knowledge kn = knowledge.get(n);

            // get knowledge from predecessor out
            HashMap<ImVar, Value> newKnowledge = HashMap.empty();
            if (!n.getPredecessors().isEmpty()) {
                Node pred1 = n.getPredecessors().get(0);
                HashMap<ImVar, Value> predKnowledgeOut = knowledge.get(pred1).varKnowledgeOut;

                // only keep knowledge that is the same for all predecessors:
                newKnowledge = predKnowledgeOut;
                if (n.getPredecessors().size() > 1) {
                    for (Tuple2<ImVar, Value> e : predKnowledgeOut) {
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

            // at the output get all from the input knowledge
            HashMap<ImVar, Value> newOut = newKnowledge;

            ImStmt stmt = n.getStmt();
            if (stmt instanceof ImSet) {
                ImSet imSet = (ImSet) stmt;
                if (imSet.getLeft() instanceof ImVarAccess) {
                    ImVar var = ((ImVarAccess) imSet.getLeft()).getVar();
                    if (!var.isGlobal()) {
                        Value newValue = null;
                        if (imSet.getRight() instanceof ImConst) {
                            newValue = Value.tryValue(imSet.getRight());
                        } else if (imSet.getRight() instanceof ImVarAccess) {
                            newValue = Value.tryValue(imSet.getRight());
                        } else if(imSet.getRight() instanceof ImTupleExpr) {
                            newValue = Value.tryValue(imSet.getRight());
                        }
                        if (newValue == null) {
                            // invalidate old value
                            newOut = newOut.remove(var);
                        } else {
                            newOut = newOut.put(var, newValue);
                        }
                        // invalidate copies of the lhs
                        // for example:
                        // x = a; [x->a]
                        // y = b; [x->a, y->b]
                        // a = 5; [y->b, a->5] // here [x->a] has been invalidated
                        Value varAsValue = new Value(var);
                        for (Tuple2<ImVar, Value> p : newOut) {
                            if (p._2().equalValue(varAsValue)) {
                                newOut = newOut.remove(p._1());
                            }
                        }
                    }
                } else if(imSet.getLeft() instanceof ImTupleSelection) {
                    ImVar var = TypesHelper.getTupleVar((ImTupleSelection) imSet.getLeft());
                    newOut = newOut.remove(var);
                    Value varAsValue = new Value(var);
                    for (Tuple2<ImVar, Value> p : newOut) {
                        if (p._2().equalValue(varAsValue)) {
                            newOut = newOut.remove(p._1());
                        }
                    }
                }
            }

            // if there are changes, revisit successors:
            if (!kn.varKnowledgeOut.equals(newOut)) {
                todo.addAll(n.getSuccessors());
            }

            // update knowledge
            kn.varKnowledge = newKnowledge;
            kn.varKnowledgeOut = newOut;

        }
        return knowledge;
    }

}
