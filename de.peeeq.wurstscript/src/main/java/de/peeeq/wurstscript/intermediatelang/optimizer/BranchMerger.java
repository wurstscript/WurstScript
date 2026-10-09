package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.ListIterator;

/**
 * merges identical nodes in branches if possible without side effects
 * <p>
 * the input must be a flattened program
 */
public class BranchMerger implements LocalPlayerAwareOptimizerPass {
    private SideEffectAnalyzer sideEffectAnalyzer;
    private LocalPlayerContextAnalyzer localPlayerContextAnalyzer;
    public int branchesMerged = 0;

    @Override
    public int optimize(ImTranslator trans, LocalPlayerContextAnalyzer analyzer) {
        branchesMerged = 0;
        ImProg prog = trans.getImProg();
        this.sideEffectAnalyzer = new SideEffectAnalyzer(prog);
        this.localPlayerContextAnalyzer = analyzer;

        for (ImFunction func : prog.getFunctions()) {
            optimizeFunc(func);
        }
        return branchesMerged;
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
                    if (s instanceof ImIf ifStmt) {
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
                                    && !localPlayerContextAnalyzer.isLocalPlayerDependent(ifStmt.getCondition())
                                    && !sideEffectAnalyzer.mightAffect(firstStmtThen, ifStmt.getCondition())
                                    && (!mayLeave(firstStmtThen)
                                        || !SideEffectAnalyzer.quickcheckHasSideeffects(ifStmt.getCondition()))) {
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



    /**
     * Whether control may leave the statements around {@code s} from inside it: by a return, or by an exitwhen of a
     * loop outside {@code s}. Moved in front of an if, such a statement runs before the condition, which then is
     * not evaluated at all when it leaves.
     */
    private static boolean mayLeave(ImStmt s) {
        boolean[] leaves = {false};
        s.accept(new Element.DefaultVisitor() {
            private int loops = 0;

            @Override
            public void visit(ImReturn r) {
                leaves[0] = true;
            }

            @Override
            public void visit(ImExitwhen e) {
                if (loops == 0) {
                    leaves[0] = true;
                }
            }

            @Override
            public void visit(ImLoop l) {
                loops++;
                super.visit(l);
                loops--;
            }

            @Override
            public void visit(ImVarargLoop l) {
                loops++;
                super.visit(l);
                loops--;
            }
        });
        return leaves[0];
    }

    @Override
    public String getName() {
        return "Branches merged";
    }
}
