package de.peeeq.wurstscript.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.peeeq.wurstscript.utils.LineOffsets;

public class WPos {
	private final String file;
	private final LineOffsets lineOffsets;
	private final int leftPos;
	private final int rightPos;
	
	public WPos(String file, LineOffsets lineOffsets, int leftPos, int rightPos) {
		this.file = file;
		this.lineOffsets = lineOffsets;
		this.leftPos = leftPos;
		this.rightPos = rightPos;
	}

	public String getFile() {
		return file;
	}

	public LineOffsets getLineOffsets() {
		return lineOffsets;
	}

	public int getLeftPos() {
		return leftPos;
	}

	public int getRightPos() {
		return rightPos;
	}

	public int getLine() {
		if (lineOffsets == null) return 0;
		return lineOffsets.getLine(leftPos) + 1;
	}

	public int getEndLine() {
		if (lineOffsets == null) return 0;
		return lineOffsets.getLine(rightPos) + 1;
	}

	public WPos withRightPos(int rightPos) {
		return new WPos(file, lineOffsets, leftPos, rightPos);
	}

	public WPos withFile(String source) {
		return new WPos(source, lineOffsets, leftPos, rightPos);
	}
	
	@Override
	public String toString() {
		// returning empty string to make AST.toString more readable
		return "";
	}

	public String print() {
		return "[" + file + ", line " + getLine() + "]";
	}
	
	public String printShort() {
		Pattern p = Pattern.compile("^.*[/\\\\]([^/\\\\]+)\\.[^\\.]*$");
		String shortFile = file;
		Matcher m = p.matcher(file);
		if (m.find()) {
		    shortFile = m.group(1);
		}
		return shortFile + ", line " + getLine();
	}
	
}
