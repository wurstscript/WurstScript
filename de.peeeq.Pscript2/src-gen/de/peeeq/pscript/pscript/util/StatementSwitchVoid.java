package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitchVoid {
	abstract public void caseStmtSetOrCall(StmtSetOrCall stmtSetOrCall);
	abstract public void caseStmtReturn(StmtReturn stmtReturn);
	abstract public void caseStmtExitwhen(StmtExitwhen stmtExitwhen);
	abstract public void caseStmtIf(StmtIf stmtIf);
	abstract public void caseStmtLoop(StmtLoop stmtLoop);
	abstract public void caseVarDef(VarDef varDef);
	abstract public void caseStmtWhile(StmtWhile stmtWhile);
	public void doSwitch(Statement statement) {
		if (statement instanceof StmtSetOrCall) { caseStmtSetOrCall((StmtSetOrCall)statement); return; }
		if (statement instanceof StmtReturn) { caseStmtReturn((StmtReturn)statement); return; }
		if (statement instanceof StmtExitwhen) { caseStmtExitwhen((StmtExitwhen)statement); return; }
		if (statement instanceof StmtIf) { caseStmtIf((StmtIf)statement); return; }
		if (statement instanceof StmtLoop) { caseStmtLoop((StmtLoop)statement); return; }
		if (statement instanceof VarDef) { caseVarDef((VarDef)statement); return; }
		if (statement instanceof StmtWhile) { caseStmtWhile((StmtWhile)statement); return; }
		throw new Error("Switch did not match any case.");
	}
}

