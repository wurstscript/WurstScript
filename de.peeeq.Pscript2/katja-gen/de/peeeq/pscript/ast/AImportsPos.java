package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface AImportsPos extends de.peeeq.pscript.ast.pscriptAST.ListPos<de.peeeq.pscript.ast.AImports, de.peeeq.pscript.ast.AImportPos, de.peeeq.pscript.ast.AImport> {

    //----- methods of AImportsPos -----

    public de.peeeq.pscript.ast.AImports term();
    public de.peeeq.pscript.ast.AImportsPos add(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImportsPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImportsPos remove(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImportsPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImportsPos appBack(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImportsPos appFront(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImportsPos conc(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImportsPos reverse();
    public de.peeeq.pscript.ast.AImportsPos toSet();
    public de.peeeq.pscript.ast.AImportsPos setAdd(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImportsPos setRemove(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImportsPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImportsPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImportsPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public KatjaList<de.peeeq.pscript.ast.AImportPos> toList();
    public de.peeeq.pscript.ast.AImportsPos replace(de.peeeq.pscript.ast.AImports term);
    public de.peeeq.pscript.ast.APackagePos parent();
    public de.peeeq.pscript.ast.StringPos lsib();
    public de.peeeq.pscript.ast.AElementsPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);

    //----- nested classes of AImportsPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AImportPos> implements KatjaList<de.peeeq.pscript.ast.AImportPos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.pscript.ast.AImportPos... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.AImportPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AImportPos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.pscript.ast.AImportPos> createInstance(de.peeeq.pscript.ast.AImportPos[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.AImportPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AImportPos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.AImportPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AImportPos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.pscript.ast.AImportPos element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            throw new UnsupportedOperationException("toJavaCode invoked on an anonymous list of positions, you do not want to do that");
        }

        public final String sortName() {
            return "<anonymous>";
        }
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.AImportPos term) throws E;
        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AImportsPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AImportsPos.VisitorType<E> {
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AImports, de.peeeq.pscript.ast.AImportPos, de.peeeq.pscript.ast.AImport> implements de.peeeq.pscript.ast.AImportsPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AImports term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.pscript.ast.AImportPos getElementInstance(int pos) {
            return de.peeeq.pscript.ast.pscriptAST.AImportPos(this, _term.get(pos), pos);
        }

        protected de.peeeq.pscript.ast.AImportPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AImportPos[size];
        }

        public de.peeeq.pscript.ast.AImportsPos add(de.peeeq.pscript.ast.AImport element) {
            return replace(_term.add(element));
        }

        public de.peeeq.pscript.ast.AImportsPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.pscript.ast.AImportsPos remove(de.peeeq.pscript.ast.AImport element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.pscript.ast.AImportsPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.pscript.ast.AImportsPos appBack(de.peeeq.pscript.ast.AImport element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.pscript.ast.AImportsPos appFront(de.peeeq.pscript.ast.AImport element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.pscript.ast.AImportsPos conc(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.pscript.ast.AImportsPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.pscript.ast.AImportsPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.pscript.ast.AImportsPos setAdd(de.peeeq.pscript.ast.AImport element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.pscript.ast.AImportsPos setRemove(de.peeeq.pscript.ast.AImport element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.pscript.ast.AImportsPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.pscript.ast.AImportsPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.pscript.ast.AImportsPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.pscript.ast.AImportPos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new AImportsPos.ListImpl(values);
        }

        public de.peeeq.pscript.ast.AImportsPos replace(de.peeeq.pscript.ast.AImports term) {
            return (de.peeeq.pscript.ast.AImportsPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.APackagePos parent() {
            return (de.peeeq.pscript.ast.APackagePos) super.parent();
        }

        public de.peeeq.pscript.ast.StringPos lsib() {
            return (de.peeeq.pscript.ast.StringPos) super.lsib();
        }

        public de.peeeq.pscript.ast.AElementsPos rsib() {
            return (de.peeeq.pscript.ast.AElementsPos) super.rsib();
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
            return "AImportsPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AImportPos> implements KatjaList<de.peeeq.pscript.ast.AImportPos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.pscript.ast.AImportPos... elements) {
                super(elements);

                for(de.peeeq.pscript.ast.AImportPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AImportPos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.pscript.ast.AImportPos> createInstance(de.peeeq.pscript.ast.AImportPos[] elements, boolean isSet) {
                for(de.peeeq.pscript.ast.AImportPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AImportPos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) pscriptAST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.pscript.ast.AImportPos[] createArray(int size) {
                return new de.peeeq.pscript.ast.AImportPos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.pscript.ast.AImportPos element : values) {
                    if(first) first = false;
                    else builder.append(", ");
                    element.toString(builder);
                }
                builder.append(" )");

                return builder;
            }

            public Appendable toJavaCode(Appendable builder) throws IOException {
                throw new UnsupportedOperationException("toJavaCode invoked on an anonymous list of positions, you do not want to do that");
            }

            public final String sortName() {
                return "<anonymous>";
            }
        }
    }
}

