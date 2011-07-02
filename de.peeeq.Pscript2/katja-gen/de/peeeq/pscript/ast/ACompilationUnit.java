package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ACompilationUnit extends KatjaList<de.peeeq.pscript.ast.APackage> {

    //----- methods of ACompilationUnit -----

    public de.peeeq.pscript.ast.ACompilationUnit add(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnit addAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnit remove(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnit removeAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnit appBack(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnit appFront(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnit conc(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnit front();
    public de.peeeq.pscript.ast.ACompilationUnit back();
    public de.peeeq.pscript.ast.ACompilationUnit reverse();
    public de.peeeq.pscript.ast.ACompilationUnit sublist(int from, int to);
    public de.peeeq.pscript.ast.ACompilationUnit replace(int ith, Object element);
    public de.peeeq.pscript.ast.ACompilationUnit toSet();
    public de.peeeq.pscript.ast.ACompilationUnit setAdd(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnit setRemove(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnit setUnion(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnit setIntersection(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnit setWithout(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);

    //----- nested classes of ACompilationUnit -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.APackage term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AImports term) throws E;
        public void visit(de.peeeq.pscript.ast.AElements term) throws E;
        public void visit(de.peeeq.pscript.ast.AImport term) throws E;
        public void visit(de.peeeq.pscript.ast.AElement term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeDef term) throws E;
        public void visit(de.peeeq.pscript.ast.AFuncDef term) throws E;
        public void visit(de.peeeq.pscript.ast.AVarDef term) throws E;
        public void visit(de.peeeq.pscript.ast.AInitBlock term) throws E;
        public void visit(de.peeeq.pscript.ast.ANativeType term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassDef term) throws E;
        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnType term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameters term) throws E;
        public void visit(de.peeeq.pscript.ast.ABlock term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.pscript.ast.AExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassMembers term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnsNothing term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AFormalParameter term) throws E;
        public void visit(de.peeeq.pscript.ast.AStatement term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassMember term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AArguments term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifier term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.math.BigDecimal term) throws E;
        public void visit(de.peeeq.pscript.ast.AIf term) throws E;
        public void visit(de.peeeq.pscript.ast.AWhile term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturn term) throws E;
        public void visit(de.peeeq.pscript.ast.AVoidReturn term) throws E;
        public void visit(de.peeeq.pscript.ast.AAssignment term) throws E;
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
        public void visit(de.peeeq.pscript.ast.ACompilationUnit term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ACompilationUnit.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AClassMember.Switch<Object, E> variantVisit$AClassMember = new de.peeeq.pscript.ast.AClassMember.Switch<Object, E>() { public final Object CaseAVarDef(de.peeeq.pscript.ast.AVarDef term) throws E { visit(term); return null; } public final Object CaseAFuncDef(de.peeeq.pscript.ast.AFuncDef term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AElement.Switch<Object, E> variantVisit$AElement = new de.peeeq.pscript.ast.AElement.Switch<Object, E>() { public final Object CaseAFuncDef(de.peeeq.pscript.ast.AFuncDef term) throws E { visit(term); return null; } public final Object CaseAVarDef(de.peeeq.pscript.ast.AVarDef term) throws E { visit(term); return null; } public final Object CaseAInitBlock(de.peeeq.pscript.ast.AInitBlock term) throws E { visit(term); return null; } public final Object CaseANativeType(de.peeeq.pscript.ast.ANativeType term) throws E { visit(term); return null; } public final Object CaseAClassDef(de.peeeq.pscript.ast.AClassDef term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AExpr.Switch<Object, E> variantVisit$AExpr = new de.peeeq.pscript.ast.AExpr.Switch<Object, E>() { public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOp.Switch<Object, E> variantVisit$AInfixOp = new de.peeeq.pscript.ast.AInfixOp.Switch<Object, E>() { public final Object CaseAEqEq(de.peeeq.pscript.ast.AEqEq term) throws E { visit(term); return null; } public final Object CaseAGt(de.peeeq.pscript.ast.AGt term) throws E { visit(term); return null; } public final Object CaseAGtEq(de.peeeq.pscript.ast.AGtEq term) throws E { visit(term); return null; } public final Object CaseALt(de.peeeq.pscript.ast.ALt term) throws E { visit(term); return null; } public final Object CaseALtEq(de.peeeq.pscript.ast.ALtEq term) throws E { visit(term); return null; } public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseAMult(de.peeeq.pscript.ast.AMult term) throws E { visit(term); return null; } public final Object CaseADiv(de.peeeq.pscript.ast.ADiv term) throws E { visit(term); return null; } public final Object CaseADivInt(de.peeeq.pscript.ast.ADivInt term) throws E { visit(term); return null; } public final Object CaseAModulo(de.peeeq.pscript.ast.AModulo term) throws E { visit(term); return null; } public final Object CaseADot(de.peeeq.pscript.ast.ADot term) throws E { visit(term); return null; } public final Object CaseAAnd(de.peeeq.pscript.ast.AAnd term) throws E { visit(term); return null; } public final Object CaseAOr(de.peeeq.pscript.ast.AOr term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOp.Switch<Object, E> variantVisit$APrefixOp = new de.peeeq.pscript.ast.APrefixOp.Switch<Object, E>() { public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseANot(de.peeeq.pscript.ast.ANot term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AReturnType.Switch<Object, E> variantVisit$AReturnType = new de.peeeq.pscript.ast.AReturnType.Switch<Object, E>() { public final Object CaseAReturnsNothing(de.peeeq.pscript.ast.AReturnsNothing term) throws E { visit(term); return null; } public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AStatement.Switch<Object, E> variantVisit$AStatement = new de.peeeq.pscript.ast.AStatement.Switch<Object, E>() { public final Object CaseABlock(de.peeeq.pscript.ast.ABlock term) throws E { visit(term); return null; } public final Object CaseAIf(de.peeeq.pscript.ast.AIf term) throws E { visit(term); return null; } public final Object CaseAWhile(de.peeeq.pscript.ast.AWhile term) throws E { visit(term); return null; } public final Object CaseAReturn(de.peeeq.pscript.ast.AReturn term) throws E { visit(term); return null; } public final Object CaseAVoidReturn(de.peeeq.pscript.ast.AVoidReturn term) throws E { visit(term); return null; } public final Object CaseAAssignment(de.peeeq.pscript.ast.AAssignment term) throws E { visit(term); return null; } public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AClassMember term) throws E {
            term.Switch(variantVisit$AClassMember);
        }

        public final void visit(de.peeeq.pscript.ast.AElement term) throws E {
            term.Switch(variantVisit$AElement);
        }

        public final void visit(de.peeeq.pscript.ast.AExpr term) throws E {
            term.Switch(variantVisit$AStatement);
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

        public final void visit(de.peeeq.pscript.ast.AReturnType term) throws E {
            term.Switch(variantVisit$AReturnType);
        }

        public final void visit(de.peeeq.pscript.ast.AStatement term) throws E {
            term.Switch(variantVisit$AStatement);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeDef term) throws E {
            term.Switch(variantVisit$AElement);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$AReturnType);
        }
    }

    static class Impl extends KatjaListImpl<de.peeeq.pscript.ast.APackage> implements de.peeeq.pscript.ast.ACompilationUnit {

        //----- methods of Impl -----

        Impl(de.peeeq.pscript.ast.APackage... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.APackage element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort ACompilationUnit invoked with null element");
        }

        private Impl() {
        }

        protected de.peeeq.pscript.ast.ACompilationUnit createInstance(de.peeeq.pscript.ast.APackage[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.APackage element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of sort ACompilationUnit invoked with null element");

            ACompilationUnit.Impl temp = new de.peeeq.pscript.ast.ACompilationUnit.Impl();
            temp.values = elements;
            temp = (ACompilationUnit.Impl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.APackage[] createArray(int size) {
            return new de.peeeq.pscript.ast.APackage[size];
        }

        public de.peeeq.pscript.ast.ACompilationUnit add(de.peeeq.pscript.ast.APackage element) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.add(element);
        }

        public de.peeeq.pscript.ast.ACompilationUnit addAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.addAll(list);
        }

        public de.peeeq.pscript.ast.ACompilationUnit remove(de.peeeq.pscript.ast.APackage element) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.remove(element);
        }

        public de.peeeq.pscript.ast.ACompilationUnit removeAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.removeAll(list);
        }

        public de.peeeq.pscript.ast.ACompilationUnit appBack(de.peeeq.pscript.ast.APackage element) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.appBack(element);
        }

        public de.peeeq.pscript.ast.ACompilationUnit appFront(de.peeeq.pscript.ast.APackage element) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.appFront(element);
        }

        public de.peeeq.pscript.ast.ACompilationUnit conc(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.conc(list);
        }

        public de.peeeq.pscript.ast.ACompilationUnit front() {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.front();
        }

        public de.peeeq.pscript.ast.ACompilationUnit back() {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.back();
        }

        public de.peeeq.pscript.ast.ACompilationUnit reverse() {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.reverse();
        }

        public de.peeeq.pscript.ast.ACompilationUnit sublist(int from, int to) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.sublist(from, to);
        }

        public de.peeeq.pscript.ast.ACompilationUnit replace(int ith, Object element) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.replace(ith, element);
        }

        public de.peeeq.pscript.ast.ACompilationUnit toSet() {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.toSet();
        }

        public de.peeeq.pscript.ast.ACompilationUnit setAdd(de.peeeq.pscript.ast.APackage element) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.setAdd(element);
        }

        public de.peeeq.pscript.ast.ACompilationUnit setRemove(de.peeeq.pscript.ast.APackage element) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.setRemove(element);
        }

        public de.peeeq.pscript.ast.ACompilationUnit setUnion(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.setUnion(list);
        }

        public de.peeeq.pscript.ast.ACompilationUnit setIntersection(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.setIntersection(list);
        }

        public de.peeeq.pscript.ast.ACompilationUnit setWithout(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return (de.peeeq.pscript.ast.ACompilationUnit) super.setWithout(list);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("pscriptAST.ACompilationUnit");
            builder.append("( ");
            for(de.peeeq.pscript.ast.APackage element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toJavaCode(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("ACompilationUnit");
            builder.append("( ");
            for(de.peeeq.pscript.ast.APackage element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ACompilationUnit";
        }
    }
}

