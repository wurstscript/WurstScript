package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ATypeExprSimple extends de.peeeq.pscript.ast.ATypeExpr, KatjaTuple {

    //----- methods of ATypeExprSimple -----

    public java.lang.String name();
    public de.peeeq.pscript.ast.ATypeExprSimple replaceName(java.lang.String name);
    public java.lang.String get(int i);
    public int size();
    public de.peeeq.pscript.ast.ATypeExprSimple replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnType.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ATypeExprSimple -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ATypeExprSimple.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.ATypeExprSimple {

        //----- attributes of Impl -----

        private java.lang.String _name = null;

        //----- methods of Impl -----

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.pscript.ast.ATypeExprSimple replaceName(java.lang.String name) {
            return replace(0, name);
        }

        Impl(java.lang.String name) {
            if(name == null)
                throw new IllegalArgumentException("constructor of sort ATypeExprSimple invoked with null parameter name");

            this._name = name;
        }

        public java.lang.String get(int i) {
            int ith = i;

            if(ith < 0) ith += 1;

            switch(ith) {
                case 0: return _name;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ATypeExprSimple invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ATypeExprSimple invoked with too large parameter "+i+", sort has only 1 components");
                    }
            }
        }

        public int size() {
            return 1;
        }

        public de.peeeq.pscript.ast.ATypeExprSimple replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ATypeExprSimple invoked with negative parameter "+pos);
            if(pos >= 1)
                throw new IllegalArgumentException("replace on sort ATypeExprSimple invoked with too large parameter "+pos+", sort has only 1 components");

            if(pos == 0 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort ATypeExprSimple invoked with term of incorrect sort, String expected");

            return (de.peeeq.pscript.ast.ATypeExprSimple) pscriptAST.unique(new de.peeeq.pscript.ast.ATypeExprSimple.Impl(
                pos == 0 ? (java.lang.String) term : _name
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseATypeExprSimple(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnType.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseATypeExprSimple(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ATypeExprSimple");
            builder.append("( ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ATypeExprSimple");
            builder.append("( ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ATypeExprSimple";
        }
    }
}

