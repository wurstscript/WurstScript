package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.AttrClosureAbstractMethod;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

public class WurstTypeClosure extends WurstType {

    private final List<WurstType> paramTypes;
    private final WurstType returnType;

    public WurstTypeClosure(List<WurstType> paramTypes, WurstType returnType) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstTypeClosure) {
            WurstTypeClosure o = (WurstTypeClosure) other;
            if (paramTypes.size() != o.paramTypes.size()) {
                return null;
            }
            // contravariant parameter types
            for (int i = 0; i < paramTypes.size(); i++) {
                mapping =  o.paramTypes.get(i).matchAgainstSupertype(paramTypes.get(i), location, mapping, variablePosition.inverse());
                if (mapping == null) {
                    return null;
                }
            }
            // covariant return types
            return returnType.matchAgainstSupertype(o.returnType, location, mapping, variablePosition);
        } else if (other instanceof WurstTypeCode) {
            if (paramTypes.size() == 0) {
                return mapping;
            }
        } else {
            FunctionSignature abstractMethod = AttrClosureAbstractMethod.getAbstractMethodSignature(other);
            if (abstractMethod != null) {
                return closureImplementsAbstractMethod(abstractMethod, location, mapping, variablePosition);
            }
        }
        return null;
    }


    private VariableBinding closureImplementsAbstractMethod(FunctionSignature abstractMethod,
                                                            Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (paramTypes.size() != abstractMethod.getParamTypes().size()) {
            return null;
        }

        // contravariant parameter types
        for (int i = 0; i < paramTypes.size(); i++) {
            mapping = abstractMethod.getParamTypes().get(i).normalize().matchAgainstSupertype(paramTypes.get(i), location, mapping, variablePosition.inverse());
            if (mapping == null) {
                return null;
            }
        }
        if (abstractMethod.getReturnType() instanceof WurstTypeVoid) {
            // closures returning void may return anything
            // this is to allow expressions
            return mapping;
        }

        // covariant return types
        return returnType.matchAgainstSupertype(abstractMethod.getReturnType().normalize(), location, mapping, variablePosition);
    }


    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean first = true;
        for (WurstType t : paramTypes) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(t.getName());
            first = false;
        }
        sb.append(") -> ");
        sb.append(returnType.getName());
        return sb.toString();
    }

    @Override
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean first = true;
        for (WurstType t : paramTypes) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(t.getFullName());
            first = false;
        }
        sb.append(") -> ");
        sb.append(returnType.getFullName());
        return sb.toString();
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        return WurstTypeInt.instance().imTranslateType(tr);
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImIntVal(0);
    }

    @Override
    protected boolean isNullable() {
        return false;
    }

    public List<WurstType> getParamTypes() {
        return paramTypes;
    }

    public WurstType getReturnType() {
        return returnType;
    }

}
