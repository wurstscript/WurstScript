package de.peeeq.pscript.ast;

import katja.common.*;

public interface AReturnType extends KatjaSort {

    //----- methods of AReturnType -----

    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnType.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AReturnType -----

    public static interface Switch<CT, E extends Throwable> extends de.peeeq.pscript.ast.ATypeExpr.Switch<CT, E> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAReturnsNothing(de.peeeq.pscript.ast.AReturnsNothing term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnType term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnsNothing term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AReturnType.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AReturnType.Switch<Object, E> variantVisit$AReturnType = new de.peeeq.pscript.ast.AReturnType.Switch<Object, E>() { public final Object CaseAReturnsNothing(de.peeeq.pscript.ast.AReturnsNothing term) throws E { visit(term); return null; } public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AReturnType term) throws E {
            term.Switch(variantVisit$AReturnType);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$AReturnType);
        }
    }
}

