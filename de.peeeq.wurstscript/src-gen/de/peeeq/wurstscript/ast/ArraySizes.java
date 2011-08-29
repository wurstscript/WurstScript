package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ArraySizes extends KatjaList<de.peeeq.wurstscript.ast.Expr> {

    //----- methods of ArraySizes -----

    public de.peeeq.wurstscript.ast.ArraySizes add(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.ArraySizes addAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.ArraySizes remove(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.ArraySizes removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.ArraySizes appBack(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.ArraySizes appFront(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.ArraySizes conc(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.ArraySizes front();
    public de.peeeq.wurstscript.ast.ArraySizes back();
    public de.peeeq.wurstscript.ast.ArraySizes reverse();
    public de.peeeq.wurstscript.ast.ArraySizes sublist(int from, int to);
    public de.peeeq.wurstscript.ast.ArraySizes replace(int ith, Object element);
    public de.peeeq.wurstscript.ast.ArraySizes toSet();
    public de.peeeq.wurstscript.ast.ArraySizes setAdd(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.ArraySizes setRemove(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.ArraySizes setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.ArraySizes setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.ArraySizes setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);

    //----- nested classes of ArraySizes -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAssignable term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNewObject term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAtomic term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprIntVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprRealVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprStringVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprThis term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNull term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Indexes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Arguments term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.lang.Double term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpOr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAnd term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpEquals term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnequals term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpLessEq term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpLess term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpGreater term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpPlus term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpMinus term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpMult term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpDivReal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpModReal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpModInt term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpDivInt term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpNot term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizes term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.ArraySizes.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.Expr.Switch<Object, E> variantVisit$Expr = new de.peeeq.wurstscript.ast.Expr.Switch<Object, E>() { public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.Expr term) throws E {
            term.Switch(variantVisit$Expr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAssignable term) throws E {
            term.Switch(variantVisit$Expr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAtomic term) throws E {
            term.Switch(variantVisit$Expr);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E {
            term.Switch(variantVisit$OpBinary);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E {
            term.Switch(variantVisit$OpUnary);
        }
    }

    static class Impl extends KatjaListImpl<de.peeeq.wurstscript.ast.Expr> implements de.peeeq.wurstscript.ast.ArraySizes {

        //----- methods of Impl -----

        Impl(de.peeeq.wurstscript.ast.Expr... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.Expr element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort ArraySizes invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.wurstscript.ast.ArraySizes createInstance(de.peeeq.wurstscript.ast.Expr[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.Expr element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort ArraySizes invoked with null element");

            ArraySizes.Impl temp = new de.peeeq.wurstscript.ast.ArraySizes.Impl();
            temp.values = elements;
            temp = (ArraySizes.Impl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.Expr[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.Expr[size];
        }

        public de.peeeq.wurstscript.ast.ArraySizes add(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.add(element);
        }

        public de.peeeq.wurstscript.ast.ArraySizes addAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.addAll(list);
        }

        public de.peeeq.wurstscript.ast.ArraySizes remove(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.remove(element);
        }

        public de.peeeq.wurstscript.ast.ArraySizes removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.removeAll(list);
        }

        public de.peeeq.wurstscript.ast.ArraySizes appBack(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.appBack(element);
        }

        public de.peeeq.wurstscript.ast.ArraySizes appFront(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.appFront(element);
        }

        public de.peeeq.wurstscript.ast.ArraySizes conc(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.conc(list);
        }

        public de.peeeq.wurstscript.ast.ArraySizes front() {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.front();
        }

        public de.peeeq.wurstscript.ast.ArraySizes back() {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.back();
        }

        public de.peeeq.wurstscript.ast.ArraySizes reverse() {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.reverse();
        }

        public de.peeeq.wurstscript.ast.ArraySizes sublist(int from, int to) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.sublist(from, to);
        }

        public de.peeeq.wurstscript.ast.ArraySizes replace(int ith, Object element) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.replace(ith, element);
        }

        public de.peeeq.wurstscript.ast.ArraySizes toSet() {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.toSet();
        }

        public de.peeeq.wurstscript.ast.ArraySizes setAdd(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.setAdd(element);
        }

        public de.peeeq.wurstscript.ast.ArraySizes setRemove(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.setRemove(element);
        }

        public de.peeeq.wurstscript.ast.ArraySizes setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.setUnion(list);
        }

        public de.peeeq.wurstscript.ast.ArraySizes setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.setIntersection(list);
        }

        public de.peeeq.wurstscript.ast.ArraySizes setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.ArraySizes) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AST.ArraySizes");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.Expr element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("ArraySizes");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.Expr element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ArraySizes";
        }
    }
}

