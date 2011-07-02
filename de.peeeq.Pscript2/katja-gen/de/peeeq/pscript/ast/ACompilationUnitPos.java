package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface ACompilationUnitPos extends de.peeeq.pscript.ast.pscriptAST.ListPos<de.peeeq.pscript.ast.ACompilationUnit, de.peeeq.pscript.ast.APackagePos, de.peeeq.pscript.ast.APackage> {

    //----- methods of ACompilationUnitPos -----

    public de.peeeq.pscript.ast.ACompilationUnit term();
    public de.peeeq.pscript.ast.ACompilationUnitPos add(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnitPos addAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnitPos remove(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnitPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnitPos appBack(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnitPos appFront(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnitPos conc(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnitPos reverse();
    public de.peeeq.pscript.ast.ACompilationUnitPos toSet();
    public de.peeeq.pscript.ast.ACompilationUnitPos setAdd(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnitPos setRemove(de.peeeq.pscript.ast.APackage element);
    public de.peeeq.pscript.ast.ACompilationUnitPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnitPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public de.peeeq.pscript.ast.ACompilationUnitPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.APackage> list);
    public KatjaList<de.peeeq.pscript.ast.APackagePos> toList();
    public de.peeeq.pscript.ast.ACompilationUnitPos replace(de.peeeq.pscript.ast.ACompilationUnit term);
    public de.peeeq.pscript.ast.pscriptAST.SortPos parent();
    public de.peeeq.pscript.ast.pscriptAST.SortPos lsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);

    //----- nested classes of ACompilationUnitPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.APackagePos> implements KatjaList<de.peeeq.pscript.ast.APackagePos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.pscript.ast.APackagePos... elements) {
            super(elements);

            for(de.peeeq.pscript.ast.APackagePos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort APackagePos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.pscript.ast.APackagePos> createInstance(de.peeeq.pscript.ast.APackagePos[] elements, boolean isSet) {
            for(de.peeeq.pscript.ast.APackagePos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort APackagePos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) pscriptAST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.pscript.ast.APackagePos[] createArray(int size) {
            return new de.peeeq.pscript.ast.APackagePos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.pscript.ast.APackagePos element : values) {
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

        public void visit(de.peeeq.pscript.ast.APackagePos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AImportsPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AElementsPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AImportPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AElementPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeDefPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFuncDefPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVarDefPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AInitBlockPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ANativeTypePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassDefPos term) throws E;
        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnTypePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParametersPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ABlockPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BooleanPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassMembersPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AFormalParameterPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AStatementPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassMemberPos term) throws E;
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
        public void visit(de.peeeq.pscript.ast.ACompilationUnitPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ACompilationUnitPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AClassMemberPos.Switch<Object, E> variantVisit$AClassMemberPos = new de.peeeq.pscript.ast.AClassMemberPos.Switch<Object, E>() { public final Object CaseAVarDefPos(de.peeeq.pscript.ast.AVarDefPos term) throws E { visit(term); return null; } public final Object CaseAFuncDefPos(de.peeeq.pscript.ast.AFuncDefPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AElementPos.Switch<Object, E> variantVisit$AElementPos = new de.peeeq.pscript.ast.AElementPos.Switch<Object, E>() { public final Object CaseAFuncDefPos(de.peeeq.pscript.ast.AFuncDefPos term) throws E { visit(term); return null; } public final Object CaseAVarDefPos(de.peeeq.pscript.ast.AVarDefPos term) throws E { visit(term); return null; } public final Object CaseAInitBlockPos(de.peeeq.pscript.ast.AInitBlockPos term) throws E { visit(term); return null; } public final Object CaseANativeTypePos(de.peeeq.pscript.ast.ANativeTypePos term) throws E { visit(term); return null; } public final Object CaseAClassDefPos(de.peeeq.pscript.ast.AClassDefPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AExprPos.Switch<Object, E> variantVisit$AExprPos = new de.peeeq.pscript.ast.AExprPos.Switch<Object, E>() { public final Object CaseAPrefixPos(de.peeeq.pscript.ast.APrefixPos term) throws E { visit(term); return null; } public final Object CaseAInfixPos(de.peeeq.pscript.ast.AInfixPos term) throws E { visit(term); return null; } public final Object CaseAFunctionCallPos(de.peeeq.pscript.ast.AFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseABuildinCallPos(de.peeeq.pscript.ast.ABuildinCallPos term) throws E { visit(term); return null; } public final Object CaseAVariableAccessPos(de.peeeq.pscript.ast.AVariableAccessPos term) throws E { visit(term); return null; } public final Object CaseAFieldAccessPos(de.peeeq.pscript.ast.AFieldAccessPos term) throws E { visit(term); return null; } public final Object CaseANoExprPos(de.peeeq.pscript.ast.ANoExprPos term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E> variantVisit$AInfixOpPos = new de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E>() { public final Object CaseAEqEqPos(de.peeeq.pscript.ast.AEqEqPos term) throws E { visit(term); return null; } public final Object CaseAGtPos(de.peeeq.pscript.ast.AGtPos term) throws E { visit(term); return null; } public final Object CaseAGtEqPos(de.peeeq.pscript.ast.AGtEqPos term) throws E { visit(term); return null; } public final Object CaseALtPos(de.peeeq.pscript.ast.ALtPos term) throws E { visit(term); return null; } public final Object CaseALtEqPos(de.peeeq.pscript.ast.ALtEqPos term) throws E { visit(term); return null; } public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseAMultPos(de.peeeq.pscript.ast.AMultPos term) throws E { visit(term); return null; } public final Object CaseADivPos(de.peeeq.pscript.ast.ADivPos term) throws E { visit(term); return null; } public final Object CaseADivIntPos(de.peeeq.pscript.ast.ADivIntPos term) throws E { visit(term); return null; } public final Object CaseAModuloPos(de.peeeq.pscript.ast.AModuloPos term) throws E { visit(term); return null; } public final Object CaseADotPos(de.peeeq.pscript.ast.ADotPos term) throws E { visit(term); return null; } public final Object CaseAAndPos(de.peeeq.pscript.ast.AAndPos term) throws E { visit(term); return null; } public final Object CaseAOrPos(de.peeeq.pscript.ast.AOrPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E> variantVisit$APrefixOpPos = new de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E>() { public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseANotPos(de.peeeq.pscript.ast.ANotPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AReturnTypePos.Switch<Object, E> variantVisit$AReturnTypePos = new de.peeeq.pscript.ast.AReturnTypePos.Switch<Object, E>() { public final Object CaseAReturnsNothingPos(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E { visit(term); return null; } public final Object CaseATypeExprSimplePos(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AStatementPos.Switch<Object, E> variantVisit$AStatementPos = new de.peeeq.pscript.ast.AStatementPos.Switch<Object, E>() { public final Object CaseABlockPos(de.peeeq.pscript.ast.ABlockPos term) throws E { visit(term); return null; } public final Object CaseAIfPos(de.peeeq.pscript.ast.AIfPos term) throws E { visit(term); return null; } public final Object CaseAWhilePos(de.peeeq.pscript.ast.AWhilePos term) throws E { visit(term); return null; } public final Object CaseAReturnPos(de.peeeq.pscript.ast.AReturnPos term) throws E { visit(term); return null; } public final Object CaseAVoidReturnPos(de.peeeq.pscript.ast.AVoidReturnPos term) throws E { visit(term); return null; } public final Object CaseAAssignmentPos(de.peeeq.pscript.ast.AAssignmentPos term) throws E { visit(term); return null; } public final Object CaseAPrefixPos(de.peeeq.pscript.ast.APrefixPos term) throws E { visit(term); return null; } public final Object CaseAInfixPos(de.peeeq.pscript.ast.AInfixPos term) throws E { visit(term); return null; } public final Object CaseAFunctionCallPos(de.peeeq.pscript.ast.AFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseABuildinCallPos(de.peeeq.pscript.ast.ABuildinCallPos term) throws E { visit(term); return null; } public final Object CaseAVariableAccessPos(de.peeeq.pscript.ast.AVariableAccessPos term) throws E { visit(term); return null; } public final Object CaseAFieldAccessPos(de.peeeq.pscript.ast.AFieldAccessPos term) throws E { visit(term); return null; } public final Object CaseANoExprPos(de.peeeq.pscript.ast.ANoExprPos term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AClassMemberPos term) throws E {
            term.Switch(variantVisit$AClassMemberPos);
        }

        public final void visit(de.peeeq.pscript.ast.AElementPos term) throws E {
            term.Switch(variantVisit$AElementPos);
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

        public final void visit(de.peeeq.pscript.ast.ATypeDefPos term) throws E {
            term.Switch(variantVisit$AElementPos);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E {
            term.Switch(variantVisit$AReturnTypePos);
        }
    }

    public static abstract class DefaultVisitor<E extends Throwable> extends de.peeeq.pscript.ast.ACompilationUnitPos.Visitor<E> {

        //----- methods of DefaultVisitor<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.APackagePos term) throws E {
            visit(term.name());
            visit(term.imports());
            visit(term.elements());
        }

        public void visit(de.peeeq.pscript.ast.StringPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AImportsPos term) throws E {
            for(de.peeeq.pscript.ast.AImportPos element : term)
                visit(element);
        }

        public void visit(de.peeeq.pscript.ast.AElementsPos term) throws E {
            for(de.peeeq.pscript.ast.AElementPos element : term)
                visit(element);
        }

        public void visit(de.peeeq.pscript.ast.AImportPos term) throws E {
            visit(term.source());
            visit(term.name());
        }

        public void visit(de.peeeq.pscript.ast.AFuncDefPos term) throws E {
            visit(term.source());
            visit(term.name());
            visit(term.retType());
            visit(term.params());
            visit(term.body());
        }

        public void visit(de.peeeq.pscript.ast.AVarDefPos term) throws E {
            visit(term.source());
            visit(term.name());
            visit(term.typeExpr());
            visit(term.constant());
            visit(term.initial());
        }

        public void visit(de.peeeq.pscript.ast.AInitBlockPos term) throws E {
            visit(term.source());
            visit(term.body());
        }

        public void visit(de.peeeq.pscript.ast.ANativeTypePos term) throws E {
            visit(term.source());
            visit(term.name());
            visit(term.origName());
            visit(term.superName());
        }

        public void visit(de.peeeq.pscript.ast.AClassDefPos term) throws E {
            visit(term.source());
            visit(term.name());
            visit(term.members());
        }

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AFormalParametersPos term) throws E {
            for(de.peeeq.pscript.ast.AFormalParameterPos element : term)
                visit(element);
        }

        public void visit(de.peeeq.pscript.ast.ABlockPos term) throws E {
            for(de.peeeq.pscript.ast.AStatementPos element : term)
                visit(element);
        }

        public void visit(de.peeeq.pscript.ast.BooleanPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AClassMembersPos term) throws E {
            for(de.peeeq.pscript.ast.AClassMemberPos element : term)
                visit(element);
        }

        public void visit(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ATypeExprSimplePos term) throws E {
            visit(term.name());
        }

        public void visit(de.peeeq.pscript.ast.APrefixPos term) throws E {
            visit(term.source());
            visit(term.op());
            visit(term.e());
        }

        public void visit(de.peeeq.pscript.ast.AInfixPos term) throws E {
            visit(term.source());
            visit(term.left());
            visit(term.op());
            visit(term.right());
        }

        public void visit(de.peeeq.pscript.ast.AFunctionCallPos term) throws E {
            visit(term.source());
            visit(term.name());
            visit(term.args());
        }

        public void visit(de.peeeq.pscript.ast.ABuildinCallPos term) throws E {
            visit(term.source());
            visit(term.name());
            visit(term.args());
        }

        public void visit(de.peeeq.pscript.ast.AVariableAccessPos term) throws E {
            visit(term.source());
            visit(term.ident());
        }

        public void visit(de.peeeq.pscript.ast.AFieldAccessPos term) throws E {
            visit(term.source());
            visit(term.owner());
            visit(term.ident());
        }

        public void visit(de.peeeq.pscript.ast.ANoExprPos term) throws E {
            visit(term.source());
        }

        public void visit(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E {
            visit(term.source());
            visit(term.value());
        }

        public void visit(de.peeeq.pscript.ast.ARealLiteralPos term) throws E {
            visit(term.source());
            visit(term.value());
        }

        public void visit(de.peeeq.pscript.ast.AStringLiteralPos term) throws E {
            visit(term.source());
            visit(term.value());
        }

        public void visit(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E {
            visit(term.source());
            visit(term.value());
        }

        public void visit(de.peeeq.pscript.ast.AFormalParameterPos term) throws E {
            visit(term.source());
            visit(term.name());
            visit(term.type());
        }

        public void visit(de.peeeq.pscript.ast.AArgumentsPos term) throws E {
            for(de.peeeq.pscript.ast.AExprPos element : term)
                visit(element);
        }

        public void visit(de.peeeq.pscript.ast.AIdentifierPos term) throws E {
            visit(term.source());
            visit(term.name());
        }

        public void visit(de.peeeq.pscript.ast.IntegerPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.BigDecimalPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AIfPos term) throws E {
            visit(term.source());
            visit(term.cond());
            visit(term.thenBlock());
            visit(term.elseBlock());
        }

        public void visit(de.peeeq.pscript.ast.AWhilePos term) throws E {
            visit(term.source());
            visit(term.cond());
            visit(term.body());
        }

        public void visit(de.peeeq.pscript.ast.AReturnPos term) throws E {
            visit(term.source());
            visit(term.retValue());
        }

        public void visit(de.peeeq.pscript.ast.AVoidReturnPos term) throws E {
            visit(term.source());
        }

        public void visit(de.peeeq.pscript.ast.AAssignmentPos term) throws E {
            visit(term.source());
            visit(term.left());
            visit(term.right());
        }

        public void visit(de.peeeq.pscript.ast.APlusPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AMinusPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ANotPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AEqEqPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AGtPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AGtEqPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ALtPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ALtEqPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AMultPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ADivPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ADivIntPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AModuloPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ADotPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AAndPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.AOrPos term) throws E {
        }

        public void visit(de.peeeq.pscript.ast.ACompilationUnitPos term) throws E {
            for(de.peeeq.pscript.ast.APackagePos element : term)
                visit(element);
        }
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.ACompilationUnit, de.peeeq.pscript.ast.APackagePos, de.peeeq.pscript.ast.APackage> implements de.peeeq.pscript.ast.ACompilationUnitPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.ACompilationUnit term, int pos) {
            super(parent, term, pos);
        }

        Impl(de.peeeq.pscript.ast.ACompilationUnit term) {
            super(term);
        }

        protected de.peeeq.pscript.ast.APackagePos getElementInstance(int pos) {
            return de.peeeq.pscript.ast.pscriptAST.APackagePos(this, _term.get(pos), pos);
        }

        protected de.peeeq.pscript.ast.APackagePos[] createArray(int size) {
            return new de.peeeq.pscript.ast.APackagePos[size];
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos add(de.peeeq.pscript.ast.APackage element) {
            return replace(_term.add(element));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos addAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos remove(de.peeeq.pscript.ast.APackage element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos removeAll(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos appBack(de.peeeq.pscript.ast.APackage element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos appFront(de.peeeq.pscript.ast.APackage element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos conc(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos setAdd(de.peeeq.pscript.ast.APackage element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos setRemove(de.peeeq.pscript.ast.APackage element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos setUnion(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos setWithout(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos setIntersection(KatjaList<? extends de.peeeq.pscript.ast.APackage> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.pscript.ast.APackagePos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new ACompilationUnitPos.ListImpl(values);
        }

        public de.peeeq.pscript.ast.ACompilationUnitPos replace(de.peeeq.pscript.ast.ACompilationUnit term) {
            return (de.peeeq.pscript.ast.ACompilationUnitPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos parent() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.parent();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos lsib() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.lsib();
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
            return "ACompilationUnitPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.pscript.ast.APackagePos> implements KatjaList<de.peeeq.pscript.ast.APackagePos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.pscript.ast.APackagePos... elements) {
                super(elements);

                for(de.peeeq.pscript.ast.APackagePos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort APackagePos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.pscript.ast.APackagePos> createInstance(de.peeeq.pscript.ast.APackagePos[] elements, boolean isSet) {
                for(de.peeeq.pscript.ast.APackagePos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort APackagePos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) pscriptAST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.pscript.ast.APackagePos[] createArray(int size) {
                return new de.peeeq.pscript.ast.APackagePos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.pscript.ast.APackagePos element : values) {
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

