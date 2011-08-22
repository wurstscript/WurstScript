package de.peeeq.wurstscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface ExprVarAccessPos extends de.peeeq.wurstscript.ast.ExprAssignablePos, de.peeeq.wurstscript.ast.ExprAtomicPos, de.peeeq.wurstscript.ast.VarRefPos, de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.ExprVarAccess> {

    //----- methods of ExprVarAccessPos -----

    public de.peeeq.wurstscript.ast.ExprVarAccess termExprAssignable();
    public de.peeeq.wurstscript.ast.ExprVarAccess termExprAtomic();
    public de.peeeq.wurstscript.ast.ExprVarAccess termVarRef();
    public de.peeeq.wurstscript.ast.ExprVarAccess termExpr();
    public de.peeeq.wurstscript.ast.ExprVarAccess termOptExpr();
    public de.peeeq.wurstscript.ast.ExprVarAccess term();
    public de.peeeq.wurstscript.ast.WPosPos source();
    public de.peeeq.wurstscript.ast.ExprVarAccessPos replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.StringPos varName();
    public de.peeeq.wurstscript.ast.ExprVarAccessPos replaceVarName(java.lang.String varName);
    public de.peeeq.wurstscript.ast.AST.TermPos<?> get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.ExprVarAccessPos replace(de.peeeq.wurstscript.ast.ExprVarAccess term);
    public de.peeeq.wurstscript.ast.AST.NodePos<?> parent();
    public de.peeeq.wurstscript.ast.AST.SortPos lsib();
    public de.peeeq.wurstscript.ast.AST.SortPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAssignablePos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VarRefPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExprPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ExprVarAccessPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.ExprVarAccessPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.ExprVarAccess> implements de.peeeq.wurstscript.ast.ExprVarAccessPos {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPosPos _source = null;
        private de.peeeq.wurstscript.ast.StringPos _varName = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.ExprVarAccess termExprAssignable() {
            return term();
        }

        public de.peeeq.wurstscript.ast.ExprVarAccess termExprAtomic() {
            return term();
        }

        public de.peeeq.wurstscript.ast.ExprVarAccess termVarRef() {
            return term();
        }

        public de.peeeq.wurstscript.ast.ExprVarAccess termExpr() {
            return term();
        }

        public de.peeeq.wurstscript.ast.ExprVarAccess termOptExpr() {
            return term();
        }

        public de.peeeq.wurstscript.ast.WPosPos source() {
            if(_source == null)
                _source = de.peeeq.wurstscript.ast.AST.WPosPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.wurstscript.ast.ExprVarAccessPos replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.wurstscript.ast.StringPos varName() {
            if(_varName == null)
                _varName = de.peeeq.wurstscript.ast.AST.StringPos(this, _term.varName(), 1);

            return _varName;
        }

        public de.peeeq.wurstscript.ast.ExprVarAccessPos replaceVarName(java.lang.String varName) {
            return replace(_term.replaceVarName(varName));
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.ExprVarAccess term, int pos) {
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
                    if(_varName == null)
                        _varName = AST.StringPos(this, _term.varName(), 1);
                    
                    return _varName;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ExprVarAccessPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ExprVarAccessPos invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.wurstscript.ast.ExprVarAccessPos replace(de.peeeq.wurstscript.ast.ExprVarAccess term) {
            return (de.peeeq.wurstscript.ast.ExprVarAccessPos) super.replace(term);
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

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAssignablePos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccessPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccessPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VarRefPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccessPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccessPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExprPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccessPos(this);
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
            return "ExprVarAccessPos";
        }
    }
}

