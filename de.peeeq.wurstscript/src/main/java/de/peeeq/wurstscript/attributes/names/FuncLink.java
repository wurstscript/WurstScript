package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeVararg;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class FuncLink extends DefLink {
    private final FunctionDefinition def;
    private final WurstType returnType;
    private final List<WurstType> parameterTypes;

    public FuncLink(Visibility visibility, WScope definedIn, List<TypeParamDef> typeParams,
                    @Nullable WurstType receiverType, FunctionDefinition def, List<WurstType> parameterTypes, WurstType returnType) {
        super(visibility, definedIn, typeParams, receiverType);
        this.def = def;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public static FuncLink create(FunctionDefinition func, WScope definedIn) {
        Visibility visibiliy = calcVisibility(definedIn, func);
        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(func)).collect(Collectors.toList());
        List<WurstType> lparameterTypes = func.getParameters().stream()
                .map(WParameter::attrTyp)
                .collect(Collectors.toList());
        WurstType lreturnType = func.attrReturnTyp();
        WurstType lreceiverType = calcReceiverType(definedIn, func);
        return new FuncLink(visibiliy, definedIn, typeParams, lreceiverType, func, lparameterTypes, lreturnType);
    }


    private static @Nullable WurstType calcReceiverType(WScope definedIn, NameDef nameDef) {
        if (nameDef instanceof ExtensionFuncDef) {
            ExtensionFuncDef exF = (ExtensionFuncDef) nameDef;
            return exF.getExtendedType().attrTyp().dynamic();
        } else if (nameDef instanceof FuncDef) {
            return getReceiverType(definedIn);
        }
        return null;
    }




    public String getName() {
        return def.getName();
    }


    @Override
    public FunctionDefinition getDef() {
        return def;
    }


    public FuncLink withVisibility(Visibility newVis) {
        if (newVis == getVisibility()) {
            return this;
        }
        return new FuncLink(newVis, getDefinedIn(), getTypeParams(), getReceiverType(), def, parameterTypes, returnType);
    }


    @Override
    public String toString() {
        String r = "" + getVisibility() + " ";
        if (getReceiverType() != null) {
            r += getReceiverType() + ".";
        }
        return r + Utils.printElementWithSource(def);
    }

    @Override
    public NameLinkType getType() {
        return NameLinkType.FUNCTION;
    }

    public FuncLink withTypeArgBinding(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        if (binding.isEmpty()) {
            return this;
        }
        WurstType newReturnType = adjustType(context, getReturnType(), binding);
        boolean changed = newReturnType != returnType;
        List<WurstType> newParamTypes;
        if (getParameterTypes().isEmpty()) {
            newParamTypes = getParameterTypes();
        } else {
            newParamTypes = Lists.newArrayListWithCapacity(getParameterTypes().size());
            for (WurstType pt : getParameterTypes()) {
                WurstType newPt = adjustType(context, pt, binding);
                if (newPt != pt) {
                    changed = true;
                }
                newParamTypes.add(newPt);
            }
        }
        if (changed) {
            List<TypeParamDef> newTypeParams = getTypeParams().stream()
                    .filter(binding::containsKey)
                    .collect(Collectors.toList());
            return new FuncLink(getVisibility(), getDefinedIn(), newTypeParams, getReceiverType(), def, newParamTypes, newReturnType);
        } else {
            return this;
        }
    }

    private WurstType adjustType(Element context, WurstType t, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        return t.setTypeArgs(binding);
    }


    public WurstType getReturnType() {
        return returnType;
    }

    public List<WurstType> getParameterTypes() {
        return parameterTypes;
    }

    public WurstType getParameterType(int i) {
        List<WurstType> parameterTypes = getParameterTypes();
        if (isVarargMethod() && i >= parameterTypes.size() - 1) {
            return ((WurstTypeVararg) parameterTypes.get(parameterTypes.size() - 1)).getBaseType();
        }
        return parameterTypes.get(i);
    }

    private boolean isVarargMethod() {
        List<WurstType> parameterTypes = getParameterTypes();
        if (parameterTypes.size() > 0) {
            return parameterTypes.get(parameterTypes.size() - 1) instanceof WurstTypeVararg;
        }
        return false;
    }

    public FuncLink withConfigDef() {
        FunctionDefinition def = (FunctionDefinition) this.def.attrConfigActualNameDef();
        return new FuncLink(getVisibility(), getDefinedIn(), getTypeParams(), getReceiverType(), def, parameterTypes, returnType);
    }

    public FuncLink hidingPrivate() {
        return (FuncLink) super.hidingPrivate();
    }

    public FuncLink hidingPrivateAndProtected() {
        return (FuncLink) super.hidingPrivateAndProtected();
    }


}
