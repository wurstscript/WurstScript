package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Arrays;
import java.util.function.Supplier;

public class ILconstArray extends ILconstAbstract {

    // Sparse storage for explicit / first-touched entries.
    // Much lighter than TreeMap both in time and memory.
    private final Int2ObjectOpenHashMap<ILconst> values;

    /** Logical length / bound (can be Integer.MAX_VALUE for “unbounded”). */
    private final int size;

    /** Supplier for default values (e.g., nested arrays or primitive defaults). */
    private final Supplier<ILconst> defaultValue;

    public ILconstArray(int size, Supplier<ILconst> defaultValue) {
        this.size = size;
        this.defaultValue = defaultValue;
        // start tiny; will grow automatically
        this.values = new Int2ObjectOpenHashMap<>(0);
    }

    @Override
    public String print() {
        // We only need a sorted view when PRINTING for determinism.
        // Sorting keys here keeps runtime fast elsewhere.
        if (values.isEmpty()) return "[]";

        int[] keys = new int[values.size()];
        int k = 0;
        for (Int2ObjectMap.Entry<ILconst> e: values.int2ObjectEntrySet()) {
            keys[k++] = e.getIntKey();
        }
        Arrays.sort(keys);

        StringBuilder sb = new StringBuilder(8 + values.size() * 16);
        sb.append('[');
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) sb.append(", ");
            int idx = keys[i];
            sb.append(idx).append(": ").append(values.get(idx));
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        return other == this;
    }

    public void set(int index, ILconst value) {
        checkIndex(index);
        values.put(index, value);
    }

    public ILconst get(int index) {
        checkIndex(index);
        ILconst v = values.get(index);
        if (v != null) return v;

        // First touch for this index: create and store default so that
        // nested writes (e.g., multi-d arrays) have a place to land.
        v = defaultValue.get();
        values.put(index, v);
        return v;
    }

    private void checkIndex(int index) {
        if (index < 0) {
            throw new InterpreterException("Array index " + index + " was negative.");
        }
        if (index >= size) {
            throw new InterpreterException("Array index " + index + " must be smaller than array size " + size);
        }
    }
}
