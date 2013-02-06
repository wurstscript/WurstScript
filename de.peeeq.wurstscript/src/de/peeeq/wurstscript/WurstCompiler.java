package de.peeeq.wurstscript;

import java.io.File;

import de.peeeq.wurstscript.ast.WurstModel;

public interface WurstCompiler {
	
	
	void loadFiles(String ... filenames);
	
	WurstModel parseFiles();

	void loadFiles(File ... files);
	
}
