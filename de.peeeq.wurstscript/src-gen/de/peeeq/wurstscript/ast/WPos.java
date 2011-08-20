package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface WPos extends KatjaTuple {

    //----- methods of WPos -----

    public java.lang.String file();
    public de.peeeq.wurstscript.ast.WPos replaceFile(java.lang.String file);
    public java.lang.Integer line();
    public de.peeeq.wurstscript.ast.WPos replaceLine(java.lang.Integer line);
    public java.lang.Integer column();
    public de.peeeq.wurstscript.ast.WPos replaceColumn(java.lang.Integer column);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.WPos replace(int pos, Object term);

    //----- nested classes of WPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.WPos.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.WPos {

        //----- attributes of Impl -----

        private java.lang.String _file = null;
        private java.lang.Integer _line = null;
        private java.lang.Integer _column = null;

        //----- methods of Impl -----

        public java.lang.String file() {
            return _file;
        }

        public de.peeeq.wurstscript.ast.WPos replaceFile(java.lang.String file) {
            return replace(0, file);
        }

        public java.lang.Integer line() {
            return _line;
        }

        public de.peeeq.wurstscript.ast.WPos replaceLine(java.lang.Integer line) {
            return replace(1, line);
        }

        public java.lang.Integer column() {
            return _column;
        }

        public de.peeeq.wurstscript.ast.WPos replaceColumn(java.lang.Integer column) {
            return replace(2, column);
        }

        Impl(java.lang.String file, java.lang.Integer line, java.lang.Integer column) {
            if(file == null)
                throw new IllegalArgumentException("constructor of sort WPos invoked with null parameter file");
            if(line == null)
                throw new IllegalArgumentException("constructor of sort WPos invoked with null parameter line");
            if(column == null)
                throw new IllegalArgumentException("constructor of sort WPos invoked with null parameter column");

            this._file = file;
            this._line = line;
            this._column = column;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 3;

            switch(ith) {
                case 0: return _file;
                case 1: return _line;
                case 2: return _column;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort WPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort WPos invoked with too large parameter "+i+", sort has only 3 components");
                    }
            }
        }

        public int size() {
            return 3;
        }

        public de.peeeq.wurstscript.ast.WPos replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort WPos invoked with negative parameter "+pos);
            if(pos >= 3)
                throw new IllegalArgumentException("replace on sort WPos invoked with too large parameter "+pos+", sort has only 3 components");

            if(pos == 0 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort WPos invoked with term of incorrect sort, String expected");
            if(pos == 1 && !(term instanceof java.lang.Integer))
                throw new IllegalArgumentException("replace on sort WPos invoked with term of incorrect sort, Integer expected");
            if(pos == 2 && !(term instanceof java.lang.Integer))
                throw new IllegalArgumentException("replace on sort WPos invoked with term of incorrect sort, Integer expected");

            return (de.peeeq.wurstscript.ast.WPos) AST.unique(new de.peeeq.wurstscript.ast.WPos.Impl(
                pos == 0 ? (java.lang.String) term : _file,
                pos == 1 ? (java.lang.Integer) term : _line,
                pos == 2 ? (java.lang.Integer) term : _column
            ));
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.WPos");
            builder.append("( ");
            builder.append("\"").append(_file.toString()).append("\"");
            builder.append(", ");
            builder.append(_line.toString());
            builder.append(", ");
            builder.append(_column.toString());
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("WPos");
            builder.append("( ");
            builder.append("\"").append(_file.toString()).append("\"");
            builder.append(", ");
            builder.append(_line.toString());
            builder.append(", ");
            builder.append(_column.toString());
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "WPos";
        }
    }
}

