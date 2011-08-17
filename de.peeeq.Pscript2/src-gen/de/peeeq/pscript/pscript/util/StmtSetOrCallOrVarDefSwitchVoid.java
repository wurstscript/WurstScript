package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StmtSetOrCallOrVarDefSwitchVoid {
	abstract public void caseStmtCall(StmtCall stmtCall);
	abstract public void caseStmtSet(StmtSet stmtSet);
	public void doSwitch(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef) {
if ( stmtSetOrCallOrVarDef == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (stmtSetOrCallOrVarDef instanceof StmtCall) { caseStmtCall((StmtCall)stmtSetOrCallOrVarDef); return; }
		if (stmtSetOrCallOrVarDef instanceof StmtSet) { caseStmtSet((StmtSet)stmtSetOrCallOrVarDef); return; }
		throw new Error("Switch did not match any case: " + stmtSetOrCallOrVarDef);
	}
}

