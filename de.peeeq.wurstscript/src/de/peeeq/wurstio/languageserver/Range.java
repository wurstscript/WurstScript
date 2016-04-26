package de.peeeq.wurstio.languageserver;

/** A range of source code */
public class Range {
	private TextPos start;
	private TextPos end;

	public Range(TextPos start, TextPos end) {
		this.start = start;
		this.end = end;
	}
	public Range(int line, int startColum, int endColumn) {
		this.start = new TextPos(line, startColum);
		this.end = new TextPos(line, endColumn);
	}

	public Range(int startLine, int startColum, int endLine, int endColumn) {
		this.start = new TextPos(startLine, startColum);
		this.end = new TextPos(endLine, endColumn);
	}

	public TextPos getStart() {
		return start;
	}

	public TextPos getEnd() {
		return end;
	}
}
