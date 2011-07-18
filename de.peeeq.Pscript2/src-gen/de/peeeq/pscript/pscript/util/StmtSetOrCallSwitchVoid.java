package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StmtSetOrCallSwitchVoid {
	abstract public void caseStmtSet(StmtSet stmtSet);
	abstract public void caseStmtCall(StmtCall stmtCall);
	public void doSwitch(StmtSetOrCall stmtSetOrCall) {
		if (stmtSetOrCall instanceof StmtSet) { caseStmtSet((StmtSet)stmtSetOrCall); return; }
		if (stmtSetOrCall instanceof StmtCall) { caseStmtCall((StmtCall)stmtSetOrCall); return; }
		throw new Error("Switch did not match any case.");
	}
}

