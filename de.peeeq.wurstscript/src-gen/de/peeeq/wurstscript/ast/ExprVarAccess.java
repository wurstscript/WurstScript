package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface ExprVarAccess extends de.peeeq.wurstscript.ast.ExprAssignable, de.peeeq.wurstscript.ast.ExprAtomic, de.peeeq.wurstscript.ast.VarRef, KatjaTuple {

    //----- methods of ExprVarAccess -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.ExprVarAccess replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public java.lang.String varName();
    public de.peeeq.wurstscript.ast.ExprVarAccess replaceVarName(java.lang.String varName);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.ExprVarAccess replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAssignable.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomic.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VarRef.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ExprVarAccess -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.ExprVarAccess.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.ExprVarAccess {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private java.lang.String _varName = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.ExprVarAccess replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public java.lang.String varName() {
            return _varName;
        }

        public de.peeeq.wurstscript.ast.ExprVarAccess replaceVarName(java.lang.String varName) {
            return replace(1, varName);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, java.lang.String varName) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ExprVarAccess invoked with null parameter source");
            if(varName == null)
                throw new IllegalArgumentException("constructor of sort ExprVarAccess invoked with null parameter varName");

            this._source = source;
            this._varName = varName;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0: return _source;
                case 1: return _varName;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ExprVarAccess invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ExprVarAccess invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.wurstscript.ast.ExprVarAccess replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ExprVarAccess invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort ExprVarAccess invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort ExprVarAccess invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort ExprVarAccess invoked with term of incorrect sort, String expected");

            return (de.peeeq.wurstscript.ast.ExprVarAccess) AST.unique(new de.peeeq.wurstscript.ast.ExprVarAccess.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (java.lang.String) term : _varName
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAssignable.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccess(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomic.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccess(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VarRef.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccess(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccess(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprVarAccess(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.ExprVarAccess");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            builder.append("\"").append(_varName.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ExprVarAccess");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            builder.append("\"").append(_varName.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ExprVarAccess";
        }
    }
}

