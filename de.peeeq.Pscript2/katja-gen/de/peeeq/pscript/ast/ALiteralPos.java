package de.peeeq.pscript.ast;

import katja.common.*;

public interface ALiteralPos extends de.peeeq.pscript.ast.AExprPos, de.peeeq.pscript.ast.pscriptAST.SortPos {

    //----- methods of ALiteralPos -----

    public de.peeeq.pscript.ast.ALiteral termAExpr();
    public de.peeeq.pscript.ast.ALiteral termAStatement();
    public KatjaSort term();
    public de.peeeq.pscript.ast.ALiteral termALiteral();
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteralPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ALiteralPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E;
        public CT CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E;
        public CT CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E;
        public CT CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BigDecimalPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BooleanPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ALiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ARealLiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AStringLiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ALiteralPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.ALiteralPos.Switch<Object, E> variantVisit$ALiteralPos = new de.peeeq.pscript.ast.ALiteralPos.Switch<Object, E>() { public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.ALiteralPos term) throws E {
            term.Switch(variantVisit$ALiteralPos);
        }
    }
}

