package de.peeeq.wurstscript.attributes;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * this attribute find the variable definition for every variable reference
 */
public class AttrExprType {


    public static WurstType calculate(ExprIntVal term) {
        return WurstTypeIntLiteral.instance();
    }


    public static WurstType calculate(ExprRealVal term) {
        return WurstTypeReal.instance();
    }


    public static WurstType calculate(ExprStringVal term) {
        return WurstTypeString.instance();
    }


    public static WurstType calculate(ExprBoolVal term) {
        return WurstTypeBool.instance();
    }


    public static WurstType calculate(ExprFuncRef term) {
        return WurstTypeCode.instance();
    }


    public static WurstType calculate(ExprVarAccess term) {
        NameLink varDef = term.attrNameLink();

        if (varDef == null) {
            String varName = term.getVarName();
            if (varName.startsWith("gg_rct_")) {
                return getHandleType(term, "rect");
            } else if (varName.startsWith("gg_trg_")) {
                return getHandleType(term, "trigger");
            } else if (varName.startsWith("gg_unit_")) {
                return getHandleType(term, "unit");
            } else if (varName.startsWith("gg_dest_")) {
                return getHandleType(term, "destructable");
            } else if (varName.startsWith("gg_cam_")) {
                return getHandleType(term, "camerasetup");
            } else if (varName.startsWith("gg_snd_")) {
                return getHandleType(term, "sound");
            } else if (varName.startsWith("gg_item_")) {
                return getHandleType(term, "item");
            }

            return WurstTypeUnknown.instance();
        }
        if (varDef.getDef() instanceof VarDef) {
            if (Utils.getParentVarDef(Optional.of(term)) == Optional.of((VarDef) varDef.getDef())) {
                term.addError("Recursive variable definition is not allowed.");
                return WurstTypeUnknown.instance();
            }
        }
        if (varDef.getDef() instanceof FunctionDefinition) {
            term.addError("Missing parantheses for function call");
        }
        return varDef.getTyp();
    }


    private static WurstType getHandleType(Element node, String typeName) {
        TypeDef def = node.lookupType(typeName);
        if (def != null) {
            return def.attrTyp().dynamic();
        } else {
            return WurstTypeUnknown.instance();
        }
    }


    public static WurstType calculate(ExprVarArrayAccess term) {
        NameLink varDef = term.attrNameLink();
        if (varDef == null) {
            return WurstTypeUnknown.instance();
        }

        WurstType varDefType = varDef.getTyp();
        if (varDefType instanceof WurstTypeArray) {
            return ((WurstTypeArray) varDefType).getBaseType();
        } else {
            term.addError("Variable " + varDef.getName() + " is of type " + varDefType + ", should be an array variable.");
        }
        return WurstTypeUnknown.instance();
    }

    public static WurstType calculate(ExprThis term) {
        return caclulateThistype(term, true, "this");
    }

