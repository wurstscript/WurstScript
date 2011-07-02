package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface AClassMembersPos extends de.peeeq.pscript.ast.pscriptAST.ListPos<de.peeeq.pscript.ast.AClassMembers, de.peeeq.pscript.ast.AClassMemberPos, de.peeeq.pscript.ast.AClassMember> {

    //----- methods of AClassMembersPos -----

    public de.peeeq.pscript.ast.AClassMembers term();
    public de.peeeq.pscript.ast.AClassMembersPos add(de.peeeq.pscript.ast.AClassMember element);
    public de.peeeq.pscript.ast.AClassMembersPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list);
    public de.peeeq.pscript.ast.AClassMembersPos remove(de.peeeq.pscript.ast.AClassMember element);
    public de.peeeq.pscript.ast.AClassMembersPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list);
    public de.peeeq.pscript.ast.AClassMembersPos appBack(de.peeeq.pscript.ast.AClassMember element);
    public de.peeeq.pscript.ast.AClassMembersPos appFront(de.peeeq.pscript.ast.AClassMember element);
    public de.peeeq.pscript.ast.AClassMembersPos conc(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list);
    public de.peeeq.pscript.ast.AClassMembersPos reverse();
    public de.peeeq.pscript.ast.AClassMembersPos toSet();
    public de.peeeq.pscript.ast.AClassMembersPos setAdd(de.peeeq.pscript.ast.AClassMember element);
    public de.peeeq.pscript.ast.AClassMembersPos setRemove(de.peeeq.pscript.ast.AClassMember element);
    public de.peeeq.pscript.ast.AClassMembersPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list);
    public de.peeeq.pscript.ast.AClassMembersPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list);
    public de.peeeq.pscript.ast.AClassMembersPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list);
    public KatjaList<de.peeeq.pscript.ast.AClassMemberPos> toList();
    public de.peeeq.pscript.ast.AClassMembersPos replace(de.peeeq.pscript.ast.AClassMembers term);
    public de.peeeq.pscript.ast.AClassDefPos parent();
    public de.peeeq.pscript.ast.StringPos lsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);

    //----- nested classes of AClassMembersPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AClassMemberPos> implements KatjaList<de.peeeq.pscript.ast.AClassMemberPos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.pscript.ast.AClassMemberPos... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.AClassMemberPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AClassMemberPos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.pscript.ast.AClassMemberPos> createInstance(de.peeeq.pscript.ast.AClassMemberPos[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.AClassMemberPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort AClassMemberPos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.AClassMemberPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AClassMemberPos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.pscript.ast.AClassMemberPos element : values) {
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

        public void visit(de.peeeq.pscript.ast.AClassMemberPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVarDefPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFuncDefPos term) throws E;
        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BooleanPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnTypePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParametersPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ABlockPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameterPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AStatementPos term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AArgumentsPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifierPos term) throws E;
        public void visit(de.peeeq.pscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BigDecimalPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIfPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AWhilePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVoidReturnPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AAssignmentPos term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AClassMembersPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AClassMembersPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AClassMemberPos.Switch<Object, E> variantVisit$AClassMemberPos = new de.peeeq.pscript.ast.AClassMemberPos.Switch<Object, E>() { public final Object CaseAVarDefPos(de.peeeq.pscript.ast.AVarDefPos term) throws E { visit(term); return null; } public final Object CaseAFuncDefPos(de.peeeq.pscript.ast.AFuncDefPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AExprPos.Switch<Object, E> variantVisit$AExprPos = new de.peeeq.pscript.ast.AExprPos.Switch<Object, E>() { public final Object CaseAPrefixPos(de.peeeq.pscript.ast.APrefixPos term) throws E { visit(term); return null; } public final Object CaseAInfixPos(de.peeeq.pscript.ast.AInfixPos term) throws E { visit(term); return null; } public final Object CaseAFunctionCallPos(de.peeeq.pscript.ast.AFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseABuildinCallPos(de.peeeq.pscript.ast.ABuildinCallPos term) throws E { visit(term); return null; } public final Object CaseAVariableAccessPos(de.peeeq.pscript.ast.AVariableAccessPos term) throws E { visit(term); return null; } public final Object CaseAFieldAccessPos(de.peeeq.pscript.ast.AFieldAccessPos term) throws E { visit(term); return null; } public final Object CaseANoExprPos(de.peeeq.pscript.ast.ANoExprPos term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E> variantVisit$AInfixOpPos = new de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E>() { public final Object CaseAEqEqPos(de.peeeq.pscript.ast.AEqEqPos term) throws E { visit(term); return null; } public final Object CaseAGtPos(de.peeeq.pscript.ast.AGtPos term) throws E { visit(term); return null; } public final Object CaseAGtEqPos(de.peeeq.pscript.ast.AGtEqPos term) throws E { visit(term); return null; } public final Object CaseALtPos(de.peeeq.pscript.ast.ALtPos term) throws E { visit(term); return null; } public final Object CaseALtEqPos(de.peeeq.pscript.ast.ALtEqPos term) throws E { visit(term); return null; } public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseAMultPos(de.peeeq.pscript.ast.AMultPos term) throws E { visit(term); return null; } public final Object CaseADivPos(de.peeeq.pscript.ast.ADivPos term) throws E { visit(term); return null; } public final Object CaseADivIntPos(de.peeeq.pscript.ast.ADivIntPos term) throws E { visit(term); return null; } public final Object CaseAModuloPos(de.peeeq.pscript.ast.AModuloPos term) throws E { visit(term); return null; } public final Object CaseADotPos(de.peeeq.pscript.ast.ADotPos term) throws E { visit(term); return null; } public final Object CaseAAndPos(de.peeeq.pscript.ast.AAndPos term) throws E { visit(term); return null; } public final Object CaseAOrPos(de.peeeq.pscript.ast.AOrPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E> variantVisit$APrefixOpPos = new de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E>() { public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseANotPos(de.peeeq.pscript.ast.ANotPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AReturnTypePos.Switch<Object, E> variantVisit$AReturnTypePos = new de.peeeq.pscript.ast.AReturnTypePos.Switch<Object, E>() { public final Object CaseAReturnsNothingPos(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E { visit(term); return null; } public final Object CaseATypeExprSimplePos(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AStatementPos.Switch<Object, E> variantVisit$AStatementPos = new de.peeeq.pscript.ast.AStatementPos.Switch<Object, E>() { public final Object CaseABlockPos(de.peeeq.pscript.ast.ABlockPos term) throws E { visit(term); return null; } public final Object CaseAIfPos(de.peeeq.pscript.ast.AIfPos term) throws E { visit(term); return null; } public final Object CaseAWhilePos(de.peeeq.pscript.ast.AWhilePos term) throws E { visit(term); return null; } public final Object CaseAReturnPos(de.peeeq.pscript.ast.AReturnPos term) throws E { visit(term); return null; } public final Object CaseAVoidReturnPos(de.peeeq.pscript.ast.AVoidReturnPos term) throws E { visit(term); return null; } public final Object CaseAAssignmentPos(de.peeeq.pscript.ast.AAssignmentPos term) throws E { visit(term); return null; } public final Object CaseAPrefixPos(de.peeeq.pscript.ast.APrefixPos term) throws E { visit(term); return null; } public final Object CaseAInfixPos(de.peeeq.pscript.ast.AInfixPos term) throws E { visit(term); return null; } public final Object CaseAFunctionCallPos(de.peeeq.pscript.ast.AFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseABuildinCallPos(de.peeeq.pscript.ast.ABuildinCallPos term) throws E { visit(term); return null; } public final Object CaseAVariableAccessPos(de.peeeq.pscript.ast.AVariableAccessPos term) throws E { visit(term); return null; } public final Object CaseAFieldAccessPos(de.peeeq.pscript.ast.AFieldAccessPos term) throws E { visit(term); return null; } public final Object CaseANoExprPos(de.peeeq.pscript.ast.ANoExprPos term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.ATypeExprPos.Switch<Object, E> variantVisit$ATypeExprPos = new de.peeeq.pscript.ast.ATypeExprPos.Switch<Object, E>() { public final Object CaseATypeExprSimplePos(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AClassMemberPos term) throws E {
            term.Switch(variantVisit$AClassMemberPos);
        }

        public final void visit(de.peeeq.pscript.ast.AExprPos term) throws E {
            term.Switch(variantVisit$AStatementPos);
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

        public final void visit(de.peeeq.pscript.ast.AReturnTypePos term) throws E {
            term.Switch(variantVisit$AReturnTypePos);
        }

        public final void visit(de.peeeq.pscript.ast.AStatementPos term) throws E {
            term.Switch(variantVisit$AStatementPos);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E {
            term.Switch(variantVisit$ATypeExprPos);
        }
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AClassMembers, de.peeeq.pscript.ast.AClassMemberPos, de.peeeq.pscript.ast.AClassMember> implements de.peeeq.pscript.ast.AClassMembersPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AClassMembers term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.pscript.ast.AClassMemberPos getElementInstance(int pos) {
            return de.peeeq.pscript.ast.pscriptAST.AClassMemberPos(this, _term.get(pos), pos);
        }

        protected de.peeeq.pscript.ast.AClassMemberPos[] createArray(int size) {
            return new de.peeeq.pscript.ast.AClassMemberPos[size];
        }

        public de.peeeq.pscript.ast.AClassMembersPos add(de.peeeq.pscript.ast.AClassMember element) {
            return replace(_term.add(element));
        }

        public de.peeeq.pscript.ast.AClassMembersPos addAll(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.pscript.ast.AClassMembersPos remove(de.peeeq.pscript.ast.AClassMember element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.pscript.ast.AClassMembersPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.pscript.ast.AClassMembersPos appBack(de.peeeq.pscript.ast.AClassMember element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.pscript.ast.AClassMembersPos appFront(de.peeeq.pscript.ast.AClassMember element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.pscript.ast.AClassMembersPos conc(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.pscript.ast.AClassMembersPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.pscript.ast.AClassMembersPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.pscript.ast.AClassMembersPos setAdd(de.peeeq.pscript.ast.AClassMember element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.pscript.ast.AClassMembersPos setRemove(de.peeeq.pscript.ast.AClassMember element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.pscript.ast.AClassMembersPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.pscript.ast.AClassMembersPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.pscript.ast.AClassMembersPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.AClassMember> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.pscript.ast.AClassMemberPos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new AClassMembersPos.ListImpl(values);
        }

        public de.peeeq.pscript.ast.AClassMembersPos replace(de.peeeq.pscript.ast.AClassMembers term) {
            return (de.peeeq.pscript.ast.AClassMembersPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AClassDefPos parent() {
            return (de.peeeq.pscript.ast.AClassDefPos) super.parent();
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
            return "AClassMembersPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.AClassMemberPos> implements KatjaList<de.peeeq.pscript.ast.AClassMemberPos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.pscript.ast.AClassMemberPos... elements) {
                super(elements);

                for(de.peeeq.pscript.ast.AClassMemberPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AClassMemberPos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.pscript.ast.AClassMemberPos> createInstance(de.peeeq.pscript.ast.AClassMemberPos[] elements, boolean isSet) {
                for(de.peeeq.pscript.ast.AClassMemberPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort AClassMemberPos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) pscriptAST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.pscript.ast.AClassMemberPos[] createArray(int size) {
                return new de.peeeq.pscript.ast.AClassMemberPos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.pscript.ast.AClassMemberPos element : values) {
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

