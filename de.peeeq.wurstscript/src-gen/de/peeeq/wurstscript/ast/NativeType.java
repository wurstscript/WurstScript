package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.io.IOException;

public interface NativeType extends de.peeeq.wurstscript.ast.JassToplevelDeclaration, de.peeeq.wurstscript.ast.TypeDef, KatjaTuple {

    //----- methods of NativeType -----

    public de.peeeq.wurstscript.ast.WPos source();
    public de.peeeq.wurstscript.ast.NativeType replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public java.lang.String name();
    public de.peeeq.wurstscript.ast.NativeType replaceName(java.lang.String name);
    public de.peeeq.wurstscript.ast.OptTypeExpr typ();
    public de.peeeq.wurstscript.ast.NativeType replaceTyp(de.peeeq.wurstscript.ast.OptTypeExpr typ);
    public Object get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.NativeType replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.JassToplevelDeclaration.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TypeDef.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TopLevelDeclaration.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WEntity.Switch<CT, E> switchClass) throws E;

    //----- nested classes of NativeType -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPos term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExpr term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizes term) throws E;
        public void visit(de.peeeq.wurstscript.ast.Expr term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.NativeType term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.NativeType.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.Expr.Switch<Object, E> variantVisit$Expr = new de.peeeq.wurstscript.ast.Expr.Switch<Object, E>() { public final Object CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) throws E { visit(term); return null; } public final Object CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) throws E { visit(term); return null; } public final Object CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) throws E { visit(term); return null; } public final Object CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) throws E { visit(term); return null; } public final Object CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) throws E { visit(term); return null; } public final Object CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) throws E { visit(term); return null; } public final Object CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) throws E { visit(term); return null; } public final Object CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) throws E { visit(term); return null; } public final Object CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) throws E { visit(term); return null; } public final Object CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) throws E { visit(term); return null; } public final Object CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) throws E { visit(term); return null; } public final Object CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) throws E { visit(term); return null; } public final Object CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) throws E { visit(term); return null; } public final Object CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E> variantVisit$OpBinary = new de.peeeq.wurstscript.ast.OpBinary.Switch<Object, E>() { public final Object CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) throws E { visit(term); return null; } public final Object CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) throws E { visit(term); return null; } public final Object CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) throws E { visit(term); return null; } public final Object CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) throws E { visit(term); return null; } public final Object CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) throws E { visit(term); return null; } public final Object CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) throws E { visit(term); return null; } public final Object CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) throws E { visit(term); return null; } public final Object CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) throws E { visit(term); return null; } public final Object CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } public final Object CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) throws E { visit(term); return null; } public final Object CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) throws E { visit(term); return null; } public final Object CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) throws E { visit(term); return null; } public final Object CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) throws E { visit(term); return null; } public final Object CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E> variantVisit$OpUnary = new de.peeeq.wurstscript.ast.OpUnary.Switch<Object, E>() { public final Object CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) throws E { visit(term); return null; } public final Object CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E> variantVisit$OptTypeExpr = new de.peeeq.wurstscript.ast.OptTypeExpr.Switch<Object, E>() { public final Object CaseNoTypeExpr(de.peeeq.wurstscript.ast.NoTypeExpr term) throws E { visit(term); return null; } public final Object CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) throws E { visit(term); return null; } };

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

        public final void visit(de.peeeq.wurstscript.ast.OptTypeExpr term) throws E {
            term.Switch(variantVisit$OptTypeExpr);
        }
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.wurstscript.ast.NativeType {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPos _source = null;
        private java.lang.String _name = null;
        private de.peeeq.wurstscript.ast.OptTypeExpr _typ = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.WPos source() {
            return _source;
        }

        public de.peeeq.wurstscript.ast.NativeType replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(0, source);
        }

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.wurstscript.ast.NativeType replaceName(java.lang.String name) {
            return replace(1, name);
        }

        public de.peeeq.wurstscript.ast.OptTypeExpr typ() {
            return _typ;
        }

        public de.peeeq.wurstscript.ast.NativeType replaceTyp(de.peeeq.wurstscript.ast.OptTypeExpr typ) {
            return replace(2, typ);
        }

        Impl(de.peeeq.wurstscript.ast.WPos source, java.lang.String name, de.peeeq.wurstscript.ast.OptTypeExpr typ) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort NativeType invoked with null parameter source");
            if(name == null)
                throw new IllegalArgumentException("constructor of sort NativeType invoked with null parameter name");
            if(typ == null)
                throw new IllegalArgumentException("constructor of sort NativeType invoked with null parameter typ");

            this._source = source;
            this._name = name;
            this._typ = typ;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 3;

            switch(ith) {
                case 0: return _source;
                case 1: return _name;
                case 2: return _typ;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort NativeType invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort NativeType invoked with too large parameter "+i+", sort has only 3 components");
                    }
            }
        }

        public int size() {
            return 3;
        }

        public de.peeeq.wurstscript.ast.NativeType replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort NativeType invoked with negative parameter "+pos);
            if(pos >= 3)
                throw new IllegalArgumentException("replace on sort NativeType invoked with too large parameter "+pos+", sort has only 3 components");

            if(pos == 0 && !(term instanceof de.peeeq.wurstscript.ast.WPos))
                throw new IllegalArgumentException("replace on sort NativeType invoked with term of incorrect sort, WPos expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort NativeType invoked with term of incorrect sort, String expected");
            if(pos == 2 && !(term instanceof de.peeeq.wurstscript.ast.OptTypeExpr))
                throw new IllegalArgumentException("replace on sort NativeType invoked with term of incorrect sort, OptTypeExpr expected");

            return (de.peeeq.wurstscript.ast.NativeType) AST.unique(new de.peeeq.wurstscript.ast.NativeType.Impl(
                pos == 0 ? (de.peeeq.wurstscript.ast.WPos) term : _source,
                pos == 1 ? (java.lang.String) term : _name,
                pos == 2 ? (de.peeeq.wurstscript.ast.OptTypeExpr) term : _typ
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.JassToplevelDeclaration.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseNativeType(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TypeDef.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseNativeType(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TopLevelDeclaration.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseNativeType(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WEntity.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseNativeType(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.NativeType");
            builder.append("( ");
            _source.toJavaCode(builder);
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _typ.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("NativeType");
            builder.append("( ");
            _source.toString(builder);
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _typ.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "NativeType";
        }
    }
}

