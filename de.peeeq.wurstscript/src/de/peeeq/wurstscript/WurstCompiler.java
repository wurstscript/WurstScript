package de.peeeq.wurstscript;

import de.peeeq.wurstscript.intermediateLang.ILprog;

public interface WurstCompiler {
	
	
	void loadFiles(String ... filenames);
	
	void parseFiles();
	ILprog getILprog();
	
	

}
