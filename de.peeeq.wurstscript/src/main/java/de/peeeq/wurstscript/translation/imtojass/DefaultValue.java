package de.peeeq.wurstscript.translation.imtojass;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassIm.*;

import java.util.List;

public class DefaultValue {

    public static ILconst get(ImArrayType t) {
        return JassIm.ImSimpleType(t.getTypename()).defaultValue();
    }

    public static ILconst get(ImSimpleType t) {
        String typename = t.getTypename();
        if (typename.equals("string")) return ILconstNull.instance();
        if (typename.equals("integer")) return new ILconstInt(0);
        if (typename.equals("real")) return new ILconstReal(0);
        if (typename.equals("boolean")) return ILconstBool.FALSE;
        WLogger.info("could not get default value for " + typename);
        return ILconstNull.instance();
    }

    public static ILconst get(ImTupleArrayType tt) {
        List<ILconst> values = Lists.newArrayList();
        for (ImType t : tt.getTypes()) {
            values.add(t.defaultValue());
        }
        return new ILconstTuple(values.toArray(new ILconst[0]));
    }

    public static ILconst get(ImTupleType tt) {
        List<ILconst> values = Lists.newArrayList();
        for (ImType t : tt.getTypes()) {
            values.add(t.defaultValue());
        }
        return new ILconstTuple(values.toArray(new ILconst[0]));
    }

    public static ILconst get(ImVoid t) {
        throw new Error("Could not get default value for void variable.");
    }

    public static ILconst get(ImArrayTypeMulti imArrayTypeMulti) {
        return JassIm.ImSimpleType(imArrayTypeMulti.getTypename()).defaultValue();
    }

    public static ILconst get(ImClassType ct) {
        return ILconstInt.create(0);
    }
}
