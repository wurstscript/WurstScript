package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ElseBlockSwitch <T> {
	abstract public T caseStatements(Statements statements);
	public T doSwitch(ElseBlock elseBlock) {
		if (elseBlock instanceof Statements) return caseStatements((Statements)elseBlock);
		throw new Error("Switch did not match any case.");
	}
}

