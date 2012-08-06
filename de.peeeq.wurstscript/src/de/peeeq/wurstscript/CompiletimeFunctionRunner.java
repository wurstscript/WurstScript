package de.peeeq.wurstscript;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class CompiletimeFunctionRunner {

	private final ImProg imProg;
	private final File mapFile;
	private ILInterpreter interpreter;
	private WurstGui gui;

	public CompiletimeFunctionRunner(ImProg imProg, File mapFile, WurstGui gui) {
		this.imProg = imProg;
		this.mapFile = mapFile;
		this.interpreter = new ILInterpreter(imProg, gui);
		this.gui = gui;
	}
	

	public void run() {
//		interpreter.executeFunction("initGlobals");
		try {
			for (ImFunction f : imProg.getFunctions()) {
				if (f.isCompiletime()) {
					interpreter.runVoidFunc(f);
				}
			}
		} catch (Throwable e) {
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
