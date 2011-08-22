package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface OpMinus extends de.peeeq.wurstscript.ast.OpBinary, de.peeeq.wurstscript.ast.OpUnary, KatjaTuple {

    //----- methods of OpMinus -----

    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.OpMinus replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinary.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpUnary.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Op.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpMinus -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpMinus term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpMinus.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.OpMinus {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort OpMinus invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort OpMinus invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.OpMinus replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort OpMinus invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort OpMinus invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.wurstscript.ast.OpMinus) AST.unique(new de.peeeq.wurstscript.ast.OpMinus.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinary.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpMinus(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpUnary.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpMinus(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Op.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpMinus(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.OpMinus");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("OpMinus");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "OpMinus";
        }
    }
}

