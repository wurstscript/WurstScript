package de.peeeq.wurstio.languageserver;

import java.util.Collections;
import java.util.List;

import de.peeeq.wurstscript.attributes.CompileError;

public class CompilationResult {
	private String filename;
	private List<CompileError> errors;
	public CompilationResult(String filename, List<CompileError> errors) {
		super();
		this.filename = filename;
		this.errors = errors;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public List<CompileError> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	

}