    /**
     * calculates the type of 'thistype' or 'this'
     *
     * @param term         where the expr 'this' or 'thistype' is used
     * @param dynamic      true when searching for 'this'
     * @param searchedTerm what to display in error messages, null for no error messages
     */
    public static WurstType caclulateThistype(Element term, boolean dynamic, @Nullable String searchedTerm) {
        boolean showErrors = searchedTerm != null;
        if (term.getParent() == null) {
            // not attached to the tree -> generated
            throw new CompileError(term.attrSource(), "Expression 'this' not attached to AST.");
        }

        // check if we are in an extension function
        FunctionImplementation func = term.attrNearestFuncDef();
        if (func instanceof ExtensionFuncDef) {
            ExtensionFuncDef extensionFuncDef = (ExtensionFuncDef) func;
            return extensionFuncDef.getExtendedType().attrTyp().dynamic();
        }
        if (dynamic && !term.attrIsDynamicContext()) {
            if (showErrors) {
                term.addError("Cannot use '" + searchedTerm + "' in static methods.");
            }
            return WurstTypeUnknown.instance();
        }

        // find nearest class-like thing
        NamedScope c = term.attrNearestNamedScope();
        if (c != null) {
            return c.match(new NamedScope.Matcher<WurstType>() {

                @SuppressWarnings("null")
                @Override
                public WurstType case_ModuleDef(ModuleDef moduleDef) {
                    return new WurstTypeModule(moduleDef, false);
                }

                @SuppressWarnings("null")
                @Override
                public WurstType case_ClassDef(ClassDef classDef) {
                    WurstTypeClass result = (WurstTypeClass) classDef.attrTyp().dynamic();
                    List<WurstTypeBoundTypeParam> boundTypes = new ArrayList<>();
                    for (TypeParamDef tp : classDef.getTypeParameters()) {
                        boundTypes.add(new WurstTypeBoundTypeParam(tp, tp.attrTyp(), tp));
                    }
                    return result.replaceTypeVars(boundTypes);
                }

                @SuppressWarnings("null")
                @Override
                public WurstType case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
                    ClassOrModule parent = moduleInstanciation.attrNearestClassOrModule();
                    return parent.attrTyp().dynamic();
                }

                @SuppressWarnings("null")
                @Override
                public WurstType case_WPackage(WPackage wPackage) {
                    // 'this' cannot be used on package level
                    return WurstTypeUnknown.instance();
                }

                @SuppressWarnings("null")
                @Override
                public WurstType case_InterfaceDef(InterfaceDef interfaceDef) {
                    WurstTypeInterface result = (WurstTypeInterface) interfaceDef.attrTyp().dynamic();
                    List<WurstTypeBoundTypeParam> boundTypes = new ArrayList<>();
                    for (TypeParamDef tp : interfaceDef.getTypeParameters()) {
                        boundTypes.add(new WurstTypeBoundTypeParam(tp, tp.attrTyp(), tp));
                    }
                    return result.replaceTypeVars(boundTypes);
                }

                @SuppressWarnings("null")
                @Override
                public WurstType case_EnumDef(EnumDef enumDef) {
                    // 'this' cannot be used in enums
                    return WurstTypeUnknown.instance();
                }

            });
        } else {
            if (showErrors) {
                term.addError("The keyword '" + searchedTerm + "' can only be used inside methods.");
            }
            return WurstTypeUnknown.instance();
        }
    }


    public static WurstType calculate(final ExprBinary term) {
        final WurstType leftType = term.getLeft().attrTyp();
        final WurstType rightType = term.getRight().attrTyp();
        switch (term.getOp()) {
            case AND:
            case OR:
                return requireEqualTypes(term, WurstTypeBool.instance(), WurstTypeBool.instance());
            case EQ:
            case NOTEQ: {
                if (leftType.equalsType(rightType, term)) {
                    return WurstTypeBool.instance();
                }

                if (leftType.isSubtypeOf(rightType, term) || rightType.isSubtypeOf(leftType, term)) {
                    return WurstTypeBool.instance();
                }

                if (Utils.isJassCode(Optional.of(term))) {
                    if (leftType.isSubtypeOf(WurstTypeReal.instance(), term) || leftType.isSubtypeOf(WurstTypeInt.instance(), term)) {
                        if (rightType.isSubtypeOf(WurstTypeReal.instance(), term) || rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
                            // in jass code it is allowed to compare reals and ints
                            return WurstTypeBool.instance();
                        }
                    }

                    if (leftType.isSubtypeOf(WurstTypeNull.instance(), term) && rightType.isSubtypeOf(WurstTypeInt.instance(), term)
                            || leftType.isSubtypeOf(WurstTypeInt.instance(), term) && rightType.isSubtypeOf(WurstTypeNull.instance(), term)) {
                        // in jass code it is allowed to compare an integer with 'null'. wat?
                        return WurstTypeBool.instance();
                    }

                    if (leftType.isSubtypeOf(WurstNativeType.instance("agent", WurstTypeNull.instance()), term) && rightType.isSubtypeOf(WurstNativeType.instance("agent", WurstTypeNull.instance()), term)) {
                        // in jass code it is allowed to compare agents
                        return WurstTypeBool.instance();
                    }
                }


                // TODO check if the intersection of the basetypes of lefttpye and righttype is
                // not empty. Example:
                // class A implements B,C
                // -> B and C should be comparable
                term.addError("Cannot compare types " + leftType + " with " + rightType);

                return WurstTypeBool.instance();
            }

            case GREATER:
            case GREATER_EQ:
            case LESS:
            case LESS_EQ: {
                if (!(leftType.isSubtypeOf(WurstTypeInt.instance(), term)
                        || leftType.isSubtypeOf(WurstTypeReal.instance(), term))) {
                    term.getLeft().addError("Can not compare with value of type " + leftType);
                }
                if (!(rightType.isSubtypeOf(WurstTypeInt.instance(), term)
                        || rightType.isSubtypeOf(WurstTypeReal.instance(), term))) {
                    term.getRight().addError("Can not compare with value of type " + rightType);
                }
                return WurstTypeBool.instance();
            }
            case PLUS:
                if (leftType instanceof WurstTypeString && rightType instanceof WurstTypeString) {
                    return WurstTypeString.instance();
                }
                if (bothTypesRealOrInt(term)) {
                    return caseMathOperation(term);
                } else {
                    return handleOperatorOverloading(term);
                }
            case MINUS:
            case MULT:
                if (bothTypesRealOrInt(term)) {
                    return caseMathOperation(term);
                } else {
                    return handleOperatorOverloading(term);
                }
            case DIV_REAL:
                if (Utils.isJassCode(Optional.of(term))) {
                    return caseMathOperation(term);
                } else if (bothTypesRealOrInt(term)) {
                    return WurstTypeReal.instance();
                } else {
                    return handleOperatorOverloading(term);
                }
            case MOD_REAL:
                if (leftType.isSubtypeOf(WurstTypeReal.instance(), term) || leftType.isSubtypeOf(WurstTypeInt.instance(), term)) {
                    if (rightType.isSubtypeOf(WurstTypeReal.instance(), term) || rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
                        return WurstTypeReal.instance();
                    }
                }
                term.addError("Operator " + term.getOp() + " is not defined for " +
                        "operands " + leftType + " and " + rightType);
                return WurstTypeUnknown.instance();
            case MOD_INT:
            case DIV_INT:
                if (leftType.isSubtypeOf(WurstTypeInt.instance(), term) && rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
                    return leftType;
                }
                term.addError("Operator " + term.getOp() + " is not defined for " +
                        "operands " + leftType + " and " + rightType);
                return WurstTypeUnknown.instance();

            case NOT:
            case UNARY_MINUS:
        }
        throw new Error("unhandled op " + term.getOp());
    }


    private static WurstType caseMathOperation(ExprBinary term) {
        WurstType leftType = term.getLeft().attrTyp();
        WurstType rightType = term.getRight().attrTyp();
        if (leftType.isSubtypeOf(WurstTypeInt.instance(), term) && rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
            return WurstTypeInt.instance();
        }
        if (leftType.isSubtypeOf(WurstTypeReal.instance(), term) || leftType.isSubtypeOf(WurstTypeInt.instance(), term)) {
            if (rightType.isSubtypeOf(WurstTypeReal.instance(), term) || rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
                return WurstTypeReal.instance();
            }
        }
        term.addError("Operator " + term.getOp() + " is not defined for " +
                "operands " + leftType + " and " + rightType);
        return WurstTypeUnknown.instance();
    }

    private static WurstType handleOperatorOverloading(ExprBinary term) {
        WurstType leftType = term.getLeft().attrTyp();
        WurstType rightType = term.getRight().attrTyp();
        FuncLink def = term.attrFuncLink();
        if (def == null) {
            term.addError("No operator overloading function for operator " + term.getOp() +
                    " was found for operands " + leftType + " and " + rightType + ". The overloading function has to be named: " + term.getOp()
                    .getOverloadingFuncName());
            return WurstTypeUnknown.instance();
        }
        return def.getReturnType();
    }

    private static WurstType requireEqualTypes(ExprBinary term,
                                               WurstTypeBool requiredType, WurstTypeBool resultType) {
        final WurstType leftType = term.getLeft().attrTyp();
        final WurstType rightType = term.getRight().attrTyp();
        if (!leftType.isSubtypeOf(requiredType, term)) {
            term.getLeft().addError("Operator " + term.getOp() + " requires two operands of " +
                    "type " + requiredType + " but left type was " + leftType);
            return WurstTypeUnknown.instance();
        }
        if (!rightType.isSubtypeOf(requiredType, term)) {
            term.getRight().addError("Operator " + term.getOp() + " requires two operands of " +
                    "type " + requiredType + " but right type was " + leftType);
            return WurstTypeUnknown.instance();
        }
        return resultType;
    }

    private static boolean bothTypesRealOrInt(ExprBinary term) {
        WurstType leftType = term.getLeft().attrTyp();
        WurstType rightType = term.getRight().attrTyp();
        return ((leftType.isSubtypeOf(WurstTypeInt.instance(), term) || leftType.isSubtypeOf(WurstTypeReal.instance(), term))
                && (rightType.isSubtypeOf(WurstTypeInt.instance(), term) || rightType.isSubtypeOf(WurstTypeReal.instance(), term)));
    }


    public static WurstType calculate(final ExprUnary term) {
        final WurstType rightType = term.getRight().attrTyp();
        if (term.getOpU() == WurstOperator.NOT) {
            if (!(rightType instanceof WurstTypeBool)) {
                term.addError("Expected Boolean after not but found " + rightType);
            }
            return WurstTypeBool.instance();
        } else if (term.getOpU() == WurstOperator.UNARY_MINUS) {
            if (rightType.isSubtypeOf(WurstTypeInt.instance(), term) || rightType.isSubtypeOf(WurstTypeReal.instance(), term)) {
                return rightType;
            }
            term.addError("Expected Int or Real after Minus but found " + rightType);
            return WurstTypeReal.instance();
        }
        throw new Error("unhandled case: " + term.getOpU());
    }


    public static WurstType calculate(ExprMemberVarDot term) {
        NameLink varDef = term.attrNameLink();
        if (varDef == null) {
            return WurstTypeUnknown.instance();
        }
        if (varDef.getDef() instanceof FunctionDefinition) {
            term.addError("Missing parantheses for function call");
        }
        if (varDef.getDef().attrIsStatic() && !term.getLeft().attrTyp().isStaticRef()) {
            term.addError("Cannot access static variable " + term.getVarName() + " via a dynamic reference.");
        }
        return varDef.getTyp(); // TODO .setTypeArgs(term.getLeft().attrTyp().getTypeArgBinding());
    }


    public static WurstType calculate(ExprMemberArrayVarDot term) {
        NameLink varDef = term.attrNameLink();
        if (varDef == null) {
            return WurstTypeUnknown.instance();
        }
        if (varDef.getDef().attrIsStatic() && !term.getLeft().attrTyp().isStaticRef()) {
            term.addError("Cannot access static array variable " + term.getVarName() + " via a dynamic reference.");
        }
        WurstType typ = varDef.getTyp();
        if (typ instanceof WurstTypeArray) {
            WurstTypeArray ar = (WurstTypeArray) typ;
            return ar.getBaseType();
        }
        term.addError("Variable " + term.getVarName() + " is not an array.");
        return typ;
    }


    public static WurstType calculate(StmtCall term) {
        return term.attrFunctionSignature().getReturnType();
    }


    public static WurstType calculate(ExprNull term) {
        return WurstTypeNull.instance();
    }

    public static WurstType calculate(ExprCast term) {
        WurstType targetTyp = term.getTyp().attrTyp().dynamic();
        WurstType exprTyp = term.getExpr().attrTyp();
        if (targetTyp.isSubtypeOf(WurstTypeInt.instance(), term) && isCastableToInt(exprTyp)) {
            // cast from classtype to int: OK
        } else if (isCastableToInt(targetTyp) && exprTyp.isSubtypeOf(WurstTypeInt.instance(), term)) {
            // cast from int to classtype: OK
        } else {
            checkCastOrInstanceOf(term, exprTyp, targetTyp, "cast expression");
        }
        return targetTyp;
    }


    public static WurstType calculate(ExprIncomplete exprIncomplete) {
        return WurstTypeUnknown.instance();
    }


    protected static boolean isCastableToInt(WurstType typ) {
        return typ.isCastableToInt();
    }


    public static WurstType calculate(ExprInstanceOf e) {
        WurstType exprType = e.getExpr().attrTyp();
        WurstType targetType = e.getTyp().attrTyp();
        checkCastOrInstanceOf(e, exprType, targetType, "instanceof expression");
        return WurstTypeBool.instance();
    }


    private static void checkCastOrInstanceOf(Element e, WurstType exprType, WurstType targetType, String msgPre) {
        if (!exprType.canBeUsedInInstanceOf()) {
            e.addError(msgPre + " not defined for expression type " + exprType);
        }
        if (!targetType.canBeUsedInInstanceOf()) {
            e.addError(msgPre + " not defined for target type " + targetType);
        }
        if (exprType.isSubtypeOf(targetType, e)) {
            e.addError("This " + msgPre + " is useless");
        } else if (!exprType.isSupertypeOf(targetType, e)) {
            e.addError(msgPre + " is not allowed because types " + exprType + " and " + targetType + " are not directly related.\n" +
                    "Consider adding a cast to a common superType first.");
        }
    }


    public static WurstType calculate(ExprSuper e) {
        ClassDef cd = e.attrNearestClassDef();
        if (cd == null) {
            e.addError("'super' can only be used inside classes.");
        } else if (cd.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
            WurstTypeClass superType = (WurstTypeClass) cd.getExtendedClass().attrTyp();
            assert superType.isStaticRef();
            return superType;
        } else {
            e.addError("No super class found.");
        }
        return WurstTypeUnknown.instance();
    }


    public static WurstType calculate(ExprTypeId e) {
        WurstType exprTyp = e.getLeft().attrTyp();
        if (exprTyp instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface t = (WurstTypeClassOrInterface) exprTyp;
            if (t.isStaticRef()) {
                // static reference to a type --> only concrete classes allowed
                if (t instanceof WurstTypeClass) {
                    WurstTypeClass wtc = (WurstTypeClass) exprTyp;
                    if (wtc.getClassDef().attrIsAbstract()) {
                        e.addError("abstract classes do not have a typeId");
                    }
                } else {
                    e.addError(t + " does not have a typeId. Only classes have one.");
                }
            }
        } else {
            e.addError(exprTyp + " does not have a typeId, because it is not an object type.");
        }
        return WurstTypeInt.instance();
    }


    public static WurstType normalizedType(Expr e) {
        return e.attrTypRaw().normalize();
    }


    public static WurstType calculate(ExprClosure e) {
        List<WurstType> paramTypes = Lists.newArrayList();
        for (WShortParameter p : e.getShortParameters()) {
            paramTypes.add(p.attrTyp());
        }
        WurstType returnType = e.getImplementation().attrTyp();
        return new WurstTypeClosure(paramTypes, returnType);
    }


    public static WurstType calculate(ExprStatementsBlock e) {
        StmtReturn r = e.getReturnStmt();
        if (r != null) {
            if (r.getReturnedObj() instanceof Expr) {
                Expr re = (Expr) r.getReturnedObj();
                return re.attrTyp();
            }
        }
        return WurstTypeVoid.instance();
    }


    public static WurstType calculate(ExprDestroy e) {
        return WurstTypeVoid.instance();
    }


    public static WurstType calculate(ExprMemberArrayVarDotDot e) {
        e.addError("The dot-dot-operator can only be used with methods.");
        return WurstTypeUnknown.instance();
    }


    public static WurstType calculate(ExprMemberVarDotDot e) {
        e.addError("The dot-dot-operator can only be used with methods.");
        return WurstTypeUnknown.instance();
    }

    public static WurstType calculate(ExprMemberMethodDotDot e) {
        e.attrFunctionSignature();
        return e.getLeft().attrTyp();
    }


    public static WurstType calculate(ExprEmpty e) {
        e.addError("Missing expression");
        return new WurstTypeUnknown("empty");
    }

    public static WurstType calculate(ExprIfElse e) {
        if (!e.getCond().attrTyp().isSubtypeOf(WurstTypeBool.instance(), e)) {
            e.getCond().addError("Condition must be of type boolean, but found " + e.getCond().attrTyp());
        }
        WurstType tt = e.getIfTrue().attrTyp();
        if (tt.isSubtypeOf(WurstTypeVoid.instance(), e)) {
            e.getIfTrue().addError("Conditional expression must return a value, but result type of then-expression is void.");
        }
        WurstType tf = e.getIfFalse().attrTyp();
        if (tf.isSubtypeOf(WurstTypeVoid.instance(), e)) {
            e.getIfTrue().addError("Conditional expression must return a value, but result type of else-expression is void.");
        }
        WurstType resT = WurstTypeUnion.create(tt, tf, e);
        if (resT instanceof WurstTypeNull) {
            e.addError("Both branches of conditional expression have type null.");
        }
        return resT;
    }
}
