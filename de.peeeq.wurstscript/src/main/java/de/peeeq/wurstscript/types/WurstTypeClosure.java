package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.AttrClosureAbstractMethod;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;

public class WurstTypeClosure extends WurstType {

    private final List<WurstType> paramTypes;
    private final WurstType returnType;

    public WurstTypeClosure(List<WurstType> paramTypes, WurstType returnType) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        if (other instanceof WurstTypeClosure) {
            WurstTypeClosure o = (WurstTypeClosure) other;
            if (paramTypes.size() != o.paramTypes.size()) {
                return null;
            }
            // contravariant parameter types
            for (int i = 0; i < paramTypes.size(); i++) {
                mapping =  o.paramTypes.get(i).matchAgainstSupertype(paramTypes.get(i), location, typeParams, mapping);
                if (mapping == null) {
                    return null;
                }
            }
            // covariant return types
            return returnType.matchAgainstSupertype(o.returnType, location, typeParams, mapping);
        } else if (other instanceof WurstTypeCode) {
            if (paramTypes.size() == 0) {
                return mapping;
            }
        } else {
            FunctionSignature abstractMethod = AttrClosureAbstractMethod.getAbstractMethodSignature(other);
            if (abstractMethod != null) {
                return closureImplementsAbstractMethod(abstractMethod, location, typeParams, mapping);
            }
        }
        return null;
    }


    private @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> closureImplementsAbstractMethod(FunctionSignature abstractMethod,
                                                                                                     Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        if (paramTypes.size() != abstractMethod.getParamTypes().size()) {
            return null;
        }

        // contravariant parameter types
        for (int i = 0; i < paramTypes.size(); i++) {
            mapping = abstractMethod.getParamTypes().get(i).matchAgainstSupertype(paramTypes.get(i), location, typeParams, mapping);
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
        return returnType.matchAgainstSupertype(abstractMethod.getReturnType(), location, typeParams, mapping);
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
        return getName();
    }

    @Override
    public ImType imTranslateType() {
        return WurstTypeInt.instance().imTranslateType();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImIntVal(0);
    }

    public List<WurstType> getParamTypes() {
        return paramTypes;
    }

    public WurstType getReturnType() {
        return returnType;
    }

}
