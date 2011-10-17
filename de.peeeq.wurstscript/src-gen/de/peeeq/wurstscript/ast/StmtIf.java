package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface StmtIf extends de.peeeq.wurstscript.ast.WStatement, KatjaTuple {

    //----- methods of StmtIf -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.StmtIf replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.Expr cond();
    public de.peeeq.wurstscript.ast.StmtIf replaceCond(de.peeeq.wurstscript.ast.Expr cond);
    public de.peeeq.wurstscript.ast.WStatements thenBlock();
    public de.peeeq.wurstscript.ast.StmtIf replaceThenBlock(de.peeeq.wurstscript.ast.WStatements thenBlock);
    public de.peeeq.wurstscript.ast.WStatements elseBlock();
    public de.peeeq.wurstscript.ast.StmtIf replaceElseBlock(de.peeeq.wurstscript.ast.WStatements elseBlock);
    public KatjaSort get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.StmtIf replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WStatement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of StmtIf -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WStatements term) throws E;
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
        public void visit(java.lang.String term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WStatement term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Indexes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Arguments term) throws E;
        public void visit(java.lang.Double term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtIf term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtWhile term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtLoop term) throws E;
        public void visit(de.peeeq.wurstscript.ast.LocalVarDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtSet term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtCall term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtReturn term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtDestroy term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtIncRefCount term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtDecRefCount term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtErr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtExitwhen term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssignment term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssign term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizes term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.StmtIf.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.Expr.Switch<Object, E> variantVisit$Expr = new de.peeeq.wurstscript.ast.Expr.Switch<Object, E>() { public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E> variantVisit$OpAssignment = new de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E>() { public final Object CaseOpAssign(de.peeeq.wurstscript.ast.OpAssign term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E> variantVisit$OptExpr = new de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E>() { public final Object CaseNoExpr(de.peeeq.wurstscript.ast.NoExpr term) throws E { visit(term); return null; } public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E> variantVisit$OptTypeExpr = new de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E>() { public final Object CaseNoTypeExpr(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E { visit(term); return null; } public final Object CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.StmtCall.Switch<Object, E> variantVisit$StmtCall = new de.peeeq.wurstscript.ast.StmtCall.Switch<Object, E>() { public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.WStatement.Switch<Object, E> variantVisit$WStatement = new de.peeeq.wurstscript.ast.WStatement.Switch<Object, E>() { public final Object CaseStmtIf(de.peeeq.wurstscript.ast.StmtIf term) throws E { visit(term); return null; } public final Object CaseStmtWhile(de.peeeq.wurstscript.ast.StmtWhile term) throws E { visit(term); return null; } public final Object CaseStmtLoop(de.peeeq.wurstscript.ast.StmtLoop term) throws E { visit(term); return null; } public final Object CaseLocalVarDef(de.peeeq.wurstscript.ast.LocalVarDef term) throws E { visit(term); return null; } public final Object CaseStmtSet(de.peeeq.wurstscript.ast.StmtSet term) throws E { visit(term); return null; } public final Object CaseStmtReturn(de.peeeq.wurstscript.ast.StmtReturn term) throws E { visit(term); return null; } public final Object CaseStmtDestroy(de.peeeq.wurstscript.ast.StmtDestroy term) throws E { visit(term); return null; } public final Object CaseStmtIncRefCount(de.peeeq.wurstscript.ast.StmtIncRefCount term) throws E { visit(term); return null; } public final Object CaseStmtDecRefCount(de.peeeq.wurstscript.ast.StmtDecRefCount term) throws E { visit(term); return null; } public final Object CaseStmtErr(de.peeeq.wurstscript.ast.StmtErr term) throws E { visit(term); return null; } public final Object CaseStmtExitwhen(de.peeeq.wurstscript.ast.StmtExitwhen term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.Expr term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAssignable term) throws E {
            term.Switch(variantVisit$Expr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAtomic term) throws E {
            term.Switch(variantVisit$Expr);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpAssignment term) throws E {
            term.Switch(variantVisit$OpAssignment);
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

        public final void visit(de.peeeq.wurstscript.ast.StmtCall term) throws E {
            term.Switch(variantVisit$StmtCall);
        }

        public final void visit(de.peeeq.wurstscript.ast.WStatement term) throws E {
            term.Switch(variantVisit$WStatement);
        }
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.StmtIf {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private de.peeeq.wurstscript.ast.Expr _cond = null;
        private de.peeeq.wurstscript.ast.WStatements _thenBlock = null;
        private de.peeeq.wurstscript.ast.WStatements _elseBlock = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.StmtIf replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public de.peeeq.wurstscript.ast.Expr cond() {
            return _cond;
        }

        public de.peeeq.wurstscript.ast.StmtIf replaceCond(de.peeeq.wurstscript.ast.Expr cond) {
            return replace(1, cond);
        }

        public de.peeeq.wurstscript.ast.WStatements thenBlock() {
            return _thenBlock;
        }

        public de.peeeq.wurstscript.ast.StmtIf replaceThenBlock(de.peeeq.wurstscript.ast.WStatements thenBlock) {
            return replace(2, thenBlock);
        }

        public de.peeeq.wurstscript.ast.WStatements elseBlock() {
            return _elseBlock;
        }

        public de.peeeq.wurstscript.ast.StmtIf replaceElseBlock(de.peeeq.wurstscript.ast.WStatements elseBlock) {
            return replace(3, elseBlock);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr cond, de.peeeq.wurstscript.ast.WStatements thenBlock, de.peeeq.wurstscript.ast.WStatements elseBlock) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort StmtIf invoked with null parameter source");
            if(cond == null)
                throw new IllegalArgumentException("constructor of sort StmtIf invoked with null parameter cond");
            if(thenBlock == null)
                throw new IllegalArgumentException("constructor of sort StmtIf invoked with null parameter thenBlock");
            if(elseBlock == null)
                throw new IllegalArgumentException("constructor of sort StmtIf invoked with null parameter elseBlock");

            this._source = source;
            this._cond = cond;
            this._thenBlock = thenBlock;
            this._elseBlock = elseBlock;
        }

        public KatjaSort get(int i) {
            int ith = i;

            if(ith < 0) ith += 4;

            switch(ith) {
                case 0: return _source;
                case 1: return _cond;
                case 2: return _thenBlock;
                case 3: return _elseBlock;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort StmtIf invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort StmtIf invoked with too large parameter "+i+", sort has only 4 components");
                    }
            }
        }

        public int size() {
            return 4;
        }

        public de.peeeq.wurstscript.ast.StmtIf replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort StmtIf invoked with negative parameter "+pos);
            if(pos >= 4)
                throw new IllegalArgumentException("replace on sort StmtIf invoked with too large parameter "+pos+", sort has only 4 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort StmtIf invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof de.peeeq.wurstscript.ast.Expr))
                throw new IllegalArgumentException("replace on sort StmtIf invoked with term of incorrect sort, Expr expected");
            if(pos == 2 && !(term instanceof de.peeeq.wurstscript.ast.WStatements))
                throw new IllegalArgumentException("replace on sort StmtIf invoked with term of incorrect sort, WStatements expected");
            if(pos == 3 && !(term instanceof de.peeeq.wurstscript.ast.WStatements))
                throw new IllegalArgumentException("replace on sort StmtIf invoked with term of incorrect sort, WStatements expected");

            return (de.peeeq.wurstscript.ast.StmtIf) AST.unique(new de.peeeq.wurstscript.ast.StmtIf.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (de.peeeq.wurstscript.ast.Expr) term : _cond,
                pos == 2 ? (de.peeeq.wurstscript.ast.WStatements) term : _thenBlock,
                pos == 3 ? (de.peeeq.wurstscript.ast.WStatements) term : _elseBlock
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WStatement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseStmtIf(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.StmtIf");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            _cond.toJavaCode(builder);
            builder.append(", ");
            _thenBlock.toJavaCode(builder);
            builder.append(", ");
            _elseBlock.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("StmtIf");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            _cond.toString(builder);
            builder.append(", ");
            _thenBlock.toString(builder);
            builder.append(", ");
            _elseBlock.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "StmtIf";
        }
    }
}

