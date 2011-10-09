package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface WImport extends KatjaTuple {

    //----- methods of WImport -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.WImport replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public java.lang.String packagename();
    public de.peeeq.wurstscript.ast.WImport replacePackagename(java.lang.String packagename);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.WImport replace(int pos, Object term);

    //----- nested classes of WImport -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WImport term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.WImport.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.WImport {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private java.lang.String _packagename = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.WImport replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public java.lang.String packagename() {
            return _packagename;
        }

        public de.peeeq.wurstscript.ast.WImport replacePackagename(java.lang.String packagename) {
            return replace(1, packagename);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, java.lang.String packagename) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort WImport invoked with null parameter source");
            if(packagename == null)
                throw new IllegalArgumentException("constructor of sort WImport invoked with null parameter packagename");

            this._source = source;
            this._packagename = packagename;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0: return _source;
                case 1: return _packagename;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort WImport invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort WImport invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.wurstscript.ast.WImport replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort WImport invoked with negative parameter "+pos);
            if(pos >= 2)
                throw new IllegalArgumentException("replace on sort WImport invoked with too large parameter "+pos+", sort has only 2 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort WImport invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort WImport invoked with term of incorrect sort, String expected");

            return (de.peeeq.wurstscript.ast.WImport) AST.unique(new de.peeeq.wurstscript.ast.WImport.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (java.lang.String) term : _packagename
            ));
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.WImport");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            builder.append("\"").append(_packagename.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("WImport");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            builder.append("\"").append(_packagename.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "WImport";
        }
    }
}

