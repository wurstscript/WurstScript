package de.peeeq.pscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface AVoidReturnPos extends de.peeeq.pscript.ast.AStatementPos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.AVoidReturn> {

    //----- methods of AVoidReturnPos -----

    public de.peeeq.pscript.ast.AVoidReturn termAStatement();
    public de.peeeq.pscript.ast.AVoidReturn term();
    public de.peeeq.pscript.ast.EObjectPos source();
    public de.peeeq.pscript.ast.AVoidReturnPos replaceSource(org.eclipse.emf.ecore.EObject source);
    public de.peeeq.pscript.ast.EObjectPos get(int i);
    public int size();
    public de.peeeq.pscript.ast.AVoidReturnPos replace(de.peeeq.pscript.ast.AVoidReturn term);
    public de.peeeq.pscript.ast.AStatementPos parent();
    public de.peeeq.pscript.ast.AStatementPos lsib();
    public de.peeeq.pscript.ast.AStatementPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatementPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AVoidReturnPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVoidReturnPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AVoidReturnPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AVoidReturn> implements de.peeeq.pscript.ast.AVoidReturnPos {

        //----- attributes of Impl -----

        private de.peeeq.pscript.ast.EObjectPos _source = null;

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.AVoidReturn termAStatement() {
            return term();
        }

        public de.peeeq.pscript.ast.EObjectPos source() {
            if(_source == null)
                _source = de.peeeq.pscript.ast.pscriptAST.EObjectPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.pscript.ast.AVoidReturnPos replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(_term.replaceSource(source));
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AVoidReturn term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.pscript.ast.EObjectPos get(int i) {
            int ith = i;

            if(ith < 0) ith += 1;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = pscriptAST.EObjectPos(this, _term.source(), 0);
                    
                    return _source;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AVoidReturnPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AVoidReturnPos invoked with too large parameter "+i+", sort has only 1 components");
                    }
            }
        }

        public int size() {
            return 1;
        }

        public de.peeeq.pscript.ast.AVoidReturnPos replace(de.peeeq.pscript.ast.AVoidReturn term) {
            return (de.peeeq.pscript.ast.AVoidReturnPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AStatementPos parent() {
            return (de.peeeq.pscript.ast.AStatementPos) super.parent();
        }

        public de.peeeq.pscript.ast.AStatementPos lsib() {
            return (de.peeeq.pscript.ast.AStatementPos) super.lsib();
        }

        public de.peeeq.pscript.ast.AStatementPos rsib() {
            return (de.peeeq.pscript.ast.AStatementPos) super.rsib();
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

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatementPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAVoidReturnPos(this);
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
            return "AVoidReturnPos";
        }
    }
}

