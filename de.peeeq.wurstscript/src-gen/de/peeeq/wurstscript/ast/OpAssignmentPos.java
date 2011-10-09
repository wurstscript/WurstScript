package de.peeeq.wurstscript.ast;

import katja.common.*;

public interface OpAssignmentPos extends de.peeeq.wurstscript.ast.AST.SortPos {

    //----- methods of OpAssignmentPos -----

    public KatjaSort term();
    public de.peeeq.wurstscript.ast.OpAssignment termOpAssignment();
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpAssignmentPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpAssignmentPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseOpAssignPos(de.peeeq.wurstscript.ast.OpAssignPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpAssignmentPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssignPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpAssignmentPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.OpAssignmentPos.Switch<Object, E> variantVisit$OpAssignmentPos = new de.peeeq.wurstscript.ast.OpAssignmentPos.Switch<Object, E>() { public final Object CaseOpAssignPos(de.peeeq.wurstscript.ast.OpAssignPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.OpAssignmentPos term) throws E {
            term.Switch(variantVisit$OpAssignmentPos);
        }
    }
}

