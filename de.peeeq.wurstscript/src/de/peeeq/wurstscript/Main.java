package de.peeeq.wurstscript;

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.peeeq.wurstscript.ast.AST;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGuiImpl;
import de.peeeq.wurstscript.intermediateLang.ILprog;
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
			"Please report this bug by sending us the file wurst.log in your jngp directory.");
			System.exit(1);
			
			
		}
		// TODO write the file back to the map or so...
		
	}

}
