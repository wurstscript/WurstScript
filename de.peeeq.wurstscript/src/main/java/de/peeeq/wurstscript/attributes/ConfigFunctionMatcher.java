package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import org.eclipse.jdt.annotation.Nullable;

/** Exact ABI matching for package functions replaced through {@code @config}. */
public final class ConfigFunctionMatcher {

    private ConfigFunctionMatcher() {
    }

    public static @Nullable FunctionDefinition findMatchingFunction(WPackage pack, FunctionDefinition function) {
        return findMatchingFunction(pack, function, false);
    }

    public static @Nullable FunctionDefinition findMatchingFunction(WPackage pack, FunctionDefinition function,
                                                                      boolean requireConfigAnnotation) {
        for (DefLink link : pack.getElements().attrNameLinks().get(function.getName())) {
            if (link.getDef() instanceof FunctionDefinition candidate
                    && isPackageFunction(candidate, pack)
                    && (!requireConfigAnnotation || candidate.hasAnnotation("@config"))
                    && matches(function, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isPackageFunction(FunctionDefinition function, WPackage pack) {
        return function.attrNearestPackage() == pack
                && (!(function instanceof FuncDef) || function.attrNearestStructureDef() == null);
    }

    public static boolean matches(FunctionDefinition first, FunctionDefinition second) {
        if (!first.getName().equals(second.getName())) {
            return false;
        }
        if ((first instanceof ExtensionFuncDef) != (second instanceof ExtensionFuncDef)) {
            return false;
        }
        if (!(first instanceof AstElementWithTypeParameters firstGeneric)
                || !(second instanceof AstElementWithTypeParameters secondGeneric)) {
            return false;
        }
        TypeParamDefs firstTypeParams = firstGeneric.getTypeParameters();
        TypeParamDefs secondTypeParams = secondGeneric.getTypeParameters();
        if (firstTypeParams.size() != secondTypeParams.size()) {
            return false;
        }

        VariableBinding alphaMapping = VariableBinding.emptyMapping();
        for (int i = 0; i < firstTypeParams.size(); i++) {
            TypeParamDef firstTypeParam = firstTypeParams.get(i);
            TypeParamDef secondTypeParam = secondTypeParams.get(i);
            alphaMapping = alphaMapping.set(firstTypeParam,
                    new WurstTypeBoundTypeParam(firstTypeParam, secondTypeParam.attrTyp(), first));
        }
        for (int i = 0; i < firstTypeParams.size(); i++) {
            TypeParamDef firstTypeParam = firstTypeParams.get(i);
            TypeParamDef secondTypeParam = secondTypeParams.get(i);
            if (!equalConstraints(firstTypeParam, secondTypeParam, alphaMapping, first)) {
                return false;
            }
        }

        if (first instanceof ExtensionFuncDef firstExtension) {
            ExtensionFuncDef secondExtension = (ExtensionFuncDef) second;
            if (!equalType(firstExtension.getExtendedType().attrTyp(),
                    secondExtension.getExtendedType().attrTyp(), alphaMapping, first)) {
                return false;
            }
        }
        if (first.getParameters().size() != second.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < first.getParameters().size(); i++) {
            if (!equalType(first.getParameters().get(i).attrTyp(), second.getParameters().get(i).attrTyp(),
                    alphaMapping, first)) {
                return false;
            }
        }
        return equalType(first.attrReturnTyp(), second.attrReturnTyp(), alphaMapping, first);
    }

    private static boolean equalConstraints(TypeParamDef first, TypeParamDef second,
                                            VariableBinding alphaMapping, Element location) {
        if ((first.getTypeParamConstraints() instanceof TypeExprList)
                != (second.getTypeParamConstraints() instanceof TypeExprList)) {
            return false;
        }
        if (!(first.getTypeParamConstraints() instanceof TypeExprList firstConstraints)) {
            return true;
        }
        TypeExprList secondConstraints = (TypeExprList) second.getTypeParamConstraints();
        if (firstConstraints.size() != secondConstraints.size()) {
            return false;
        }
        for (int i = 0; i < firstConstraints.size(); i++) {
            if (!equalType(firstConstraints.get(i).attrTyp(), secondConstraints.get(i).attrTyp(),
                    alphaMapping, location)) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalType(WurstType first, WurstType second,
                                     VariableBinding alphaMapping, Element location) {
        return first.setTypeArgs(alphaMapping).equalsType(second, location);
    }
}
