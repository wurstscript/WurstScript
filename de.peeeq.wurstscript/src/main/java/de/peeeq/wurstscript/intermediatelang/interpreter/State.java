package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.base.Objects;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstArray;
import de.peeeq.wurstscript.jassIm.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Lazily allocates internal maps ONLY when needed.
 */
public abstract class State {

    // in State:
    private @Nullable Object2ObjectOpenHashMap<ImVar, ILconst> values;
    private @Nullable Object2ObjectOpenHashMap<ImVar, ILconstArray> arrayValues;


    private Object2ObjectOpenHashMap<ImVar, ILconst> ensureValues() {
        if (values == null) {
            values = new Object2ObjectOpenHashMap<>(8);
        }
        return values;
    }

    protected Object2ObjectOpenHashMap<ImVar, ILconstArray> ensureArrayValues() {
        if (arrayValues == null) {
            arrayValues = new Object2ObjectOpenHashMap<>(4);
        }
        return arrayValues;
    }


    public void setVal(ImVar v, ILconst val) {
        ensureValues().put(v, val);
    }

    public @Nullable ILconst getVal(ImVar v) {
        Map<ImVar, ILconst> vmap = values;
        return vmap == null ? null : vmap.get(v);
    }

    /**
     * Returns the (lazy) array object for variable v, allocating only when first accessed.
     */
    protected ILconstArray getArray(ImVar v) {
        Map<ImVar, ILconstArray> amap = ensureArrayValues();
        ILconstArray arr = amap.get(v);
        if (arr == null) {
            arr = createArrayConstantFromType(v.getType());
            amap.put(v, arr);
        }
        return arr;
    }

    static ILconstArray createArrayConstantFromType(ImType vType) {
        if (!(vType instanceof ImArrayLikeType)) {
            throw new InterpreterException("Cannot get array for variable of type " + vType);
        }
        ImType componentType = ((ImArrayLikeType) vType).getEntryType();

        // Use declared first dimension if present; otherwise use "unbounded" sentinel.
        int size = Integer.MAX_VALUE;
        if (vType instanceof ImArrayTypeMulti) {
            List<Integer> arraySize = ((ImArrayTypeMulti) vType).getArraySize();
            if (!arraySize.isEmpty()) {
                size = arraySize.get(0);
            }
        }

        return new ILconstArray(size, componentType::defaultValue);
    }

    public void setArrayVal(ImVar v, List<Integer> indexes, ILconst val) {
        ILconstArray ar = getArray(v);
        for (int i = 0; i < indexes.size() - 1; i++) {
            ar = (ILconstArray) ar.get(indexes.get(i));
        }
        ar.set(indexes.get(indexes.size() - 1), val);
    }

    public @Nullable ILconst getArrayVal(ImVar v, List<Integer> indexes) {
        ILconstArray ar = getArray(v);
        for (int i = 0; i < indexes.size() - 1; i++) {
            ar = (ILconstArray) ar.get(indexes.get(i));
        }
        return ar.get(indexes.get(indexes.size() - 1));
    }

}
