package de.peeeq.pscript.ast;

import katja.common.*;

public interface AClassMemberPos extends de.peeeq.pscript.ast.pscriptAST.SortPos {

    //----- methods of AClassMemberPos -----

    public KatjaSort term();
    public de.peeeq.pscript.ast.AClassMember termAClassMember();
    public de.peeeq.pscript.ast.EObjectPos source();
    public de.peeeq.pscript.ast.StringPos name();
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AClassMemberPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AClassMemberPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseAVarDefPos(de.peeeq.pscript.ast.AVarDefPos term) throws E;
        public CT CaseAFuncDefPos(de.peeeq.pscript.ast.AFuncDefPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BooleanPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AExprPos term) throws E;
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
        public void visit(de.peeeq.pscript.ast.APrefixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOpPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AArgumentsPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifierPos term) throws E;
        public void visit(de.peeeq.pscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.pscript.ast.BigDecimalPos term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AReturnTypePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParametersPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ABlockPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnsNothingPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameterPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AStatementPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIfPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AWhilePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVoidReturnPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AAssignmentPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassMemberPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVarDefPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AFuncDefPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AClassMemberPos.VisitorType<E> {

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
}

