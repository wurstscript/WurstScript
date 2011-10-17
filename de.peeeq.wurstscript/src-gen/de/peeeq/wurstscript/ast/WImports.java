package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaList;
import katja.common.KatjaListImpl;

public interface WImports extends KatjaList<de.peeeq.wurstscript.ast.WImport> {

    //----- methods of WImports -----

    public de.peeeq.wurstscript.ast.WImports add(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImports addAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImports remove(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImports removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImports appBack(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImports appFront(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImports conc(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImports front();
    public de.peeeq.wurstscript.ast.WImports back();
    public de.peeeq.wurstscript.ast.WImports reverse();
    public de.peeeq.wurstscript.ast.WImports sublist(int from, int to);
    public de.peeeq.wurstscript.ast.WImports replace(int ith, Object element);
    public de.peeeq.wurstscript.ast.WImports toSet();
    public de.peeeq.wurstscript.ast.WImports setAdd(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImports setRemove(de.peeeq.wurstscript.ast.WImport element);
    public de.peeeq.wurstscript.ast.WImports setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImports setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);
    public de.peeeq.wurstscript.ast.WImports setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list);

    //----- nested classes of WImports -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WImport term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WImports term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.WImports.VisitorType<E> {
    }

    static class Impl extends KatjaListImpl<de.peeeq.wurstscript.ast.WImport> implements de.peeeq.wurstscript.ast.WImports {

        //----- methods of Impl -----

        Impl(de.peeeq.wurstscript.ast.WImport... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.WImport element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort WImports invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.wurstscript.ast.WImports createInstance(de.peeeq.wurstscript.ast.WImport[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.WImport element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort WImports invoked with null element");

            WImports.Impl temp = new de.peeeq.wurstscript.ast.WImports.Impl();
            temp.values = elements;
            temp = (WImports.Impl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.WImport[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.WImport[size];
        }

        public de.peeeq.wurstscript.ast.WImports add(de.peeeq.wurstscript.ast.WImport element) {
            return (de.peeeq.wurstscript.ast.WImports) super.add(element);
        }

        public de.peeeq.wurstscript.ast.WImports addAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return (de.peeeq.wurstscript.ast.WImports) super.addAll(list);
        }

        public de.peeeq.wurstscript.ast.WImports remove(de.peeeq.wurstscript.ast.WImport element) {
            return (de.peeeq.wurstscript.ast.WImports) super.remove(element);
        }

        public de.peeeq.wurstscript.ast.WImports removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return (de.peeeq.wurstscript.ast.WImports) super.removeAll(list);
        }

        public de.peeeq.wurstscript.ast.WImports appBack(de.peeeq.wurstscript.ast.WImport element) {
            return (de.peeeq.wurstscript.ast.WImports) super.appBack(element);
        }

        public de.peeeq.wurstscript.ast.WImports appFront(de.peeeq.wurstscript.ast.WImport element) {
            return (de.peeeq.wurstscript.ast.WImports) super.appFront(element);
        }

        public de.peeeq.wurstscript.ast.WImports conc(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return (de.peeeq.wurstscript.ast.WImports) super.conc(list);
        }

        public de.peeeq.wurstscript.ast.WImports front() {
            return (de.peeeq.wurstscript.ast.WImports) super.front();
        }

        public de.peeeq.wurstscript.ast.WImports back() {
            return (de.peeeq.wurstscript.ast.WImports) super.back();
        }

        public de.peeeq.wurstscript.ast.WImports reverse() {
            return (de.peeeq.wurstscript.ast.WImports) super.reverse();
        }

        public de.peeeq.wurstscript.ast.WImports sublist(int from, int to) {
            return (de.peeeq.wurstscript.ast.WImports) super.sublist(from, to);
        }

        public de.peeeq.wurstscript.ast.WImports replace(int ith, Object element) {
            return (de.peeeq.wurstscript.ast.WImports) super.replace(ith, element);
        }

        public de.peeeq.wurstscript.ast.WImports toSet() {
            return (de.peeeq.wurstscript.ast.WImports) super.toSet();
        }

        public de.peeeq.wurstscript.ast.WImports setAdd(de.peeeq.wurstscript.ast.WImport element) {
            return (de.peeeq.wurstscript.ast.WImports) super.setAdd(element);
        }

        public de.peeeq.wurstscript.ast.WImports setRemove(de.peeeq.wurstscript.ast.WImport element) {
            return (de.peeeq.wurstscript.ast.WImports) super.setRemove(element);
        }

        public de.peeeq.wurstscript.ast.WImports setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return (de.peeeq.wurstscript.ast.WImports) super.setUnion(list);
        }

        public de.peeeq.wurstscript.ast.WImports setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return (de.peeeq.wurstscript.ast.WImports) super.setIntersection(list);
        }

        public de.peeeq.wurstscript.ast.WImports setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.WImport> list) {
            return (de.peeeq.wurstscript.ast.WImports) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AST.WImports");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.WImport element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("WImports");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.WImport element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "WImports";
        }
    }
}

