package de.peeeq.wurstscript.ast;

import katja.common.KatjaSort;

public interface VisibilityModifierPos extends de.peeeq.wurstscript.ast.AST.SortPos {

    //----- methods of VisibilityModifierPos -----

    public KatjaSort term();
    public de.peeeq.wurstscript.ast.VisibilityModifier termVisibilityModifier();
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifierPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of VisibilityModifierPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseVisibilityPublicPos(de.peeeq.wurstscript.ast.VisibilityPublicPos term) throws E;
        public CT CaseVisibilityPrivatePos(de.peeeq.wurstscript.ast.VisibilityPrivatePos term) throws E;
        public CT CaseVisibilityPublicreadPos(de.peeeq.wurstscript.ast.VisibilityPublicreadPos term) throws E;
        public CT CaseVisibilityProtectedPos(de.peeeq.wurstscript.ast.VisibilityProtectedPos term) throws E;
        public CT CaseVisibilityDefaultPos(de.peeeq.wurstscript.ast.VisibilityDefaultPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityModifierPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublicPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPrivatePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublicreadPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityProtectedPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityDefaultPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.VisibilityModifierPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.VisibilityModifierPos.Switch<Object, E> variantVisit$VisibilityModifierPos = new de.peeeq.wurstscript.ast.VisibilityModifierPos.Switch<Object, E>() { public final Object CaseVisibilityPublicPos(de.peeeq.wurstscript.ast.VisibilityPublicPos term) throws E { visit(term); return null; } public final Object CaseVisibilityPrivatePos(de.peeeq.wurstscript.ast.VisibilityPrivatePos term) throws E { visit(term); return null; } public final Object CaseVisibilityPublicreadPos(de.peeeq.wurstscript.ast.VisibilityPublicreadPos term) throws E { visit(term); return null; } public final Object CaseVisibilityProtectedPos(de.peeeq.wurstscript.ast.VisibilityProtectedPos term) throws E { visit(term); return null; } public final Object CaseVisibilityDefaultPos(de.peeeq.wurstscript.ast.VisibilityDefaultPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.VisibilityModifierPos term) throws E {
            term.Switch(variantVisit$VisibilityModifierPos);
        }
    }
}

