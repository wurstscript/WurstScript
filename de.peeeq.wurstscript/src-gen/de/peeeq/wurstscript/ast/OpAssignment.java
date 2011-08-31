package de.peeeq.wurstscript.ast;

import katja.common.KatjaSort;

public interface OpAssignment extends KatjaSort {

    //----- methods of OpAssignment -----

    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpAssignment.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpAssignment -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseOpAssign(de.peeeq.wurstscript.ast.OpAssign term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpAssignment term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssign term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpAssignment.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E> variantVisit$OpAssignment = new de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E>() { public final Object CaseOpAssign(de.peeeq.wurstscript.ast.OpAssign term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.OpAssignment term) throws E {
            term.Switch(variantVisit$OpAssignment);
        }
    }
}

