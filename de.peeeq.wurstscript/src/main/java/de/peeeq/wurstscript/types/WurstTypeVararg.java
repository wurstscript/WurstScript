package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeVararg extends WurstType {

    private WurstType baseType;

    public WurstTypeVararg(WurstType baseType) {
        this.baseType = baseType;
    }

    public WurstType getBaseType() {
        return baseType;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstTypeVararg) {
            WurstTypeVararg otherArray = (WurstTypeVararg) other;
            return baseType.matchTypes(otherArray.baseType, location, mapping, variablePosition);
        }
        return null;
    }

    @Override
    public String getName() {
        return "vararg " + baseType.getName();
    }

    @Override
    public String getFullName() {
        return getName();
    }


    @Override
    public ImType imTranslateType() {
        return baseType.imTranslateType();
    }


    @Override
    public ImExprOpt getDefaultValue() {
        throw new Error();
    }


    @Override
    public WurstType setTypeArgs(VariableBinding t) {
        WurstType b = this.baseType.setTypeArgs(t);
        if (b == baseType) {
            return this;
        }
        return new WurstTypeVararg(b);
    }
}
