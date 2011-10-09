package de.peeeq.wurstscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface DoublePos extends de.peeeq.wurstscript.ast.AST.LeafPos<java.lang.Double> {

    //----- methods of DoublePos -----

    public java.lang.Double term();
    public de.peeeq.wurstscript.ast.ExprRealValPos parent();
    public de.peeeq.wurstscript.ast.WPosPos lsib();
    public de.peeeq.wurstscript.ast.AST.SortPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);

    //----- nested classes of DoublePos -----

    static class Impl extends KatjaLeafPosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, java.lang.Double> implements de.peeeq.wurstscript.ast.DoublePos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, java.lang.Double term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.ExprRealValPos parent() {
            return (de.peeeq.wurstscript.ast.ExprRealValPos) super.parent();
        }

        public de.peeeq.wurstscript.ast.WPosPos lsib() {
            return (de.peeeq.wurstscript.ast.WPosPos) super.lsib();
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
            return "DoublePos";
        }
    }
}

