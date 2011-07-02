package de.peeeq.pscript.ast;

import katja.common.*;

public interface AReturnTypePos extends de.peeeq.pscript.ast.pscriptAST.SortPos {

    //----- methods of AReturnTypePos -----

    public KatjaSort term();
    public de.peeeq.pscript.ast.AReturnType termAReturnType();
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnTypePos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AReturnTypePos -----

    public static interface Switch<CT, E extends Throwable> extends de.peeeq.pscript.ast.ATypeExprPos.Switch<CT, E> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAReturnsNothingPos(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnTypePos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AReturnTypePos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AReturnTypePos.Switch<Object, E> variantVisit$AReturnTypePos = new de.peeeq.pscript.ast.AReturnTypePos.Switch<Object, E>() { public final Object CaseAReturnsNothingPos(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E { visit(term); return null; } public final Object CaseATypeExprSimplePos(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AReturnTypePos term) throws E {
            term.Switch(variantVisit$AReturnTypePos);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E {
            term.Switch(variantVisit$AReturnTypePos);
        }
    }
}

