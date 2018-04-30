package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Generics {

    public static ImmutableMap<TypeParamDef, WurstTypeBoundTypeParam> getTypeParameterBindings(FunctionCall fc) {
        FunctionDefinition def = fc.attrFuncDef();
        if (def == null) {
            return ImmutableMap.of();
        }
        TypeParamDefs typeParams = getTypeParameters(def);
        if (hasTypeParams(fc, typeParams)) {
            return givenBinding(fc, typeParams);
        }

        Map<TypeParamDef, WurstTypeBoundTypeParam> result = Maps.newLinkedHashMap();
        if (fc instanceof ExprMemberMethod) {
            ExprMemberMethod emm = (ExprMemberMethod) fc;
            inferTypeParameterUsingReceiver(result, emm, typeParams);
        }
        inferTypeParametersUsingArguments(result, fc.getArgs(), def.getParameters(), typeParams);
        return ImmutableMap.copyOf(result);
    }

    private static void inferTypeParameterUsingReceiver(
            Map<TypeParamDef, WurstTypeBoundTypeParam> result, ExprMemberMethod emm, TypeParamDefs typeParams) {
        FunctionDefinition def = emm.attrFuncDef();
        if (def instanceof ExtensionFuncDef) {
            ExtensionFuncDef ef = (ExtensionFuncDef) def;
            WurstType argType = emm.getLeft().attrTyp();
            WurstType paramType = ef.attrReceiverType();
            inferTypeParameters(result, emm, argType, paramType, typeParams);
        }
    }

    // TODO in the future this should also take return type into account
    // e.g. List<String> = new List() // infer String here
    private static void inferTypeParametersUsingArguments(Map<TypeParamDef, WurstTypeBoundTypeParam> result, Arguments args, WParameters params, TypeParamDefs typeParams) {
        // calculate (most general) unifier
        for (int i = 0; i < args.size() && i < params.size(); i++) {
            WurstType argType;
            Expr arg = args.get(i);
            if (arg instanceof ExprClosure) {
                ExprClosure closure = (ExprClosure) arg;
                List<WurstType> clParamTypes = new ArrayList<>();
                for (WShortParameter param : closure.getShortParameters()) {
                    WurstType paramType;
                    if (param.getTypOpt() instanceof TypeExpr) {
                        paramType = param.getTypOpt().attrTyp();
                    } else {
                        paramType = new WurstTypeDeferred(() -> param.attrTyp());
                    }
                    clParamTypes.add(paramType);
                }
                WurstTypeDeferred ret = new WurstTypeDeferred(() -> closure.getImplementation().attrTyp());
                argType = new WurstTypeClosure(clParamTypes, ret);
            } else {
                argType = arg.attrTyp();
            }
            inferTypeParameters(result, args, argType, params.get(i).attrTyp(), typeParams);
        }
        if (result.size() < typeParams.size()) {
            for (TypeParamDef tp : typeParams) {
                if (!result.containsKey(tp)) {
                    args.getParent().addError("Could not infer type parameter " + tp.getName() + ".");
                }
            }
        }
    }

    private static void inferTypeParameters(Map<TypeParamDef, WurstTypeBoundTypeParam> result, Element pos, WurstType argType, WurstType paramTyp,
                                            TypeParamDefs typeParams) {
        if(paramTyp instanceof WurstTypeVararg) {
            WurstTypeVararg bt = (WurstTypeVararg) paramTyp;
            inferTypeParameters(result, pos, argType, bt.getBaseType(), typeParams);
            return;
        }
        if (paramTyp instanceof WurstTypeBoundTypeParam) {
            WurstTypeBoundTypeParam bt = (WurstTypeBoundTypeParam) paramTyp;
            inferTypeParameters(result, pos, argType, bt.getBaseType(), typeParams);
            return;
        }
        if (paramTyp instanceof WurstTypeTypeParam) {
            WurstTypeTypeParam paramTyp2 = (WurstTypeTypeParam) paramTyp;
            if (typeParams.contains(paramTyp2.getDef())) {
                WurstType previousType = result.put(paramTyp2.getDef(), new WurstTypeBoundTypeParam(paramTyp2.getDef(), argType, pos));
                if (previousType != null && !previousType.equalsType(argType, pos)) {
                    pos.addError("Cannot infer type parameters, there is a conflict between "
                            + previousType + " and " + argType + " for type parameter " + paramTyp2.getName());
                }
            }
        }
        if (paramTyp instanceof WurstTypeNamedScope) {
            WurstTypeNamedScope paramTyp2 = (WurstTypeNamedScope) paramTyp;
            if (argType instanceof WurstTypeNamedScope) {
                WurstTypeNamedScope argTyp2 = (WurstTypeNamedScope) argType;
                List<WurstTypeBoundTypeParam> paramTyp2childs = paramTyp2.getTypeParameters();
                List<WurstTypeBoundTypeParam> argTyp2childs = argTyp2.getTypeParameters();
                for (int i = 0; i < paramTyp2childs.size() && i < argTyp2childs.size(); i++) {
                    inferTypeParameters(result, pos, argTyp2childs.get(i), paramTyp2childs.get(i), typeParams);
                }
            }
        }
        if (paramTyp instanceof WurstTypeClassOrInterface && argType instanceof WurstTypeClosure) {
            WurstTypeClassOrInterface paramTyp2 = (WurstTypeClassOrInterface) paramTyp;
            WurstTypeClosure argTyp2 = (WurstTypeClosure) argType;
            NameLink sa = paramTyp2.findSingleAbstractMethod().withTypeArgBinding(pos, paramTyp2.getTypeArgBinding());
            if (sa != null) {
                for (int i = 0; i < sa.getParameterTypes().size() && i < argTyp2.getParamTypes().size(); i++) {
                    inferTypeParameters(result, pos, argTyp2.getParamTypes().get(i), sa.getParameterTypes().get(i), typeParams);
                }
                inferTypeParameters(result, pos, argTyp2.getReturnType(), sa.getReturnType(), typeParams);
            }
        }
    }

    public static ImmutableMap<TypeParamDef, WurstTypeBoundTypeParam> getTypeParameterBindings(ExprNewObject e) {
        ConstructorDef constrDef = e.attrConstructorDef();
        if (constrDef == null) {
            return ImmutableMap.of();
        }
        ClassOrModule classDef = constrDef.attrNearestClassOrModule();
        TypeParamDefs typeParams = getTypeParameters(classDef);
        if (hasTypeParams(e, typeParams)) {
            return givenBinding(e, typeParams);
        }

        Map<TypeParamDef, WurstTypeBoundTypeParam> result = Maps.newLinkedHashMap();
        inferTypeParametersUsingArguments(result, e.getArgs(), constrDef.getParameters(), typeParams);
        return ImmutableMap.copyOf(result);
    }

    public static ImmutableMap<TypeParamDef, WurstTypeBoundTypeParam> getTypeParameterBindings(ModuleUse m) {
        ModuleDef usedModule = m.attrModuleDef();
        TypeParamDefs typeParams = getTypeParameters(usedModule);
        if (hasTypeParams(m, typeParams)) {
            return givenBinding(m, typeParams);
        }
        m.addError("Missing type arguments for module " + m.getModuleName());
        return ImmutableMap.of();
    }

    public static ImmutableMap<TypeParamDef, WurstTypeBoundTypeParam> getTypeParameterBindings(TypeExprSimple t) {
        TypeDef def = t.attrTypeDef();
        TypeParamDefs typeParams = getTypeParameters(def);
        if (hasTypeParams(t, typeParams)) {
            return givenBinding(t, typeParams);
        }
        t.addError("Missing type arguments for " + Utils.printElement(t));
        return ImmutableMap.of();
    }


    /**
     * returns the binding given by the user
     */
    private static ImmutableMap<TypeParamDef, WurstTypeBoundTypeParam> givenBinding(AstElementWithTypeArgs fc, TypeParamDefs typeParams) {
        Map<TypeParamDef, WurstTypeBoundTypeParam> result = Maps.newLinkedHashMap();
        for (int i = 0; i < typeParams.size(); i++) {
            TypeParamDef tp = typeParams.get(i);
            WurstType t = fc.getTypeArgs().get(i).attrTyp().dynamic();
            result.put(tp, new WurstTypeBoundTypeParam(tp, t, fc));
        }
        return ImmutableMap.copyOf(result);
    }

    /**
     * returns wether the user has given all necessary bindings
     */
    private static boolean hasTypeParams(AstElementWithTypeArgs fc, TypeParamDefs typeParams) {
        if (typeParams.size() == fc.getTypeArgs().size()) {
            return true;
        }
        if (fc.getTypeArgs().size() > 0) {
            fc.addError("Wrong number of type arguments. Expected " + typeParams.size());
        }
        return false;
    }

    private static TypeParamDefs getTypeParameters(Element def) {
        if (def instanceof AstElementWithTypeParameters) {
            AstElementWithTypeParameters wtp = (AstElementWithTypeParameters) def;
            return wtp.getTypeParameters();
        } else {
            return Ast.TypeParamDefs();
        }
    }


}
