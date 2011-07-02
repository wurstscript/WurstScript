package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface AArgumentsPos extends de.peeeq.pscript.ast.pscriptAST.ListPos<de.peeeq.pscript.ast.AArguments, de.peeeq.pscript.ast.AExprPos, de.peeeq.pscript.ast.AExpr> {

    //----- methods of AArgumentsPos -----

    public de.peeeq.pscript.ast.AArguments term();
    public de.peeeq.pscript.ast.AArgumentsPos add(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArgumentsPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArgumentsPos remove(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArgumentsPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArgumentsPos appBack(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArgumentsPos appFront(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArgumentsPos conc(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArgumentsPos reverse();
    public de.peeeq.pscript.ast.AArgumentsPos toSet();
    public de.peeeq.pscript.ast.AArgumentsPos setAdd(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArgumentsPos setRemove(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArgumentsPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArgumentsPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArgumentsPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public KatjaList<de.peeeq.pscript.ast.AExprPos> toList();
    public de.peeeq.pscript.ast.AArgumentsPos replace(de.peeeq.pscript.ast.AArguments term);
    public de.peeeq.pscript.ast.AExprPos parent();
    public de.peeeq.pscript.ast.StringPos lsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);

    //----- nested classes of AArgumentsPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AExprPos> implements KatjaList<de.peeeq.pscript.ast.AExprPos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.pscript.ast.AExprPos... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.AExprPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AExprPos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.pscript.ast.AExprPos> createInstance(de.peeeq.pscript.ast.AExprPos[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.AExprPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AExprPos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.AExprPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AExprPos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.pscript.ast.AExprPos element : values) {
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

        public void visit(de.peeeq.pscript.ast.AExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefixPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ALiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFunctionCallPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ABuildinCallPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVariableAccessPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFieldAccessPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ANoExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ARealLiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AStringLiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E;
        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AArgumentsPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifierPos term) throws E;
        public void visit(de.peeeq.pscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BigDecimalPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BooleanPos term) throws E;
        public void visit(de.peeeq.pscript.ast.APlusPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AMinusPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ANotPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AEqEqPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AGtPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AGtEqPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ALtPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ALtEqPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AMultPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ADivPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ADivIntPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AModuloPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ADotPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AAndPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AOrPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AArgumentsPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AExprPos.Switch<Object, E> variantVisit$AExprPos = new de.peeeq.pscript.ast.AExprPos.Switch<Object, E>() { public final Object CaseAPrefixPos(de.peeeq.pscript.ast.APrefixPos term) throws E { visit(term); return null; } public final Object CaseAInfixPos(de.peeeq.pscript.ast.AInfixPos term) throws E { visit(term); return null; } public final Object CaseAFunctionCallPos(de.peeeq.pscript.ast.AFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseABuildinCallPos(de.peeeq.pscript.ast.ABuildinCallPos term) throws E { visit(term); return null; } public final Object CaseAVariableAccessPos(de.peeeq.pscript.ast.AVariableAccessPos term) throws E { visit(term); return null; } public final Object CaseAFieldAccessPos(de.peeeq.pscript.ast.AFieldAccessPos term) throws E { visit(term); return null; } public final Object CaseANoExprPos(de.peeeq.pscript.ast.ANoExprPos term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E> variantVisit$AInfixOpPos = new de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E>() { public final Object CaseAEqEqPos(de.peeeq.pscript.ast.AEqEqPos term) throws E { visit(term); return null; } public final Object CaseAGtPos(de.peeeq.pscript.ast.AGtPos term) throws E { visit(term); return null; } public final Object CaseAGtEqPos(de.peeeq.pscript.ast.AGtEqPos term) throws E { visit(term); return null; } public final Object CaseALtPos(de.peeeq.pscript.ast.ALtPos term) throws E { visit(term); return null; } public final Object CaseALtEqPos(de.peeeq.pscript.ast.ALtEqPos term) throws E { visit(term); return null; } public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseAMultPos(de.peeeq.pscript.ast.AMultPos term) throws E { visit(term); return null; } public final Object CaseADivPos(de.peeeq.pscript.ast.ADivPos term) throws E { visit(term); return null; } public final Object CaseADivIntPos(de.peeeq.pscript.ast.ADivIntPos term) throws E { visit(term); return null; } public final Object CaseAModuloPos(de.peeeq.pscript.ast.AModuloPos term) throws E { visit(term); return null; } public final Object CaseADotPos(de.peeeq.pscript.ast.ADotPos term) throws E { visit(term); return null; } public final Object CaseAAndPos(de.peeeq.pscript.ast.AAndPos term) throws E { visit(term); return null; } public final Object CaseAOrPos(de.peeeq.pscript.ast.AOrPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E> variantVisit$APrefixOpPos = new de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E>() { public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseANotPos(de.peeeq.pscript.ast.ANotPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AExprPos term) throws E {
            term.Switch(variantVisit$AExprPos);
        }

        public final void visit(de.peeeq.pscript.ast.AInfixOpPos term) throws E {
            term.Switch(variantVisit$AInfixOpPos);
        }

        public final void visit(de.peeeq.pscript.ast.ALiteralPos term) throws E {
            term.Switch(variantVisit$AExprPos);
        }

        public final void visit(de.peeeq.pscript.ast.APrefixOpPos term) throws E {
            term.Switch(variantVisit$APrefixOpPos);
        }
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AArguments, de.peeeq.pscript.ast.AExprPos, de.peeeq.pscript.ast.AExpr> implements de.peeeq.pscript.ast.AArgumentsPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AArguments term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.pscript.ast.AExprPos getElementInstance(int pos) {
            return de.peeeq.pscript.ast.pscriptAST.AExprPos(this, _term.get(pos), pos);
        }

        protected de.peeeq.pscript.ast.AExprPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AExprPos[size];
        }

        public de.peeeq.pscript.ast.AArgumentsPos add(de.peeeq.pscript.ast.AExpr element) {
            return replace(_term.add(element));
        }

        public de.peeeq.pscript.ast.AArgumentsPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.pscript.ast.AArgumentsPos remove(de.peeeq.pscript.ast.AExpr element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.pscript.ast.AArgumentsPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.pscript.ast.AArgumentsPos appBack(de.peeeq.pscript.ast.AExpr element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.pscript.ast.AArgumentsPos appFront(de.peeeq.pscript.ast.AExpr element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.pscript.ast.AArgumentsPos conc(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.pscript.ast.AArgumentsPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.pscript.ast.AArgumentsPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.pscript.ast.AArgumentsPos setAdd(de.peeeq.pscript.ast.AExpr element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.pscript.ast.AArgumentsPos setRemove(de.peeeq.pscript.ast.AExpr element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.pscript.ast.AArgumentsPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.pscript.ast.AArgumentsPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.pscript.ast.AArgumentsPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.pscript.ast.AExprPos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new AArgumentsPos.ListImpl(values);
        }

        public de.peeeq.pscript.ast.AArgumentsPos replace(de.peeeq.pscript.ast.AArguments term) {
            return (de.peeeq.pscript.ast.AArgumentsPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AExprPos parent() {
            return (de.peeeq.pscript.ast.AExprPos) super.parent();
        }

        public de.peeeq.pscript.ast.StringPos lsib() {
            return (de.peeeq.pscript.ast.StringPos) super.lsib();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos rsib() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.rsib();
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
            return "AArgumentsPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AExprPos> implements KatjaList<de.peeeq.pscript.ast.AExprPos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.pscript.ast.AExprPos... elements) {
                super(elements);

                for(de.peeeq.pscript.ast.AExprPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AExprPos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.pscript.ast.AExprPos> createInstance(de.peeeq.pscript.ast.AExprPos[] elements, boolean isSet) {
                for(de.peeeq.pscript.ast.AExprPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AExprPos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) pscriptAST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.pscript.ast.AExprPos[] createArray(int size) {
                return new de.peeeq.pscript.ast.AExprPos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.pscript.ast.AExprPos element : values) {
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

