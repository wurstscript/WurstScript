package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface APackage extends KatjaTuple {

    //----- methods of APackage -----

    public java.lang.String name();
    public de.peeeq.pscript.ast.APackage replaceName(java.lang.String name);
    public de.peeeq.pscript.ast.AImports imports();
    public de.peeeq.pscript.ast.APackage replaceImports(de.peeeq.pscript.ast.AImports imports);
    public de.peeeq.pscript.ast.AElements elements();
    public de.peeeq.pscript.ast.APackage replaceElements(de.peeeq.pscript.ast.AElements elements);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.APackage replace(int pos, Object term);

    //----- nested classes of APackage -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.AImports term) throws E;
        public void visit(de.peeeq.pscript.ast.AElements term) throws E;
        public void visit(de.peeeq.pscript.ast.AImport term) throws E;
        public void visit(de.peeeq.pscript.ast.AElement term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeDef term) throws E;
        public void visit(de.peeeq.pscript.ast.AFuncDef term) throws E;
        public void visit(de.peeeq.pscript.ast.AVarDef term) throws E;
        public void visit(de.peeeq.pscript.ast.AInitBlock term) throws E;
        public void visit(de.peeeq.pscript.ast.ANativeType term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassDef term) throws E;
        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnType term) throws E;
        public void visit(de.peeeq.pscript.ast.AFormalParameters term) throws E;
        public void visit(de.peeeq.pscript.ast.ABlock term) throws E;
        public void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E;
        public void visit(java.lang.Boolean term) throws E;
        public void visit(de.peeeq.pscript.ast.AExpr term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassMembers term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturnsNothing term) throws E;
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
        public void visit(de.peeeq.pscript.ast.AFormalParameter term) throws E;
        public void visit(de.peeeq.pscript.ast.AStatement term) throws E;
        public void visit(de.peeeq.pscript.ast.AClassMember term) throws E;
        public void visit(de.peeeq.pscript.ast.APrefixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AInfixOp term) throws E;
        public void visit(de.peeeq.pscript.ast.AArguments term) throws E;
        public void visit(de.peeeq.pscript.ast.AIdentifier term) throws E;
        public void visit(java.lang.Integer term) throws E;
        public void visit(java.math.BigDecimal term) throws E;
        public void visit(de.peeeq.pscript.ast.AIf term) throws E;
        public void visit(de.peeeq.pscript.ast.AWhile term) throws E;
        public void visit(de.peeeq.pscript.ast.AReturn term) throws E;
        public void visit(de.peeeq.pscript.ast.AVoidReturn term) throws E;
        public void visit(de.peeeq.pscript.ast.AAssignment term) throws E;
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
        public void visit(de.peeeq.pscript.ast.APackage term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.APackage.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.pscript.ast.AClassMember.Switch<Object, E> variantVisit$AClassMember = new de.peeeq.pscript.ast.AClassMember.Switch<Object, E>() { public final Object CaseAVarDef(de.peeeq.pscript.ast.AVarDef term) throws E { visit(term); return null; } public final Object CaseAFuncDef(de.peeeq.pscript.ast.AFuncDef term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AElement.Switch<Object, E> variantVisit$AElement = new de.peeeq.pscript.ast.AElement.Switch<Object, E>() { public final Object CaseAFuncDef(de.peeeq.pscript.ast.AFuncDef term) throws E { visit(term); return null; } public final Object CaseAVarDef(de.peeeq.pscript.ast.AVarDef term) throws E { visit(term); return null; } public final Object CaseAInitBlock(de.peeeq.pscript.ast.AInitBlock term) throws E { visit(term); return null; } public final Object CaseANativeType(de.peeeq.pscript.ast.ANativeType term) throws E { visit(term); return null; } public final Object CaseAClassDef(de.peeeq.pscript.ast.AClassDef term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AExpr.Switch<Object, E> variantVisit$AExpr = new de.peeeq.pscript.ast.AExpr.Switch<Object, E>() { public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AInfixOp.Switch<Object, E> variantVisit$AInfixOp = new de.peeeq.pscript.ast.AInfixOp.Switch<Object, E>() { public final Object CaseAEqEq(de.peeeq.pscript.ast.AEqEq term) throws E { visit(term); return null; } public final Object CaseAGt(de.peeeq.pscript.ast.AGt term) throws E { visit(term); return null; } public final Object CaseAGtEq(de.peeeq.pscript.ast.AGtEq term) throws E { visit(term); return null; } public final Object CaseALt(de.peeeq.pscript.ast.ALt term) throws E { visit(term); return null; } public final Object CaseALtEq(de.peeeq.pscript.ast.ALtEq term) throws E { visit(term); return null; } public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseAMult(de.peeeq.pscript.ast.AMult term) throws E { visit(term); return null; } public final Object CaseADiv(de.peeeq.pscript.ast.ADiv term) throws E { visit(term); return null; } public final Object CaseADivInt(de.peeeq.pscript.ast.ADivInt term) throws E { visit(term); return null; } public final Object CaseAModulo(de.peeeq.pscript.ast.AModulo term) throws E { visit(term); return null; } public final Object CaseADot(de.peeeq.pscript.ast.ADot term) throws E { visit(term); return null; } public final Object CaseAAnd(de.peeeq.pscript.ast.AAnd term) throws E { visit(term); return null; } public final Object CaseAOr(de.peeeq.pscript.ast.AOr term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.APrefixOp.Switch<Object, E> variantVisit$APrefixOp = new de.peeeq.pscript.ast.APrefixOp.Switch<Object, E>() { public final Object CaseAPlus(de.peeeq.pscript.ast.APlus term) throws E { visit(term); return null; } public final Object CaseAMinus(de.peeeq.pscript.ast.AMinus term) throws E { visit(term); return null; } public final Object CaseANot(de.peeeq.pscript.ast.ANot term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AReturnType.Switch<Object, E> variantVisit$AReturnType = new de.peeeq.pscript.ast.AReturnType.Switch<Object, E>() { public final Object CaseAReturnsNothing(de.peeeq.pscript.ast.AReturnsNothing term) throws E { visit(term); return null; } public final Object CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) throws E { visit(term); return null; } };
        private final de.peeeq.pscript.ast.AStatement.Switch<Object, E> variantVisit$AStatement = new de.peeeq.pscript.ast.AStatement.Switch<Object, E>() { public final Object CaseABlock(de.peeeq.pscript.ast.ABlock term) throws E { visit(term); return null; } public final Object CaseAIf(de.peeeq.pscript.ast.AIf term) throws E { visit(term); return null; } public final Object CaseAWhile(de.peeeq.pscript.ast.AWhile term) throws E { visit(term); return null; } public final Object CaseAReturn(de.peeeq.pscript.ast.AReturn term) throws E { visit(term); return null; } public final Object CaseAVoidReturn(de.peeeq.pscript.ast.AVoidReturn term) throws E { visit(term); return null; } public final Object CaseAAssignment(de.peeeq.pscript.ast.AAssignment term) throws E { visit(term); return null; } public final Object CaseAPrefix(de.peeeq.pscript.ast.APrefix term) throws E { visit(term); return null; } public final Object CaseAInfix(de.peeeq.pscript.ast.AInfix term) throws E { visit(term); return null; } public final Object CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) throws E { visit(term); return null; } public final Object CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) throws E { visit(term); return null; } public final Object CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) throws E { visit(term); return null; } public final Object CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) throws E { visit(term); return null; } public final Object CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) throws E { visit(term); return null; } public final Object CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) throws E { visit(term); return null; } public final Object CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) throws E { visit(term); return null; } public final Object CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) throws E { visit(term); return null; } public final Object CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.pscript.ast.AClassMember term) throws E {
            term.Switch(variantVisit$AClassMember);
        }

        public final void visit(de.peeeq.pscript.ast.AElement term) throws E {
            term.Switch(variantVisit$AElement);
        }

        public final void visit(de.peeeq.pscript.ast.AExpr term) throws E {
            term.Switch(variantVisit$AStatement);
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

        public final void visit(de.peeeq.pscript.ast.ATypeDef term) throws E {
            term.Switch(variantVisit$AElement);
        }

        public final void visit(de.peeeq.pscript.ast.ATypeExpr term) throws E {
            term.Switch(variantVisit$AReturnType);
        }
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.APackage {

        //----- attributes of Impl -----

        private java.lang.String _name = null;
        private de.peeeq.pscript.ast.AImports _imports = null;
        private de.peeeq.pscript.ast.AElements _elements = null;

        //----- methods of Impl -----

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.pscript.ast.APackage replaceName(java.lang.String name) {
            return replace(0, name);
        }

        public de.peeeq.pscript.ast.AImports imports() {
            return _imports;
        }

        public de.peeeq.pscript.ast.APackage replaceImports(de.peeeq.pscript.ast.AImports imports) {
            return replace(1, imports);
        }

        public de.peeeq.pscript.ast.AElements elements() {
            return _elements;
        }

        public de.peeeq.pscript.ast.APackage replaceElements(de.peeeq.pscript.ast.AElements elements) {
            return replace(2, elements);
        }

        Impl(java.lang.String name, de.peeeq.pscript.ast.AImports imports, de.peeeq.pscript.ast.AElements elements) {
            if(name == null)
                throw new IllegalArgumentException("constructor of sort APackage invoked with null parameter name");
            if(imports == null)
                throw new IllegalArgumentException("constructor of sort APackage invoked with null parameter imports");
            if(elements == null)
                throw new IllegalArgumentException("constructor of sort APackage invoked with null parameter elements");

            this._name = name;
            this._imports = imports;
            this._elements = elements;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 3;

            switch(ith) {
                case 0: return _name;
                case 1: return _imports;
                case 2: return _elements;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort APackage invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort APackage invoked with too large parameter "+i+", sort has only 3 components");
                    }
            }
        }

        public int size() {
            return 3;
        }

        public de.peeeq.pscript.ast.APackage replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort APackage invoked with negative parameter "+pos);
            if(pos >= 3)
                throw new IllegalArgumentException("replace on sort APackage invoked with too large parameter "+pos+", sort has only 3 components");

            if(pos == 0 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort APackage invoked with term of incorrect sort, String expected");
            if(pos == 1 && !(term instanceof de.peeeq.pscript.ast.AImports))
                throw new IllegalArgumentException("replace on sort APackage invoked with term of incorrect sort, AImports expected");
            if(pos == 2 && !(term instanceof de.peeeq.pscript.ast.AElements))
                throw new IllegalArgumentException("replace on sort APackage invoked with term of incorrect sort, AElements expected");

            return (de.peeeq.pscript.ast.APackage) pscriptAST.unique(new de.peeeq.pscript.ast.APackage.Impl(
                pos == 0 ? (java.lang.String) term : _name,
                pos == 1 ? (de.peeeq.pscript.ast.AImports) term : _imports,
                pos == 2 ? (de.peeeq.pscript.ast.AElements) term : _elements
            ));
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.APackage");
            builder.append("( ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _imports.toJavaCode(builder);
            builder.append(", ");
            _elements.toJavaCode(builder);
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("APackage");
            builder.append("( ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            _imports.toString(builder);
            builder.append(", ");
            _elements.toString(builder);
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "APackage";
        }
    }
}

