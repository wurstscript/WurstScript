package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpEqualitySwitch <T> {
	abstract public T caseOpEquals(OpEquals opEquals);
	abstract public T caseOpUnequals(OpUnequals opUnequals);
	public T doSwitch(OpEquality opEquality) {
if ( opEquality == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (opEquality instanceof OpEquals) return caseOpEquals((OpEquals)opEquality);
		if (opEquality instanceof OpUnequals) return caseOpUnequals((OpUnequals)opEquality);
		throw new Error("Switch did not match any case: " + opEquality);
	}
}

