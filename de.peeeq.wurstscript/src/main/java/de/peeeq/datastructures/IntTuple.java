package de.peeeq.datastructures;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

public class IntTuple {
    private final int[] ar;

    private IntTuple(int len) {
        ar = new int[len];
    }

    public static IntTuple of(int... is) {
        IntTuple r = new IntTuple(is.length);
        System.arraycopy(is, 0, r.ar, 0, is.length);
        return r;
    }


    public int head() {
        return ar[0];
    }

    public IntTuple tail() {
        IntTuple r = new IntTuple(ar.length - 1);
        System.arraycopy(ar, 1, r.ar, 0, ar.length - 1);
        return r;
    }


    @Override
    public String toString() {
        return Arrays.toString(ar);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ar);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntTuple other = (IntTuple) obj;
        return Arrays.equals(ar, other.ar);
    }


}
