package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ListIterator;

/**
 * merges identical nodes in branches if possible without side effects
 * <p>
 * the input must be a flattened program
 */
public class BranchMerger {
    private final SideEffectAnalyzer sideEffectAnalyzer;
    public int branchesMerged = 0;
    private final ImProg prog;

    public BranchMerger(ImTranslator trans) {
        this.prog = trans.getImProg();
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
        func.getBody().accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImStmts stmts) {
                ListIterator<ImStmt> it = stmts.listIterator();
                while (it.hasNext()) {
                    ImStmt s = it.next();
                    if (s instanceof ImIf) {
                        ImIf ifStmt = (ImIf) s;
                        // first optimize inner statements
                        ifStmt.getThenBlock().accept(this);
                        ifStmt.getElseBlock().accept(this);

                        while (!ifStmt.getThenBlock().isEmpty()
                                && !ifStmt.getElseBlock().isEmpty()) {
                            ImStmt firstStmtThen = ifStmt.getThenBlock().get(0);
                            ImStmt firstStmtElse = ifStmt.getElseBlock().get(0);
                            // if first statement in both branches is the same
                            // and has no side-effects that could affect the if-condition:
                            if (firstStmtThen.structuralEquals(firstStmtElse)
                                    && !sideEffectAnalyzer.mightAffect(firstStmtThen, ifStmt.getCondition())) {
                                // remove statements
                                ifStmt.getThenBlock().remove(0);
                                ifStmt.getElseBlock().remove(0);
                                // and add before the if-statement
                                it.previous();
                                it.add(firstStmtThen);
                                it.next();

                                branchesMerged++;
                            } else {
                                break;
                            }
                        }

                    } else {
                        s.accept(this);
                    }
                }
            }
        });
    }



}
