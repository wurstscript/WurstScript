package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface ExprFuncRef extends de.peeeq.wurstscript.ast.ExprAtomic, de.peeeq.wurstscript.ast.FuncRef, KatjaTuple {

    //----- methods of ExprFuncRef -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.ExprFuncRef replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public java.lang.String funcName();
    public de.peeeq.wurstscript.ast.ExprFuncRef replaceFuncName(java.lang.String funcName);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.ExprFuncRef replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomic.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.FuncRef.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ExprFuncRef -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.ExprFuncRef.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.ExprFuncRef {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private java.lang.String _funcName = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.ExprFuncRef replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public java.lang.String funcName() {
            return _funcName;
        }

        public de.peeeq.wurstscript.ast.ExprFuncRef replaceFuncName(java.lang.String funcName) {
            return replace(1, funcName);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, java.lang.String funcName) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ExprFuncRef invoked with null parameter source");
            if(funcName == null)
                throw new IllegalArgumentException("constructor of sort ExprFuncRef invoked with null parameter funcName");

            this._source = source;
            this._funcName = funcName;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0: return _source;
                case 1: return _funcName;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ExprFuncRef invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ExprFuncRef invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.wurstscript.ast.ExprFuncRef replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ExprFuncRef invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort ExprFuncRef invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort ExprFuncRef invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort ExprFuncRef invoked with term of incorrect sort, String expected");

            return (de.peeeq.wurstscript.ast.ExprFuncRef) AST.unique(new de.peeeq.wurstscript.ast.ExprFuncRef.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (java.lang.String) term : _funcName
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomic.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprFuncRef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.FuncRef.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprFuncRef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprFuncRef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprFuncRef(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.ExprFuncRef");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            builder.append("\"").append(_funcName.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ExprFuncRef");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            builder.append("\"").append(_funcName.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ExprFuncRef";
        }
    }
}

