package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface ALtEqPos extends de.peeeq.pscript.ast.AInfixOpPos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.ALtEq> {

    //----- methods of ALtEqPos -----

    public de.peeeq.pscript.ast.ALtEq termAInfixOp();
    public de.peeeq.pscript.ast.ALtEq term();
    public KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos> get(int i);
    public int size();
    public de.peeeq.pscript.ast.ALtEqPos replace(de.peeeq.pscript.ast.ALtEq term);
    public de.peeeq.pscript.ast.AInfixPos parent();
    public de.peeeq.pscript.ast.AExprPos lsib();
    public de.peeeq.pscript.ast.AExprPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AInfixOpPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ALtEqPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.ALtEqPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ALtEqPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.ALtEq> implements de.peeeq.pscript.ast.ALtEqPos {

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.ALtEq termAInfixOp() {
            return term();
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.ALtEq term, int pos) {
            super(parent, term, pos);
        }

        public KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos> get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ALtEqPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ALtEqPos invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.pscript.ast.ALtEqPos replace(de.peeeq.pscript.ast.ALtEq term) {
            return (de.peeeq.pscript.ast.ALtEqPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AInfixPos parent() {
            return (de.peeeq.pscript.ast.AInfixPos) super.parent();
        }

        public de.peeeq.pscript.ast.AExprPos lsib() {
            return (de.peeeq.pscript.ast.AExprPos) super.lsib();
        }

        public de.peeeq.pscript.ast.AExprPos rsib() {
            return (de.peeeq.pscript.ast.AExprPos) super.rsib();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.preOrder();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.preOrderSkip();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.postOrder();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.postOrderStart();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path) {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.follow(path);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AInfixOpPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseALtEqPos(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ACompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            term().toString(builder);
            builder.append("@ACompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "ALtEqPos";
        }
    }
}

