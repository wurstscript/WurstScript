package de.peeeq.wurstscript.ast;

import java.io.IOException;

import katja.common.KatjaTuple;
import katja.common.KatjaTupleImpl;

public interface GlobalVarDef extends de.peeeq.wurstscript.ast.ClassMember, de.peeeq.wurstscript.ast.VarDef, de.peeeq.wurstscript.ast.WEntity, KatjaTuple {

    //----- methods of GlobalVarDef -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.GlobalVarDef replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.VisibilityModifier visibility();
    public de.peeeq.wurstscript.ast.GlobalVarDef replaceVisibility(de.peeeq.wurstscript.ast.VisibilityModifier visibility);
    public java.lang.Boolean isConstant();
    public de.peeeq.wurstscript.ast.GlobalVarDef replaceIsConstant(java.lang.Boolean isConstant);
    public de.peeeq.wurstscript.ast.OptTypeExpr typ();
    public de.peeeq.wurstscript.ast.GlobalVarDef replaceTyp(de.peeeq.wurstscript.ast.OptTypeExpr typ);
    public java.lang.String name();
    public de.peeeq.wurstscript.ast.GlobalVarDef replaceName(java.lang.String name);
    public de.peeeq.wurstscript.ast.OptExpr initialExpr();
    public de.peeeq.wurstscript.ast.GlobalVarDef replaceInitialExpr(de.peeeq.wurstscript.ast.OptExpr initialExpr);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.GlobalVarDef replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ClassMember.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VarDef.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WEntity.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ClassSlot.Switch<CT, E> switchClass) throws E;

    //----- nested classes of GlobalVarDef -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityModifier term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublic term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPrivate term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublicread term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
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
        public void visit(java.lang.Integer term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnary term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Indexes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Arguments term) throws E;
        public void visit(java.lang.Double term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.GlobalVarDef term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.GlobalVarDef.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.Expr.Switch<Object, E> variantVisit$Expr = new de.peeeq.wurstscript.ast.Expr.Switch<Object, E>() { public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprCast(de.peeeq.wurstscript.ast.ExprCast term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E> variantVisit$OptExpr = new de.peeeq.wurstscript.ast.OptExpr.Switch<Object, E>() { public final Object CaseNoExpr(de.peeeq.wurstscript.ast.NoExpr term) throws E { visit(term); return null; } public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprCast(de.peeeq.wurstscript.ast.ExprCast term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E> variantVisit$OptTypeExpr = new de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E>() { public final Object CaseNoTypeExpr(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E { visit(term); return null; } public final Object CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.VisibilityModifier.Switch<Object, E> variantVisit$VisibilityModifier = new de.peeeq.wurstscript.ast.VisibilityModifier.Switch<Object, E>() { public final Object CaseVisibilityPublic(de.peeeq.wurstscript.ast.VisibilityPublic term) throws E { visit(term); return null; } public final Object CaseVisibilityPrivate(de.peeeq.wurstscript.ast.VisibilityPrivate term) throws E { visit(term); return null; } public final Object CaseVisibilityPublicread(de.peeeq.wurstscript.ast.VisibilityPublicread term) throws E { visit(term); return null; } public final Object CaseVisibilityProtected(de.peeeq.wurstscript.ast.VisibilityProtected term) throws E { visit(term); return null; } public final Object CaseVisibilityDefault(de.peeeq.wurstscript.ast.VisibilityDefault term) throws E { visit(term); return null; } };

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

        public final void visit(de.peeeq.wurstscript.ast.OptExpr term) throws E {
            term.Switch(variantVisit$OptExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E {
            term.Switch(variantVisit$OptTypeExpr);
        }

        public final void visit(de.peeeq.wurstscript.ast.VisibilityModifier term) throws E {
            term.Switch(variantVisit$VisibilityModifier);
        }
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.GlobalVarDef {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private de.peeeq.wurstscript.ast.VisibilityModifier _visibility = null;
        private java.lang.Boolean _isConstant = null;
        private de.peeeq.wurstscript.ast.OptTypeExpr _typ = null;
        private java.lang.String _name = null;
        private de.peeeq.wurstscript.ast.OptExpr _initialExpr = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.GlobalVarDef replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public de.peeeq.wurstscript.ast.VisibilityModifier visibility() {
            return _visibility;
        }

        public de.peeeq.wurstscript.ast.GlobalVarDef replaceVisibility(de.peeeq.wurstscript.ast.VisibilityModifier visibility) {
            return replace(1, visibility);
        }

        public java.lang.Boolean isConstant() {
            return _isConstant;
        }

        public de.peeeq.wurstscript.ast.GlobalVarDef replaceIsConstant(java.lang.Boolean isConstant) {
            return replace(2, isConstant);
        }

        public de.peeeq.wurstscript.ast.OptTypeExpr typ() {
            return _typ;
        }

        public de.peeeq.wurstscript.ast.GlobalVarDef replaceTyp(de.peeeq.wurstscript.ast.OptTypeExpr typ) {
            return replace(3, typ);
        }

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.wurstscript.ast.GlobalVarDef replaceName(java.lang.String name) {
            return replace(4, name);
        }

        public de.peeeq.wurstscript.ast.OptExpr initialExpr() {
            return _initialExpr;
        }

        public de.peeeq.wurstscript.ast.GlobalVarDef replaceInitialExpr(de.peeeq.wurstscript.ast.OptExpr initialExpr) {
            return replace(5, initialExpr);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.VisibilityModifier visibility, java.lang.Boolean isConstant, de.peeeq.wurstscript.ast.OptTypeExpr typ, java.lang.String name, de.peeeq.wurstscript.ast.OptExpr initialExpr) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort GlobalVarDef invoked with null parameter source");
            if(visibility == null)
                throw new IllegalArgumentException("constructor of sort GlobalVarDef invoked with null parameter visibility");
            if(isConstant == null)
                throw new IllegalArgumentException("constructor of sort GlobalVarDef invoked with null parameter isConstant");
            if(typ == null)
                throw new IllegalArgumentException("constructor of sort GlobalVarDef invoked with null parameter typ");
            if(name == null)
                throw new IllegalArgumentException("constructor of sort GlobalVarDef invoked with null parameter name");
            if(initialExpr == null)
                throw new IllegalArgumentException("constructor of sort GlobalVarDef invoked with null parameter initialExpr");

            this._source = source;
            this._visibility = visibility;
            this._isConstant = isConstant;
            this._typ = typ;
            this._name = name;
            this._initialExpr = initialExpr;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 6;

            switch(ith) {
                case 0: return _source;
                case 1: return _visibility;
                case 2: return _isConstant;
                case 3: return _typ;
                case 4: return _name;
                case 5: return _initialExpr;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort GlobalVarDef invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort GlobalVarDef invoked with too large parameter "+i+", sort has only 6 components");
                    }
            }
        }

        public int size() {
            return 6;
        }

        public de.peeeq.wurstscript.ast.GlobalVarDef replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with negative parameter "+pos);
            if(pos >= 6)
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with too large parameter "+pos+", sort has only 6 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof de.peeeq.wurstscript.ast.VisibilityModifier))
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with term of incorrect sort, VisibilityModifier expected");
            if(pos == 2 && !(term instanceof java.lang.Boolean))
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with term of incorrect sort, Boolean expected");
            if(pos == 3 && !(term instanceof de.peeeq.wurstscript.ast.OptTypeExpr))
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with term of incorrect sort, OptTypeExpr expected");
            if(pos == 4 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with term of incorrect sort, String expected");
            if(pos == 5 && !(term instanceof de.peeeq.wurstscript.ast.OptExpr))
                throw new IllegalArgumentException("replace on sort GlobalVarDef invoked with term of incorrect sort, OptExpr expected");

            return (de.peeeq.wurstscript.ast.GlobalVarDef) AST.unique(new de.peeeq.wurstscript.ast.GlobalVarDef.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (de.peeeq.wurstscript.ast.VisibilityModifier) term : _visibility,
                pos == 2 ? (java.lang.Boolean) term : _isConstant,
                pos == 3 ? (de.peeeq.wurstscript.ast.OptTypeExpr) term : _typ,
                pos == 4 ? (java.lang.String) term : _name,
                pos == 5 ? (de.peeeq.wurstscript.ast.OptExpr) term : _initialExpr
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ClassMember.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseGlobalVarDef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VarDef.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseGlobalVarDef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WEntity.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseGlobalVarDef(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.ClassSlot.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseGlobalVarDef(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.GlobalVarDef");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            _visibility.toJavaCode(builder);
            builder.append(", ");
            builder.append(_isConstant.toString());
            builder.append(", ");
            _typ.toJavaCode(builder);
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _initialExpr.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("GlobalVarDef");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            _visibility.toString(builder);
            builder.append(", ");
            builder.append(_isConstant.toString());
            builder.append(", ");
            _typ.toString(builder);
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _initialExpr.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "GlobalVarDef";
        }
    }
}

