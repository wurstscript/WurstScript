package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;

public class ProgramState extends State {

	public static final int GENERATED_BY_WURST = 42;
	private ImStmt lastStatement;
	private WurstGui gui;
	private File mapFile;
	private int id = 0;
	private PrintStream outStream = System.out;
	private List<NativesProvider> nativeProviders = Lists.newArrayList();
	private ImProg prog;

	public ProgramState(File mapFile, WurstGui gui) {
		this.gui = gui;
		this.mapFile = mapFile;
	}

	public void setLastStatement(ImStmt s) {
		lastStatement = s;		
	}

	public ImStmt getLastStatement() {
		return lastStatement;
	}

	public WurstGui getGui() {
		return gui;
	}



	public void writeBack() {
		gui.sendProgress("Writing back generated objects", 0.9);
	}


	public PrintStream getOutStream() {
		return outStream ;
	}
	
	public void setOutStream(PrintStream os) {
		outStream = os;
		for (NativesProvider natives : nativeProviders) {
			natives.setOutStream(os);
		}
	}

	public void addNativeProvider(NativesProvider np) {
		np.setOutStream(outStream);
		nativeProviders.add(np);
	}

	public Iterable<NativesProvider> getNativeProviders() {
		return nativeProviders;
	}

	public ProgramState setProg(ImProg p) {
		prog = p;
		return this;
	}
	
	public ImProg getProg() {
		return prog;
	}
	

	
}

