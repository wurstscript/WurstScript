package de.peeeq.wurstscript.types;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.attributes.AttrConstantValue;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

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
        this.sizes = new int[] { -1 };
    }

    public WurstTypeArray(WurstType baseType, int size) {
        if (baseType instanceof WurstTypeArray) {
            throw new Error("cannot have array of arrays...");
        }
        this.baseType = baseType;
        this.sizes = new int[] { size };
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
                if (val < 0) {
                    arSize.addError("Array size must be at least 0");
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
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstTypeArray) {
            WurstTypeArray otherArray = (WurstTypeArray) other;
            mapping = baseType.matchTypes(otherArray.baseType, location, mapping, VariablePosition.RIGHT);
            if (mapping == null) {
                return null;
            }
            if (getDimensions() != otherArray.getDimensions()) {
                return null;
            }
            return mapping;
        }
        return null;
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
    public ImType imTranslateType(ImTranslator tr) {
        initSizes();
        ImType bt = baseType.imTranslateType(tr);
        if (sizes.length > 0) {
            if (sizes[0] < 0) {
                return JassIm.ImArrayType(bt);
            }
            List<Integer> nsizes = Lists.newArrayList();
            for (int size : sizes) {
                nsizes.add(size);
            }

            return JassIm.ImArrayTypeMulti(bt, nsizes);
        }
        return JassIm.ImArrayType(bt);
    }


    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        throw new Error();
    }

    @Override
    protected boolean isNullable() {
        return false;
    }

}
