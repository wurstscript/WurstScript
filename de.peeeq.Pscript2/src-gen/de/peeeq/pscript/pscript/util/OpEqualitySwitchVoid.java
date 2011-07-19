package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpEqualitySwitchVoid {
	abstract public void caseOpUnequals(OpUnequals opUnequals);
	abstract public void caseOpEquals(OpEquals opEquals);
	public void doSwitch(OpEquality opEquality) {
if ( opEquality == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (opEquality instanceof OpUnequals) { caseOpUnequals((OpUnequals)opEquality); return; }
		if (opEquality instanceof OpEquals) { caseOpEquals((OpEquals)opEquality); return; }
		throw new Error("Switch did not match any case: " + opEquality);
	}
}

