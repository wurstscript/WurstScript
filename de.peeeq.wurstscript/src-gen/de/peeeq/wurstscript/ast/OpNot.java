package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface OpNot extends de.peeeq.wurstscript.ast.OpUnary, KatjaTuple {

    //----- methods of OpNot -----

    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.OpNot replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpUnary.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Op.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpNot -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpNot term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpNot.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.OpNot {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort OpNot invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort OpNot invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.OpNot replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort OpNot invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort OpNot invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.wurstscript.ast.OpNot) AST.unique(new de.peeeq.wurstscript.ast.OpNot.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpUnary.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpNot(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Op.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpNot(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.OpNot");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("OpNot");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "OpNot";
        }
    }
}

