package de.peeeq.wurstscript.intermediatelang.optimizer;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
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
                    if (leftStmt.getStmt().structuralEquals(rightStmt.getStmt())) {
                        if (n.getPredecessors().size() == 1) {
                            // Possible match. At last check if condition causes sideeffects.
                            if (hasNoSideEffects(n, leftStmt)) {

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


    private List<ImVar> calculateVarUses(Node node) {
        List<ImVar> result = Lists.newArrayList();
        ImStmt stmt = node.getStmt();
        if (stmt != null) {
            stmt.accept(new ImStmt.DefaultVisitor() {
                @Override
                public void visit(ImFunctionCall call) {
                    super.visit(call);
                    visit(call.getFunc());
                }

                @Override
                public void visit(ImVarAccess va) {
                    super.visit(va);
                    result.add(va.getVar());
                }

                @Override
                public void visit(ImVarArrayAccess va) {
                    super.visit(va);
                    result.add(va.getVar());
                }

                @Override
                public void visit(ImVarArrayMultiAccess va) {
                    super.visit(va);
                    result.add(va.getVar());
                }

                @Override
                public void visit(ImMemberAccess va) {
                    super.visit(va);
                    result.add(va.getVar());
                }

                @Override
                public void visit(ImSet va) {
                    super.visit(va);
                    result.add(va.getLeft());
                }

                @Override
                public void visit(ImSetTuple va) {
                    super.visit(va);
                    result.add(va.getLeft());
                }

                @Override
                public void visit(ImSetArrayTuple va) {
                    super.visit(va);
                    result.add(va.getLeft());
                }

                @Override
                public void visit(ImVarargLoop va) {
                    super.visit(va);
                    result.add(va.getLoopVar());
                }

            });
        }
        return result;
    }

    private boolean hasNoSideEffects(Node ifNode, Node stmtNode) {
        List<ImVar> usesIf = calculateVarUses(ifNode);
        List<ImVar> usesStmt = calculateVarUses(stmtNode);

        usesIf.retainAll(usesStmt);
        return usesIf.size() == 0;
    }

}
