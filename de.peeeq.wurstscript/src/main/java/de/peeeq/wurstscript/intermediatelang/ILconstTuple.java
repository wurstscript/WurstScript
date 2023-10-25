package de.peeeq.wurstscript.intermediatelang;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeString;

import java.util.List;

public class ILconstTuple extends ILconstAbstract {

    private final ILconst[] values;

    public ILconstTuple(ILconst... values) {
        this.values = values;
    }


    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean first = true;
        for (ILconst v : values) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(v.print());
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public WurstType getType() {
        return WurstTypeString.instance();
    }


    @Override
    public boolean isEqualTo(ILconst other) {
        if (other instanceof ILconstTuple) {
            ILconstTuple o = (ILconstTuple) other;
            if (o.values.length != values.length) {
                return false;
            }
            for (int i = 0; i < values.length; i++) {
                if (!values[i].equals(o.values[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public ILconst getValue(int index) {
        return values[index];
    }

    public List<ILconst> values() {
        return ImmutableList.copyOf(values);
    }

    public ILconstTuple updated(int tupleIndex, ILconst newVal) {
        ILconst[] newValues = new ILconst[values.length];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[tupleIndex] = newVal;
        return new ILconstTuple(newValues);
    }


}
