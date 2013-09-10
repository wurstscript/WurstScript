package de.peeeq.wurstscript.attributes;

import java.io.File;
import java.io.Serializable;

import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;


public class CompileError extends Error implements Serializable {
	private static final long serialVersionUID = 5589441532198109034L;
	
	private final WPos source;
	private final String message;
	
	public CompileError(WPos source, String message) {
		if (source == null) {
			this.source = new WPos("", new LineOffsets(), 0, 0);
		} else {
			this.source = source;
		}
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
		return "Error in File " + file.getName()+ " line " + (source.getLine()-1) + ":\n " + 
				message;
	}
}
