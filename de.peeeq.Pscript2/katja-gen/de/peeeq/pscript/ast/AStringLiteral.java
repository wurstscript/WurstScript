package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AStringLiteral extends de.peeeq.pscript.ast.ALiteral, KatjaTuple {

    //----- methods of AStringLiteral -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.AStringLiteral replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.lang.String value();
    public de.peeeq.pscript.ast.AStringLiteral replaceValue(java.lang.String value);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.AStringLiteral replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteral.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AStringLiteral -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AStringLiteral term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AStringLiteral.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.AStringLiteral {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.lang.String _value = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.AStringLiteral replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.lang.String value() {
            return _value;
        }

        public de.peeeq.pscript.ast.AStringLiteral replaceValue(java.lang.String value) {
            return replace(1, value);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.lang.String value) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort AStringLiteral invoked with null parameter source");
            if(value == null)
                throw new IllegalArgumentException("constructor of sort AStringLiteral invoked with null parameter value");

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
                        throw new IllegalArgumentException("get on sort AStringLiteral invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AStringLiteral invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.AStringLiteral replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort AStringLiteral invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort AStringLiteral invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort AStringLiteral invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort AStringLiteral invoked with term of incorrect sort, String expected");

            return (de.peeeq.pscript.ast.AStringLiteral) pscriptAST.unique(new de.peeeq.pscript.ast.AStringLiteral.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.lang.String) term : _value
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ALiteral.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAStringLiteral(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAStringLiteral(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAStringLiteral(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.AStringLiteral");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            builder.append("\"").append(_value.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("AStringLiteral");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append("\"").append(_value.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AStringLiteral";
        }
    }
}

