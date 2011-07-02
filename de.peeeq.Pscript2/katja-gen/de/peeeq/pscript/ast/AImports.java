package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AImports extends KatjaList<de.peeeq.pscript.ast.AImport> {

    //----- methods of AImports -----

    public de.peeeq.pscript.ast.AImports add(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImports addAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImports remove(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImports removeAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImports appBack(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImports appFront(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImports conc(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImports front();
    public de.peeeq.pscript.ast.AImports back();
    public de.peeeq.pscript.ast.AImports reverse();
    public de.peeeq.pscript.ast.AImports sublist(int from, int to);
    public de.peeeq.pscript.ast.AImports replace(int ith, Object element);
    public de.peeeq.pscript.ast.AImports toSet();
    public de.peeeq.pscript.ast.AImports setAdd(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImports setRemove(de.peeeq.pscript.ast.AImport element);
    public de.peeeq.pscript.ast.AImports setUnion(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImports setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);
    public de.peeeq.pscript.ast.AImports setWithout(KatjaList<? extends de.peeeq.pscript.ast.AImport> list);

    //----- nested classes of AImports -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.AImport term) throws E;
        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AImports term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AImports.VisitorType<E> {
    }

    static class Impl extends KatjaListImpl<de.peeeq.pscript.ast.AImport> implements de.peeeq.pscript.ast.AImports {

        //----- methods of Impl -----

        Impl(de.peeeq.pscript.ast.AImport... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.AImport element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort AImports invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.pscript.ast.AImports createInstance(de.peeeq.pscript.ast.AImport[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.AImport element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort AImports invoked with null element");

            AImports.Impl temp = new de.peeeq.pscript.ast.AImports.Impl();
            temp.values = elements;
            temp = (AImports.Impl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.AImport[] createArray(int size) {
            return new de.peeeq.pscript.ast.AImport[size];
        }

        public de.peeeq.pscript.ast.AImports add(de.peeeq.pscript.ast.AImport element) {
            return (de.peeeq.pscript.ast.AImports) super.add(element);
        }

        public de.peeeq.pscript.ast.AImports addAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return (de.peeeq.pscript.ast.AImports) super.addAll(list);
        }

        public de.peeeq.pscript.ast.AImports remove(de.peeeq.pscript.ast.AImport element) {
            return (de.peeeq.pscript.ast.AImports) super.remove(element);
        }

        public de.peeeq.pscript.ast.AImports removeAll(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return (de.peeeq.pscript.ast.AImports) super.removeAll(list);
        }

        public de.peeeq.pscript.ast.AImports appBack(de.peeeq.pscript.ast.AImport element) {
            return (de.peeeq.pscript.ast.AImports) super.appBack(element);
        }

        public de.peeeq.pscript.ast.AImports appFront(de.peeeq.pscript.ast.AImport element) {
            return (de.peeeq.pscript.ast.AImports) super.appFront(element);
        }

        public de.peeeq.pscript.ast.AImports conc(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return (de.peeeq.pscript.ast.AImports) super.conc(list);
        }

        public de.peeeq.pscript.ast.AImports front() {
            return (de.peeeq.pscript.ast.AImports) super.front();
        }

        public de.peeeq.pscript.ast.AImports back() {
            return (de.peeeq.pscript.ast.AImports) super.back();
        }

        public de.peeeq.pscript.ast.AImports reverse() {
            return (de.peeeq.pscript.ast.AImports) super.reverse();
        }

        public de.peeeq.pscript.ast.AImports sublist(int from, int to) {
            return (de.peeeq.pscript.ast.AImports) super.sublist(from, to);
        }

        public de.peeeq.pscript.ast.AImports replace(int ith, Object element) {
            return (de.peeeq.pscript.ast.AImports) super.replace(ith, element);
        }

        public de.peeeq.pscript.ast.AImports toSet() {
            return (de.peeeq.pscript.ast.AImports) super.toSet();
        }

        public de.peeeq.pscript.ast.AImports setAdd(de.peeeq.pscript.ast.AImport element) {
            return (de.peeeq.pscript.ast.AImports) super.setAdd(element);
        }

        public de.peeeq.pscript.ast.AImports setRemove(de.peeeq.pscript.ast.AImport element) {
            return (de.peeeq.pscript.ast.AImports) super.setRemove(element);
        }

        public de.peeeq.pscript.ast.AImports setUnion(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return (de.peeeq.pscript.ast.AImports) super.setUnion(list);
        }

        public de.peeeq.pscript.ast.AImports setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return (de.peeeq.pscript.ast.AImports) super.setIntersection(list);
        }

        public de.peeeq.pscript.ast.AImports setWithout(KatjaList<? extends de.peeeq.pscript.ast.AImport> list) {
            return (de.peeeq.pscript.ast.AImports) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("pscriptAST.AImports");
            builder.append("( ");
            for(de.peeeq.pscript.ast.AImport element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AImports");
            builder.append("( ");
            for(de.peeeq.pscript.ast.AImport element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AImports";
        }
    }
}

