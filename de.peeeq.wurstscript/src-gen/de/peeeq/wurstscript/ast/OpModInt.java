package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface OpModInt extends de.peeeq.wurstscript.ast.OpBinary, KatjaTuple {

    //----- methods of OpModInt -----

    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.OpModInt replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinary.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Op.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpModInt -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpModInt term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpModInt.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.OpModInt {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort OpModInt invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort OpModInt invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.OpModInt replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort OpModInt invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort OpModInt invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.wurstscript.ast.OpModInt) AST.unique(new de.peeeq.wurstscript.ast.OpModInt.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinary.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpModInt(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Op.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpModInt(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.OpModInt");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("OpModInt");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "OpModInt";
        }
    }
}

