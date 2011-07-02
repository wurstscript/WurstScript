package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ADivInt extends de.peeeq.pscript.ast.AInfixOp, KatjaTuple {

    //----- methods of ADivInt -----

    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.ADivInt replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AInfixOp.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ADivInt -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.ADivInt term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ADivInt.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.ADivInt {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ADivInt invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ADivInt invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.pscript.ast.ADivInt replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ADivInt invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort ADivInt invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.pscript.ast.ADivInt) pscriptAST.unique(new de.peeeq.pscript.ast.ADivInt.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AInfixOp.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseADivInt(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ADivInt");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ADivInt");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ADivInt";
        }
    }
}

