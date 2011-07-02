package de.peeeq.pscript.ast;

import katja.common.*;

public interface ATypeExpr extends de.peeeq.pscript.ast.AReturnType, KatjaSort {

    //----- methods of ATypeExpr -----

    public java.lang.String name();
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeExpr.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ATypeExpr -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ATypeExpr.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E> variantVisit$ATypeExpr = new de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E>() { public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$ATypeExpr);
        }
    }
}

