package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface AReturnsNothingPos extends de.peeeq.pscript.ast.AReturnTypePos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.AReturnsNothing> {

    //----- methods of AReturnsNothingPos -----

    public de.peeeq.pscript.ast.AReturnsNothing termAReturnType();
    public de.peeeq.pscript.ast.AReturnsNothing term();
    public KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos> get(int i);
    public int size();
    public de.peeeq.pscript.ast.AReturnsNothingPos replace(de.peeeq.pscript.ast.AReturnsNothing term);
    public de.peeeq.pscript.ast.AFuncDefPos parent();
    public de.peeeq.pscript.ast.StringPos lsib();
    public de.peeeq.pscript.ast.AFormalParametersPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnTypePos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AReturnsNothingPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AReturnsNothingPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AReturnsNothing> implements de.peeeq.pscript.ast.AReturnsNothingPos {

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.AReturnsNothing termAReturnType() {
            return term();
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AReturnsNothing term, int pos) {
            super(parent, term, pos);
        }

        public KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos> get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AReturnsNothingPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AReturnsNothingPos invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.pscript.ast.AReturnsNothingPos replace(de.peeeq.pscript.ast.AReturnsNothing term) {
            return (de.peeeq.pscript.ast.AReturnsNothingPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AFuncDefPos parent() {
            return (de.peeeq.pscript.ast.AFuncDefPos) super.parent();
        }

        public de.peeeq.pscript.ast.StringPos lsib() {
            return (de.peeeq.pscript.ast.StringPos) super.lsib();
        }

        public de.peeeq.pscript.ast.AFormalParametersPos rsib() {
            return (de.peeeq.pscript.ast.AFormalParametersPos) super.rsib();
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

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AReturnTypePos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAReturnsNothingPos(this);
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
            return "AReturnsNothingPos";
        }
    }
}

