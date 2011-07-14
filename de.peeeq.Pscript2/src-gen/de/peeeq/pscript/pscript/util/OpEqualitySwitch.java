package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpEqualitySwitch <T> {
	abstract public T caseOpUnequals(OpUnequals opUnequals);
	abstract public T caseOpEquals(OpEquals opEquals);
	public T doSwitch(OpEquality opEquality) {
		if (opEquality instanceof OpUnequals) return caseOpUnequals((OpUnequals)opEquality);
		if (opEquality instanceof OpEquals) return caseOpEquals((OpEquals)opEquality);
		throw new Error("Switch did not match any case.");
	}
}

