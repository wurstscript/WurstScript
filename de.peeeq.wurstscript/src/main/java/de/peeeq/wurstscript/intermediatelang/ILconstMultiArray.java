package de.peeeq.wurstscript.intermediatelang;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Arrays;


public class ILconstMultiArray extends ILconstAbstract {

    private final Object2ObjectOpenHashMap<IntList, ILconst> contents = new Object2ObjectOpenHashMap<>();


    @Override
    public boolean isEqualTo(ILconst other) {
        throw new Error("not implemented");
    }

    @Override
    public String print() {
        return contents.toString();
    }

    public ILconst get(IntList key) {
        return contents.get(key);
    }

    public ILconst get(int... key) {
        return contents.get(copyKey(key));
    }

    public void set(IntList key, ILconst val) {
        contents.put(copyKey(key), val);
    }

    public void set(ILconst val, int... key) {
        contents.put(copyKey(key), val);
    }

    private static IntArrayList copyKey(IntList key) {
        return new IntArrayList(key);
    }

    private static IntArrayList copyKey(int... key) {
        return IntArrayList.wrap(Arrays.copyOf(key, key.length));
    }

}
