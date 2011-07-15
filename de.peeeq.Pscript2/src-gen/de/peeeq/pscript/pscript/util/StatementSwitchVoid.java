package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitchVoid {
	abstract public void caseStmtIf(StmtIf stmtIf);
	abstract public void caseStmtWhile(StmtWhile stmtWhile);
	abstract public void caseStmtReturn(StmtReturn stmtReturn);
	abstract public void caseVarDef(VarDef varDef);
	abstract public void caseStmtExpr(StmtExpr stmtExpr);
	public void doSwitch(Statement statement) {
		if (statement instanceof StmtIf) { caseStmtIf((StmtIf)statement); return; }
		if (statement instanceof StmtWhile) { caseStmtWhile((StmtWhile)statement); return; }
		if (statement instanceof StmtReturn) { caseStmtReturn((StmtReturn)statement); return; }
		if (statement instanceof VarDef) { caseVarDef((VarDef)statement); return; }
		if (statement instanceof StmtExpr) { caseStmtExpr((StmtExpr)statement); return; }
		throw new Error("Switch did not match any case.");
	}
}

