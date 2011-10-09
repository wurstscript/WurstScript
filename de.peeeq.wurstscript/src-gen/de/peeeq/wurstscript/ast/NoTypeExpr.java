package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface NoTypeExpr extends de.peeeq.wurstscript.ast.OptTypeExpr, KatjaTuple {

    //----- methods of NoTypeExpr -----

    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.NoTypeExpr replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptTypeExpr.Switch<CT, E> switchClass) throws E;

    //----- nested classes of NoTypeExpr -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.NoTypeExpr.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.NoTypeExpr {

        //----- methods of Impl -----

        Impl() {

        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort NoTypeExpr invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort NoTypeExpr invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.NoTypeExpr replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort NoTypeExpr invoked with negative parameter "+pos);
            if(pos >= 0)
                throw new IllegalArgumentException("replace on sort NoTypeExpr invoked with too large parameter "+pos+", sort has only 0 components");


            return (de.peeeq.wurstscript.ast.NoTypeExpr) AST.unique(new de.peeeq.wurstscript.ast.NoTypeExpr.Impl(
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptTypeExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseNoTypeExpr(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.NoTypeExpr");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("NoTypeExpr");
            builder.append("( ");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "NoTypeExpr";
        }
    }
}

