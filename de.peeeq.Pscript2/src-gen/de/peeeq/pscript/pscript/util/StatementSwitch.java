package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitch <T> {
	abstract public T caseStmtIf(StmtIf stmtIf);
	abstract public T caseStmtSetOrCallOrVarDef(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef);
	abstract public T caseStmtReturn(StmtReturn stmtReturn);
	abstract public T caseVarDef(VarDef varDef);
	abstract public T caseStmtWhile(StmtWhile stmtWhile);
	public T doSwitch(Statement statement) {
if ( statement == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (statement instanceof StmtIf) return caseStmtIf((StmtIf)statement);
		if (statement instanceof StmtSetOrCallOrVarDef) return caseStmtSetOrCallOrVarDef((StmtSetOrCallOrVarDef)statement);
		if (statement instanceof StmtReturn) return caseStmtReturn((StmtReturn)statement);
		if (statement instanceof VarDef) return caseVarDef((VarDef)statement);
		if (statement instanceof StmtWhile) return caseStmtWhile((StmtWhile)statement);
		throw new Error("Switch did not match any case: " + statement);
	}
}

