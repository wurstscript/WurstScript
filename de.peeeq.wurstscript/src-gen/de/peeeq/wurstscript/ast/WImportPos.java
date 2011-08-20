package de.peeeq.wurstscript.ast;

import java.io.IOException;
import java.util.List;

import katja.common.KatjaNodePos;
import katja.common.KatjaSort;
import katja.common.KatjaTuplePosImpl;

public interface WImportPos extends de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.WImport> {

    //----- methods of WImportPos -----

    public de.peeeq.wurstscript.ast.WImport term();
    public de.peeeq.wurstscript.ast.WPosPos source();
    public de.peeeq.wurstscript.ast.WImportPos replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.StringPos packagename();
    public de.peeeq.wurstscript.ast.WImportPos replacePackagename(java.lang.String packagename);
    public de.peeeq.wurstscript.ast.AST.TermPos<?> get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.WImportPos replace(de.peeeq.wurstscript.ast.WImport term);
    public de.peeeq.wurstscript.ast.WImportsPos parent();
    public de.peeeq.wurstscript.ast.WImportPos lsib();
    public de.peeeq.wurstscript.ast.WImportPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);

    //----- nested classes of WImportPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WImportPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.WImportPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.WImport> implements de.peeeq.wurstscript.ast.WImportPos {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPosPos _source = null;
        private de.peeeq.wurstscript.ast.StringPos _packagename = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPosPos source() {
            if(_source == null)
                _source = de.peeeq.wurstscript.ast.AST.WPosPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.wurstscript.ast.WImportPos replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.wurstscript.ast.StringPos packagename() {
            if(_packagename == null)
                _packagename = de.peeeq.wurstscript.ast.AST.StringPos(this, _term.packagename(), 1);

            return _packagename;
        }

        public de.peeeq.wurstscript.ast.WImportPos replacePackagename(java.lang.String packagename) {
            return replace(_term.replacePackagename(packagename));
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.WImport term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.wurstscript.ast.AST.TermPos<?> get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = AST.WPosPos(this, _term.source(), 0);
                    
                    return _source;
                case 1:
                    if(_packagename == null)
                        _packagename = AST.StringPos(this, _term.packagename(), 1);
                    
                    return _packagename;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort WImportPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort WImportPos invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.wurstscript.ast.WImportPos replace(de.peeeq.wurstscript.ast.WImport term) {
            return (de.peeeq.wurstscript.ast.WImportPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.WImportsPos parent() {
            return (de.peeeq.wurstscript.ast.WImportsPos) super.parent();
        }

        public de.peeeq.wurstscript.ast.WImportPos lsib() {
            return (de.peeeq.wurstscript.ast.WImportPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.WImportPos rsib() {
            return (de.peeeq.wurstscript.ast.WImportPos) super.rsib();
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
            term().toString(builder);
            builder.append("@CompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "WImportPos";
        }
    }
}

