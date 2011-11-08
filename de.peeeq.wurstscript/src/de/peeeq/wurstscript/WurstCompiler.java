package de.peeeq.wurstscript;

import java.io.File;

import de.peeeq.wurstscript.intermediateLang.ILprog;

public interface WurstCompiler {
	
	
	void loadFiles(String ... filenames);
	
	void parseFiles();


	void loadFiles(File ... files);
	
	

}
