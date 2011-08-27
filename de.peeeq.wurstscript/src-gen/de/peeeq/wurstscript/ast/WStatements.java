package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface WStatements extends KatjaList<de.peeeq.wurstscript.ast.WStatement> {

    //----- methods of WStatements -----

    public de.peeeq.wurstscript.ast.WStatements add(de.peeeq.wurstscript.ast.WStatement element);
    public de.peeeq.wurstscript.ast.WStatements addAll(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list);
    public de.peeeq.wurstscript.ast.WStatements remove(de.peeeq.wurstscript.ast.WStatement element);
    public de.peeeq.wurstscript.ast.WStatements removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list);
    public de.peeeq.wurstscript.ast.WStatements appBack(de.peeeq.wurstscript.ast.WStatement element);
    public de.peeeq.wurstscript.ast.WStatements appFront(de.peeeq.wurstscript.ast.WStatement element);
    public de.peeeq.wurstscript.ast.WStatements conc(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list);
    public de.peeeq.wurstscript.ast.WStatements front();
    public de.peeeq.wurstscript.ast.WStatements back();
    public de.peeeq.wurstscript.ast.WStatements reverse();
    public de.peeeq.wurstscript.ast.WStatements sublist(int from, int to);
    public de.peeeq.wurstscript.ast.WStatements replace(int ith, Object element);
    public de.peeeq.wurstscript.ast.WStatements toSet();
    public de.peeeq.wurstscript.ast.WStatements setAdd(de.peeeq.wurstscript.ast.WStatement element);
    public de.peeeq.wurstscript.ast.WStatements setRemove(de.peeeq.wurstscript.ast.WStatement element);
    public de.peeeq.wurstscript.ast.WStatements setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list);
    public de.peeeq.wurstscript.ast.WStatements setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list);
    public de.peeeq.wurstscript.ast.WStatements setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list);

    //----- nested classes of WStatements -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WStatement term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtIf term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtWhile term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtLoop term) throws E;
        public void visit(de.peeeq.wurstscript.ast.LocalVarDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtSet term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtCall term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtReturn term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtDestroy term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtIncRefCount term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtDecRefCount term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtErr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtExitwhen term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNewObject term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WStatements term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAssignable term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssignment term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Arguments term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAtomic term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprIntVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprRealVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprStringVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprThis term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssign term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Indexes term) throws E;
        public void visit(java.lang.Double term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizes term) throws E;
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

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.WStatements.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E> variantVisit$OpAssignment = new de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E>() { public final Object CaseOpAssign(de.peeeq.wurstscript.ast.OpAssign term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E> variantVisit$OptExpr = new de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E>() { public final Object CaseNoExpr(de.peeeq.wurstscript.ast.NoExpr term) throws E { visit(term); return null; } public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E> variantVisit$OptTypeExpr = new de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E>() { public final Object CaseNoTypeExpr(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E { visit(term); return null; } public final Object CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.StmtCall.Switch<Object, E> variantVisit$StmtCall = new de.peeeq.wurstscript.ast.StmtCall.Switch<Object, E>() { public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.WStatement.Switch<Object, E> variantVisit$WStatement = new de.peeeq.wurstscript.ast.WStatement.Switch<Object, E>() { public final Object CaseStmtIf(de.peeeq.wurstscript.ast.StmtIf term) throws E { visit(term); return null; } public final Object CaseStmtWhile(de.peeeq.wurstscript.ast.StmtWhile term) throws E { visit(term); return null; } public final Object CaseStmtLoop(de.peeeq.wurstscript.ast.StmtLoop term) throws E { visit(term); return null; } public final Object CaseLocalVarDef(de.peeeq.wurstscript.ast.LocalVarDef term) throws E { visit(term); return null; } public final Object CaseStmtSet(de.peeeq.wurstscript.ast.StmtSet term) throws E { visit(term); return null; } public final Object CaseStmtReturn(de.peeeq.wurstscript.ast.StmtReturn term) throws E { visit(term); return null; } public final Object CaseStmtDestroy(de.peeeq.wurstscript.ast.StmtDestroy term) throws E { visit(term); return null; } public final Object CaseStmtIncRefCount(de.peeeq.wurstscript.ast.StmtIncRefCount term) throws E { visit(term); return null; } public final Object CaseStmtDecRefCount(de.peeeq.wurstscript.ast.StmtDecRefCount term) throws E { visit(term); return null; } public final Object CaseStmtErr(de.peeeq.wurstscript.ast.StmtErr term) throws E { visit(term); return null; } public final Object CaseStmtExitwhen(de.peeeq.wurstscript.ast.StmtExitwhen term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.Expr term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAssignable term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAtomic term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpAssignment term) throws E {
            term.Switch(variantVisit$OpAssignment);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E {
            term.Switch(variantVisit$OpBinary);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E {
            term.Switch(variantVisit$OpUnary);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E {
            term.Switch(variantVisit$OptTypeExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.StmtCall term) throws E {
            term.Switch(variantVisit$StmtCall);
        }

        public final void visit(de.peeeq.wurstscript.ast.WStatement term) throws E {
            term.Switch(variantVisit$WStatement);
        }
    }

    static class Impl extends KatjaListImpl<de.peeeq.wurstscript.ast.WStatement> implements de.peeeq.wurstscript.ast.WStatements {

        //----- methods of Impl -----

        Impl(de.peeeq.wurstscript.ast.WStatement... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.WStatement element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort WStatements invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.wurstscript.ast.WStatements createInstance(de.peeeq.wurstscript.ast.WStatement[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.WStatement element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort WStatements invoked with null element");

            WStatements.Impl temp = new de.peeeq.wurstscript.ast.WStatements.Impl();
            temp.values = elements;
            temp = (WStatements.Impl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.WStatement[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.WStatement[size];
        }

        public de.peeeq.wurstscript.ast.WStatements add(de.peeeq.wurstscript.ast.WStatement element) {
            return (de.peeeq.wurstscript.ast.WStatements) super.add(element);
        }

        public de.peeeq.wurstscript.ast.WStatements addAll(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list) {
            return (de.peeeq.wurstscript.ast.WStatements) super.addAll(list);
        }

        public de.peeeq.wurstscript.ast.WStatements remove(de.peeeq.wurstscript.ast.WStatement element) {
            return (de.peeeq.wurstscript.ast.WStatements) super.remove(element);
        }

        public de.peeeq.wurstscript.ast.WStatements removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list) {
            return (de.peeeq.wurstscript.ast.WStatements) super.removeAll(list);
        }

        public de.peeeq.wurstscript.ast.WStatements appBack(de.peeeq.wurstscript.ast.WStatement element) {
            return (de.peeeq.wurstscript.ast.WStatements) super.appBack(element);
        }

        public de.peeeq.wurstscript.ast.WStatements appFront(de.peeeq.wurstscript.ast.WStatement element) {
            return (de.peeeq.wurstscript.ast.WStatements) super.appFront(element);
        }

        public de.peeeq.wurstscript.ast.WStatements conc(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list) {
            return (de.peeeq.wurstscript.ast.WStatements) super.conc(list);
        }

        public de.peeeq.wurstscript.ast.WStatements front() {
            return (de.peeeq.wurstscript.ast.WStatements) super.front();
        }

        public de.peeeq.wurstscript.ast.WStatements back() {
            return (de.peeeq.wurstscript.ast.WStatements) super.back();
        }

        public de.peeeq.wurstscript.ast.WStatements reverse() {
            return (de.peeeq.wurstscript.ast.WStatements) super.reverse();
        }

        public de.peeeq.wurstscript.ast.WStatements sublist(int from, int to) {
            return (de.peeeq.wurstscript.ast.WStatements) super.sublist(from, to);
        }

        public de.peeeq.wurstscript.ast.WStatements replace(int ith, Object element) {
            return (de.peeeq.wurstscript.ast.WStatements) super.replace(ith, element);
        }

        public de.peeeq.wurstscript.ast.WStatements toSet() {
            return (de.peeeq.wurstscript.ast.WStatements) super.toSet();
        }

        public de.peeeq.wurstscript.ast.WStatements setAdd(de.peeeq.wurstscript.ast.WStatement element) {
            return (de.peeeq.wurstscript.ast.WStatements) super.setAdd(element);
        }

        public de.peeeq.wurstscript.ast.WStatements setRemove(de.peeeq.wurstscript.ast.WStatement element) {
            return (de.peeeq.wurstscript.ast.WStatements) super.setRemove(element);
        }

        public de.peeeq.wurstscript.ast.WStatements setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list) {
            return (de.peeeq.wurstscript.ast.WStatements) super.setUnion(list);
        }

        public de.peeeq.wurstscript.ast.WStatements setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list) {
            return (de.peeeq.wurstscript.ast.WStatements) super.setIntersection(list);
        }

        public de.peeeq.wurstscript.ast.WStatements setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.WStatement> list) {
            return (de.peeeq.wurstscript.ast.WStatements) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AST.WStatements");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.WStatement element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("WStatements");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.WStatement element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "WStatements";
        }
    }
}

