package de.peeeq.wurstscript.ast;

import java.io.IOException;
import java.util.List;

import katja.common.KatjaNodePos;
import katja.common.KatjaSort;
import katja.common.KatjaTuplePosImpl;

public interface ExprIntValPos extends de.peeeq.wurstscript.ast.ExprAtomicPos, de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.ExprIntVal> {

    //----- methods of ExprIntValPos -----

    public de.peeeq.wurstscript.ast.ExprIntVal termExprAtomic();
    public de.peeeq.wurstscript.ast.ExprIntVal termExpr();
    public de.peeeq.wurstscript.ast.ExprIntVal termOptExpr();
    public de.peeeq.wurstscript.ast.ExprIntVal term();
    public de.peeeq.wurstscript.ast.WPosPos source();
    public de.peeeq.wurstscript.ast.ExprIntValPos replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.IntegerPos val();
    public de.peeeq.wurstscript.ast.ExprIntValPos replaceVal(java.lang.Integer val);
    public de.peeeq.wurstscript.ast.AST.TermPos<?> get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.ExprIntValPos replace(de.peeeq.wurstscript.ast.ExprIntVal term);
    public de.peeeq.wurstscript.ast.AST.NodePos<?> parent();
    public de.peeeq.wurstscript.ast.AST.SortPos lsib();
    public de.peeeq.wurstscript.ast.AST.SortPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExprPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ExprIntValPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.ExprIntValPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.ExprIntVal> implements de.peeeq.wurstscript.ast.ExprIntValPos {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPosPos _source = null;
        private de.peeeq.wurstscript.ast.IntegerPos _val = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.ExprIntVal termExprAtomic() {
            return term();
        }

        public de.peeeq.wurstscript.ast.ExprIntVal termExpr() {
            return term();
        }

        public de.peeeq.wurstscript.ast.ExprIntVal termOptExpr() {
            return term();
        }

        public de.peeeq.wurstscript.ast.WPosPos source() {
            if(_source == null)
                _source = de.peeeq.wurstscript.ast.AST.WPosPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.wurstscript.ast.ExprIntValPos replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.wurstscript.ast.IntegerPos val() {
            if(_val == null)
                _val = de.peeeq.wurstscript.ast.AST.IntegerPos(this, _term.val(), 1);

            return _val;
        }

        public de.peeeq.wurstscript.ast.ExprIntValPos replaceVal(java.lang.Integer val) {
            return replace(_term.replaceVal(val));
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.ExprIntVal term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.wurstscript.ast.AST.TermPos<?> get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = AST.WPosPos(this, _term.source(), 0);
                    
                    return _source;
                case 1:
                    if(_val == null)
                        _val = AST.IntegerPos(this, _term.val(), 1);
                    
                    return _val;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ExprIntValPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ExprIntValPos invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.wurstscript.ast.ExprIntValPos replace(de.peeeq.wurstscript.ast.ExprIntVal term) {
            return (de.peeeq.wurstscript.ast.ExprIntValPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.AST.NodePos<?> parent() {
            return (de.peeeq.wurstscript.ast.AST.NodePos<?>) super.parent();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos lsib() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos rsib() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.rsib();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrderSkip();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrderStart();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path) {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.follow(path);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprIntValPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprIntValPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExprPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprIntValPos(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.CompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            term().toString(builder);
            builder.append("@CompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "ExprIntValPos";
        }
    }
}

