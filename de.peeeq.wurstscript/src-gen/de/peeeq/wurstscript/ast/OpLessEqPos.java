package de.peeeq.wurstscript.ast;

import java.io.IOException;
import java.util.List;

import katja.common.KatjaNodePos;
import katja.common.KatjaSort;
import katja.common.KatjaSortPos;
import katja.common.KatjaTuplePosImpl;

public interface OpLessEqPos extends de.peeeq.wurstscript.ast.OpBinaryPos, de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.OpLessEq> {

    //----- methods of OpLessEqPos -----

    public de.peeeq.wurstscript.ast.OpLessEq termOpBinary();
    public de.peeeq.wurstscript.ast.OpLessEq termOp();
    public de.peeeq.wurstscript.ast.OpLessEq term();
    public KatjaSortPos<de.peeeq.wurstscript.ast.CompilationUnitPos> get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.OpLessEqPos replace(de.peeeq.wurstscript.ast.OpLessEq term);
    public de.peeeq.wurstscript.ast.ExprBinaryPos parent();
    public de.peeeq.wurstscript.ast.ExprPos lsib();
    public de.peeeq.wurstscript.ast.ExprPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinaryPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpLessEqPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpLessEqPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.OpLessEq> implements de.peeeq.wurstscript.ast.OpLessEqPos {

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.OpLessEq termOpBinary() {
            return term();
        }

        public de.peeeq.wurstscript.ast.OpLessEq termOp() {
            return term();
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.OpLessEq term, int pos) {
            super(parent, term, pos);
        }

        public KatjaSortPos<de.peeeq.wurstscript.ast.CompilationUnitPos> get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort OpLessEqPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort OpLessEqPos invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.wurstscript.ast.OpLessEqPos replace(de.peeeq.wurstscript.ast.OpLessEq term) {
            return (de.peeeq.wurstscript.ast.OpLessEqPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.ExprBinaryPos parent() {
            return (de.peeeq.wurstscript.ast.ExprBinaryPos) super.parent();
        }

        public de.peeeq.wurstscript.ast.ExprPos lsib() {
            return (de.peeeq.wurstscript.ast.ExprPos) super.lsib();
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
            return switchClass.CaseOpLessEqPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseOpLessEqPos(this);
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
            return "OpLessEqPos";
        }
    }
}

