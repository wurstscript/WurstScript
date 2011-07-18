package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class StmtSetOrCallSwitch <T> {
	abstract public T caseStmtSet(StmtSet stmtSet);
	abstract public T caseStmtCall(StmtCall stmtCall);
	public T doSwitch(StmtSetOrCall stmtSetOrCall) {
if ( stmtSetOrCall == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (stmtSetOrCall instanceof StmtSet) return caseStmtSet((StmtSet)stmtSetOrCall);
		if (stmtSetOrCall instanceof StmtCall) return caseStmtCall((StmtCall)stmtSetOrCall);
		throw new Error("Switch did not match any case: " + stmtSetOrCall);
	}
}

