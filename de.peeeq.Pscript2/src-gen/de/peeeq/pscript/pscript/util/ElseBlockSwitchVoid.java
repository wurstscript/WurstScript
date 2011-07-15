package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ElseBlockSwitchVoid {
	abstract public void caseStatements(Statements statements);
	public void doSwitch(ElseBlock elseBlock) {
		if (elseBlock instanceof Statements) { caseStatements((Statements)elseBlock); return; }
		throw new Error("Switch did not match any case.");
	}
}

