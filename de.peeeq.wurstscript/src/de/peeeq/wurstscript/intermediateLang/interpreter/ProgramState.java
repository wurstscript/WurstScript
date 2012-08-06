package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVar;

public class ProgramState extends State {

	private ImStmt lastStatement;
	private WurstGui gui;

	public ProgramState(WurstGui gui) {
		this.gui = gui;
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
	

	
}
