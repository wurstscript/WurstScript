package de.peeeq.wurstscript.attributes;

import java.io.File;

import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WPosPos;

public class CompileError {
	private WPos source;
	private String message;
	
	public CompileError(WPos source, String message) {
		this.source = source;
		this.message = message;
	}
	
	public CompileError(WPosPos source, String message) {
		this.source = source.term();
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
		File file = new File(source.file());
		return "Error in File " + file.getName()+ " line " + source.line() + ":" + source.column() + ":\n" + 
				message;
	}
}
