package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ANoExpr extends de.peeeq.pscript.ast.AExpr, KatjaTuple {

    //----- methods of ANoExpr -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.ANoExpr replaceSource(org.eclipse.emf.ecore.EObject source);
    public org.eclipse.emf.ecore.EObject get(int i);
    public int size();
    public de.peeeq.pscript.ast.ANoExpr replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ANoExpr -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(de.peeeq.pscript.ast.ANoExpr term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ANoExpr.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.ANoExpr {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.ANoExpr replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        Impl(org.eclipse.emf.ecore.EObject source) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ANoExpr invoked with null parameter source");

            this._source = source;
        }

        public org.eclipse.emf.ecore.EObject get(int i) {
            int ith = i;

            if(ith < 0) ith += 1;

            switch(ith) {
                case 0: return _source;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ANoExpr invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ANoExpr invoked with too large parameter "+i+", sort has only 1 components");
                    }
            }
        }

        public int size() {
            return 1;
        }

        public de.peeeq.pscript.ast.ANoExpr replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ANoExpr invoked with negative parameter "+pos);
            if(pos >= 1)
                throw new IllegalArgumentException("replace on sort ANoExpr invoked with too large parameter "+pos+", sort has only 1 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort ANoExpr invoked with term of incorrect sort, EObject expected");

            return (de.peeeq.pscript.ast.ANoExpr) pscriptAST.unique(new de.peeeq.pscript.ast.ANoExpr.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseANoExpr(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseANoExpr(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ANoExpr");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ANoExpr");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ANoExpr";
        }
    }
}

