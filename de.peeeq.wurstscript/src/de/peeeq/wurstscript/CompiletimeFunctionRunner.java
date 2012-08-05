package de.peeeq.wurstscript;

import java.io.File;

import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;

public class CompiletimeFunctionRunner {

	private final ImProg imProg;
	private final File mapFile;
	private ILInterpreter interpreter;

	public CompiletimeFunctionRunner(ImProg imProg, File mapFile) {
		this.imProg = imProg;
		this.mapFile = mapFile;
		this.interpreter = new ILInterpreter(imProg);
	}

	public void run() {
		for (ImFunction f : imProg.getFunctions()) {
			if (f.isCompiletime()) {
				interpreter.runVoidFunc(f);
			}
		}
		
	}

}
