package de.peeeq.pscript.ast;

import katja.common.*;

public interface ALiteral extends de.peeeq.pscript.ast.AExpr, KatjaSort {

    //----- methods of ALiteral -----

    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteral.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ALiteral -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E;
        public CT CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E;
        public CT CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E;
        public CT CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.math.BigDecimal term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.pscript.ast.ALiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.AIntegerLiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.ARealLiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.AStringLiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.ABooleanLiteral term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ALiteral.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.ALiteral.Switch<Object, E> variantVisit$ALiteral = new de.peeeq.pscript.ast.ALiteral.Switch<Object, E>() { public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.ALiteral term) throws E {
            term.Switch(variantVisit$ALiteral);
        }
    }
}

