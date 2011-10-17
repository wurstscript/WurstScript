package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface VisibilityProtected extends de.peeeq.wurstscript.ast.VisibilityModifier, KatjaTuple {

    //----- methods of VisibilityProtected -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.VisibilityProtected replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.WPos get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.VisibilityProtected replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifier.Switch<CT, E> switchClass) throws E;

    //----- nested classes of VisibilityProtected -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.VisibilityProtected.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.VisibilityProtected {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.VisibilityProtected replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort VisibilityProtected invoked with null parameter source");

            this._source = source;
        }

        public de.peeeq.wurstscript.ast.WPos get(int i) {
            int ith = i;

            if(ith < 0) ith += 1;

            switch(ith) {
                case 0: return _source;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort VisibilityProtected invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort VisibilityProtected invoked with too large parameter "+i+", sort has only 1 components");
                    }
            }
        }

        public int size() {
            return 1;
        }

        public de.peeeq.wurstscript.ast.VisibilityProtected replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort VisibilityProtected invoked with negative parameter "+pos);
            if(pos >= 1)
                throw new IllegalArgumentException("replace on sort VisibilityProtected invoked with too large parameter "+pos+", sort has only 1 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort VisibilityProtected invoked with term of incorrect sort, WPos expected");

            return (de.peeeq.wurstscript.ast.VisibilityProtected) AST.unique(new de.peeeq.wurstscript.ast.VisibilityProtected.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifier.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseVisibilityProtected(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.VisibilityProtected");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("VisibilityProtected");
            builder.append("( ");
            _source.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "VisibilityProtected";
        }
    }
}

