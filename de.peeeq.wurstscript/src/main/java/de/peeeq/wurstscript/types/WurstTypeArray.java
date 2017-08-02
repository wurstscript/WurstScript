package de.peeeq.wurstscript.types;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.List;


public class WurstTypeArray extends WurstType {

    private WurstType baseType;
    private int[] sizes;

    public WurstTypeArray(WurstType baseType, int[] sizes) {
        if (baseType instanceof WurstTypeArray) {
            throw new Error("cannot have array of arrays...");
        }
        this.baseType = baseType;
        this.sizes = Utils.copyArray(sizes);
    }


    public WurstTypeArray(WurstType baseType) {
        this.baseType = baseType;
        this.sizes = new int[1];
    }


    public WurstType getBaseType() {
        return baseType;
    }


    public int getDimensions() {
        return sizes.length;
    }


    @Override
    public boolean isSubtypeOfIntern(WurstType other, Element location) {
        if (other instanceof WurstTypeArray) {
            WurstTypeArray otherArray = (WurstTypeArray) other;
            return baseType.equalsType(otherArray.baseType, location) && getDimensions() == otherArray.getDimensions();
        }
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return baseType.getName() + " array(dimensions = " + getDimensions() + ")";
    }

    @Override
    public String getFullName() {
        return getName();
    }


    public int getSize(int i) {
        return sizes[i];
    }


    @Override
    public ImType imTranslateType() {
        ImType bt = baseType.imTranslateType();

        if (bt instanceof ImSimpleType) {
            String typename = ((ImSimpleType) bt).getTypename();
            if (sizes.length > 0) {
                if (sizes[0] == 0) {
                    return JassIm.ImArrayType(typename);
                }
                List<Integer> nsizes = Lists.<Integer>newArrayList();
                for (int size : sizes) {
                    nsizes.add(size);
                }

                return JassIm.ImArrayTypeMulti(typename, nsizes);
            }
            return JassIm.ImArrayType(typename);
        } else if (bt instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) bt;
            return JassIm.ImTupleArrayType(tt.getTypes(), tt.getNames());
        } else {
            throw new Error("cannot translate array type " + getName() + "  " + bt);
        }
    }


    @Override
    public ImExprOpt getDefaultValue() {
        throw new Error();
    }

}
