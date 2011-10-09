package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface OpMinusPos extends de.peeeq.wurstscript.ast.OpBinaryPos, de.peeeq.wurstscript.ast.OpUnaryPos, de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.OpMinus> {

    //----- methods of OpMinusPos -----

    public de.peeeq.wurstscript.ast.OpMinus termOpBinary();
    public de.peeeq.wurstscript.ast.OpMinus termOpUnary();
    public de.peeeq.wurstscript.ast.OpMinus termOp();
    public de.peeeq.wurstscript.ast.OpMinus term();
    public KatjaSortPos<de.peeeq.wurstscript.ast.CompilationUnitPos> get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.OpMinusPos replace(de.peeeq.wurstscript.ast.OpMinus term);
    public de.peeeq.wurstscript.ast.ExprPos parent();
    public de.peeeq.wurstscript.ast.AST.SortPos lsib();
    public de.peeeq.wurstscript.ast.ExprPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinaryPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpUnaryPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpMinusPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpMinusPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpMinusPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.OpMinus> implements de.peeeq.wurstscript.ast.OpMinusPos {

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.OpMinus termOpBinary() {
            return term();
        }

        public de.peeeq.wurstscript.ast.OpMinus termOpUnary() {
            return term();
        }

        public de.peeeq.wurstscript.ast.OpMinus termOp() {
            return term();
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.OpMinus term, int pos) {
            super(parent, term, pos);
        }

        public KatjaSortPos<de.peeeq.wurstscript.ast.CompilationUnitPos> get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort OpMinusPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort OpMinusPos invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.OpMinusPos replace(de.peeeq.wurstscript.ast.OpMinus term) {
            return (de.peeeq.wurstscript.ast.OpMinusPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.ExprPos parent() {
            return (de.peeeq.wurstscript.ast.ExprPos) super.parent();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos lsib() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.ExprPos rsib() {
            return (de.peeeq.wurstscript.ast.ExprPos) super.rsib();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrderSkip();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrderStart();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path) {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.follow(path);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinaryPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpMinusPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpUnaryPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpMinusPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpMinusPos(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.CompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            term().toString(builder);
            builder.append("@CompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "OpMinusPos";
        }
    }
}

