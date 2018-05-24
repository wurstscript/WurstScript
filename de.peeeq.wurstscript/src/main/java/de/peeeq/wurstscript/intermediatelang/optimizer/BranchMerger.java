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
    private final SideEffectAnalyzer sideEffectAnalyzer;
    public int totalLocalsMerged = 0;
    private final ImProg prog;
    private final ImTranslator trans;

    public BranchMerger(ImTranslator trans) {
        this.prog = trans.getImProg();
        this.trans = trans;
        this.sideEffectAnalyzer = new SideEffectAnalyzer(prog);
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


    /** Checking if executing stmtNode could affect the condition of the ifNode. */
    private boolean sideEffectsCanAffectNode(Node ifNode, Node stmtNode) {
        return sideEffectAnalyzer.mightAffect(ifNode.getStmt(), stmtNode.getStmt());
    }

}
