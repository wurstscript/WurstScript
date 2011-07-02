package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AArguments extends KatjaList<de.peeeq.pscript.ast.AExpr> {

    //----- methods of AArguments -----

    public de.peeeq.pscript.ast.AArguments add(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArguments addAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArguments remove(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArguments removeAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArguments appBack(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArguments appFront(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArguments conc(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArguments front();
    public de.peeeq.pscript.ast.AArguments back();
    public de.peeeq.pscript.ast.AArguments reverse();
    public de.peeeq.pscript.ast.AArguments sublist(int from, int to);
    public de.peeeq.pscript.ast.AArguments replace(int ith, Object element);
    public de.peeeq.pscript.ast.AArguments toSet();
    public de.peeeq.pscript.ast.AArguments setAdd(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArguments setRemove(de.peeeq.pscript.ast.AExpr element);
    public de.peeeq.pscript.ast.AArguments setUnion(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArguments setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);
    public de.peeeq.pscript.ast.AArguments setWithout(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list);

    //----- nested classes of AArguments -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.AExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefix term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfix term) throws E;
        public void visit(de.peeeq.pscript.ast.ALiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.AFunctionCall term) throws E;
        public void visit(de.peeeq.pscript.ast.ABuildinCall term) throws E;
        public void visit(de.peeeq.pscript.ast.AVariableAccess term) throws E;
        public void visit(de.peeeq.pscript.ast.AFieldAccess term) throws E;
        public void visit(de.peeeq.pscript.ast.ANoExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.AIntegerLiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.ARealLiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.AStringLiteral term) throws E;
        public void visit(de.peeeq.pscript.ast.ABooleanLiteral term) throws E;
        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOp term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AArguments term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifier term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.math.BigDecimal term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.pscript.ast.APlus term) throws E;
        public void visit(de.peeeq.pscript.ast.AMinus term) throws E;
        public void visit(de.peeeq.pscript.ast.ANot term) throws E;
        public void visit(de.peeeq.pscript.ast.AEqEq term) throws E;
        public void visit(de.peeeq.pscript.ast.AGt term) throws E;
        public void visit(de.peeeq.pscript.ast.AGtEq term) throws E;
        public void visit(de.peeeq.pscript.ast.ALt term) throws E;
        public void visit(de.peeeq.pscript.ast.ALtEq term) throws E;
        public void visit(de.peeeq.pscript.ast.AMult term) throws E;
        public void visit(de.peeeq.pscript.ast.ADiv term) throws E;
        public void visit(de.peeeq.pscript.ast.ADivInt term) throws E;
        public void visit(de.peeeq.pscript.ast.AModulo term) throws E;
        public void visit(de.peeeq.pscript.ast.ADot term) throws E;
        public void visit(de.peeeq.pscript.ast.AAnd term) throws E;
        public void visit(de.peeeq.pscript.ast.AOr term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AArguments.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AExpr.Switch<Object, E> variantVisit$AExpr = new de.peeeq.pscript.ast.AExpr.Switch<Object, E>() { public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOp.Switch<Object, E> variantVisit$AInfixOp = new de.peeeq.pscript.ast.AInfixOp.Switch<Object, E>() { public final Object CaseAEqEq(de.peeeq.pscript.ast.AEqEq term) throws E { visit(term); return null; } public final Object CaseAGt(de.peeeq.pscript.ast.AGt term) throws E { visit(term); return null; } public final Object CaseAGtEq(de.peeeq.pscript.ast.AGtEq term) throws E { visit(term); return null; } public final Object CaseALt(de.peeeq.pscript.ast.ALt term) throws E { visit(term); return null; } public final Object CaseALtEq(de.peeeq.pscript.ast.ALtEq term) throws E { visit(term); return null; } public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseAMult(de.peeeq.pscript.ast.AMult term) throws E { visit(term); return null; } public final Object CaseADiv(de.peeeq.pscript.ast.ADiv term) throws E { visit(term); return null; } public final Object CaseADivInt(de.peeeq.pscript.ast.ADivInt term) throws E { visit(term); return null; } public final Object CaseAModulo(de.peeeq.pscript.ast.AModulo term) throws E { visit(term); return null; } public final Object CaseADot(de.peeeq.pscript.ast.ADot term) throws E { visit(term); return null; } public final Object CaseAAnd(de.peeeq.pscript.ast.AAnd term) throws E { visit(term); return null; } public final Object CaseAOr(de.peeeq.pscript.ast.AOr term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOp.Switch<Object, E> variantVisit$APrefixOp = new de.peeeq.pscript.ast.APrefixOp.Switch<Object, E>() { public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseANot(de.peeeq.pscript.ast.ANot term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AExpr term) throws E {
            term.Switch(variantVisit$AExpr);
        }

        public final void visit(de.peeeq.pscript.ast.AInfixOp term) throws E {
            term.Switch(variantVisit$AInfixOp);
        }

        public final void visit(de.peeeq.pscript.ast.ALiteral term) throws E {
            term.Switch(variantVisit$AExpr);
        }

        public final void visit(de.peeeq.pscript.ast.APrefixOp term) throws E {
            term.Switch(variantVisit$APrefixOp);
        }
    }

    static class Impl extends KatjaListImpl<de.peeeq.pscript.ast.AExpr> implements de.peeeq.pscript.ast.AArguments {

        //----- methods of Impl -----

        Impl(de.peeeq.pscript.ast.AExpr... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.AExpr element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort AArguments invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.pscript.ast.AArguments createInstance(de.peeeq.pscript.ast.AExpr[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.AExpr element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort AArguments invoked with null element");

            AArguments.Impl temp = new de.peeeq.pscript.ast.AArguments.Impl();
            temp.values = elements;
            temp = (AArguments.Impl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.AExpr[] createArray(int size) {
            return new de.peeeq.pscript.ast.AExpr[size];
        }

        public de.peeeq.pscript.ast.AArguments add(de.peeeq.pscript.ast.AExpr element) {
            return (de.peeeq.pscript.ast.AArguments) super.add(element);
        }

        public de.peeeq.pscript.ast.AArguments addAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return (de.peeeq.pscript.ast.AArguments) super.addAll(list);
        }

        public de.peeeq.pscript.ast.AArguments remove(de.peeeq.pscript.ast.AExpr element) {
            return (de.peeeq.pscript.ast.AArguments) super.remove(element);
        }

        public de.peeeq.pscript.ast.AArguments removeAll(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return (de.peeeq.pscript.ast.AArguments) super.removeAll(list);
        }

        public de.peeeq.pscript.ast.AArguments appBack(de.peeeq.pscript.ast.AExpr element) {
            return (de.peeeq.pscript.ast.AArguments) super.appBack(element);
        }

        public de.peeeq.pscript.ast.AArguments appFront(de.peeeq.pscript.ast.AExpr element) {
            return (de.peeeq.pscript.ast.AArguments) super.appFront(element);
        }

        public de.peeeq.pscript.ast.AArguments conc(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return (de.peeeq.pscript.ast.AArguments) super.conc(list);
        }

        public de.peeeq.pscript.ast.AArguments front() {
            return (de.peeeq.pscript.ast.AArguments) super.front();
        }

        public de.peeeq.pscript.ast.AArguments back() {
            return (de.peeeq.pscript.ast.AArguments) super.back();
        }

        public de.peeeq.pscript.ast.AArguments reverse() {
            return (de.peeeq.pscript.ast.AArguments) super.reverse();
        }

        public de.peeeq.pscript.ast.AArguments sublist(int from, int to) {
            return (de.peeeq.pscript.ast.AArguments) super.sublist(from, to);
        }

        public de.peeeq.pscript.ast.AArguments replace(int ith, Object element) {
            return (de.peeeq.pscript.ast.AArguments) super.replace(ith, element);
        }

        public de.peeeq.pscript.ast.AArguments toSet() {
            return (de.peeeq.pscript.ast.AArguments) super.toSet();
        }

        public de.peeeq.pscript.ast.AArguments setAdd(de.peeeq.pscript.ast.AExpr element) {
            return (de.peeeq.pscript.ast.AArguments) super.setAdd(element);
        }

        public de.peeeq.pscript.ast.AArguments setRemove(de.peeeq.pscript.ast.AExpr element) {
            return (de.peeeq.pscript.ast.AArguments) super.setRemove(element);
        }

        public de.peeeq.pscript.ast.AArguments setUnion(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return (de.peeeq.pscript.ast.AArguments) super.setUnion(list);
        }

        public de.peeeq.pscript.ast.AArguments setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return (de.peeeq.pscript.ast.AArguments) super.setIntersection(list);
        }

        public de.peeeq.pscript.ast.AArguments setWithout(KatjaList<? extends de.peeeq.pscript.ast.AExpr> list) {
            return (de.peeeq.pscript.ast.AArguments) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("pscriptAST.AArguments");
            builder.append("( ");
            for(de.peeeq.pscript.ast.AExpr element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AArguments");
            builder.append("( ");
            for(de.peeeq.pscript.ast.AExpr element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AArguments";
        }
    }
}

