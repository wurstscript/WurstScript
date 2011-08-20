package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpComparisonSwitch <T> {
	abstract public T caseOpLess(OpLess opLess);
	abstract public T caseOpLessEq(OpLessEq opLessEq);
	abstract public T caseOpGreaterEq(OpGreaterEq opGreaterEq);
	abstract public T caseOpGreater(OpGreater opGreater);
	public T doSwitch(OpComparison opComparison) {
if ( opComparison == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (opComparison instanceof OpLess) return caseOpLess((OpLess)opComparison);
		if (opComparison instanceof OpLessEq) return caseOpLessEq((OpLessEq)opComparison);
		if (opComparison instanceof OpGreaterEq) return caseOpGreaterEq((OpGreaterEq)opComparison);
		if (opComparison instanceof OpGreater) return caseOpGreater((OpGreater)opComparison);
		throw new Error("Switch did not match any case: " + opComparison);
	}
}

