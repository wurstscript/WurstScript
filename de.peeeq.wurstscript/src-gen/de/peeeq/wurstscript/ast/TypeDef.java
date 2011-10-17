package de.peeeq.wurstscript.ast;

import katja.common.KatjaSort;

public interface TypeDef extends de.peeeq.wurstscript.ast.WEntity, KatjaSort {

    //----- methods of TypeDef -----

    public de.peeeq.wurstscript.ast.VisibilityModifier visibility();
    public java.lang.String name();
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TypeDef.Switch<CT, E> switchClass) throws E;

    //----- nested classes of TypeDef -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseNativeType(de.peeeq.wurstscript.ast.NativeType term) throws E;
        public CT CaseClassDef(de.peeeq.wurstscript.ast.ClassDef term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityModifier term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ClassSlots term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublic term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPrivate term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublicread term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExpr term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ClassSlot term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ConstructorDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OnDestroyDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ClassMember term) throws E;
        public void visit(de.peeeq.wurstscript.ast.GlobalVarDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.FuncDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WParameters term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WStatements term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.FuncSignature term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAssignable term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNewObject term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprCast term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.NoExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WParameter term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WStatement term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Indexes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Arguments term) throws E;
        public void visit(java.lang.Double term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.OpAssignment term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssign term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeDef term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NativeType term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ClassDef term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.TypeDef.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.ClassMember.Switch<Object, E> variantVisit$ClassMember = new de.peeeq.wurstscript.ast.ClassMember.Switch<Object, E>() { public final Object CaseGlobalVarDef(de.peeeq.wurstscript.ast.GlobalVarDef term) throws E { visit(term); return null; } public final Object CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.ClassSlot.Switch<Object, E> variantVisit$ClassSlot = new de.peeeq.wurstscript.ast.ClassSlot.Switch<Object, E>() { public final Object CaseConstructorDef(de.peeeq.wurstscript.ast.ConstructorDef term) throws E { visit(term); return null; } public final Object CaseOnDestroyDef(de.peeeq.wurstscript.ast.OnDestroyDef term) throws E { visit(term); return null; } public final Object CaseGlobalVarDef(de.peeeq.wurstscript.ast.GlobalVarDef term) throws E { visit(term); return null; } public final Object CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E> variantVisit$OpAssignment = new de.peeeq.wurstscript.ast.OpAssignment.Switch<Object, E>() { public final Object CaseOpAssign(de.peeeq.wurstscript.ast.OpAssign term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E> variantVisit$OptExpr = new de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E>() { public final Object CaseNoExpr(de.peeeq.wurstscript.ast.NoExpr term) throws E { visit(term); return null; } public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprCast(de.peeeq.wurstscript.ast.ExprCast term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E> variantVisit$OptTypeExpr = new de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E>() { public final Object CaseNoTypeExpr(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E { visit(term); return null; } public final Object CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.StmtCall.Switch<Object, E> variantVisit$StmtCall = new de.peeeq.wurstscript.ast.StmtCall.Switch<Object, E>() { public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.TypeDef.Switch<Object, E> variantVisit$TypeDef = new de.peeeq.wurstscript.ast.TypeDef.Switch<Object, E>() { public final Object CaseNativeType(de.peeeq.wurstscript.ast.NativeType term) throws E { visit(term); return null; } public final Object CaseClassDef(de.peeeq.wurstscript.ast.ClassDef term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.VisibilityModifier.Switch<Object, E> variantVisit$VisibilityModifier = new de.peeeq.wurstscript.ast.VisibilityModifier.Switch<Object, E>() { public final Object CaseVisibilityPublic(de.peeeq.wurstscript.ast.VisibilityPublic term) throws E { visit(term); return null; } public final Object CaseVisibilityPrivate(de.peeeq.wurstscript.ast.VisibilityPrivate term) throws E { visit(term); return null; } public final Object CaseVisibilityPublicread(de.peeeq.wurstscript.ast.VisibilityPublicread term) throws E { visit(term); return null; } public final Object CaseVisibilityProtected(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E { visit(term); return null; } public final Object CaseVisibilityDefault(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.WStatement.Switch<Object, E> variantVisit$WStatement = new de.peeeq.wurstscript.ast.WStatement.Switch<Object, E>() { public final Object CaseStmtIf(de.peeeq.wurstscript.ast.StmtIf term) throws E { visit(term); return null; } public final Object CaseStmtWhile(de.peeeq.wurstscript.ast.StmtWhile term) throws E { visit(term); return null; } public final Object CaseStmtLoop(de.peeeq.wurstscript.ast.StmtLoop term) throws E { visit(term); return null; } public final Object CaseLocalVarDef(de.peeeq.wurstscript.ast.LocalVarDef term) throws E { visit(term); return null; } public final Object CaseStmtSet(de.peeeq.wurstscript.ast.StmtSet term) throws E { visit(term); return null; } public final Object CaseStmtReturn(de.peeeq.wurstscript.ast.StmtReturn term) throws E { visit(term); return null; } public final Object CaseStmtDestroy(de.peeeq.wurstscript.ast.StmtDestroy term) throws E { visit(term); return null; } public final Object CaseStmtIncRefCount(de.peeeq.wurstscript.ast.StmtIncRefCount term) throws E { visit(term); return null; } public final Object CaseStmtDecRefCount(de.peeeq.wurstscript.ast.StmtDecRefCount term) throws E { visit(term); return null; } public final Object CaseStmtErr(de.peeeq.wurstscript.ast.StmtErr term) throws E { visit(term); return null; } public final Object CaseStmtExitwhen(de.peeeq.wurstscript.ast.StmtExitwhen term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.ClassMember term) throws E {
            term.Switch(variantVisit$ClassMember);
        }

        public final void visit(de.peeeq.wurstscript.ast.ClassSlot term) throws E {
            term.Switch(variantVisit$ClassSlot);
        }

        public final void visit(de.peeeq.wurstscript.ast.Expr term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAssignable term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAtomic term) throws E {
            term.Switch(variantVisit$OptExpr);
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

        public final void visit(de.peeeq.wurstscript.ast.TypeDef term) throws E {
            term.Switch(variantVisit$TypeDef);
        }

        public final void visit(de.peeeq.wurstscript.ast.VisibilityModifier term) throws E {
            term.Switch(variantVisit$VisibilityModifier);
        }

        public final void visit(de.peeeq.wurstscript.ast.WStatement term) throws E {
            term.Switch(variantVisit$WStatement);
        }
    }
}

