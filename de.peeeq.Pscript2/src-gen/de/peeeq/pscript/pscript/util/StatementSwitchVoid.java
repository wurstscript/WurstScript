package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitchVoid {
	abstract public void caseStmtIf(StmtIf stmtIf);
	abstract public void caseStmtSetOrCallOrVarDef(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef);
	abstract public void caseStmtReturn(StmtReturn stmtReturn);
	abstract public void caseVarDef(VarDef varDef);
	abstract public void caseStmtWhile(StmtWhile stmtWhile);
	public void doSwitch(Statement statement) {
if ( statement == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (statement instanceof StmtIf) { caseStmtIf((StmtIf)statement); return; }
		if (statement instanceof StmtSetOrCallOrVarDef) { caseStmtSetOrCallOrVarDef((StmtSetOrCallOrVarDef)statement); return; }
		if (statement instanceof StmtReturn) { caseStmtReturn((StmtReturn)statement); return; }
		if (statement instanceof VarDef) { caseVarDef((VarDef)statement); return; }
		if (statement instanceof StmtWhile) { caseStmtWhile((StmtWhile)statement); return; }
		throw new Error("Switch did not match any case: " + statement);
	}
}

