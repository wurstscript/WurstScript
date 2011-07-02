package de.peeeq.pscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface ATypeExprSimplePos extends de.peeeq.pscript.ast.ATypeExprPos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.ATypeExprSimple> {

    //----- methods of ATypeExprSimplePos -----

    public de.peeeq.pscript.ast.ATypeExprSimple termATypeExpr();
    public de.peeeq.pscript.ast.ATypeExprSimple termAReturnType();
    public de.peeeq.pscript.ast.ATypeExprSimple term();
    public de.peeeq.pscript.ast.StringPos name();
    public de.peeeq.pscript.ast.ATypeExprSimplePos replaceName(java.lang.String name);
    public de.peeeq.pscript.ast.StringPos get(int i);
    public int size();
    public de.peeeq.pscript.ast.ATypeExprSimplePos replace(de.peeeq.pscript.ast.ATypeExprSimple term);
    public de.peeeq.pscript.ast.pscriptAST.TuplePos<?> parent();
    public de.peeeq.pscript.ast.StringPos lsib();
    public de.peeeq.pscript.ast.pscriptAST.TermPos<?> rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeExprPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnTypePos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ATypeExprSimplePos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ATypeExprSimplePos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.ATypeExprSimple> implements de.peeeq.pscript.ast.ATypeExprSimplePos {

        //----- attributes of Impl -----

        private de.peeeq.pscript.ast.StringPos _name = null;

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.ATypeExprSimple termATypeExpr() {
            return term();
        }

        public de.peeeq.pscript.ast.ATypeExprSimple termAReturnType() {
            return term();
        }

        public de.peeeq.pscript.ast.StringPos name() {
            if(_name == null)
                _name = de.peeeq.pscript.ast.pscriptAST.StringPos(this, _term.name(), 0);

            return _name;
        }

        public de.peeeq.pscript.ast.ATypeExprSimplePos replaceName(java.lang.String name) {
            return replace(_term.replaceName(name));
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.ATypeExprSimple term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.pscript.ast.StringPos get(int i) {
            int ith = i;

            if(ith < 0) ith += 1;

            switch(ith) {
                case 0:
                    if(_name == null)
                        _name = pscriptAST.StringPos(this, _term.name(), 0);
                    
                    return _name;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ATypeExprSimplePos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ATypeExprSimplePos invoked with too large parameter "+i+", sort has only 1 components");
                    }
            }
        }

        public int size() {
            return 1;
        }

        public de.peeeq.pscript.ast.ATypeExprSimplePos replace(de.peeeq.pscript.ast.ATypeExprSimple term) {
            return (de.peeeq.pscript.ast.ATypeExprSimplePos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.pscriptAST.TuplePos<?> parent() {
            return (de.peeeq.pscript.ast.pscriptAST.TuplePos<?>) super.parent();
        }

        public de.peeeq.pscript.ast.StringPos lsib() {
            return (de.peeeq.pscript.ast.StringPos) super.lsib();
        }

        public de.peeeq.pscript.ast.pscriptAST.TermPos<?> rsib() {
            return (de.peeeq.pscript.ast.pscriptAST.TermPos<?>) super.rsib();
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

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeExprPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseATypeExprSimplePos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnTypePos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseATypeExprSimplePos(this);
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
            return "ATypeExprSimplePos";
        }
    }
}

