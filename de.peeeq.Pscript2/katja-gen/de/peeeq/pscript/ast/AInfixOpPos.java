package de.peeeq.pscript.ast;

import katja.common.*;

public interface AInfixOpPos extends de.peeeq.pscript.ast.pscriptAST.SortPos {

    //----- methods of AInfixOpPos -----

    public KatjaSort term();
    public de.peeeq.pscript.ast.AInfixOp termAInfixOp();
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AInfixOpPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AInfixOpPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAEqEqPos(de.peeeq.pscript.ast.AEqEqPos term) throws E;
        public CT CaseAGtPos(de.peeeq.pscript.ast.AGtPos term) throws E;
        public CT CaseAGtEqPos(de.peeeq.pscript.ast.AGtEqPos term) throws E;
        public CT CaseALtPos(de.peeeq.pscript.ast.ALtPos term) throws E;
        public CT CaseALtEqPos(de.peeeq.pscript.ast.ALtEqPos term) throws E;
        public CT CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E;
        public CT CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E;
        public CT CaseAMultPos(de.peeeq.pscript.ast.AMultPos term) throws E;
        public CT CaseADivPos(de.peeeq.pscript.ast.ADivPos term) throws E;
        public CT CaseADivIntPos(de.peeeq.pscript.ast.ADivIntPos term) throws E;
        public CT CaseAModuloPos(de.peeeq.pscript.ast.AModuloPos term) throws E;
        public CT CaseADotPos(de.peeeq.pscript.ast.ADotPos term) throws E;
        public CT CaseAAndPos(de.peeeq.pscript.ast.AAndPos term) throws E;
        public CT CaseAOrPos(de.peeeq.pscript.ast.AOrPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.AInfixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AEqEqPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AGtPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AGtEqPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ALtPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ALtEqPos term) throws E;
        public void visit(de.peeeq.pscript.ast.APlusPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AMinusPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AMultPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ADivPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ADivIntPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AModuloPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ADotPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AAndPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AOrPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AInfixOpPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E> variantVisit$AInfixOpPos = new de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E>() { public final Object CaseAEqEqPos(de.peeeq.pscript.ast.AEqEqPos term) throws E { visit(term); return null; } public final Object CaseAGtPos(de.peeeq.pscript.ast.AGtPos term) throws E { visit(term); return null; } public final Object CaseAGtEqPos(de.peeeq.pscript.ast.AGtEqPos term) throws E { visit(term); return null; } public final Object CaseALtPos(de.peeeq.pscript.ast.ALtPos term) throws E { visit(term); return null; } public final Object CaseALtEqPos(de.peeeq.pscript.ast.ALtEqPos term) throws E { visit(term); return null; } public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseAMultPos(de.peeeq.pscript.ast.AMultPos term) throws E { visit(term); return null; } public final Object CaseADivPos(de.peeeq.pscript.ast.ADivPos term) throws E { visit(term); return null; } public final Object CaseADivIntPos(de.peeeq.pscript.ast.ADivIntPos term) throws E { visit(term); return null; } public final Object CaseAModuloPos(de.peeeq.pscript.ast.AModuloPos term) throws E { visit(term); return null; } public final Object CaseADotPos(de.peeeq.pscript.ast.ADotPos term) throws E { visit(term); return null; } public final Object CaseAAndPos(de.peeeq.pscript.ast.AAndPos term) throws E { visit(term); return null; } public final Object CaseAOrPos(de.peeeq.pscript.ast.AOrPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AInfixOpPos term) throws E {
            term.Switch(variantVisit$AInfixOpPos);
        }
    }
}

