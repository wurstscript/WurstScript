package de.peeeq.wurstscript.attributes;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Optional;


/**
 * this attribute can give you the type of a variable definition
 */
public class AttrVarDefType {

    public static WurstType calculate(GlobalVarDef node) {
        return defaultCase(node);
    }

    public static WurstType calculate(LocalVarDef node) {
        return defaultCase(node);
    }

    public static WurstType calculate(WParameter node) {
        if (node.attrIsVararg()) {
            return new WurstTypeVararg(node.getTyp().attrTyp().dynamic());
        }
        return node.getTyp().attrTyp().dynamic();
    }

    public static WurstType calculate(WShortParameter p) {
        if (p.getTypOpt() instanceof TypeExpr) {
            return p.getTypOpt().attrTyp().dynamic();
        }
        // if the type of the lambda parameter is not specified,
        // we have to look up the expected type of the lambda
        ExprClosure parentClosure = (ExprClosure) p.getParent().getParent();
        int paramIndex = parentClosure.getShortParameters().indexOf(p);
//        WurstType expectedTyp = parentClosure.attrExpectedTypRaw();
//        return WurstTypeInfer.instance();
        WurstType expectedTyp = parentClosure.attrExpectedTyp();
        return getParameterTypeFromClosureType(p, paramIndex, expectedTyp, true);
    }

    public static WurstType getParameterTypeFromClosureType(WShortParameter p, int paramIndex, WurstType expectedTyp, boolean addError) {
        if (expectedTyp instanceof WurstTypeUnion) {
            WurstTypeUnion union = (WurstTypeUnion) expectedTyp;
            WurstType t1 = getParameterTypeFromClosureType(p, paramIndex, union.getTypeA(), addError);
            if (t1 instanceof WurstTypeInfer) {
                return t1;
            }
            WurstType t2 = getParameterTypeFromClosureType(p, paramIndex, union.getTypeB(), addError);
            return WurstTypeUnion.create(t1, t2, p);
        }

        FunctionSignature sig = AttrClosureAbstractMethod.getAbstractMethodSignature(expectedTyp);
        if (sig == null) {
            if (addError) {
                p.addError("Could not infer type for parameter " + p.getName() + ". " +
                    "The target type could not be uniquely determined for expected type " + expectedTyp + ".");
            }
            return WurstTypeInfer.instance();
        }

        if (sig.getParamTypes().size() <= paramIndex) {
            if (addError) {
                p.addError("Could not infer type for parameter " + p.getName() + ". " +
                    "Closure type " + expectedTyp + " does not take so many parameters.");
            }
            return WurstTypeInfer.instance();
        }
        // we call normalize here, because we do not want to get implicit conversions
        // for closures these are handled by creating wrapper functions
        return sig.getParamTypes().get(paramIndex).normalize();
    }

    public static WurstTypeClass calculate(ClassDef c) {
        List<WurstTypeBoundTypeParam> typeArgs = Lists.newArrayList();
        for (TypeParamDef tp : c.getTypeParameters()) {
            WurstTypeTypeParam typParam = new WurstTypeTypeParam(tp);
            typeArgs.add(new WurstTypeBoundTypeParam(tp, typParam, tp));
        }
        return new WurstTypeClass(c, typeArgs, true);
    }

    private static WurstType defaultCase(GlobalOrLocalVarDef v) {
        OptTypeExpr typ = v.getOptTyp();
        final VarInitialization initialExpr = v.getInitialExpr();
        if (typ instanceof TypeExpr) {
            return typ.attrTyp().dynamic();
        } else {
            if (initialExpr instanceof Expr) {
                WurstType result = ((Expr) initialExpr).attrTyp();
                if (result instanceof WurstTypeIntLiteral) {
                    // let a = 1 // we want an int here
                    return WurstTypeInt.instance();
                }
                return result.normalize();
            } else if (initialExpr instanceof ArrayInitializer) {
                ArrayInitializer ai = (ArrayInitializer) initialExpr;
                ExprList values = ai.getValues();
                if (values.isEmpty()) {
                    v.addError("Could not infer the type of variable '" + v.getName() + "' because the array is empty.");
                    return new WurstTypeArray(WurstTypeUnknown.instance());
                }

                // infer the type from the first expression
                // we can make this smarter later by finding a common supertype
                // for all given values
                WurstType valueType = values.get(0).attrTyp();
                if (valueType instanceof WurstTypeIntLiteral) {
                    valueType = WurstTypeInt.instance();
                } else if (valueType instanceof WurstTypeArray) {
                    v.addError("Array parameters are not permitted. Remember that initialized arrays do not have an identity nor length.");
                    return new WurstTypeArray(WurstTypeUnknown.instance());
                }
                return new WurstTypeArray(valueType, ai.getValues().size());
            } else if (v.getParent() instanceof StmtForEach) {
                StmtForEach forEach = (StmtForEach) v.getParent();
                @Nullable NameDef nameDef = forEach.getIn().tryGetNameDef();
                if (nameDef instanceof WParameter && nameDef.attrTyp() instanceof WurstTypeVararg) {
                    return ((WurstTypeVararg) nameDef.attrTyp()).getBaseType();
                }
                Optional<FuncLink> nameLink = forEach.attrGetNextFunc();
                if (nameLink.isPresent()) {
                    return nameLink.get().getReturnType().normalize();
                }
                return WurstTypeUnknown.instance();
            } else {
                v.addError("Could not infer the type of variable '" + v.getName() + "' because it does not have an initial expression.\n"
                        + "Fix this error by providing a type (e.g. 'int " + v.getName() + "' or 'string " + v.getName() + "').");
                return WurstTypeUnknown.instance();
            }
        }
    }

    public static WurstType calculate(ModuleDef moduleDef) {
        return new WurstTypeModule(moduleDef, true);
    }

    public static WurstType calculate(ModuleInstanciation m) {
        return new WurstTypeModuleInstanciation(m, true);
    }

    public static WurstType calculate(NativeType n) {
        WurstNativeType base = WurstNativeType.instance(n.getName(), n.getOptTyp().attrTyp());
        return new WurstTypeStaticTypeRef(base);
    }

    public static WurstType calculate(FunctionDefinition f) {
        return f.attrReturnTyp();
    }

    public static WurstType calculate(TypeParamDef t) {
        return new WurstTypeTypeParam(t);
    }

    public static WurstTypeInterface calculate(InterfaceDef i) {
        List<WurstTypeBoundTypeParam> typeArgs = Lists.newArrayList();
        for (TypeParamDef tp : i.getTypeParameters()) {
            WurstTypeTypeParam tpType = new WurstTypeTypeParam(tp);
            typeArgs.add(new WurstTypeBoundTypeParam(tp, tpType, tp));
        }
        return new WurstTypeInterface(i, typeArgs, true);
    }

    public static WurstType calculate(TupleDef t) {
        return new WurstTypeTuple(t);
    }

    public static WurstType calculate(WPackage p) {
        return new WurstTypePackage(p);
    }

    public static WurstType calculate(EnumDef enumDef) {
        return new WurstTypeEnum(true, enumDef);
    }

    public static WurstType calculate(EnumMember enumMember) {
        return new WurstTypeEnum(false, (EnumDef) enumMember.getParent().getParent());
    }

    public static WurstType calculate(ConstructorDef constructorDef) {
        return WurstTypeVoid.instance();
    }

    public static WurstType calculate(InitBlock constructorDef) {
        return WurstTypeVoid.instance();
    }

    public static WurstType calculate(OnDestroyDef constructorDef) {
        return WurstTypeVoid.instance();
    }


}
