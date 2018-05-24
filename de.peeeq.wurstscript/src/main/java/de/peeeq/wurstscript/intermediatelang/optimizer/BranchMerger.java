package de.peeeq.wurstscript.intermediatelang.optimizer;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

/**
 * merges identical nodes in branches if possible without side effects
 * <p>
 * the input must be a flattened program
 */
public class BranchMerger {
    public int totalLocalsMerged = 0;
    private final ImProg prog;
    private final ImTranslator trans;

    public BranchMerger(ImTranslator trans) {
        this.prog = trans.getImProg();
        this.trans = trans;
    }

    public void optimize() {
        for (ImFunction func : prog.getFunctions()) {
            optimizeFunc(func);
        }
    }

    private void optimizeFunc(ImFunction func) {
        mergeBranches(func);
    }


    private void mergeBranches(ImFunction func) {
        ControlFlowGraph cfg = new ControlFlowGraph(func.getBody());

        // init in and out with empty sets
        for (Node n : cfg.getNodes()) {
            @Nullable ImStmt stmt = n.getStmt();
            if (stmt != null && stmt.getParent() != null && stmt.getParent() instanceof ImIf) {
                ImIf imIf = (ImIf) stmt.getParent();
                if (n.getPredecessors().size() <= 1 && n.getSuccessors().size() == 2) {
                    Node leftStmt = n.getSuccessors().get(0);
                    Node rightStmt = n.getSuccessors().get(1);
                    if (leftStmt.getStmt() != null && rightStmt.getStmt() != null && leftStmt.getStmt().toString().equals(rightStmt.getStmt().toString())) {
                        if (n.getPredecessors().size() == 1) {
                            // Possible match. At last check if condition causes sideeffects.
                            if (sideEffectsCanAffectNode(n, leftStmt)) {

                                ImStmt mergedStmt = leftStmt.getStmt();
                                mergedStmt.setParent(null);

                                ImIf oldIf = imIf.copy();
                                oldIf.getThenBlock().get(0).replaceBy(JassIm.ImNull());
                                oldIf.getElseBlock().get(0).replaceBy(JassIm.ImNull());
                                imIf.replaceBy(JassIm.ImStatementExpr(JassIm.ImStmts(mergedStmt, oldIf), JassIm.ImNull()));
                            }
                        }
                    }
                }
                // if the first statement of each branch of the if is the same, it can be moved before the branch
            }
        }
    }


    private Pair<List<ImVar>, List<ImFunction>> calculateVarUses(Node node) {
        final Multimap<ImFunction, ImFunctionCall> calls = LinkedListMultimap.create();
        final Multimap<ImFunction, ImFunction> callRelation = LinkedListMultimap.create();
        final List<ImFuncRef> funcRefs = Lists.newArrayList();
        prog.accept(new ImProg.DefaultVisitor() {

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                calls.put(c.getFunc(), c);
                ImFunction caller = c.getNearestFunc();
                callRelation.put(caller, c.getFunc());
            }

            @Override
            public void visit(ImFuncRef imFuncRef) {
                super.visit(imFuncRef);
                funcRefs.add(imFuncRef);
            }
        });

        Multimap<ImFunction, ImFunction> callRelationTr = Utils.transientClosure(callRelation);
        List<ImVar> imVars = Lists.newArrayList();
        List<ImFunction> imFunctions = Lists.newArrayList();
        ImStmt stmt = node.getStmt();
        if (stmt != null) {
            stmt.accept(new ImStmt.DefaultVisitor() {
                @Override
                public void visit(ImFunctionCall call) {
                    super.visit(call);
                    callRelationTr.get(call.getFunc()).forEach(this::visit);
                    if (call.getFunc().isNative()) {
                        imFunctions.add(call.getFunc());
                    }
                }

                @Override
                public void visit(ImVarAccess va) {
                    super.visit(va);
                    imVars.add(va.getVar());
                }

                @Override
                public void visit(ImVarArrayAccess va) {
                    super.visit(va);
                    imVars.add(va.getVar());
                }

                @Override
                public void visit(ImVarArrayMultiAccess va) {
                    super.visit(va);
                    imVars.add(va.getVar());
                }

                @Override
                public void visit(ImMemberAccess va) {
                    super.visit(va);
                    imVars.add(va.getVar());
                }

                @Override
                public void visit(ImSet va) {
                    super.visit(va);
                    imVars.add(va.getLeft());
                }

                @Override
                public void visit(ImSetTuple va) {
                    super.visit(va);
                    imVars.add(va.getLeft());
                }

                @Override
                public void visit(ImSetArrayTuple va) {
                    super.visit(va);
                    imVars.add(va.getLeft());
                }

                @Override
                public void visit(ImVarargLoop va) {
                    super.visit(va);
                    imVars.add(va.getLoopVar());
                }

            });
        }
        return Pair.create(imVars, imFunctions);
    }

    /** Checking if executing stmtNode could affect the condition of the ifNode. */
    private boolean sideEffectsCanAffectNode(Node ifNode, Node stmtNode) {
        Pair<List<ImVar>, List<ImFunction>> usesIf = calculateVarUses(ifNode);
        Pair<List<ImVar>, List<ImFunction>> usesStmt = calculateVarUses(stmtNode);

        usesIf.getA().retainAll(usesStmt.getA());
        return usesIf.getA().size() == 0 && usesStmt.getB().size() == 0;
    }

}
