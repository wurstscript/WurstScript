package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AFuncDef extends de.peeeq.pscript.ast.AClassMember, de.peeeq.pscript.ast.AElement, KatjaTuple {

    //----- methods of AFuncDef -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.AFuncDef replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.lang.String name();
    public de.peeeq.pscript.ast.AFuncDef replaceName(java.lang.String name);
    public de.peeeq.pscript.ast.AReturnType retType();
    public de.peeeq.pscript.ast.AFuncDef replaceRetType(de.peeeq.pscript.ast.AReturnType retType);
    public de.peeeq.pscript.ast.AFormalParameters params();
    public de.peeeq.pscript.ast.AFuncDef replaceParams(de.peeeq.pscript.ast.AFormalParameters params);
    public de.peeeq.pscript.ast.ABlock body();
    public de.peeeq.pscript.ast.AFuncDef replaceBody(de.peeeq.pscript.ast.ABlock body);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.AFuncDef replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AClassMember.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AFuncDef -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnType term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameters term) throws E;
        public void visit(de.peeeq.pscript.ast.ABlock term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnsNothing term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExprSimple term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameter term) throws E;
        public void visit(de.peeeq.pscript.ast.AStatement term) throws E;
        public void visit(de.peeeq.pscript.ast.AIf term) throws E;
        public void visit(de.peeeq.pscript.ast.AWhile term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturn term) throws E;
        public void visit(de.peeeq.pscript.ast.AVoidReturn term) throws E;
        public void visit(de.peeeq.pscript.ast.AAssignment term) throws E;
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
        public void visit(de.peeeq.pscript.ast.APrefixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOp term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AFuncDef term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AFuncDef.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AExpr.Switch<Object, E> variantVisit$AExpr = new de.peeeq.pscript.ast.AExpr.Switch<Object, E>() { public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOp.Switch<Object, E> variantVisit$AInfixOp = new de.peeeq.pscript.ast.AInfixOp.Switch<Object, E>() { public final Object CaseAEqEq(de.peeeq.pscript.ast.AEqEq term) throws E { visit(term); return null; } public final Object CaseAGt(de.peeeq.pscript.ast.AGt term) throws E { visit(term); return null; } public final Object CaseAGtEq(de.peeeq.pscript.ast.AGtEq term) throws E { visit(term); return null; } public final Object CaseALt(de.peeeq.pscript.ast.ALt term) throws E { visit(term); return null; } public final Object CaseALtEq(de.peeeq.pscript.ast.ALtEq term) throws E { visit(term); return null; } public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseAMult(de.peeeq.pscript.ast.AMult term) throws E { visit(term); return null; } public final Object CaseADiv(de.peeeq.pscript.ast.ADiv term) throws E { visit(term); return null; } public final Object CaseADivInt(de.peeeq.pscript.ast.ADivInt term) throws E { visit(term); return null; } public final Object CaseAModulo(de.peeeq.pscript.ast.AModulo term) throws E { visit(term); return null; } public final Object CaseADot(de.peeeq.pscript.ast.ADot term) throws E { visit(term); return null; } public final Object CaseAAnd(de.peeeq.pscript.ast.AAnd term) throws E { visit(term); return null; } public final Object CaseAOr(de.peeeq.pscript.ast.AOr term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOp.Switch<Object, E> variantVisit$APrefixOp = new de.peeeq.pscript.ast.APrefixOp.Switch<Object, E>() { public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseANot(de.peeeq.pscript.ast.ANot term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AReturnType.Switch<Object, E> variantVisit$AReturnType = new de.peeeq.pscript.ast.AReturnType.Switch<Object, E>() { public final Object CaseAReturnsNothing(de.peeeq.pscript.ast.AReturnsNothing term) throws E { visit(term); return null; } public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AStatement.Switch<Object, E> variantVisit$AStatement = new de.peeeq.pscript.ast.AStatement.Switch<Object, E>() { public final Object CaseABlock(de.peeeq.pscript.ast.ABlock term) throws E { visit(term); return null; } public final Object CaseAIf(de.peeeq.pscript.ast.AIf term) throws E { visit(term); return null; } public final Object CaseAWhile(de.peeeq.pscript.ast.AWhile term) throws E { visit(term); return null; } public final Object CaseAReturn(de.peeeq.pscript.ast.AReturn term) throws E { visit(term); return null; } public final Object CaseAVoidReturn(de.peeeq.pscript.ast.AVoidReturn term) throws E { visit(term); return null; } public final Object CaseAAssignment(de.peeeq.pscript.ast.AAssignment term) throws E { visit(term); return null; } public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };

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

        public final void visit(de.peeeq.pscript.ast.AReturnType term) throws E {
            term.Switch(variantVisit$AReturnType);
        }

        public final void visit(de.peeeq.pscript.ast.AStatement term) throws E {
            term.Switch(variantVisit$AStatement);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$AReturnType);
        }
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.AFuncDef {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.lang.String _name = null;
        private de.peeeq.pscript.ast.AReturnType _retType = null;
        private de.peeeq.pscript.ast.AFormalParameters _params = null;
        private de.peeeq.pscript.ast.ABlock _body = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.AFuncDef replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.pscript.ast.AFuncDef replaceName(java.lang.String name) {
            return replace(1, name);
        }

        public de.peeeq.pscript.ast.AReturnType retType() {
            return _retType;
        }

        public de.peeeq.pscript.ast.AFuncDef replaceRetType(de.peeeq.pscript.ast.AReturnType retType) {
            return replace(2, retType);
        }

        public de.peeeq.pscript.ast.AFormalParameters params() {
            return _params;
        }

        public de.peeeq.pscript.ast.AFuncDef replaceParams(de.peeeq.pscript.ast.AFormalParameters params) {
            return replace(3, params);
        }

        public de.peeeq.pscript.ast.ABlock body() {
            return _body;
        }

        public de.peeeq.pscript.ast.AFuncDef replaceBody(de.peeeq.pscript.ast.ABlock body) {
            return replace(4, body);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AReturnType retType, de.peeeq.pscript.ast.AFormalParameters params, de.peeeq.pscript.ast.ABlock body) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort AFuncDef invoked with null parameter source");
            if(name == null)
                throw new IllegalArgumentException("constructor of sort AFuncDef invoked with null parameter name");
            if(retType == null)
                throw new IllegalArgumentException("constructor of sort AFuncDef invoked with null parameter retType");
            if(params == null)
                throw new IllegalArgumentException("constructor of sort AFuncDef invoked with null parameter params");
            if(body == null)
                throw new IllegalArgumentException("constructor of sort AFuncDef invoked with null parameter body");

            this._source = source;
            this._name = name;
            this._retType = retType;
            this._params = params;
            this._body = body;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 5;

            switch(ith) {
                case 0: return _source;
                case 1: return _name;
                case 2: return _retType;
                case 3: return _params;
                case 4: return _body;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AFuncDef invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AFuncDef invoked with too large parameter "+i+", sort has only 5 components");
                    }
            }
        }

        public int size() {
            return 5;
        }

        public de.peeeq.pscript.ast.AFuncDef replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort AFuncDef invoked with negative parameter "+pos);
            if(pos >= 5)
                throw new IllegalArgumentException("replace on sort AFuncDef invoked with too large parameter "+pos+", sort has only 5 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort AFuncDef invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort AFuncDef invoked with term of incorrect sort, String expected");
            if(pos == 2 && !(term instanceof de.peeeq.pscript.ast.AReturnType))
                throw new IllegalArgumentException("replace on sort AFuncDef invoked with term of incorrect sort, AReturnType expected");
            if(pos == 3 && !(term instanceof de.peeeq.pscript.ast.AFormalParameters))
                throw new IllegalArgumentException("replace on sort AFuncDef invoked with term of incorrect sort, AFormalParameters expected");
            if(pos == 4 && !(term instanceof de.peeeq.pscript.ast.ABlock))
                throw new IllegalArgumentException("replace on sort AFuncDef invoked with term of incorrect sort, ABlock expected");

            return (de.peeeq.pscript.ast.AFuncDef) pscriptAST.unique(new de.peeeq.pscript.ast.AFuncDef.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.lang.String) term : _name,
                pos == 2 ? (de.peeeq.pscript.ast.AReturnType) term : _retType,
                pos == 3 ? (de.peeeq.pscript.ast.AFormalParameters) term : _params,
                pos == 4 ? (de.peeeq.pscript.ast.ABlock) term : _body
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AClassMember.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAFuncDef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAFuncDef(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.AFuncDef");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _retType.toJavaCode(builder);
            builder.append(", ");
            _params.toJavaCode(builder);
            builder.append(", ");
            _body.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("AFuncDef");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _retType.toString(builder);
            builder.append(", ");
            _params.toString(builder);
            builder.append(", ");
            _body.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AFuncDef";
        }
    }
}

