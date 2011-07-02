package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ARealLiteral extends de.peeeq.pscript.ast.ALiteral, KatjaTuple {

    //----- methods of ARealLiteral -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.ARealLiteral replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.math.BigDecimal value();
    public de.peeeq.pscript.ast.ARealLiteral replaceValue(java.math.BigDecimal value);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.ARealLiteral replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteral.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ARealLiteral -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.math.BigDecimal term) throws E;
        public void visit(de.peeeq.pscript.ast.ARealLiteral term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ARealLiteral.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.ARealLiteral {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.math.BigDecimal _value = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.ARealLiteral replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.math.BigDecimal value() {
            return _value;
        }

        public de.peeeq.pscript.ast.ARealLiteral replaceValue(java.math.BigDecimal value) {
            return replace(1, value);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.math.BigDecimal value) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ARealLiteral invoked with null parameter source");
            if(value == null)
                throw new IllegalArgumentException("constructor of sort ARealLiteral invoked with null parameter value");

            this._source = source;
            this._value = value;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0: return _source;
                case 1: return _value;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ARealLiteral invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ARealLiteral invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.ARealLiteral replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ARealLiteral invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort ARealLiteral invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort ARealLiteral invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.math.BigDecimal))
                throw new IllegalArgumentException("replace on sort ARealLiteral invoked with term of incorrect sort, BigDecimal expected");

            return (de.peeeq.pscript.ast.ARealLiteral) pscriptAST.unique(new de.peeeq.pscript.ast.ARealLiteral.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.math.BigDecimal) term : _value
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteral.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseARealLiteral(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseARealLiteral(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseARealLiteral(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ARealLiteral");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            if(_value instanceof KatjaElement) ((KatjaElement) _value).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort BigDecimal can't be unparsed to Java code (don't know how to do it)");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ARealLiteral");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append(_value.toString());
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ARealLiteral";
        }
    }
}

