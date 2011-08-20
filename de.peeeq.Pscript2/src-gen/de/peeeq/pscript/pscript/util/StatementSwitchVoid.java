package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitchVoid {
	abstract public void caseStmtReturn(StmtReturn stmtReturn);
	abstract public void caseStmtSetOrCallOrVarDef(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef);
	abstract public void caseStmtWhile(StmtWhile stmtWhile);
	abstract public void caseStmtDestroy(StmtDestroy stmtDestroy);
	abstract public void caseVarDef(VarDef varDef);
	abstract public void caseStmtChangeRefCount(StmtChangeRefCount stmtChangeRefCount);
	abstract public void caseStmtIf(StmtIf stmtIf);
	public void doSwitch(Statement statement) {
if ( statement == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (statement instanceof StmtReturn) { caseStmtReturn((StmtReturn)statement); return; }
		if (statement instanceof StmtSetOrCallOrVarDef) { caseStmtSetOrCallOrVarDef((StmtSetOrCallOrVarDef)statement); return; }
		if (statement instanceof StmtWhile) { caseStmtWhile((StmtWhile)statement); return; }
		if (statement instanceof StmtDestroy) { caseStmtDestroy((StmtDestroy)statement); return; }
		if (statement instanceof VarDef) { caseVarDef((VarDef)statement); return; }
		if (statement instanceof StmtChangeRefCount) { caseStmtChangeRefCount((StmtChangeRefCount)statement); return; }
		if (statement instanceof StmtIf) { caseStmtIf((StmtIf)statement); return; }
		throw new Error("Switch did not match any case: " + statement);
	}
}

