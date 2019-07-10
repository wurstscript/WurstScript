package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeString;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class ILconstArray extends ILconstAbstract {

    private final Map<Integer, ILconst> values = new TreeMap<>(); // including the quotes
    private final int size;
    private final Supplier<ILconst> defaultValue;

    public ILconstArray(int size, Supplier<ILconst> defaultValue) {
        this.size = size;
        this.defaultValue = defaultValue;
    }

    @Override
    public String print() {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (Map.Entry<Integer, ILconst> e : values.entrySet()) {
            if (s.length() > 1) {
                s.append(", ");
            }
            s.append(e.getKey());
            s.append(": ");
            s.append(e.getValue());
        }
        s.append("]");
        return s.toString();
    }


    @Override
    public boolean isEqualTo(ILconst other) {
        return other == this;
    }

    public void set(int index, ILconst value) {
        values.put(index, value);
    }

    public ILconst get(int index) {
        if (index < 0)
            throw new InterpreterException("Array index " + index + " was negative.");
        if (index >= size)
            throw new InterpreterException("Array index " + index + " must be smaller than array size " + size);

        return values.computeIfAbsent(index, i -> defaultValue.get());
    }

}
