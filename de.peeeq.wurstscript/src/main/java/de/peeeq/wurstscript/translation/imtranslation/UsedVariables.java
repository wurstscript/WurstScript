package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.jassIm.*;

import java.util.Set;

public class UsedVariables {

    public static Set<ImVar> calculate(ImFunction f) {
        Set<ImVar> result = Sets.newLinkedHashSet();
        collectAllVars(f, result);
        return result;
    }

    public static Set<ImVar> calculateReadVars(ImFunction f) {
        Set<ImVar> result = Sets.newLinkedHashSet();
        ReadVarCollector collector = new ReadVarCollector(result);
        f.accept(collector);
        return result;
    }

    // Fastest: Direct recursive collection without visitor overhead
    private static void collectAllVars(Element e, Set<ImVar> result) {
        if (e instanceof ImVarAccess) {
            result.add(((ImVarAccess) e).getVar());
        } else if (e instanceof ImVarArrayAccess) {
            result.add(((ImVarArrayAccess) e).getVar());
        } else if (e instanceof ImMemberAccess) {
            result.add(((ImMemberAccess) e).getVar());
        }

        // Continue traversal
        for (int i = 0; i < e.size(); i++) {
            collectAllVars(e.get(i), result);
        }
    }

    private static class ReadVarCollector extends ImFunction.DefaultVisitor {
        private final Set<ImVar> result;

        ReadVarCollector(Set<ImVar> result) {
            this.result = result;
        }

        @Override
        public void visit(ImSet e) {
            ImLExpr left = e.getLeft();

            // Fast path: simple variable assignment (most common case)
            if (left instanceof ImVarAccess) {
                // Pure write, skip it
                e.getRight().accept(this);
                return;
            }

            // Complex left-hand side
            handleLExprReads(left);
            e.getRight().accept(this);
        }

        private void handleLExprReads(ImLExpr expr) {
            // Use type checks in order of frequency (optimize for common case)
            if (expr instanceof ImVarAccess) {
                // Write only, skip
            } else if (expr instanceof ImMemberAccess) {
                ((ImMemberAccess) expr).getReceiver().accept(this);
            } else if (expr instanceof ImVarArrayAccess) {
                ((ImVarArrayAccess) expr).getIndexes().accept(this);
            } else if (expr instanceof ImTupleSelection) {
                handleLExprReads((ImLExpr) ((ImTupleSelection) expr).getTupleExpr());
            } else if (expr instanceof ImStatementExpr) {
                ImStatementExpr se = (ImStatementExpr) expr;
                se.getStatements().accept(this);
                handleLExprReads((ImLExpr) se.getExpr());
            } else if (expr instanceof ImTupleExpr) {
                for (ImExpr ie : ((ImTupleExpr) expr).getExprs()) {
                    handleLExprReads((ImLExpr) ie);
                }
            }
        }

        @Override
        public void visit(ImVarAccess e) {
            result.add(e.getVar());
        }

        @Override
        public void visit(ImVarArrayAccess e) {
            result.add(e.getVar());
            super.visit(e); // Visit indexes
        }

        @Override
        public void visit(ImMemberAccess e) {
            result.add(e.getVar());
            super.visit(e); // Visit receiver
        }
    }
}
