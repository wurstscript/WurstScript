package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpComparisonSwitch <T> {
	abstract public T caseOpGreaterEq(OpGreaterEq opGreaterEq);
	abstract public T caseOpLess(OpLess opLess);
	abstract public T caseOpGreater(OpGreater opGreater);
	abstract public T caseOpLessEq(OpLessEq opLessEq);
	public T doSwitch(OpComparison opComparison) {
if ( opComparison == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (opComparison instanceof OpGreaterEq) return caseOpGreaterEq((OpGreaterEq)opComparison);
		if (opComparison instanceof OpLess) return caseOpLess((OpLess)opComparison);
		if (opComparison instanceof OpGreater) return caseOpGreater((OpGreater)opComparison);
		if (opComparison instanceof OpLessEq) return caseOpLessEq((OpLessEq)opComparison);
		throw new Error("Switch did not match any case: " + opComparison);
	}
}

