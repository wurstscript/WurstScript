package de.peeeq.eclipsewurstplugin.editor;

import de.peeeq.wurstscript.ast.CompilationUnit;

public interface CompilationUnitChangeListener {
	void onCompilationUnitChanged(CompilationUnit newCu);
}
