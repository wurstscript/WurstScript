package de.peeeq.pscript.ast;

import katja.common.*;

public interface APrefixOpPos extends de.peeeq.pscript.ast.pscriptAST.SortPos {

    //----- methods of APrefixOpPos -----

    public KatjaSort term();
    public de.peeeq.pscript.ast.APrefixOp termAPrefixOp();
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.APrefixOpPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of APrefixOpPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E;
        public CT CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E;
        public CT CaseANotPos(de.peeeq.pscript.ast.ANotPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.APrefixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.APlusPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AMinusPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ANotPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.APrefixOpPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E> variantVisit$APrefixOpPos = new de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E>() { public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseANotPos(de.peeeq.pscript.ast.ANotPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.APrefixOpPos term) throws E {
            term.Switch(variantVisit$APrefixOpPos);
        }
    }
}

