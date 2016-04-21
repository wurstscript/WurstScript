package de.peeeq.wurstio.languageserver;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.peeeq.wurstscript.attributes.CompileError;

public class CompilationResult {
	private String filename;
	private List<ExternCompileError> errors;

	public static CompilationResult create(String filename, List<CompileError> errors) {
		return new CompilationResult(filename, errors.stream().map(ExternCompileError::convert).collect(Collectors.toList()));
	}

	public CompilationResult(String filename, List<ExternCompileError> errors) {
		this.filename = filename;
		this.errors = errors;
	}

	public String getFilename() {
		return filename;
	}

	public List<ExternCompileError> getErrors() {
		return Collections.unmodifiableList(errors);
	}

}
