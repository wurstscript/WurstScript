package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaList;
import katja.common.KatjaListImpl;

public interface Indexes extends KatjaList<de.peeeq.wurstscript.ast.Expr> {

    //----- methods of Indexes -----

    public de.peeeq.wurstscript.ast.Indexes add(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.Indexes addAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.Indexes remove(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.Indexes removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.Indexes appBack(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.Indexes appFront(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.Indexes conc(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.Indexes front();
    public de.peeeq.wurstscript.ast.Indexes back();
    public de.peeeq.wurstscript.ast.Indexes reverse();
    public de.peeeq.wurstscript.ast.Indexes sublist(int from, int to);
    public de.peeeq.wurstscript.ast.Indexes replace(int ith, Object element);
    public de.peeeq.wurstscript.ast.Indexes toSet();
    public de.peeeq.wurstscript.ast.Indexes setAdd(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.Indexes setRemove(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.Indexes setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.Indexes setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.Indexes setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);

    //----- nested classes of Indexes -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNewObject term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAtomic term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprIntVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprRealVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprStringVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprThis term) throws E;
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
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.Indexes.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.Expr.Switch<Object, E> variantVisit$Expr = new de.peeeq.wurstscript.ast.Expr.Switch<Object, E>() { public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.Expr term) throws E {
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

    static class Impl extends KatjaListImpl<de.peeeq.wurstscript.ast.Expr> implements de.peeeq.wurstscript.ast.Indexes {

        //----- methods of Impl -----

        Impl(de.peeeq.wurstscript.ast.Expr... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.Expr element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort Indexes invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.wurstscript.ast.Indexes createInstance(de.peeeq.wurstscript.ast.Expr[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.Expr element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort Indexes invoked with null element");

            Indexes.Impl temp = new de.peeeq.wurstscript.ast.Indexes.Impl();
            temp.values = elements;
            temp = (Indexes.Impl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.Expr[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.Expr[size];
        }

        public de.peeeq.wurstscript.ast.Indexes add(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.Indexes) super.add(element);
        }

        public de.peeeq.wurstscript.ast.Indexes addAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.Indexes) super.addAll(list);
        }

        public de.peeeq.wurstscript.ast.Indexes remove(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.Indexes) super.remove(element);
        }

        public de.peeeq.wurstscript.ast.Indexes removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.Indexes) super.removeAll(list);
        }

        public de.peeeq.wurstscript.ast.Indexes appBack(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.Indexes) super.appBack(element);
        }

        public de.peeeq.wurstscript.ast.Indexes appFront(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.Indexes) super.appFront(element);
        }

        public de.peeeq.wurstscript.ast.Indexes conc(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.Indexes) super.conc(list);
        }

        public de.peeeq.wurstscript.ast.Indexes front() {
            return (de.peeeq.wurstscript.ast.Indexes) super.front();
        }

        public de.peeeq.wurstscript.ast.Indexes back() {
            return (de.peeeq.wurstscript.ast.Indexes) super.back();
        }

        public de.peeeq.wurstscript.ast.Indexes reverse() {
            return (de.peeeq.wurstscript.ast.Indexes) super.reverse();
        }

        public de.peeeq.wurstscript.ast.Indexes sublist(int from, int to) {
            return (de.peeeq.wurstscript.ast.Indexes) super.sublist(from, to);
        }

        public de.peeeq.wurstscript.ast.Indexes replace(int ith, Object element) {
            return (de.peeeq.wurstscript.ast.Indexes) super.replace(ith, element);
        }

        public de.peeeq.wurstscript.ast.Indexes toSet() {
            return (de.peeeq.wurstscript.ast.Indexes) super.toSet();
        }

        public de.peeeq.wurstscript.ast.Indexes setAdd(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.Indexes) super.setAdd(element);
        }

        public de.peeeq.wurstscript.ast.Indexes setRemove(de.peeeq.wurstscript.ast.Expr element) {
            return (de.peeeq.wurstscript.ast.Indexes) super.setRemove(element);
        }

        public de.peeeq.wurstscript.ast.Indexes setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.Indexes) super.setUnion(list);
        }

        public de.peeeq.wurstscript.ast.Indexes setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.Indexes) super.setIntersection(list);
        }

        public de.peeeq.wurstscript.ast.Indexes setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return (de.peeeq.wurstscript.ast.Indexes) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AST.Indexes");
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

            builder.append("Indexes");
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
            return "Indexes";
        }
    }
}

