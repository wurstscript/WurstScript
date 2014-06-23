package de.peeeq.datastructures;

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

public class IntTuple {
	private final int[] ar;
	
	private IntTuple(int len) {
		ar = new int[len];
	}
	
	public static IntTuple of(int ... is) {
		IntTuple r = new IntTuple(is.length);
		for (int i = 0; i < is.length; i++) {
			r.ar[i] = is[i];
		}
		return r;
	}
	
	public int get(int i) {
		return ar[i];
	}
	
	public int head() {
		return ar[0];
	}
	
	public IntTuple tail() {
		IntTuple r = new IntTuple(ar.length-1);
		for (int i = 1; i < ar.length; i++) {
			r.ar[i-1] = ar[i];
		}
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
