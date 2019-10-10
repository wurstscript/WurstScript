package de.peeeq.wurstscript.translation.imtojass;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassIm.*;

import java.util.List;
import java.util.function.Supplier;

public class DefaultValue {

    public static ILconst get(ImArrayType t) {
        return t.getEntryType().defaultValue();
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

    public static ILconst get(ImArrayTypeMulti t) {
        List<Integer> sizes = t.getArraySize();
        return new ILconstArray(sizes.get(0), makeSupplier(sizes.size() - 1, t.getEntryType()));
    }

    private static Supplier<ILconst> makeSupplier(int depth, ImType entryType) {
        if (depth <= 1) {
            return entryType::defaultValue;
        }
        return () -> new ILconstArray(Integer.MAX_VALUE, makeSupplier(depth - 1, entryType));
    }

    public static ILconst get(ImTypeVarRef e) {
        return new ILconstUnsafeDefault(e.getTypeVariable());
    }

    public static ILconst get(ImClassType ct) {
        return new ILconstInt(0);
    }

    public static ILconst get(ImAnyType imAnyType) {
        return ILconstNull.instance();
    }
}
