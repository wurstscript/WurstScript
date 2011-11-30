package de.peeeq.wurstscript.gui;

import de.peeeq.wurstscript.attributes.CompileError;

public interface WurstGui {
	
	void sendError(CompileError err);
	
	void sendProgress(String whatsRunningNow, double percent);
	
	void sendFinished();

	int getErrorCount();

	String getErrors();
	

}
