package de.peeeq.wurstscript.translation.imtranslation;

public class IntRange {
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
	
}
