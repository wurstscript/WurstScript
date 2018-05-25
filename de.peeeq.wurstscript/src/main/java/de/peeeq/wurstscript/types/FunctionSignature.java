package de.peeeq.wurstscript.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionSignature {
    public static final FunctionSignature empty = new FunctionSignature(null, Collections.emptyList(), Collections.emptyList(), WurstTypeUnknown.instance(),
            false);
    private final @Nullable WurstType receiverType;
    private final List<WurstType> paramTypes;
    private final List<String> paramNames; // optional list of parameter names
    private final WurstType returnType;
    private boolean isVararg;


    public FunctionSignature(@Nullable WurstType receiverType, List<WurstType> paramTypes, List<String> paramNames, WurstType returnType, boolean isVararg) {
        Preconditions.checkNotNull(paramTypes);
        Preconditions.checkNotNull(returnType);
        this.isVararg = isVararg;
        this.receiverType = receiverType;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.paramNames = paramNames;
    }


    public List<WurstType> getParamTypes() {
        return paramTypes;
    }

    public WurstType getReturnType() {
        return returnType;
    }

    public @Nullable WurstType getReceiverType() {
        return receiverType;
    }

    public FunctionSignature setTypeArgs(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> typeArgBinding) {
        WurstType r2 = returnType.setTypeArgs(typeArgBinding);
        List<WurstType> pt2 = Lists.newArrayList();
        for (WurstType p : paramTypes) {
            pt2.add(p.setTypeArgs(typeArgBinding));
        }
        return new FunctionSignature(receiverType, pt2, paramNames, r2, isVararg);
    }


    public static FunctionSignature forFunctionDefinition(@Nullable FunctionDefinition f) {
//		return new FunctionSignature(def.attrReceiverType(), def.attrParameterTypes(), def.attrReturnTyp());
        if (f == null) {
            return FunctionSignature.empty;
        }
        WurstType returnType = f.attrReturnTyp();
        if (f instanceof TupleDef) {
            TupleDef tupleDef = (TupleDef) f;
            returnType = tupleDef.attrTyp().dynamic();
        }


        List<WurstType> paramTypes = f.attrParameterTypes();
        List<String> paramNames = getParamNames(f.getParameters());
        return new FunctionSignature(f.attrReceiverType(), paramTypes, paramNames, returnType,
                f.getParameters().size() == 1 && f.getParameters().get(0).attrIsVararg());
    }


    public static List<String> getParamNames(WParameters parameters) {
        return parameters.stream()
                .map(WParameter::getName)
                .collect(Collectors.toList());
    }


    public static FunctionSignature fromNameLink(FuncLink f) {
        List<String> pNames = Collections.emptyList();
        if (f.getDef() instanceof AstElementWithParameters) {
            AstElementWithParameters n = (AstElementWithParameters) f.getDef();
            pNames = getParamNames(n.getParameters());
        }
        return new FunctionSignature(f.getReceiverType(), f.getParameterTypes(), pNames, f.getReturnType(), f.getDef().attrIsVararg());
    }


    public boolean isEmpty() {
        return receiverType == null && paramTypes.isEmpty() && returnType instanceof WurstTypeUnknown;
    }


    public String getParameterDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramTypes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(paramTypes.get(i).toString());
            if (i < paramNames.size()) {
                sb.append(" ");
                sb.append(paramNames.get(i));
            }
        }
        return sb.toString();
    }


    public String getParamName(int i) {
        if (i >= 0 && i < paramNames.size()) {
            return paramNames.get(i);
        } else if (isVararg) {
            return paramNames.get(paramNames.size() - 1);
        }
        return "";
    }

    public WurstType getParamType(int i) {
        if (isVararg && i >= paramTypes.size() - 1) {
            return getVarargType();
        }
        if (i >= 0 && i < paramTypes.size()) {
            return paramTypes.get(i);
        }
        throw new RuntimeException("Parameter index out of bounds: " + i);
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

    public boolean isVararg() {
        return isVararg;
    }

    public WurstType getVarargType() {
        Preconditions.checkArgument(isVararg);
        return ((WurstTypeVararg) paramTypes.get(paramTypes.size() - 1)).getBaseType();
    }
}
