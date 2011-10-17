package de.peeeq.wurstscript.ast;

import katja.common.KatjaSort;

public interface VisibilityModifier extends KatjaSort {

    //----- methods of VisibilityModifier -----

    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifier.Switch<CT, E> switchClass) throws E;

    //----- nested classes of VisibilityModifier -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseVisibilityPublic(de.peeeq.wurstscript.ast.VisibilityPublic term) throws E;
        public CT CaseVisibilityPrivate(de.peeeq.wurstscript.ast.VisibilityPrivate term) throws E;
        public CT CaseVisibilityPublicread(de.peeeq.wurstscript.ast.VisibilityPublicread term) throws E;
        public CT CaseVisibilityProtected(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E;
        public CT CaseVisibilityDefault(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityModifier term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublic term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPrivate term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublicread term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.VisibilityModifier.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.VisibilityModifier.Switch<Object, E> variantVisit$VisibilityModifier = new de.peeeq.wurstscript.ast.VisibilityModifier.Switch<Object, E>() { public final Object CaseVisibilityPublic(de.peeeq.wurstscript.ast.VisibilityPublic term) throws E { visit(term); return null; } public final Object CaseVisibilityPrivate(de.peeeq.wurstscript.ast.VisibilityPrivate term) throws E { visit(term); return null; } public final Object CaseVisibilityPublicread(de.peeeq.wurstscript.ast.VisibilityPublicread term) throws E { visit(term); return null; } public final Object CaseVisibilityProtected(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E { visit(term); return null; } public final Object CaseVisibilityDefault(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.VisibilityModifier term) throws E {
            term.Switch(variantVisit$VisibilityModifier);
        }
    }
}

