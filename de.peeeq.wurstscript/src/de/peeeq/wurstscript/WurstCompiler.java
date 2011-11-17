package de.peeeq.wurstscript;

import java.io.File;

public interface WurstCompiler {
	
	
	void loadFiles(String ... filenames);
	
	void parseFiles();


	void loadFiles(File ... files);
	
	

}
