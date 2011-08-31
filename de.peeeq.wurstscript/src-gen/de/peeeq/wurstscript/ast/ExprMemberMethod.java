package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface ExprMemberMethod extends de.peeeq.wurstscript.ast.Expr, de.peeeq.wurstscript.ast.FuncRef, de.peeeq.wurstscript.ast.StmtCall, KatjaTuple {

    //----- methods of ExprMemberMethod -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.ExprMemberMethod replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.Expr left();
    public de.peeeq.wurstscript.ast.ExprMemberMethod replaceLeft(de.peeeq.wurstscript.ast.Expr left);
    public java.lang.String funcName();
    public de.peeeq.wurstscript.ast.ExprMemberMethod replaceFuncName(java.lang.String funcName);
    public de.peeeq.wurstscript.ast.Arguments args();
    public de.peeeq.wurstscript.ast.ExprMemberMethod replaceArgs(de.peeeq.wurstscript.ast.Arguments args);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.ExprMemberMethod replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.FuncRef.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.StmtCall.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ExprMemberMethod -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Arguments term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.ExprNull term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Indexes term) throws E;
        public void visit(java.lang.Double term) throws E;
        public void visit(java.lang.Boolean term) throws E;
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

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.ExprMemberMethod.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.Expr.Switch<Object, E> variantVisit$Expr = new de.peeeq.wurstscript.ast.Expr.Switch<Object, E>() { public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };

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
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.ExprMemberMethod {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private de.peeeq.wurstscript.ast.Expr _left = null;
        private java.lang.String _funcName = null;
        private de.peeeq.wurstscript.ast.Arguments _args = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.ExprMemberMethod replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public de.peeeq.wurstscript.ast.Expr left() {
            return _left;
        }

        public de.peeeq.wurstscript.ast.ExprMemberMethod replaceLeft(de.peeeq.wurstscript.ast.Expr left) {
            return replace(1, left);
        }

        public java.lang.String funcName() {
            return _funcName;
        }

        public de.peeeq.wurstscript.ast.ExprMemberMethod replaceFuncName(java.lang.String funcName) {
            return replace(2, funcName);
        }

        public de.peeeq.wurstscript.ast.Arguments args() {
            return _args;
        }

        public de.peeeq.wurstscript.ast.ExprMemberMethod replaceArgs(de.peeeq.wurstscript.ast.Arguments args) {
            return replace(3, args);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr left, java.lang.String funcName, de.peeeq.wurstscript.ast.Arguments args) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ExprMemberMethod invoked with null parameter source");
            if(left == null)
                throw new IllegalArgumentException("constructor of sort ExprMemberMethod invoked with null parameter left");
            if(funcName == null)
                throw new IllegalArgumentException("constructor of sort ExprMemberMethod invoked with null parameter funcName");
            if(args == null)
                throw new IllegalArgumentException("constructor of sort ExprMemberMethod invoked with null parameter args");

            this._source = source;
            this._left = left;
            this._funcName = funcName;
            this._args = args;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 4;

            switch(ith) {
                case 0: return _source;
                case 1: return _left;
                case 2: return _funcName;
                case 3: return _args;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ExprMemberMethod invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ExprMemberMethod invoked with too large parameter "+i+", sort has only 4 components");
                    }
            }
        }

        public int size() {
            return 4;
        }

        public de.peeeq.wurstscript.ast.ExprMemberMethod replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ExprMemberMethod invoked with negative parameter "+pos);
            if(pos >= 4)
                throw new IllegalArgumentException("replace on sort ExprMemberMethod invoked with too large parameter "+pos+", sort has only 4 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort ExprMemberMethod invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof de.peeeq.wurstscript.ast.Expr))
                throw new IllegalArgumentException("replace on sort ExprMemberMethod invoked with term of incorrect sort, Expr expected");
            if(pos == 2 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort ExprMemberMethod invoked with term of incorrect sort, String expected");
            if(pos == 3 && !(term instanceof de.peeeq.wurstscript.ast.Arguments))
                throw new IllegalArgumentException("replace on sort ExprMemberMethod invoked with term of incorrect sort, Arguments expected");

            return (de.peeeq.wurstscript.ast.ExprMemberMethod) AST.unique(new de.peeeq.wurstscript.ast.ExprMemberMethod.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (de.peeeq.wurstscript.ast.Expr) term : _left,
                pos == 2 ? (java.lang.String) term : _funcName,
                pos == 3 ? (de.peeeq.wurstscript.ast.Arguments) term : _args
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.Expr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprMemberMethod(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.FuncRef.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprMemberMethod(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.StmtCall.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprMemberMethod(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OptExpr.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprMemberMethod(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseExprMemberMethod(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.ExprMemberMethod");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            _left.toJavaCode(builder);
            builder.append(", ");
            builder.append("\"").append(_funcName.toString()).append("\"");
            builder.append(", ");
            _args.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ExprMemberMethod");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            _left.toString(builder);
            builder.append(", ");
            builder.append("\"").append(_funcName.toString()).append("\"");
            builder.append(", ");
            _args.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ExprMemberMethod";
        }
    }
}

