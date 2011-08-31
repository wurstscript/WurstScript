package de.peeeq.wurstscript;

import de.peeeq.wurstscript.gui.WurstGuiImpl;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WurstGuiImpl gui = new WurstGuiImpl();		
		WurstCompilerImpl compiler = new WurstCompilerImpl(gui);
		compiler.loadFiles(args);
		compiler.parseFiles();
		// TODO write the file back to the map or so...
		
	}

}
