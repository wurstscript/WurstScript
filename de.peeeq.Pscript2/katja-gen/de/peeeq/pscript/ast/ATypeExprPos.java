package de.peeeq.pscript.ast;

import katja.common.*;

public interface ATypeExprPos extends de.peeeq.pscript.ast.AReturnTypePos, de.peeeq.pscript.ast.pscriptAST.SortPos {

    //----- methods of ATypeExprPos -----

    public de.peeeq.pscript.ast.ATypeExpr termAReturnType();
    public KatjaSort term();
    public de.peeeq.pscript.ast.ATypeExpr termATypeExpr();
    public de.peeeq.pscript.ast.StringPos name();
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeExprPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ATypeExprPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseATypeExprSimplePos(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ATypeExprPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.ATypeExprPos.Switch<Object, E> variantVisit$ATypeExprPos = new de.peeeq.pscript.ast.ATypeExprPos.Switch<Object, E>() { public final Object CaseATypeExprSimplePos(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E {
            term.Switch(variantVisit$ATypeExprPos);
        }
    }
}

