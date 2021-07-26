package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstAbstract;

public class VarargArray extends ILconstAbstract {

    private final ILconst[] values;

    public VarargArray(ILconst[] values) {
        this.values = values;
    }

    @Override
    public String print() {
        StringBuilder res = new StringBuilder("varargs[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                res.append(", ");
            }
            res.append(values[i]);
        }
        res.append("]");
        return res.toString();
    }

    public ILconst get(int key) {
        return values[key];
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        throw new Error("Cannot compare arrays.");
    }

    public int size() {
        return values.length;
    }
}
