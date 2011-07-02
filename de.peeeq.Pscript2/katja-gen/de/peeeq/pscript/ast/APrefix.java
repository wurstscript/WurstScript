package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface APrefix extends de.peeeq.pscript.ast.AExpr, KatjaTuple {

    //----- methods of APrefix -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.APrefix replaceSource(org.eclipse.emf.ecore.EObject source);
    public de.peeeq.pscript.ast.APrefixOp op();
    public de.peeeq.pscript.ast.APrefix replaceOp(de.peeeq.pscript.ast.APrefixOp op);
    public de.peeeq.pscript.ast.AExpr e();
    public de.peeeq.pscript.ast.APrefix replaceE(de.peeeq.pscript.ast.AExpr e);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.APrefix replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of APrefix -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.APlus term) throws E;
        public void visit(de.peeeq.pscript.ast.AMinus term) throws E;
        public void visit(de.peeeq.pscript.ast.ANot term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AInfixOp term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AArguments term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifier term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.math.BigDecimal term) throws E;
        public void visit(java.lang.Boolean term) throws E;
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
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.APrefix.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AExpr.Switch<Object, E> variantVisit$AExpr = new de.peeeq.pscript.ast.AExpr.Switch<Object, E>() { public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOp.Switch<Object, E> variantVisit$AInfixOp = new de.peeeq.pscript.ast.AInfixOp.Switch<Object, E>() { public final Object CaseAEqEq(de.peeeq.pscript.ast.AEqEq term) throws E { visit(term); return null; } public final Object CaseAGt(de.peeeq.pscript.ast.AGt term) throws E { visit(term); return null; } public final Object CaseAGtEq(de.peeeq.pscript.ast.AGtEq term) throws E { visit(term); return null; } public final Object CaseALt(de.peeeq.pscript.ast.ALt term) throws E { visit(term); return null; } public final Object CaseALtEq(de.peeeq.pscript.ast.ALtEq term) throws E { visit(term); return null; } public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseAMult(de.peeeq.pscript.ast.AMult term) throws E { visit(term); return null; } public final Object CaseADiv(de.peeeq.pscript.ast.ADiv term) throws E { visit(term); return null; } public final Object CaseADivInt(de.peeeq.pscript.ast.ADivInt term) throws E { visit(term); return null; } public final Object CaseAModulo(de.peeeq.pscript.ast.AModulo term) throws E { visit(term); return null; } public final Object CaseADot(de.peeeq.pscript.ast.ADot term) throws E { visit(term); return null; } public final Object CaseAAnd(de.peeeq.pscript.ast.AAnd term) throws E { visit(term); return null; } public final Object CaseAOr(de.peeeq.pscript.ast.AOr term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOp.Switch<Object, E> variantVisit$APrefixOp = new de.peeeq.pscript.ast.APrefixOp.Switch<Object, E>() { public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseANot(de.peeeq.pscript.ast.ANot term) throws E { visit(term); return null; } };

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
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.APrefix {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private de.peeeq.pscript.ast.APrefixOp _op = null;
        private de.peeeq.pscript.ast.AExpr _e = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.APrefix replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public de.peeeq.pscript.ast.APrefixOp op() {
            return _op;
        }

        public de.peeeq.pscript.ast.APrefix replaceOp(de.peeeq.pscript.ast.APrefixOp op) {
            return replace(1, op);
        }

        public de.peeeq.pscript.ast.AExpr e() {
            return _e;
        }

        public de.peeeq.pscript.ast.APrefix replaceE(de.peeeq.pscript.ast.AExpr e) {
            return replace(2, e);
        }

        Impl(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.APrefixOp op, de.peeeq.pscript.ast.AExpr e) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort APrefix invoked with null parameter source");
            if(op == null)
                throw new IllegalArgumentException("constructor of sort APrefix invoked with null parameter op");
            if(e == null)
                throw new IllegalArgumentException("constructor of sort APrefix invoked with null parameter e");

            this._source = source;
            this._op = op;
            this._e = e;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 3;

            switch(ith) {
                case 0: return _source;
                case 1: return _op;
                case 2: return _e;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort APrefix invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort APrefix invoked with too large parameter "+i+", sort has only 3 components");
                    }
            }
        }

        public int size() {
            return 3;
        }

        public de.peeeq.pscript.ast.APrefix replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort APrefix invoked with negative parameter "+pos);
            if(pos >= 3)
                throw new IllegalArgumentException("replace on sort APrefix invoked with too large parameter "+pos+", sort has only 3 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort APrefix invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof de.peeeq.pscript.ast.APrefixOp))
                throw new IllegalArgumentException("replace on sort APrefix invoked with term of incorrect sort, APrefixOp expected");
            if(pos == 2 && !(term instanceof de.peeeq.pscript.ast.AExpr))
                throw new IllegalArgumentException("replace on sort APrefix invoked with term of incorrect sort, AExpr expected");

            return (de.peeeq.pscript.ast.APrefix) pscriptAST.unique(new de.peeeq.pscript.ast.APrefix.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (de.peeeq.pscript.ast.APrefixOp) term : _op,
                pos == 2 ? (de.peeeq.pscript.ast.AExpr) term : _e
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAPrefix(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseAPrefix(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.APrefix");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            _op.toJavaCode(builder);
            builder.append(", ");
            _e.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("APrefix");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            _op.toString(builder);
            builder.append(", ");
            _e.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "APrefix";
        }
    }
}

