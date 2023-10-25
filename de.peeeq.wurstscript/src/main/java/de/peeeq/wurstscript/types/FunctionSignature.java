package de.peeeq.wurstscript.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionSignature {
    public static final FunctionSignature empty = new FunctionSignature(null, VariableBinding.emptyMapping(), null, "?", Collections.emptyList(), Collections.emptyList(), WurstTypeUnknown.instance());
    private final HasFunctionSignature trace;
    private final @Nullable WurstType receiverType;
    private final List<WurstType> paramTypes;
    private final List<String> paramNames; // optional list of parameter names
    private final WurstType returnType;
    private final VariableBinding mapping;
    private final boolean isVararg;
    private final String name;


    public FunctionSignature(HasFunctionSignature trace, VariableBinding mapping, @Nullable WurstType receiverType, String name, List<WurstType> paramTypes, List<String> paramNames, WurstType returnType) {
        this.trace = trace;
        this.mapping = mapping;
        this.name = name;
        Preconditions.checkNotNull(paramTypes);
        Preconditions.checkNotNull(returnType);
        this.isVararg = hasVarargParam(paramTypes);
        this.receiverType = receiverType;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.paramNames = paramNames;
    }

    private boolean hasVarargParam(List<WurstType> paramTypes) {
        if (paramTypes.isEmpty()) {
            return false;
        }
        return Utils.getLast(paramTypes) instanceof WurstTypeVararg;
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

    @CheckReturnValue
    public FunctionSignature setTypeArgs(Element context, VariableBinding newMapping) {
        if (newMapping.isEmpty()) {
            return this;
        }

        WurstType r2 = returnType.setTypeArgs(newMapping);
        List<WurstType> pt2 = Lists.newArrayList();
        for (WurstType p : paramTypes) {
            pt2.add(p.setTypeArgs(newMapping));
        }
        return new FunctionSignature(trace, newMapping, receiverType, name, pt2, paramNames, r2);
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
        List<TypeParamDef> typeParams = Collections.emptyList();
        if (f instanceof AstElementWithTypeParameters) {
            typeParams = ((AstElementWithTypeParameters) f).getTypeParameters();
        }
        return new FunctionSignature(f, VariableBinding.emptyMapping().withTypeVariables(typeParams), f.attrReceiverType(), f.getName(), paramTypes, paramNames, returnType);
    }


    public static List<String> getParamNames(WParameters parameters) {
        return parameters.stream()
                .map(WParameter::getName)
                .collect(Collectors.toList());
    }


    public static FunctionSignature fromNameLink(FuncLink f) {
        VariableBinding mapping = f.getVariableBinding();
        mapping = mapping.withTypeVariables(f.getTypeParams());
        return new FunctionSignature(f.getDef(), mapping, f.getReceiverType(), f.getName(), f.getParameterTypes(), getParamNames(f.getDef().getParameters()), f.getReturnType());
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

    public boolean isValidParameterNumber(int numParams) {
        if (isVararg) {
            return numParams >= paramTypes.size() - 1;
        } else {
            return numParams == paramTypes.size();
        }
    }

    public int getMinNumParams() {
        if (isVararg) {
            return paramTypes.size() - 1;
        } else {
            return paramTypes.size();
        }
    }

    public int getMaxNumParams() {
        if (isVararg) {
            return Integer.MAX_VALUE;
        } else {
            return paramTypes.size();
        }
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
        if (receiverType != null) {
            result.append(receiverType).append(".");
        }
        result.append(name);
        if (!mapping.isEmpty()) {
            result.append(mapping);
        }
        result.append("(");
        result.append(getParameterDescription());
        result.append(") returns ");
        result.append(returnType);
        WPos src = trace.attrSource();
        result.append(", defined in ")
                .append(src.getFile())
                .append(": ")
                .append(src.getLine());
        return result.toString();
    }

    public boolean isVararg() {
        return isVararg;
    }

    public WurstType getVarargType() {
        Preconditions.checkArgument(isVararg);
        return ((WurstTypeVararg) paramTypes.get(paramTypes.size() - 1)).getBaseType();
    }

    public @Nullable FunctionSignature matchAgainstArgs(List<WurstType> argTypes, Element location) {
        if (!isValidParameterNumber(argTypes.size())) {
            return null;
        }
        VariableBinding mapping = this.mapping;
        for (int i = 0; i < argTypes.size(); i++) {
            WurstType pt = getParamType(i);
            WurstType at = argTypes.get(i);
            mapping = at.matchAgainstSupertype(pt, location, mapping, VariablePosition.RIGHT);
            if (mapping == null) {
                return null;
            }

        }

        return setTypeArgs(location, mapping);
    }

    public List<TypeParamDef> getDefinitionTypeVariables() {
        return trace.match(new HasFunctionSignature.Matcher<List<TypeParamDef>>() {
            @Override
            public List<TypeParamDef> case_TupleDef(TupleDef tupleDef) {
                return Collections.emptyList();
            }

            @Override
            public List<TypeParamDef> case_NativeFunc(NativeFunc nativeFunc) {
                return Collections.emptyList();
            }

            @Override
            public List<TypeParamDef> case_FuncDef(FuncDef f) {
                return f.getTypeParameters();
            }

            @Override
            public List<TypeParamDef> case_ExtensionFuncDef(ExtensionFuncDef f) {
                return f.getTypeParameters();
            }

            @Override
            public List<TypeParamDef> case_ConstructorDef(ConstructorDef c) {
                ClassDef classDef = c.attrNearestClassDef();
                assert classDef != null;
                return classDef.getTypeParameters();
            }
        });
    }

    public boolean hasIfNotDefinedAnnotation() {
        if (trace instanceof HasModifier) {
            HasModifier m = (HasModifier) this.trace;
            return m.attrHasAnnotation("ifNotDefined");
        }
        return false;
    }

    public static class ArgsMatchResult {
        private final FunctionSignature sig;
        private final ImmutableList<CompileError> errors;
        private final int badness;

        public ArgsMatchResult(FunctionSignature sig, ImmutableList<CompileError> errors, int badness) {
            this.sig = sig;
            this.errors = ImmutableList.copyOf(errors);
            this.badness = badness;
        }

        public FunctionSignature getSig() {
            return sig;
        }

        public ImmutableList<CompileError> getErrors() {
            return errors;
        }

        public int getBadness() {
            return badness;
        }

    }

    /**
     * Tries to match as much as possible to get a good errors message
     */
    public ArgsMatchResult tryMatchAgainstArgs(List<WurstType> argTypes, List<Expr> args, Element location) {
        ImmutableList.Builder<CompileError> errors = ImmutableList.builder();
        int badness = 0;
        if (!isValidParameterNumber(argTypes.size())) {
            if (argTypes.size() > getMaxNumParams()) {
                errors.add(new CompileError(location, "Too many arguments: " + argTypes.size() + " given, but only " + getMaxNumParams() +
                        " expected."));
                badness += argTypes.size() - getMaxNumParams();
            } else if (argTypes.size() < getMinNumParams()) {
                errors.add(new CompileError(location, "Not enough arguments: " + argTypes.size() + " given, but  " + getMinNumParams() + " expected."));
                badness += getMinNumParams() - argTypes.size();
            }
        }
        VariableBinding mapping = this.mapping;
        for (int i = 0; i < argTypes.size() && i < getMaxNumParams(); i++) {
            WurstType pt = getParamType(i);
            WurstType at = argTypes.get(i);
            VariableBinding mapping2 = at.matchAgainstSupertype(pt, location, mapping, VariablePosition.RIGHT);
            if (mapping2 == null) {
                WurstType ptBound = pt.setTypeArgs(mapping);
                Expr arg = args.get(i);
                errors.add(new CompileError(arg.attrErrorPos(), "Wrong argument for parameter " + getParamName(i) + ": expected " + ptBound + ", but found " + at + "."));
                badness++;
            } else {
                mapping = mapping2;
            }
        }

        if (mapping.hasUnboundTypeVars()) {
            errors.add(new CompileError(location, "Could not infer type for type variables " + mapping.printUnboundTypeVars()));
        }
        errors.addAll(mapping.getErrors());


        return new ArgsMatchResult(setTypeArgs(location, mapping), errors.build(), badness);
    }

    public VariableBinding getMapping() {
        return mapping;
    }
}
