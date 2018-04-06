package de.peeeq.wurstscript.attributes.funcs;

import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class FuncSig {
    private final ElementWithSignature funcDef;
    private final List<TypeParamDef> typeParams;
    private final List<WurstType> paramTypes;
    private final List<String> paramNames;
    private final WurstType returnType;
    // errors to add if this function signature is chosen
    private final List<CompileError> deferredErrors;


    public FuncSig(ElementWithSignature funcDef, List<TypeParamDef> typeParams, List<WurstType> paramTypes, List<String> paramNames, WurstType returnType , List<CompileError> deferredErrors) {
        this.funcDef = funcDef;
        this.typeParams = typeParams;
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
        this.returnType = returnType;
        this.deferredErrors = deferredErrors;
    }

    public static FuncSig fromFunc(FunctionDefinition func) {
        List<TypeParamDef> tps;
        if (func instanceof FunctionImplementation) {
            tps = ((FunctionImplementation) func).getTypeParameters();
        } else {
            tps = Collections.emptyList();
        }
        List<WurstType> pTypes = func.getParameters().stream().map(p -> p.attrTyp().dynamic()).collect(Collectors.toList());
        List<String> pNames = func.getParameters().stream().map(p -> p.getName()).collect(Collectors.toList());
        WurstType rType;
        if (func instanceof TupleDef) {
            TupleDef tupleDef = (TupleDef) func;
            rType = tupleDef.attrTyp().dynamic();
        } else {
            rType = func.getReturnTyp().attrTyp().dynamic();
        }
        return new FuncSig(func, tps, pTypes, pNames, rType, Collections.emptyList());
    }

    public ElementWithSignature getFuncDef() {
        return funcDef;
    }

    public List<TypeParamDef> getTypeParams() {
        return typeParams;
    }

    public List<WurstType> getParamTypes() {
        return paramTypes;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public FuncSig withTypeArgs(TypeExprList typeArgs) {
        // replace type in args:
        ImmutableMap.Builder<TypeParamDef, WurstTypeBoundTypeParam> builder = ImmutableMap.builder();
        for (int i = 0; i < typeArgs.size() && i < typeParams.size(); i++) {
            TypeParamDef tp = typeParams.get(i);
            TypeExpr ta = typeArgs.get(i);
            WurstType t = ta.attrTyp();
            WurstTypeBoundTypeParam bt = new WurstTypeBoundTypeParam(tp, t, ta);
            builder.put(tp, bt);
        }
        Map<TypeParamDef, WurstTypeBoundTypeParam> mapping = builder.build();
        List<WurstType> newParamTypes = paramTypes.stream()
                .map(t -> t.setTypeArgs(mapping))
                .collect(Collectors.toList());


        WurstType newReturnType = returnType.setTypeArgs(mapping);
        return new FuncSig(funcDef, Collections.emptyList(), newParamTypes, paramNames, newReturnType, deferredErrors);
    }

    public List<CompileError> getDeferredErrors() {
        return deferredErrors;
    }

    public WurstType getReturnType() {
        return returnType;
    }

    public String getParamName(int i) {
        return paramNames.get(i);
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
}
