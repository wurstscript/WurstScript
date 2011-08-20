package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitch <T> {
	abstract public T caseStmtReturn(StmtReturn stmtReturn);
	abstract public T caseStmtSetOrCallOrVarDef(StmtSetOrCallOrVarDef stmtSetOrCallOrVarDef);
	abstract public T caseStmtWhile(StmtWhile stmtWhile);
	abstract public T caseStmtDestroy(StmtDestroy stmtDestroy);
	abstract public T caseVarDef(VarDef varDef);
	abstract public T caseStmtChangeRefCount(StmtChangeRefCount stmtChangeRefCount);
	abstract public T caseStmtIf(StmtIf stmtIf);
	public T doSwitch(Statement statement) {
if ( statement == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (statement instanceof StmtReturn) return caseStmtReturn((StmtReturn)statement);
		if (statement instanceof StmtSetOrCallOrVarDef) return caseStmtSetOrCallOrVarDef((StmtSetOrCallOrVarDef)statement);
		if (statement instanceof StmtWhile) return caseStmtWhile((StmtWhile)statement);
		if (statement instanceof StmtDestroy) return caseStmtDestroy((StmtDestroy)statement);
		if (statement instanceof VarDef) return caseVarDef((VarDef)statement);
		if (statement instanceof StmtChangeRefCount) return caseStmtChangeRefCount((StmtChangeRefCount)statement);
		if (statement instanceof StmtIf) return caseStmtIf((StmtIf)statement);
		throw new Error("Switch did not match any case: " + statement);
	}
}

