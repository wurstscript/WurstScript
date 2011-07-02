package de.peeeq.pscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface AImportPos extends de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.AImport> {

    //----- methods of AImportPos -----

    public de.peeeq.pscript.ast.AImport term();
    public de.peeeq.pscript.ast.EObjectPos source();
    public de.peeeq.pscript.ast.AImportPos replaceSource(org.eclipse.emf.ecore.EObject source);
    public de.peeeq.pscript.ast.StringPos name();
    public de.peeeq.pscript.ast.AImportPos replaceName(java.lang.String name);
    public de.peeeq.pscript.ast.pscriptAST.LeafPos<?> get(int i);
    public int size();
    public de.peeeq.pscript.ast.AImportPos replace(de.peeeq.pscript.ast.AImport term);
    public de.peeeq.pscript.ast.AImportsPos parent();
    public de.peeeq.pscript.ast.AImportPos lsib();
    public de.peeeq.pscript.ast.AImportPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);

    //----- nested classes of AImportPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AImportPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AImportPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AImport> implements de.peeeq.pscript.ast.AImportPos {

        //----- attributes of Impl -----

        private de.peeeq.pscript.ast.EObjectPos _source = null;
        private de.peeeq.pscript.ast.StringPos _name = null;

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.EObjectPos source() {
            if(_source == null)
                _source = de.peeeq.pscript.ast.pscriptAST.EObjectPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.pscript.ast.AImportPos replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.pscript.ast.StringPos name() {
            if(_name == null)
                _name = de.peeeq.pscript.ast.pscriptAST.StringPos(this, _term.name(), 1);

            return _name;
        }

        public de.peeeq.pscript.ast.AImportPos replaceName(java.lang.String name) {
            return replace(_term.replaceName(name));
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AImport term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.pscript.ast.pscriptAST.LeafPos<?> get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = pscriptAST.EObjectPos(this, _term.source(), 0);
                    
                    return _source;
                case 1:
                    if(_name == null)
                        _name = pscriptAST.StringPos(this, _term.name(), 1);
                    
                    return _name;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AImportPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AImportPos invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.AImportPos replace(de.peeeq.pscript.ast.AImport term) {
            return (de.peeeq.pscript.ast.AImportPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AImportsPos parent() {
            return (de.peeeq.pscript.ast.AImportsPos) super.parent();
        }

        public de.peeeq.pscript.ast.AImportPos lsib() {
            return (de.peeeq.pscript.ast.AImportPos) super.lsib();
        }

        public de.peeeq.pscript.ast.AImportPos rsib() {
            return (de.peeeq.pscript.ast.AImportPos) super.rsib();
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
            return "AImportPos";
        }
    }
}

