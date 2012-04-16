package de.peeeq.wurstscript.compilationserver;

import java.io.File;
import java.util.List;

import de.peeeq.wurstscript.attributes.CompileError;

public interface CompilationServer {
	List<CompileError> compile(List<WInput> inputs, File outFile);
}
