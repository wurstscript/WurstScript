package de.peeeq.wurstscript.gui;

import de.peeeq.wurstscript.attributes.CompileError;

/**
 * implementation for use with cli interfaces
 */
public class WurstGuiCliImpl implements WurstGui {

	@Override
	public void sendError(CompileError err) {
		System.out.println(err);
	}

	@Override
	public void sendProgress(String msg, double percent) {
	}

	@Override
	public void sendFinished() {
		System.out.println("done");
	}

}
