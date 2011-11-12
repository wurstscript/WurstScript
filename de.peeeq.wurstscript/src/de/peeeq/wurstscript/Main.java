package de.peeeq.wurstscript;

import javax.swing.JOptionPane;

import de.peeeq.wurstscript.gui.WurstGuiImpl;
import de.peeeq.wurstscript.utils.Utils;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		JOptionPane.showMessageDialog(null , "time to connect profiler ^^");
		WLogger.info("Started compiler with args " + Utils.printSep(", ", args));
		
		
		try {
			WurstGuiImpl gui = new WurstGuiImpl();
			WurstCompilerImpl compiler = new WurstCompilerImpl(gui);
			compiler.loadFiles(args);
			compiler.parseFiles();
			
//			ILprog prog = compiler.getILprog();
//			StringBuilder sb = new StringBuilder();
//			prog.printJass(sb , 0);
//			TODO output stuff and inject it into map
			
			
			gui.sendFinished();
		} catch (Throwable t) {
			
			
			WLogger.severe(t);
			JOptionPane.showMessageDialog(null , "An severe error has occured\n"+
			"Please report this bug by sending us the file wurst.log in your Wurstpack/wurstscript directory.");
			System.exit(1);
			
			
		}
		// TODO write the file back to the map or so...
		
	}

}
