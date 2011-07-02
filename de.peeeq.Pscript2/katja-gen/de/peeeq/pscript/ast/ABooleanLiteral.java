package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ABooleanLiteral extends de.peeeq.pscript.ast.ALiteral, KatjaTuple {

    //----- methods of ABooleanLiteral -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.ABooleanLiteral replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.lang.Boolean value();
    public de.peeeq.pscript.ast.ABooleanLiteral replaceValue(java.lang.Boolean value);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.ABooleanLiteral replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteral.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ABooleanLiteral -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.pscript.ast.ABooleanLiteral term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ABooleanLiteral.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.ABooleanLiteral {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.lang.Boolean _value = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.ABooleanLiteral replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.lang.Boolean value() {
            return _value;
        }

        public de.peeeq.pscript.ast.ABooleanLiteral replaceValue(java.lang.Boolean value) {
            return replace(1, value);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.lang.Boolean value) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ABooleanLiteral invoked with null parameter source");
            if(value == null)
                throw new IllegalArgumentException("constructor of sort ABooleanLiteral invoked with null parameter value");

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
                        throw new IllegalArgumentException("get on sort ABooleanLiteral invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ABooleanLiteral invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.ABooleanLiteral replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ABooleanLiteral invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort ABooleanLiteral invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort ABooleanLiteral invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.lang.Boolean))
                throw new IllegalArgumentException("replace on sort ABooleanLiteral invoked with term of incorrect sort, Boolean expected");

            return (de.peeeq.pscript.ast.ABooleanLiteral) pscriptAST.unique(new de.peeeq.pscript.ast.ABooleanLiteral.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.lang.Boolean) term : _value
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteral.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseABooleanLiteral(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseABooleanLiteral(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseABooleanLiteral(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ABooleanLiteral");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            builder.append(_value.toString());
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ABooleanLiteral");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append(_value.toString());
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ABooleanLiteral";
        }
    }
}

