package de.peeeq.wurstscript.attributes;

import java.io.File;

import de.peeeq.wurstscript.ast.WPos;

public class CompileError extends Error {
	private WPos source;
	private String message;
	
	public CompileError(WPos source, String message) {
		this.source = source;
		this.message = message;
	}
	
	
	public WPos getSource() {
		return source;
	}
	public String getMessage() {
		return message;
	}
	@Override
	public String toString() {
		File file = new File(source.getFile());
		return "Error in File " + file.getName()+ " line " + source.getLine() + ":" + source.getColumn() + ":\n" + 
				message;
	}
}
