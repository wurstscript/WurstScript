package de.peeeq.pscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface ARealLiteralPos extends de.peeeq.pscript.ast.ALiteralPos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.ARealLiteral> {

    //----- methods of ARealLiteralPos -----

    public de.peeeq.pscript.ast.ARealLiteral termALiteral();
    public de.peeeq.pscript.ast.ARealLiteral termAExpr();
    public de.peeeq.pscript.ast.ARealLiteral termAStatement();
    public de.peeeq.pscript.ast.ARealLiteral term();
    public de.peeeq.pscript.ast.EObjectPos source();
    public de.peeeq.pscript.ast.ARealLiteralPos replaceSource(org.eclipse.emf.ecore.EObject source);
    public de.peeeq.pscript.ast.BigDecimalPos value();
    public de.peeeq.pscript.ast.ARealLiteralPos replaceValue(java.math.BigDecimal value);
    public de.peeeq.pscript.ast.pscriptAST.LeafPos<?> get(int i);
    public int size();
    public de.peeeq.pscript.ast.ARealLiteralPos replace(de.peeeq.pscript.ast.ARealLiteral term);
    public de.peeeq.pscript.ast.pscriptAST.NodePos<?> parent();
    public de.peeeq.pscript.ast.pscriptAST.SortPos lsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteralPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExprPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatementPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ARealLiteralPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BigDecimalPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ARealLiteralPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ARealLiteralPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.ARealLiteral> implements de.peeeq.pscript.ast.ARealLiteralPos {

        //----- attributes of Impl -----

        private de.peeeq.pscript.ast.EObjectPos _source = null;
        private de.peeeq.pscript.ast.BigDecimalPos _value = null;

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.ARealLiteral termALiteral() {
            return term();
        }

        public de.peeeq.pscript.ast.ARealLiteral termAExpr() {
            return term();
        }

        public de.peeeq.pscript.ast.ARealLiteral termAStatement() {
            return term();
        }

        public de.peeeq.pscript.ast.EObjectPos source() {
            if(_source == null)
                _source = de.peeeq.pscript.ast.pscriptAST.EObjectPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.pscript.ast.ARealLiteralPos replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.pscript.ast.BigDecimalPos value() {
            if(_value == null)
                _value = de.peeeq.pscript.ast.pscriptAST.BigDecimalPos(this, _term.value(), 1);

            return _value;
        }

        public de.peeeq.pscript.ast.ARealLiteralPos replaceValue(java.math.BigDecimal value) {
            return replace(_term.replaceValue(value));
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.ARealLiteral term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.pscript.ast.pscriptAST.LeafPos<?> get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = pscriptAST.EObjectPos(this, _term.source(), 0);
                    
                    return _source;
                case 1:
                    if(_value == null)
                        _value = pscriptAST.BigDecimalPos(this, _term.value(), 1);
                    
                    return _value;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ARealLiteralPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ARealLiteralPos invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.ARealLiteralPos replace(de.peeeq.pscript.ast.ARealLiteral term) {
            return (de.peeeq.pscript.ast.ARealLiteralPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.pscriptAST.NodePos<?> parent() {
            return (de.peeeq.pscript.ast.pscriptAST.NodePos<?>) super.parent();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos lsib() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.lsib();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos rsib() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.rsib();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.preOrder();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.preOrderSkip();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.postOrder();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.postOrderStart();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path) {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.follow(path);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteralPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseARealLiteralPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExprPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseARealLiteralPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatementPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseARealLiteralPos(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ACompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            term().toString(builder);
            builder.append("@ACompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "ARealLiteralPos";
        }
    }
}

