package de.peeeq.wurstscript.gui;

import de.peeeq.wurstscript.attributes.CompileError;

public interface WurstGui {
	
	void sendError(CompileError err);
	
	void sendProgress(double percent);
	
	void sendFinished();
	

}
