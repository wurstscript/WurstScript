package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface AVarDef extends de.peeeq.pscript.ast.AClassMember, de.peeeq.pscript.ast.AElement, KatjaTuple {

    //----- methods of AVarDef -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.AVarDef replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.lang.String name();
    public de.peeeq.pscript.ast.AVarDef replaceName(java.lang.String name);
    public de.peeeq.pscript.ast.ATypeExpr typeExpr();
    public de.peeeq.pscript.ast.AVarDef replaceTypeExpr(de.peeeq.pscript.ast.ATypeExpr typeExpr);
    public java.lang.Boolean constant();
    public de.peeeq.pscript.ast.AVarDef replaceConstant(java.lang.Boolean constant);
    public de.peeeq.pscript.ast.AExpr initial();
    public de.peeeq.pscript.ast.AVarDef replaceInitial(de.peeeq.pscript.ast.AExpr initial);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.AVarDef replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AClassMember.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of AVarDef -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.pscript.ast.AExpr term) throws E;
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
        public void visit(de.peeeq.pscript.ast.APrefixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AArguments term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifier term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.math.BigDecimal term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AVarDef term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.AVarDef.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AExpr.Switch<Object, E> variantVisit$AExpr = new de.peeeq.pscript.ast.AExpr.Switch<Object, E>() { public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOp.Switch<Object, E> variantVisit$AInfixOp = new de.peeeq.pscript.ast.AInfixOp.Switch<Object, E>() { public final Object CaseAEqEq(de.peeeq.pscript.ast.AEqEq term) throws E { visit(term); return null; } public final Object CaseAGt(de.peeeq.pscript.ast.AGt term) throws E { visit(term); return null; } public final Object CaseAGtEq(de.peeeq.pscript.ast.AGtEq term) throws E { visit(term); return null; } public final Object CaseALt(de.peeeq.pscript.ast.ALt term) throws E { visit(term); return null; } public final Object CaseALtEq(de.peeeq.pscript.ast.ALtEq term) throws E { visit(term); return null; } public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseAMult(de.peeeq.pscript.ast.AMult term) throws E { visit(term); return null; } public final Object CaseADiv(de.peeeq.pscript.ast.ADiv term) throws E { visit(term); return null; } public final Object CaseADivInt(de.peeeq.pscript.ast.ADivInt term) throws E { visit(term); return null; } public final Object CaseAModulo(de.peeeq.pscript.ast.AModulo term) throws E { visit(term); return null; } public final Object CaseADot(de.peeeq.pscript.ast.ADot term) throws E { visit(term); return null; } public final Object CaseAAnd(de.peeeq.pscript.ast.AAnd term) throws E { visit(term); return null; } public final Object CaseAOr(de.peeeq.pscript.ast.AOr term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOp.Switch<Object, E> variantVisit$APrefixOp = new de.peeeq.pscript.ast.APrefixOp.Switch<Object, E>() { public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseANot(de.peeeq.pscript.ast.ANot term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E> variantVisit$ATypeExpr = new de.peeeq.pscript.ast.ATypeExpr.Switch<Object, E>() { public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };

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

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$ATypeExpr);
        }
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.AVarDef {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.lang.String _name = null;
        private de.peeeq.pscript.ast.ATypeExpr _typeExpr = null;
        private java.lang.Boolean _constant = null;
        private de.peeeq.pscript.ast.AExpr _initial = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.AVarDef replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.pscript.ast.AVarDef replaceName(java.lang.String name) {
            return replace(1, name);
        }

        public de.peeeq.pscript.ast.ATypeExpr typeExpr() {
            return _typeExpr;
        }

        public de.peeeq.pscript.ast.AVarDef replaceTypeExpr(de.peeeq.pscript.ast.ATypeExpr typeExpr) {
            return replace(2, typeExpr);
        }

        public java.lang.Boolean constant() {
            return _constant;
        }

        public de.peeeq.pscript.ast.AVarDef replaceConstant(java.lang.Boolean constant) {
            return replace(3, constant);
        }

        public de.peeeq.pscript.ast.AExpr initial() {
            return _initial;
        }

        public de.peeeq.pscript.ast.AVarDef replaceInitial(de.peeeq.pscript.ast.AExpr initial) {
            return replace(4, initial);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.ATypeExpr typeExpr, java.lang.Boolean constant, de.peeeq.pscript.ast.AExpr initial) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort AVarDef invoked with null parameter source");
            if(name == null)
                throw new IllegalArgumentException("constructor of sort AVarDef invoked with null parameter name");
            if(typeExpr == null)
                throw new IllegalArgumentException("constructor of sort AVarDef invoked with null parameter typeExpr");
            if(constant == null)
                throw new IllegalArgumentException("constructor of sort AVarDef invoked with null parameter constant");
            if(initial == null)
                throw new IllegalArgumentException("constructor of sort AVarDef invoked with null parameter initial");

            this._source = source;
            this._name = name;
            this._typeExpr = typeExpr;
            this._constant = constant;
            this._initial = initial;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 5;

            switch(ith) {
                case 0: return _source;
                case 1: return _name;
                case 2: return _typeExpr;
                case 3: return _constant;
                case 4: return _initial;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort AVarDef invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort AVarDef invoked with too large parameter "+i+", sort has only 5 components");
                    }
            }
        }

        public int size() {
            return 5;
        }

        public de.peeeq.pscript.ast.AVarDef replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort AVarDef invoked with negative parameter "+pos);
            if(pos >= 5)
                throw new IllegalArgumentException("replace on sort AVarDef invoked with too large parameter "+pos+", sort has only 5 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort AVarDef invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort AVarDef invoked with term of incorrect sort, String expected");
            if(pos == 2 && !(term instanceof de.peeeq.pscript.ast.ATypeExpr))
                throw new IllegalArgumentException("replace on sort AVarDef invoked with term of incorrect sort, ATypeExpr expected");
            if(pos == 3 && !(term instanceof java.lang.Boolean))
                throw new IllegalArgumentException("replace on sort AVarDef invoked with term of incorrect sort, Boolean expected");
            if(pos == 4 && !(term instanceof de.peeeq.pscript.ast.AExpr))
                throw new IllegalArgumentException("replace on sort AVarDef invoked with term of incorrect sort, AExpr expected");

            return (de.peeeq.pscript.ast.AVarDef) pscriptAST.unique(new de.peeeq.pscript.ast.AVarDef.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.lang.String) term : _name,
                pos == 2 ? (de.peeeq.pscript.ast.ATypeExpr) term : _typeExpr,
                pos == 3 ? (java.lang.Boolean) term : _constant,
                pos == 4 ? (de.peeeq.pscript.ast.AExpr) term : _initial
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AClassMember.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAVarDef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAVarDef(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.AVarDef");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _typeExpr.toJavaCode(builder);
            builder.append(", ");
            builder.append(_constant.toString());
            builder.append(", ");
            _initial.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("AVarDef");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _typeExpr.toString(builder);
            builder.append(", ");
            builder.append(_constant.toString());
            builder.append(", ");
            _initial.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "AVarDef";
        }
    }
}

