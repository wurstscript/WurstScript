package de.peeeq.wurstscript.ast;

import katja.common.KatjaSort;

public interface OpUnaryPos extends de.peeeq.wurstscript.ast.OpPos, de.peeeq.wurstscript.ast.AST.SortPos {

    //----- methods of OpUnaryPos -----

    public de.peeeq.wurstscript.ast.OpUnary termOp();
    public KatjaSort term();
    public de.peeeq.wurstscript.ast.OpUnary termOpUnary();
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpUnaryPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpUnaryPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseOpNotPos(de.peeeq.wurstscript.ast.OpNotPos term) throws E;
        public CT CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpNotPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpMinusPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpUnaryPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E> variantVisit$OpUnaryPos = new de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E>() { public final Object CaseOpNotPos(de.peeeq.wurstscript.ast.OpNotPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E {
            term.Switch(variantVisit$OpUnaryPos);
        }
    }
}

