package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StmtSetOrCallOrVarDefSwitch <T> {
	abstract public T caseStmtSet(StmtSet stmtSet);
	abstract public T caseVarDef(VarDef varDef);
	abstract public T caseStmtCall(StmtCall stmtCall);
	public T doSwitch(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef) {
if ( stmtSetOrCallOrVarDef == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (stmtSetOrCallOrVarDef instanceof StmtSet) return caseStmtSet((StmtSet)stmtSetOrCallOrVarDef);
		if (stmtSetOrCallOrVarDef instanceof VarDef) return caseVarDef((VarDef)stmtSetOrCallOrVarDef);
		if (stmtSetOrCallOrVarDef instanceof StmtCall) return caseStmtCall((StmtCall)stmtSetOrCallOrVarDef);
		throw new Error("Switch did not match any case: " + stmtSetOrCallOrVarDef);
	}
}

