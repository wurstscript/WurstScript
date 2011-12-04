package de.peeeq.wurstscript;

import java.io.File;

import javax.swing.JOptionPane;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.gui.WurstGuiImpl;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizer;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizerImpl;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.utils.Utils;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		JOptionPane.showMessageDialog(null , "time to connect profiler ^^");
		WLogger.info("Started compiler with args " + Utils.printSep(", ", args));
		
		try {
			RunArgs runArgs = new RunArgs(args);
		
		
		
			WurstGui gui;
			if (runArgs.isGui()) {
				gui = new WurstGuiImpl();
			} else {
				gui = new WurstGuiCliImpl();
			}
			WurstCompilerImpl compiler = new WurstCompilerImpl(gui);
			compiler.loadFiles(args);
			compiler.parseFiles();
			
			JassProg jassProg = compiler.getILprog();
			
			boolean withSpace;
			if (runArgs.isOptimize()) {
				gui.sendProgress("Optimizing Jass", 0.85);
				JassOptimizer optimizer = new JassOptimizerImpl();
				optimizer.optimize(jassProg);
				withSpace = false;
			} else {
				withSpace = true;
			}
			
			gui.sendProgress("Printing Jass", 0.90);
			JassPrinter printer = new JassPrinter(withSpace);
			CharSequence mapScript = printer.printProg(jassProg);

			
			if (runArgs.getOutFile() != null) { // output to file
				gui.sendProgress("Writing output file", 0.98);
				File outputFile = new File(runArgs.getOutFile());
				outputFile.mkdirs();
				Files.write(mapScript, outputFile, Charsets.UTF_8);
			}
			if (runArgs.getMapFile() != null) { // output to map
				gui.sendProgress("Writing to map", 0.99);
				File mapFile = new File(runArgs.getMapFile());
				File tempFile = new File(runArgs.getMapFile() + ".temp.j");
			}
			
			
			gui.sendFinished();
		} catch (Throwable t) {
			
			
			WLogger.severe(t);
			JOptionPane.showMessageDialog(null , "An severe error has occured\n"+
			"Please report this bug by sending us the file wurst.log in your Wurstpack/wurstscript directory.");
			System.exit(1);
			
			
		}
	}

}
