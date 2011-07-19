package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitchVoid {
	abstract public void caseStmtLoop(StmtLoop stmtLoop);
	abstract public void caseStmtSet(StmtSet stmtSet);
	abstract public void caseStmtCall(StmtCall stmtCall);
	abstract public void caseStmtIf(StmtIf stmtIf);
	abstract public void caseStmtWhile(StmtWhile stmtWhile);
	abstract public void caseVarDef(VarDef varDef);
	abstract public void caseStmtSetOrCallOrVarDef(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef);
	abstract public void caseStmtExitwhen(StmtExitwhen stmtExitwhen);
	abstract public void caseStmtReturn(StmtReturn stmtReturn);
	public void doSwitch(Statement statement) {
if ( statement == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (statement instanceof StmtLoop) { caseStmtLoop((StmtLoop)statement); return; }
		if (statement instanceof StmtSet) { caseStmtSet((StmtSet)statement); return; }
		if (statement instanceof StmtCall) { caseStmtCall((StmtCall)statement); return; }
		if (statement instanceof StmtIf) { caseStmtIf((StmtIf)statement); return; }
		if (statement instanceof StmtWhile) { caseStmtWhile((StmtWhile)statement); return; }
		if (statement instanceof VarDef) { caseVarDef((VarDef)statement); return; }
		if (statement instanceof StmtSetOrCallOrVarDef) { caseStmtSetOrCallOrVarDef((StmtSetOrCallOrVarDef)statement); return; }
		if (statement instanceof StmtExitwhen) { caseStmtExitwhen((StmtExitwhen)statement); return; }
		if (statement instanceof StmtReturn) { caseStmtReturn((StmtReturn)statement); return; }
		throw new Error("Switch did not match any case: " + statement);
	}
}

