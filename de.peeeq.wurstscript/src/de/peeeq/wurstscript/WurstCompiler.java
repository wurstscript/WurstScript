package de.peeeq.wurstscript;

import java.io.File;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.WurstModel;

public interface WurstCompiler {
	
	
	void loadFiles(String ... filenames);
	
	@Nullable WurstModel parseFiles();

	void loadFiles(File ... files);
	
}
