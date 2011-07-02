package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AVariableAccess extends de.peeeq.pscript.ast.AExpr, KatjaTuple {

    //----- methods of AVariableAccess -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.AVariableAccess replaceSource(org.eclipse.emf.ecore.EObject source);
    public de.peeeq.pscript.ast.AIdentifier ident();
    public de.peeeq.pscript.ast.AVariableAccess replaceIdent(de.peeeq.pscript.ast.AIdentifier ident);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.AVariableAccess replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AVariableAccess -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifier term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AVariableAccess term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AVariableAccess.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.AVariableAccess {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private de.peeeq.pscript.ast.AIdentifier _ident = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.AVariableAccess replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public de.peeeq.pscript.ast.AIdentifier ident() {
            return _ident;
        }

        public de.peeeq.pscript.ast.AVariableAccess replaceIdent(de.peeeq.pscript.ast.AIdentifier ident) {
            return replace(1, ident);
        }

        Impl(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AIdentifier ident) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort AVariableAccess invoked with null parameter source");
            if(ident == null)
                throw new IllegalArgumentException("constructor of sort AVariableAccess invoked with null parameter ident");

            this._source = source;
            this._ident = ident;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0: return _source;
                case 1: return _ident;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AVariableAccess invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AVariableAccess invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.AVariableAccess replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort AVariableAccess invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort AVariableAccess invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort AVariableAccess invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof de.peeeq.pscript.ast.AIdentifier))
                throw new IllegalArgumentException("replace on sort AVariableAccess invoked with term of incorrect sort, AIdentifier expected");

            return (de.peeeq.pscript.ast.AVariableAccess) pscriptAST.unique(new de.peeeq.pscript.ast.AVariableAccess.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (de.peeeq.pscript.ast.AIdentifier) term : _ident
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAVariableAccess(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAVariableAccess(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.AVariableAccess");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            _ident.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("AVariableAccess");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            _ident.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AVariableAccess";
        }
    }
}

