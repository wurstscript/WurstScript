package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AReturnsNothing extends de.peeeq.pscript.ast.AReturnType, KatjaTuple {

    //----- methods of AReturnsNothing -----

    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.AReturnsNothing replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnType.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AReturnsNothing -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.AReturnsNothing term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AReturnsNothing.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.AReturnsNothing {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AReturnsNothing invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AReturnsNothing invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.pscript.ast.AReturnsNothing replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort AReturnsNothing invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort AReturnsNothing invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.pscript.ast.AReturnsNothing) pscriptAST.unique(new de.peeeq.pscript.ast.AReturnsNothing.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnType.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAReturnsNothing(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.AReturnsNothing");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("AReturnsNothing");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AReturnsNothing";
        }
    }
}

