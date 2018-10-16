package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.jassIm.*;

import java.util.Set;

public class UsedVariables {

    public static Set<ImVar> calculate(ImFunction f) {
        final Set<ImVar> result = Sets.newLinkedHashSet();
        f.accept(new ImFunction.DefaultVisitor() {

            @Override
            public void visit(ImVarAccess e) {
                super.visit(e);
                result.add(e.getVar());
            }

            @Override
            public void visit(ImVarArrayAccess e) {
                super.visit(e);
                result.add(e.getVar());
            }
        });
        return result;
    }

    public static Set<ImVar> calculateReadVars(ImFunction f) {
        final Set<ImVar> result = Sets.newLinkedHashSet();
        f.accept(new ImFunction.DefaultVisitor() {

            @Override
            public void visit(ImSet e) {
                Element.DefaultVisitor thiz = this;
                e.getLeft().match(new ImLExpr.MatcherVoid() {
                    @Override
                    public void case_ImVarAccess(ImVarAccess e) {
                        // only written, not read
                    }

                    @Override
                    public void case_ImStatementExpr(ImStatementExpr e) {
                        e.getStatements().accept(thiz);
                        ((ImLExpr) e.getExpr()).match(this);
                    }

                    @Override
                    public void case_ImTupleSelection(ImTupleSelection e) {
                        ((ImLExpr) e.getTupleExpr()).match(this);
                    }

                    @Override
                    public void case_ImVarArrayAccess(ImVarArrayAccess e) {
                        e.getIndexes().accept(thiz);
                    }

                    @Override
                    public void case_ImMemberAccess(ImMemberAccess e) {
                        e.getReceiver().accept(thiz);
                    }

                    @Override
                    public void case_ImTupleExpr(ImTupleExpr e) {
                        for (ImExpr ie : e.getExprs()) {
                            ((ImLExpr) ie).match(this);
                        }
                    }
                });
                e.getRight().accept(this);
            }

            @Override
            public void visit(ImVarAccess e) {
                super.visit(e);
                result.add(e.getVar());
            }

            @Override
            public void visit(ImVarArrayAccess e) {
                super.visit(e);
                result.add(e.getVar());
            }
        });
        return result;
    }
}
