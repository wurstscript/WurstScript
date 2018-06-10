package de.peeeq.wurstscript.types;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.attributes.AttrConstantValue;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.List;


public class WurstTypeArray extends WurstType {

    private Expr arSize;
    private WurstType baseType;
    private int[] sizes;


    public WurstTypeArray(WurstType baseType, Expr arSize) {
        if (baseType instanceof WurstTypeArray) {
            throw new Error("cannot have array of arrays...");
        }
        this.baseType = baseType;
        this.arSize = arSize;
    }

    public WurstTypeArray(WurstType baseType) {
        if (baseType instanceof WurstTypeArray) {
            throw new Error("cannot have array of arrays...");
        }
        this.baseType = baseType;
        this.sizes = new int[1];
    }

    private void initSizes() {
        if (sizes != null) {
            return;
        }
        // default is to have no array sizes:
        int[] sizes = {};
        // when there is an array size given, try to evaluate it:
        try {
            ILconst i = arSize.attrConstantValue();
            if (i instanceof ILconstInt) {
                int val = ((ILconstInt) i).getVal();
                sizes = new int[]{val};
                if (val <= 0) {
                    arSize.addError("Array size must be at least 1");
                }
            } else {
                arSize.addError("Array sizes should be integer...");
            }
        } catch (AttrConstantValue.ConstantValueCalculationException e) {
            arSize.addError("Array size is not a constant expression.");
        }
        this.sizes = sizes;
    }


    public WurstType getBaseType() {
        return baseType;
    }


    public int getDimensions() {
        initSizes();
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
        initSizes();
        return sizes[i];
    }


    @Override
    public ImType imTranslateType() {
        initSizes();
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
