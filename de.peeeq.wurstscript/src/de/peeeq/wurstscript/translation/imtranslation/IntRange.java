package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Iterator;

public class IntRange implements Iterable<Integer> {
	final int start;
	final int end;
	
	public IntRange(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String toString() {
		return start + ".." + end;
	}

	public int size() {
		return end - start;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			int i = start;
			@Override
			public boolean hasNext() {
				return i<end;
			}

			@Override
			public Integer next() {
				return i++;
			}

			@Override
			public void remove() {
				throw new Error("Ranges are immutable");
			}
		};
	}
	
}
