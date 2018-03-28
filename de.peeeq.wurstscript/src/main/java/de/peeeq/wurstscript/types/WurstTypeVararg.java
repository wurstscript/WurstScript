package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;


public class WurstTypeVararg extends WurstType {

    private WurstType baseType;

    public WurstTypeVararg(WurstType baseType) {
        this.baseType = baseType;
    }

    public WurstType getBaseType() {
        return baseType;
    }

    @Override
    public boolean isSubtypeOfIntern(WurstType other, Element location) {
        if (other instanceof WurstTypeVararg) {
            WurstTypeVararg otherArray = (WurstTypeVararg) other;
            return baseType.equalsType(otherArray.baseType, location);
        }
        return false;
    }

    @Override
    public String getName() {
        return baseType.getName() + " vararg";
    }

    @Override
    public String getFullName() {
        return getName();
    }


    @Override
    public ImType imTranslateType() {
        ImType bt = baseType.imTranslateType();

        if (bt instanceof ImSimpleType) {
            String typename = ((ImSimpleType) bt).getTypename();
            return JassIm.ImArrayType(typename);
        } else if (bt instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) bt;
            return JassIm.ImTupleArrayType(tt.getTypes(), tt.getNames());
        } else {
            throw new Error("cannot translate vararg type " + getName() + "  " + bt);
        }
    }


    @Override
    public ImExprOpt getDefaultValue() {
        throw new Error();
    }

}
