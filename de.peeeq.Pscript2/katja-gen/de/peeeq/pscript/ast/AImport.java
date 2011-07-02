package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AImport extends KatjaTuple {

    //----- methods of AImport -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.AImport replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.lang.String name();
    public de.peeeq.pscript.ast.AImport replaceName(java.lang.String name);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.AImport replace(int pos, Object term);

    //----- nested classes of AImport -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AImport term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AImport.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.AImport {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.lang.String _name = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.AImport replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.pscript.ast.AImport replaceName(java.lang.String name) {
            return replace(1, name);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.lang.String name) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort AImport invoked with null parameter source");
            if(name == null)
                throw new IllegalArgumentException("constructor of sort AImport invoked with null parameter name");

            this._source = source;
            this._name = name;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0: return _source;
                case 1: return _name;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AImport invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AImport invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.AImport replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort AImport invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort AImport invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort AImport invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort AImport invoked with term of incorrect sort, String expected");

            return (de.peeeq.pscript.ast.AImport) pscriptAST.unique(new de.peeeq.pscript.ast.AImport.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.lang.String) term : _name
            ));
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.AImport");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("AImport");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AImport";
        }
    }
}

