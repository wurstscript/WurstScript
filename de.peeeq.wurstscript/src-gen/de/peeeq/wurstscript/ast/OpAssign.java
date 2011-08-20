package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface OpAssign extends de.peeeq.wurstscript.ast.OpAssignment, KatjaTuple {

    //----- methods of OpAssign -----

    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.OpAssign replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpAssignment.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpAssign -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpAssign term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpAssign.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.OpAssign {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort OpAssign invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort OpAssign invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.OpAssign replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort OpAssign invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort OpAssign invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.wurstscript.ast.OpAssign) AST.unique(new de.peeeq.wurstscript.ast.OpAssign.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpAssignment.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpAssign(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.OpAssign");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("OpAssign");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "OpAssign";
        }
    }
}

