package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface AFormalParametersPos extends de.peeeq.pscript.ast.pscriptAST.ListPos<de.peeeq.pscript.ast.AFormalParameters, de.peeeq.pscript.ast.AFormalParameterPos, de.peeeq.pscript.ast.AFormalParameter> {

    //----- methods of AFormalParametersPos -----

    public de.peeeq.pscript.ast.AFormalParameters term();
    public de.peeeq.pscript.ast.AFormalParametersPos add(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParametersPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParametersPos remove(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParametersPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParametersPos appBack(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParametersPos appFront(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParametersPos conc(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParametersPos reverse();
    public de.peeeq.pscript.ast.AFormalParametersPos toSet();
    public de.peeeq.pscript.ast.AFormalParametersPos setAdd(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParametersPos setRemove(de.peeeq.pscript.ast.AFormalParameter element);
    public de.peeeq.pscript.ast.AFormalParametersPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParametersPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public de.peeeq.pscript.ast.AFormalParametersPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list);
    public KatjaList<de.peeeq.pscript.ast.AFormalParameterPos> toList();
    public de.peeeq.pscript.ast.AFormalParametersPos replace(de.peeeq.pscript.ast.AFormalParameters term);
    public de.peeeq.pscript.ast.AFuncDefPos parent();
    public de.peeeq.pscript.ast.AReturnTypePos lsib();
    public de.peeeq.pscript.ast.ABlockPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);

    //----- nested classes of AFormalParametersPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AFormalParameterPos> implements KatjaList<de.peeeq.pscript.ast.AFormalParameterPos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.pscript.ast.AFormalParameterPos... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.AFormalParameterPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AFormalParameterPos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.pscript.ast.AFormalParameterPos> createInstance(de.peeeq.pscript.ast.AFormalParameterPos[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.AFormalParameterPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AFormalParameterPos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.AFormalParameterPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AFormalParameterPos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.pscript.ast.AFormalParameterPos element : values) {
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

        public void visit(de.peeeq.pscript.ast.AFormalParameterPos term) throws E;
        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParametersPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AFormalParametersPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.ATypeExprPos.Switch<Object, E> variantVisit$ATypeExprPos = new de.peeeq.pscript.ast.ATypeExprPos.Switch<Object, E>() { public final Object CaseATypeExprSimplePos(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E {
            term.Switch(variantVisit$ATypeExprPos);
        }
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AFormalParameters, de.peeeq.pscript.ast.AFormalParameterPos, de.peeeq.pscript.ast.AFormalParameter> implements de.peeeq.pscript.ast.AFormalParametersPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AFormalParameters term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.pscript.ast.AFormalParameterPos getElementInstance(int pos) {
            return de.peeeq.pscript.ast.pscriptAST.AFormalParameterPos(this, _term.get(pos), pos);
        }

        protected de.peeeq.pscript.ast.AFormalParameterPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AFormalParameterPos[size];
        }

        public de.peeeq.pscript.ast.AFormalParametersPos add(de.peeeq.pscript.ast.AFormalParameter element) {
            return replace(_term.add(element));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos remove(de.peeeq.pscript.ast.AFormalParameter element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos appBack(de.peeeq.pscript.ast.AFormalParameter element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos appFront(de.peeeq.pscript.ast.AFormalParameter element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos conc(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.pscript.ast.AFormalParametersPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.pscript.ast.AFormalParametersPos setAdd(de.peeeq.pscript.ast.AFormalParameter element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos setRemove(de.peeeq.pscript.ast.AFormalParameter element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.pscript.ast.AFormalParametersPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AFormalParameter> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.pscript.ast.AFormalParameterPos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new AFormalParametersPos.ListImpl(values);
        }

        public de.peeeq.pscript.ast.AFormalParametersPos replace(de.peeeq.pscript.ast.AFormalParameters term) {
            return (de.peeeq.pscript.ast.AFormalParametersPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AFuncDefPos parent() {
            return (de.peeeq.pscript.ast.AFuncDefPos) super.parent();
        }

        public de.peeeq.pscript.ast.AReturnTypePos lsib() {
            return (de.peeeq.pscript.ast.AReturnTypePos) super.lsib();
        }

        public de.peeeq.pscript.ast.ABlockPos rsib() {
            return (de.peeeq.pscript.ast.ABlockPos) super.rsib();
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
            return "AFormalParametersPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AFormalParameterPos> implements KatjaList<de.peeeq.pscript.ast.AFormalParameterPos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.pscript.ast.AFormalParameterPos... elements) {
                super(elements);

                for(de.peeeq.pscript.ast.AFormalParameterPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AFormalParameterPos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.pscript.ast.AFormalParameterPos> createInstance(de.peeeq.pscript.ast.AFormalParameterPos[] elements, boolean isSet) {
                for(de.peeeq.pscript.ast.AFormalParameterPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AFormalParameterPos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) pscriptAST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.pscript.ast.AFormalParameterPos[] createArray(int size) {
                return new de.peeeq.pscript.ast.AFormalParameterPos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.pscript.ast.AFormalParameterPos element : values) {
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

