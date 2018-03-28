package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;


public class WurstTypeVararg extends WurstType {

    private WurstType baseType;
    private int size;

    public WurstTypeVararg(WurstType baseType, int size) {
        if (baseType instanceof WurstTypeVararg) {
            throw new Error("cannot have array of varargs...");
        }
        this.baseType = baseType;
        this.size = size;
    }


    public WurstTypeVararg(WurstType baseType) {
        this.baseType = baseType;
        this.size = 0;
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
        return baseType.getName() + " vararg(size = " + size + ")";
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
