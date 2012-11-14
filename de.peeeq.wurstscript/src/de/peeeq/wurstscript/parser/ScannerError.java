package de.peeeq.wurstscript.parser;

import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.CompileError;

public class ScannerError {

	private final int start;
	private final int end;
	private final String message;

	public ScannerError(int start, int end, String message) {
		this.start = start;
		this.end = end;
		this.message = message;
	}

	public CompileError makeCompilerError(WPos p) {
		WPos pos = p.copy();
		pos.setLeftPos(start);
		pos.setRightPos(end);
		return new CompileError(pos, message);
	}

}
