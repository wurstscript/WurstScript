package de.peeeq.pscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface AInitBlockPos extends de.peeeq.pscript.ast.AElementPos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.AInitBlock> {

    //----- methods of AInitBlockPos -----

    public de.peeeq.pscript.ast.AInitBlock termAElement();
    public de.peeeq.pscript.ast.AInitBlock term();
    public de.peeeq.pscript.ast.EObjectPos source();
    public de.peeeq.pscript.ast.AInitBlockPos replaceSource(org.eclipse.emf.ecore.EObject source);
    public de.peeeq.pscript.ast.ABlockPos body();
    public de.peeeq.pscript.ast.AInitBlockPos replaceBody(de.peeeq.pscript.ast.ABlock body);
    public de.peeeq.pscript.ast.pscriptAST.TermPos<?> get(int i);
    public int size();
    public de.peeeq.pscript.ast.AInitBlockPos replace(de.peeeq.pscript.ast.AInitBlock term);
    public de.peeeq.pscript.ast.AElementsPos parent();
    public de.peeeq.pscript.ast.AElementPos lsib();
    public de.peeeq.pscript.ast.AElementPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElementPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AInitBlockPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ABlockPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AStatementPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AIfPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AWhilePos term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AVoidReturnPos term) throws E;
        public void visit(de.peeeq.pscript.ast.AAssignmentPos term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AInitBlockPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AInitBlockPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AExprPos.Switch<Object, E> variantVisit$AExprPos = new de.peeeq.pscript.ast.AExprPos.Switch<Object, E>() { public final Object CaseAPrefixPos(de.peeeq.pscript.ast.APrefixPos term) throws E { visit(term); return null; } public final Object CaseAInfixPos(de.peeeq.pscript.ast.AInfixPos term) throws E { visit(term); return null; } public final Object CaseAFunctionCallPos(de.peeeq.pscript.ast.AFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseABuildinCallPos(de.peeeq.pscript.ast.ABuildinCallPos term) throws E { visit(term); return null; } public final Object CaseAVariableAccessPos(de.peeeq.pscript.ast.AVariableAccessPos term) throws E { visit(term); return null; } public final Object CaseAFieldAccessPos(de.peeeq.pscript.ast.AFieldAccessPos term) throws E { visit(term); return null; } public final Object CaseANoExprPos(de.peeeq.pscript.ast.ANoExprPos term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E> variantVisit$AInfixOpPos = new de.peeeq.pscript.ast.AInfixOpPos.Switch<Object, E>() { public final Object CaseAEqEqPos(de.peeeq.pscript.ast.AEqEqPos term) throws E { visit(term); return null; } public final Object CaseAGtPos(de.peeeq.pscript.ast.AGtPos term) throws E { visit(term); return null; } public final Object CaseAGtEqPos(de.peeeq.pscript.ast.AGtEqPos term) throws E { visit(term); return null; } public final Object CaseALtPos(de.peeeq.pscript.ast.ALtPos term) throws E { visit(term); return null; } public final Object CaseALtEqPos(de.peeeq.pscript.ast.ALtEqPos term) throws E { visit(term); return null; } public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseAMultPos(de.peeeq.pscript.ast.AMultPos term) throws E { visit(term); return null; } public final Object CaseADivPos(de.peeeq.pscript.ast.ADivPos term) throws E { visit(term); return null; } public final Object CaseADivIntPos(de.peeeq.pscript.ast.ADivIntPos term) throws E { visit(term); return null; } public final Object CaseAModuloPos(de.peeeq.pscript.ast.AModuloPos term) throws E { visit(term); return null; } public final Object CaseADotPos(de.peeeq.pscript.ast.ADotPos term) throws E { visit(term); return null; } public final Object CaseAAndPos(de.peeeq.pscript.ast.AAndPos term) throws E { visit(term); return null; } public final Object CaseAOrPos(de.peeeq.pscript.ast.AOrPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E> variantVisit$APrefixOpPos = new de.peeeq.pscript.ast.APrefixOpPos.Switch<Object, E>() { public final Object CaseAPlusPos(de.peeeq.pscript.ast.APlusPos term) throws E { visit(term); return null; } public final Object CaseAMinusPos(de.peeeq.pscript.ast.AMinusPos term) throws E { visit(term); return null; } public final Object CaseANotPos(de.peeeq.pscript.ast.ANotPos term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AStatementPos.Switch<Object, E> variantVisit$AStatementPos = new de.peeeq.pscript.ast.AStatementPos.Switch<Object, E>() { public final Object CaseABlockPos(de.peeeq.pscript.ast.ABlockPos term) throws E { visit(term); return null; } public final Object CaseAIfPos(de.peeeq.pscript.ast.AIfPos term) throws E { visit(term); return null; } public final Object CaseAWhilePos(de.peeeq.pscript.ast.AWhilePos term) throws E { visit(term); return null; } public final Object CaseAReturnPos(de.peeeq.pscript.ast.AReturnPos term) throws E { visit(term); return null; } public final Object CaseAVoidReturnPos(de.peeeq.pscript.ast.AVoidReturnPos term) throws E { visit(term); return null; } public final Object CaseAAssignmentPos(de.peeeq.pscript.ast.AAssignmentPos term) throws E { visit(term); return null; } public final Object CaseAPrefixPos(de.peeeq.pscript.ast.APrefixPos term) throws E { visit(term); return null; } public final Object CaseAInfixPos(de.peeeq.pscript.ast.AInfixPos term) throws E { visit(term); return null; } public final Object CaseAFunctionCallPos(de.peeeq.pscript.ast.AFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseABuildinCallPos(de.peeeq.pscript.ast.ABuildinCallPos term) throws E { visit(term); return null; } public final Object CaseAVariableAccessPos(de.peeeq.pscript.ast.AVariableAccessPos term) throws E { visit(term); return null; } public final Object CaseAFieldAccessPos(de.peeeq.pscript.ast.AFieldAccessPos term) throws E { visit(term); return null; } public final Object CaseANoExprPos(de.peeeq.pscript.ast.ANoExprPos term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteralPos(de.peeeq.pscript.ast.AIntegerLiteralPos term) throws E { visit(term); return null; } public final Object CaseARealLiteralPos(de.peeeq.pscript.ast.ARealLiteralPos term) throws E { visit(term); return null; } public final Object CaseAStringLiteralPos(de.peeeq.pscript.ast.AStringLiteralPos term) throws E { visit(term); return null; } public final Object CaseABooleanLiteralPos(de.peeeq.pscript.ast.ABooleanLiteralPos term) throws E { visit(term); return null; } };

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

        public final void visit(de.peeeq.pscript.ast.AStatementPos term) throws E {
            term.Switch(variantVisit$AStatementPos);
        }
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.AInitBlock> implements de.peeeq.pscript.ast.AInitBlockPos {

        //----- attributes of Impl -----

        private de.peeeq.pscript.ast.EObjectPos _source = null;
        private de.peeeq.pscript.ast.ABlockPos _body = null;

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.AInitBlock termAElement() {
            return term();
        }

        public de.peeeq.pscript.ast.EObjectPos source() {
            if(_source == null)
                _source = de.peeeq.pscript.ast.pscriptAST.EObjectPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.pscript.ast.AInitBlockPos replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.pscript.ast.ABlockPos body() {
            if(_body == null)
                _body = de.peeeq.pscript.ast.pscriptAST.ABlockPos(this, _term.body(), 1);

            return _body;
        }

        public de.peeeq.pscript.ast.AInitBlockPos replaceBody(de.peeeq.pscript.ast.ABlock body) {
            return replace(_term.replaceBody(body));
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.AInitBlock term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.pscript.ast.pscriptAST.TermPos<?> get(int i) {
            int ith = i;

            if(ith < 0) ith += 2;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = pscriptAST.EObjectPos(this, _term.source(), 0);
                    
                    return _source;
                case 1:
                    if(_body == null)
                        _body = pscriptAST.ABlockPos(this, _term.body(), 1);
                    
                    return _body;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AInitBlockPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AInitBlockPos invoked with too large parameter "+i+", sort has only 2 components");
                    }
            }
        }

        public int size() {
            return 2;
        }

        public de.peeeq.pscript.ast.AInitBlockPos replace(de.peeeq.pscript.ast.AInitBlock term) {
            return (de.peeeq.pscript.ast.AInitBlockPos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AElementsPos parent() {
            return (de.peeeq.pscript.ast.AElementsPos) super.parent();
        }

        public de.peeeq.pscript.ast.AElementPos lsib() {
            return (de.peeeq.pscript.ast.AElementPos) super.lsib();
        }

        public de.peeeq.pscript.ast.AElementPos rsib() {
            return (de.peeeq.pscript.ast.AElementPos) super.rsib();
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

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElementPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAInitBlockPos(this);
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
            return "AInitBlockPos";
        }
    }
}

