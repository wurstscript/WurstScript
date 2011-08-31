package de.peeeq.wurstscript.ast;

import java.io.IOException;
import java.util.List;

import katja.common.KatjaLeafPosImpl;
import katja.common.KatjaNodePos;
import katja.common.KatjaSort;

public interface BooleanPos extends de.peeeq.wurstscript.ast.AST.LeafPos<java.lang.Boolean> {

    //----- methods of BooleanPos -----

    public java.lang.Boolean term();
    public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent();
    public de.peeeq.wurstscript.ast.AST.TermPos<?> lsib();
    public de.peeeq.wurstscript.ast.AST.SortPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);

    //----- nested classes of BooleanPos -----

    static class Impl extends KatjaLeafPosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, java.lang.Boolean> implements de.peeeq.wurstscript.ast.BooleanPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, java.lang.Boolean term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent() {
            return (de.peeeq.wurstscript.ast.AST.TuplePos<?>) super.parent();
        }

        public de.peeeq.wurstscript.ast.AST.TermPos<?> lsib() {
            return (de.peeeq.wurstscript.ast.AST.TermPos<?>) super.lsib();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos rsib() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.rsib();
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

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.CompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append(term().toString());
            builder.append("@CompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "BooleanPos";
        }
    }
}

