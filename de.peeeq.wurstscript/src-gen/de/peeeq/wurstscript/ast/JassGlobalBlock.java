package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface JassGlobalBlock extends de.peeeq.wurstscript.ast.JassToplevelDeclaration, KatjaList<de.peeeq.wurstscript.ast.GlobalVarDef> {

    //----- methods of JassGlobalBlock -----

    public de.peeeq.wurstscript.ast.JassGlobalBlock add(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlock addAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlock remove(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlock removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlock appBack(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlock appFront(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlock conc(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlock front();
    public de.peeeq.wurstscript.ast.JassGlobalBlock back();
    public de.peeeq.wurstscript.ast.JassGlobalBlock reverse();
    public de.peeeq.wurstscript.ast.JassGlobalBlock sublist(int from, int to);
    public de.peeeq.wurstscript.ast.JassGlobalBlock replace(int ith, Object element);
    public de.peeeq.wurstscript.ast.JassGlobalBlock toSet();
    public de.peeeq.wurstscript.ast.JassGlobalBlock setAdd(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlock setRemove(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlock setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlock setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlock setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.JassToplevelDeclaration.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TopLevelDeclaration.Switch<CT, E> switchClass) throws E;

    //----- nested classes of JassGlobalBlock -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.GlobalVarDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoExpr term) throws E;
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
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Indexes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Arguments term) throws E;
        public void visit(java.lang.Double term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.JassGlobalBlock term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.JassGlobalBlock.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.Expr.Switch<Object, E> variantVisit$Expr = new de.peeeq.wurstscript.ast.Expr.Switch<Object, E>() { public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E> variantVisit$OptExpr = new de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E>() { public final Object CaseNoExpr(de.peeeq.wurstscript.ast.NoExpr term) throws E { visit(term); return null; } public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E> variantVisit$OptTypeExpr = new de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E>() { public final Object CaseNoTypeExpr(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E { visit(term); return null; } public final Object CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) throws E { visit(term); return null; } };

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

        public final void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E {
            term.Switch(variantVisit$OptTypeExpr);
        }
    }

    static class Impl extends KatjaListImpl<de.peeeq.wurstscript.ast.GlobalVarDef> implements de.peeeq.wurstscript.ast.JassGlobalBlock {

        //----- methods of Impl -----

        Impl(de.peeeq.wurstscript.ast.GlobalVarDef... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.GlobalVarDef element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort JassGlobalBlock invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.wurstscript.ast.JassGlobalBlock createInstance(de.peeeq.wurstscript.ast.GlobalVarDef[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.GlobalVarDef element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort JassGlobalBlock invoked with null element");

            JassGlobalBlock.Impl temp = new de.peeeq.wurstscript.ast.JassGlobalBlock.Impl();
            temp.values = elements;
            temp = (JassGlobalBlock.Impl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.GlobalVarDef[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.GlobalVarDef[size];
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock add(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.add(element);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock addAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.addAll(list);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock remove(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.remove(element);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.removeAll(list);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock appBack(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.appBack(element);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock appFront(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.appFront(element);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock conc(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.conc(list);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock front() {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.front();
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock back() {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.back();
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock reverse() {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.reverse();
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock sublist(int from, int to) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.sublist(from, to);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock replace(int ith, Object element) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.replace(ith, element);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock toSet() {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.toSet();
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock setAdd(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.setAdd(element);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock setRemove(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.setRemove(element);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.setUnion(list);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.setIntersection(list);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlock) super.setWithout(list);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.JassToplevelDeclaration.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseJassGlobalBlock(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TopLevelDeclaration.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseJassGlobalBlock(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("AST.JassGlobalBlock");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.GlobalVarDef element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("JassGlobalBlock");
            builder.append("( ");
            for(de.peeeq.wurstscript.ast.GlobalVarDef element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "JassGlobalBlock";
        }
    }
}

