package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface ExprIntVal extends de.peeeq.wurstscript.ast.ExprAtomic, KatjaTuple {

    //----- methods of ExprIntVal -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.ExprIntVal replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public java.lang.Integer val();
    public de.peeeq.wurstscript.ast.ExprIntVal replaceVal(java.lang.Integer val);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.ExprIntVal replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomic.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ExprIntVal -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprIntVal term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.ExprIntVal.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.ExprIntVal {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private java.lang.Integer _val = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.ExprIntVal replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public java.lang.Integer val() {
            return _val;
        }

        public de.peeeq.wurstscript.ast.ExprIntVal replaceVal(java.lang.Integer val) {
            return replace(1, val);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, java.lang.Integer val) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ExprIntVal invoked with null parameter source");
            if(val == null)
                throw new IllegalArgumentException("constructor of sort ExprIntVal invoked with null parameter val");

            this._source = source;
            this._val = val;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0: return _source;
                case 1: return _val;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ExprIntVal invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ExprIntVal invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.wurstscript.ast.ExprIntVal replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ExprIntVal invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort ExprIntVal invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort ExprIntVal invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof java.lang.Integer))
                throw new IllegalArgumentException("replace on sort ExprIntVal invoked with term of incorrect sort, Integer expected");

            return (de.peeeq.wurstscript.ast.ExprIntVal) AST.unique(new de.peeeq.wurstscript.ast.ExprIntVal.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (java.lang.Integer) term : _val
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ExprAtomic.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprIntVal(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprIntVal(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprIntVal(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.ExprIntVal");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            builder.append(_val.toString());
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ExprIntVal");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            builder.append(_val.toString());
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ExprIntVal";
        }
    }
}

