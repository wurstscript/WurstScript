package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StmtSetOrCallOrVarDefSwitchVoid {
	abstract public void caseVarDef(VarDef varDef);
	abstract public void caseStmtCall(StmtCall stmtCall);
	abstract public void caseStmtSet(StmtSet stmtSet);
	public void doSwitch(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef) {
		if (stmtSetOrCallOrVarDef instanceof VarDef) { caseVarDef((VarDef)stmtSetOrCallOrVarDef); return; }
		if (stmtSetOrCallOrVarDef instanceof StmtCall) { caseStmtCall((StmtCall)stmtSetOrCallOrVarDef); return; }
		if (stmtSetOrCallOrVarDef instanceof StmtSet) { caseStmtSet((StmtSet)stmtSetOrCallOrVarDef); return; }
		throw new Error("Switch did not match any case.");
	}
}

