package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface VisibilityDefault extends de.peeeq.wurstscript.ast.VisibilityModifier, KatjaTuple {

    //----- methods of VisibilityDefault -----

    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.VisibilityDefault replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifier.Switch<CT, E> switchClass) throws E;

    //----- nested classes of VisibilityDefault -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.VisibilityDefault.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.VisibilityDefault {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort VisibilityDefault invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort VisibilityDefault invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.VisibilityDefault replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort VisibilityDefault invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort VisibilityDefault invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.wurstscript.ast.VisibilityDefault) AST.unique(new de.peeeq.wurstscript.ast.VisibilityDefault.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifier.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseVisibilityDefault(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.VisibilityDefault");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("VisibilityDefault");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "VisibilityDefault";
        }
    }
}

