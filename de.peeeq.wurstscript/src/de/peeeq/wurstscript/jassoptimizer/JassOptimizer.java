package de.peeeq.wurstscript.jassoptimizer;

import java.io.FileNotFoundException;

import de.peeeq.wurstscript.jassAst.JassProg;

public interface JassOptimizer {
	void optimize(JassProg prog) throws FileNotFoundException;
}
