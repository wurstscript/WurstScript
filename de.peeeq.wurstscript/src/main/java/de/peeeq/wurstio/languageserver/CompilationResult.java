package de.peeeq.wurstio.languageserver;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.peeeq.wurstscript.attributes.CompileError;

public class CompilationResult {
	private final String extra;
	private final String filename;
	private final List<ExternCompileError> errors;

	public static CompilationResult create(String extra, String filename, List<CompileError> errors) {
		return new CompilationResult(extra, filename, errors.stream().map(ExternCompileError::convert).collect(Collectors.toList()));
	}

	public CompilationResult(String extra, String filename, List<ExternCompileError> errors) {
		this.extra = extra;
		this.filename = filename;
		this.errors = errors;
	}

	public String getFilename() {
		return filename;
	}

	public List<ExternCompileError> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	public String getExtra() {
		return extra;
	}
}
