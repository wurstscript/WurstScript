package de.peeeq.wurstscript;

import de.peeeq.wurstscript.intermediateLang.ILprog;

public interface WurstCompiler {
	
	
	void loadFile(String filename);
	
	void parseFile();
	ILprog getILprog();
	
	

}
