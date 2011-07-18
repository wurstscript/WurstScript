package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StatementSwitch <T> {
	abstract public T caseStmtSetOrCall(StmtSetOrCall stmtSetOrCall);
	abstract public T caseStmtReturn(StmtReturn stmtReturn);
	abstract public T caseStmtExitwhen(StmtExitwhen stmtExitwhen);
	abstract public T caseStmtIf(StmtIf stmtIf);
	abstract public T caseStmtLoop(StmtLoop stmtLoop);
	abstract public T caseVarDef(VarDef varDef);
	abstract public T caseStmtWhile(StmtWhile stmtWhile);
	public T doSwitch(Statement statement) {
if ( statement == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (statement instanceof StmtSetOrCall) return caseStmtSetOrCall((StmtSetOrCall)statement);
		if (statement instanceof StmtReturn) return caseStmtReturn((StmtReturn)statement);
		if (statement instanceof StmtExitwhen) return caseStmtExitwhen((StmtExitwhen)statement);
		if (statement instanceof StmtIf) return caseStmtIf((StmtIf)statement);
		if (statement instanceof StmtLoop) return caseStmtLoop((StmtLoop)statement);
		if (statement instanceof VarDef) return caseVarDef((VarDef)statement);
		if (statement instanceof StmtWhile) return caseStmtWhile((StmtWhile)statement);
		throw new Error("Switch did not match any case: " + statement);
	}
}

