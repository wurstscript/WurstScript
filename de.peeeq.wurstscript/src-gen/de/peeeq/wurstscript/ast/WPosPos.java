package de.peeeq.wurstscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface WPosPos extends de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.WPos> {

    //----- methods of WPosPos -----

    public de.peeeq.wurstscript.ast.WPos term();
    public de.peeeq.wurstscript.ast.StringPos file();
    public de.peeeq.wurstscript.ast.WPosPos replaceFile(java.lang.String file);
    public de.peeeq.wurstscript.ast.IntegerPos line();
    public de.peeeq.wurstscript.ast.WPosPos replaceLine(java.lang.Integer line);
    public de.peeeq.wurstscript.ast.IntegerPos column();
    public de.peeeq.wurstscript.ast.WPosPos replaceColumn(java.lang.Integer column);
    public de.peeeq.wurstscript.ast.AST.LeafPos<?> get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.WPosPos replace(de.peeeq.wurstscript.ast.WPos term);
    public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent();
    public de.peeeq.wurstscript.ast.AST.SortPos lsib();
    public de.peeeq.wurstscript.ast.AST.SortPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);

    //----- nested classes of WPosPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.WPosPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.WPos> implements de.peeeq.wurstscript.ast.WPosPos {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.StringPos _file = null;
        private de.peeeq.wurstscript.ast.IntegerPos _line = null;
        private de.peeeq.wurstscript.ast.IntegerPos _column = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.StringPos file() {
            if(_file == null)
                _file = de.peeeq.wurstscript.ast.AST.StringPos(this, _term.file(), 0);

            return _file;
        }

        public de.peeeq.wurstscript.ast.WPosPos replaceFile(java.lang.String file) {
            return replace(_term.replaceFile(file));
        }

        public de.peeeq.wurstscript.ast.IntegerPos line() {
            if(_line == null)
                _line = de.peeeq.wurstscript.ast.AST.IntegerPos(this, _term.line(), 1);

            return _line;
        }

        public de.peeeq.wurstscript.ast.WPosPos replaceLine(java.lang.Integer line) {
            return replace(_term.replaceLine(line));
        }

        public de.peeeq.wurstscript.ast.IntegerPos column() {
            if(_column == null)
                _column = de.peeeq.wurstscript.ast.AST.IntegerPos(this, _term.column(), 2);

            return _column;
        }

        public de.peeeq.wurstscript.ast.WPosPos replaceColumn(java.lang.Integer column) {
            return replace(_term.replaceColumn(column));
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.WPos term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.wurstscript.ast.AST.LeafPos<?> get(int i) {
            int ith = i;

            if(ith < 0) ith += 3;

            switch(ith) {
                case 0:
                    if(_file == null)
                        _file = AST.StringPos(this, _term.file(), 0);
                    
                    return _file;
                case 1:
                    if(_line == null)
                        _line = AST.IntegerPos(this, _term.line(), 1);
                    
                    return _line;
                case 2:
                    if(_column == null)
                        _column = AST.IntegerPos(this, _term.column(), 2);
                    
                    return _column;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort WPosPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort WPosPos invoked with too large parameter "+i+", sort has only 3 components");
                    }
            }
        }

        public int size() {
            return 3;
        }

        public de.peeeq.wurstscript.ast.WPosPos replace(de.peeeq.wurstscript.ast.WPos term) {
            return (de.peeeq.wurstscript.ast.WPosPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent() {
            return (de.peeeq.wurstscript.ast.AST.TuplePos<?>) super.parent();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos lsib() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.lsib();
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
            term().toString(builder);
            builder.append("@CompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "WPosPos";
        }
    }
}

