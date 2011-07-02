package de.peeeq.pscript.ast;

import katja.common.*;

public interface APrefixOp extends KatjaSort {

    //----- methods of APrefixOp -----

    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.APrefixOp.Switch<CT, E> switchClass) throws E;

    //----- nested classes of APrefixOp -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E;
        public CT CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E;
        public CT CaseANot(de.peeeq.pscript.ast.ANot term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.APrefixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.APlus term) throws E;
        public void visit(de.peeeq.pscript.ast.AMinus term) throws E;
        public void visit(de.peeeq.pscript.ast.ANot term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.APrefixOp.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.APrefixOp.Switch<Object, E> variantVisit$APrefixOp = new de.peeeq.pscript.ast.APrefixOp.Switch<Object, E>() { public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseANot(de.peeeq.pscript.ast.ANot term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.APrefixOp term) throws E {
            term.Switch(variantVisit$APrefixOp);
        }
    }
}

