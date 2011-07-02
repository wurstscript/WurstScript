package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AFormalParameters extends KatjaList<de.peeeq.pscript.ast.AFormalParameter> {

    //----- methods of AFormalParameters -----

    public de.peeeq.pscript.ast.AFormalParameters add(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParameters addAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParameters remove(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParameters removeAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParameters appBack(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParameters appFront(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParameters conc(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParameters front();
    public de.peeeq.pscript.ast.AFormalParameters back();
    public de.peeeq.pscript.ast.AFormalParameters reverse();
    public de.peeeq.pscript.ast.AFormalParameters sublist(int from, int to);
    public de.peeeq.pscript.ast.AFormalParameters replace(int ith, Object element);
    public de.peeeq.pscript.ast.AFormalParameters toSet();
    public de.peeeq.pscript.ast.AFormalParameters setAdd(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParameters setRemove(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParameters setUnion(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParameters setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParameters setWithout(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);

    //----- nested classes of AFormalParameters -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.AFormalParameter term) throws E;
        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameters term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AFormalParameters.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E> variantVisit$ATypeExpr = new de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E>() { public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$ATypeExpr);
        }
    }

    static class Impl extends KatjaListImpl<de.peeeq.pscript.ast.AFormalParameter> implements de.peeeq.pscript.ast.AFormalParameters {

        //----- methods of Impl -----

        Impl(de.peeeq.pscript.ast.AFormalParameter... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.AFormalParameter element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort AFormalParameters invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.pscript.ast.AFormalParameters createInstance(de.peeeq.pscript.ast.AFormalParameter[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.AFormalParameter element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort AFormalParameters invoked with null element");

            AFormalParameters.Impl temp = new de.peeeq.pscript.ast.AFormalParameters.Impl();
            temp.values = elements;
            temp = (AFormalParameters.Impl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.AFormalParameter[] createArray(int size) {
            return new de.peeeq.pscript.ast.AFormalParameter[size];
        }

        public de.peeeq.pscript.ast.AFormalParameters add(de.peeeq.pscript.ast.AFormalParameter element) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.add(element);
        }

        public de.peeeq.pscript.ast.AFormalParameters addAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.addAll(list);
        }

        public de.peeeq.pscript.ast.AFormalParameters remove(de.peeeq.pscript.ast.AFormalParameter element) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.remove(element);
        }

        public de.peeeq.pscript.ast.AFormalParameters removeAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.removeAll(list);
        }

        public de.peeeq.pscript.ast.AFormalParameters appBack(de.peeeq.pscript.ast.AFormalParameter element) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.appBack(element);
        }

        public de.peeeq.pscript.ast.AFormalParameters appFront(de.peeeq.pscript.ast.AFormalParameter element) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.appFront(element);
        }

        public de.peeeq.pscript.ast.AFormalParameters conc(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.conc(list);
        }

        public de.peeeq.pscript.ast.AFormalParameters front() {
            return (de.peeeq.pscript.ast.AFormalParameters) super.front();
        }

        public de.peeeq.pscript.ast.AFormalParameters back() {
            return (de.peeeq.pscript.ast.AFormalParameters) super.back();
        }

        public de.peeeq.pscript.ast.AFormalParameters reverse() {
            return (de.peeeq.pscript.ast.AFormalParameters) super.reverse();
        }

        public de.peeeq.pscript.ast.AFormalParameters sublist(int from, int to) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.sublist(from, to);
        }

        public de.peeeq.pscript.ast.AFormalParameters replace(int ith, Object element) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.replace(ith, element);
        }

        public de.peeeq.pscript.ast.AFormalParameters toSet() {
            return (de.peeeq.pscript.ast.AFormalParameters) super.toSet();
        }

        public de.peeeq.pscript.ast.AFormalParameters setAdd(de.peeeq.pscript.ast.AFormalParameter element) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.setAdd(element);
        }

        public de.peeeq.pscript.ast.AFormalParameters setRemove(de.peeeq.pscript.ast.AFormalParameter element) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.setRemove(element);
        }

        public de.peeeq.pscript.ast.AFormalParameters setUnion(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.setUnion(list);
        }

        public de.peeeq.pscript.ast.AFormalParameters setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.setIntersection(list);
        }

        public de.peeeq.pscript.ast.AFormalParameters setWithout(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return (de.peeeq.pscript.ast.AFormalParameters) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("pscriptAST.AFormalParameters");
            builder.append("( ");
            for(de.peeeq.pscript.ast.AFormalParameter element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AFormalParameters");
            builder.append("( ");
            for(de.peeeq.pscript.ast.AFormalParameter element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AFormalParameters";
        }
    }
}

