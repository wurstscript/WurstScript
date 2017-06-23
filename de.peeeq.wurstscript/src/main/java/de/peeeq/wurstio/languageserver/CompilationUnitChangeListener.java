package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.CompilationUnit;

public interface CompilationUnitChangeListener {
    void onCompilationUnitChanged(CompilationUnit newCu);
}
