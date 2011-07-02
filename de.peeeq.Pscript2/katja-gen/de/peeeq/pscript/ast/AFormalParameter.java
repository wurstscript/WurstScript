package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AFormalParameter extends KatjaTuple {

    //----- methods of AFormalParameter -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.AFormalParameter replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.lang.String name();
    public de.peeeq.pscript.ast.AFormalParameter replaceName(java.lang.String name);
    public de.peeeq.pscript.ast.ATypeExpr type();
    public de.peeeq.pscript.ast.AFormalParameter replaceType(de.peeeq.pscript.ast.ATypeExpr type);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.AFormalParameter replace(int pos, Object term);

    //----- nested classes of AFormalParameter -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameter term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AFormalParameter.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E> variantVisit$ATypeExpr = new de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E>() { public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$ATypeExpr);
        }
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.AFormalParameter {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.lang.String _name = null;
        private de.peeeq.pscript.ast.ATypeExpr _type = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.AFormalParameter replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.pscript.ast.AFormalParameter replaceName(java.lang.String name) {
            return replace(1, name);
        }

        public de.peeeq.pscript.ast.ATypeExpr type() {
            return _type;
        }

        public de.peeeq.pscript.ast.AFormalParameter replaceType(de.peeeq.pscript.ast.ATypeExpr type) {
            return replace(2, type);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.ATypeExpr type) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort AFormalParameter invoked with null parameter source");
            if(name == null)
                throw new IllegalArgumentException("constructor of sort AFormalParameter invoked with null parameter name");
            if(type == null)
                throw new IllegalArgumentException("constructor of sort AFormalParameter invoked with null parameter type");

            this._source = source;
            this._name = name;
            this._type = type;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 3;

            switch(ith) {
                case 0: return _source;
                case 1: return _name;
                case 2: return _type;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AFormalParameter invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AFormalParameter invoked with too large parameter "+i+", sort has only 3 components");
                    }
            }
        }

        public int size() {
            return 3;
        }

        public de.peeeq.pscript.ast.AFormalParameter replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort AFormalParameter invoked with negative parameter "+pos);
            if(pos >= 3)
                throw new IllegalArgumentException("replace on sort AFormalParameter invoked with too large parameter "+pos+", sort has only 3 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort AFormalParameter invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort AFormalParameter invoked with term of incorrect sort, String expected");
            if(pos == 2 && !(term instanceof de.peeeq.pscript.ast.ATypeExpr))
                throw new IllegalArgumentException("replace on sort AFormalParameter invoked with term of incorrect sort, ATypeExpr expected");

            return (de.peeeq.pscript.ast.AFormalParameter) pscriptAST.unique(new de.peeeq.pscript.ast.AFormalParameter.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.lang.String) term : _name,
                pos == 2 ? (de.peeeq.pscript.ast.ATypeExpr) term : _type
            ));
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.AFormalParameter");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _type.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("AFormalParameter");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _type.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AFormalParameter";
        }
    }
}

