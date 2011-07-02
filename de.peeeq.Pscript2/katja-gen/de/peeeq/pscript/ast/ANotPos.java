package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface ANotPos extends de.peeeq.pscript.ast.APrefixOpPos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.ANot> {

    //----- methods of ANotPos -----

    public de.peeeq.pscript.ast.ANot termAPrefixOp();
    public de.peeeq.pscript.ast.ANot term();
    public KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos> get(int i);
    public int size();
    public de.peeeq.pscript.ast.ANotPos replace(de.peeeq.pscript.ast.ANot term);
    public de.peeeq.pscript.ast.APrefixPos parent();
    public de.peeeq.pscript.ast.EObjectPos lsib();
    public de.peeeq.pscript.ast.AExprPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.APrefixOpPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ANotPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.ANotPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ANotPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.ANot> implements de.peeeq.pscript.ast.ANotPos {

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.ANot termAPrefixOp() {
            return term();
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.ANot term, int pos) {
            super(parent, term, pos);
        }

        public KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos> get(int i) {
            int ith = i;

            if(ith < 0) ith += 0;

            switch(ith) {
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ANotPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ANotPos invoked with too large parameter "+i+", sort has only 0 components");
                    }
            }
        }

        public int size() {
            return 0;
        }

        public de.peeeq.pscript.ast.ANotPos replace(de.peeeq.pscript.ast.ANot term) {
            return (de.peeeq.pscript.ast.ANotPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.APrefixPos parent() {
            return (de.peeeq.pscript.ast.APrefixPos) super.parent();
        }

        public de.peeeq.pscript.ast.EObjectPos lsib() {
            return (de.peeeq.pscript.ast.EObjectPos) super.lsib();
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

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.APrefixOpPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseANotPos(this);
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
            return "ANotPos";
        }
    }
}

