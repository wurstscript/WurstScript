package de.peeeq.wurstscript.ast;

import java.io.IOException;
import java.util.List;

import katja.common.KatjaList;
import katja.common.KatjaListImpl;
import katja.common.KatjaListPosImpl;
import katja.common.KatjaNodePos;
import katja.common.KatjaSort;

public interface WImportsPos extends de.peeeq.wurstscript.ast.AST.ListPos<de.peeeq.wurstscript.ast.WImports, de.peeeq.wurstscript.ast.WImportPos, de.peeeq.wurstscript.ast.WImport> {

    //----- methods of WImportsPos -----

    public de.peeeq.wurstscript.ast.WImports term();
    public de.peeeq.wurstscript.ast.WImportsPos add(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImportsPos addAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImportsPos remove(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImportsPos removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImportsPos appBack(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImportsPos appFront(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImportsPos conc(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImportsPos reverse();
    public de.peeeq.wurstscript.ast.WImportsPos toSet();
    public de.peeeq.wurstscript.ast.WImportsPos setAdd(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImportsPos setRemove(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImportsPos setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImportsPos setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImportsPos setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public KatjaList<de.peeeq.wurstscript.ast.WImportPos> toList();
    public de.peeeq.wurstscript.ast.WImportsPos replace(de.peeeq.wurstscript.ast.WImports term);
    public de.peeeq.wurstscript.ast.WPackagePos parent();
    public de.peeeq.wurstscript.ast.StringPos lsib();
    public de.peeeq.wurstscript.ast.WEntitiesPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);

    //----- nested classes of WImportsPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.wurstscript.ast.WImportPos> implements KatjaList<de.peeeq.wurstscript.ast.WImportPos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.wurstscript.ast.WImportPos... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.WImportPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort WImportPos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.wurstscript.ast.WImportPos> createInstance(de.peeeq.wurstscript.ast.WImportPos[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.WImportPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort WImportPos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.WImportPos[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.WImportPos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.wurstscript.ast.WImportPos element : values) {
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

        public void visit(de.peeeq.wurstscript.ast.WImportPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WImportsPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.WImportsPos.VisitorType<E> {
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.WImports, de.peeeq.wurstscript.ast.WImportPos, de.peeeq.wurstscript.ast.WImport> implements de.peeeq.wurstscript.ast.WImportsPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.WImports term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.wurstscript.ast.WImportPos getElementInstance(int pos) {
            return de.peeeq.wurstscript.ast.AST.WImportPos(this, _term.get(pos), pos);
        }

        protected de.peeeq.wurstscript.ast.WImportPos[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.WImportPos[size];
        }

        public de.peeeq.wurstscript.ast.WImportsPos add(de.peeeq.wurstscript.ast.WImport element) {
            return replace(_term.add(element));
        }

        public de.peeeq.wurstscript.ast.WImportsPos addAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.wurstscript.ast.WImportsPos remove(de.peeeq.wurstscript.ast.WImport element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.wurstscript.ast.WImportsPos removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.wurstscript.ast.WImportsPos appBack(de.peeeq.wurstscript.ast.WImport element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.wurstscript.ast.WImportsPos appFront(de.peeeq.wurstscript.ast.WImport element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.wurstscript.ast.WImportsPos conc(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.wurstscript.ast.WImportsPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.wurstscript.ast.WImportsPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.wurstscript.ast.WImportsPos setAdd(de.peeeq.wurstscript.ast.WImport element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.wurstscript.ast.WImportsPos setRemove(de.peeeq.wurstscript.ast.WImport element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.wurstscript.ast.WImportsPos setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.wurstscript.ast.WImportsPos setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.wurstscript.ast.WImportsPos setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.wurstscript.ast.WImportPos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new WImportsPos.ListImpl(values);
        }

        public de.peeeq.wurstscript.ast.WImportsPos replace(de.peeeq.wurstscript.ast.WImports term) {
            return (de.peeeq.wurstscript.ast.WImportsPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.WPackagePos parent() {
            return (de.peeeq.wurstscript.ast.WPackagePos) super.parent();
        }

        public de.peeeq.wurstscript.ast.StringPos lsib() {
            return (de.peeeq.wurstscript.ast.StringPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.WEntitiesPos rsib() {
            return (de.peeeq.wurstscript.ast.WEntitiesPos) super.rsib();
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
            return "WImportsPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.wurstscript.ast.WImportPos> implements KatjaList<de.peeeq.wurstscript.ast.WImportPos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.wurstscript.ast.WImportPos... elements) {
                super(elements);

                for(de.peeeq.wurstscript.ast.WImportPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort WImportPos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.wurstscript.ast.WImportPos> createInstance(de.peeeq.wurstscript.ast.WImportPos[] elements, boolean isSet) {
                for(de.peeeq.wurstscript.ast.WImportPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort WImportPos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) AST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.wurstscript.ast.WImportPos[] createArray(int size) {
                return new de.peeeq.wurstscript.ast.WImportPos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.wurstscript.ast.WImportPos element : values) {
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

