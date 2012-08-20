package de.peeeq.wurstscript;

import java.io.File;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;

public class CompiletimeFunctionRunner {

	private final ImProg imProg;
	private final File mapFile;
	private ILInterpreter interpreter;
	private WurstGui gui;

	public CompiletimeFunctionRunner(ImProg imProg, File mapFile, WurstGui gui) {
		this.imProg = imProg;
		this.mapFile = mapFile;
		this.interpreter = new ILInterpreter(imProg, gui, mapFile);
		this.gui = gui;
	}
	

	public void run() {
		gui.sendProgress("Running compiletime functions", 0.9);
//		interpreter.executeFunction("initGlobals");
		try {
			for (ImFunction f : imProg.getFunctions()) {
				if (f.isCompiletime()) {
					interpreter.runVoidFunc(f);
				}
			}
			
			interpreter.writebackGlobalState();
		} catch (Throwable e) {
			WLogger.severe(e);
			ImStmt s = interpreter.getLastStatement();
			AstElement origin = s.attrTrace();
			if (origin != null) { 
				gui.sendError(new CompileError(origin.attrSource(), e.getMessage()));
			} else {
				throw new Error("could not get origin");
			}
		}
		
	}



}
