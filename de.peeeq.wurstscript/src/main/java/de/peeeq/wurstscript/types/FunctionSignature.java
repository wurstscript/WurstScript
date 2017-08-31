package de.peeeq.wurstscript.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.ParamTypes;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionSignature {
    public static final FunctionSignature empty = new FunctionSignature(null, ParamTypes.noParams(), WurstTypeUnknown.instance());
    private final @Nullable WurstType receiverType;
    private final ParamTypes paramTypes;
    private final WurstType returnType;


    public FunctionSignature(@Nullable WurstType receiverType, ParamTypes paramTypes, WurstType returnType) {
        Preconditions.checkNotNull(paramTypes);
        Preconditions.checkNotNull(returnType);
        this.receiverType = receiverType;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }


    public ParamTypes getParamTypes() {
        return paramTypes;
    }

    public List<WurstType> getParamTypeList() {
        return paramTypes.getTypeList();
    }

    public WurstType getReturnType() {
        return returnType;
    }

    public @Nullable WurstType getReceiverType() {
        return receiverType;
    }

    public FunctionSignature setTypeArgs(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> typeArgBinding) {
        WurstType r2 = returnType.setTypeArgs(typeArgBinding);
        return new FunctionSignature(receiverType, paramTypes.setTypeArgs(typeArgBinding) , r2);
    }


    public static FunctionSignature forFunctionDefinition(@Nullable FunctionDefinition f) {
//		return new FunctionSignature(def.attrReceiverType(), def.attrParameterTypes(), def.getReturnTyp().attrTyp());
        if (f == null) {
            return FunctionSignature.empty;
        }
        WurstType returnType = f.getReturnTyp().attrTyp().dynamic();
        if (f instanceof TupleDef) {
            TupleDef tupleDef = (TupleDef) f;
            returnType = tupleDef.attrTyp().dynamic();
        }


        ParamTypes paramTypes = f.attrParameterTypes();
        return new FunctionSignature(f.attrReceiverType(), paramTypes, returnType);
    }


    public static List<String> getParamNames(WParameters parameters) {
        return parameters.stream()
                .map(WParameter::getName)
                .collect(Collectors.toList());
    }


    public static FunctionSignature fromNameLink(NameLink f) {
        List<String> pNames = Collections.emptyList();
        if (f.getNameDef() instanceof AstElementWithParameters) {
            AstElementWithParameters n = (AstElementWithParameters) f.getNameDef();
            pNames = getParamNames(n.getParameters());
        }
        return new FunctionSignature(f.getReceiverType(), f.getParameterTypes(), f.getReturnType());
    }


    public boolean isEmpty() {
        return receiverType == null && paramTypes.getParams().isEmpty() && returnType instanceof WurstTypeUnknown;
    }


    public String getParameterDescription() {
        return paramTypes.toString();
    }


    public String getParamName(int i) {
        return paramTypes.getParamName(i);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(returnType).append(" ");
        if (receiverType != null) {
            result.append(receiverType).append(".");
        }
        result.append("(");
        result.append(getParameterDescription());
        result.append(")");
        return result.toString();
    }

}
