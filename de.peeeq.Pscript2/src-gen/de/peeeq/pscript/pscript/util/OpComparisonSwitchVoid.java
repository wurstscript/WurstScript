package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpComparisonSwitchVoid {
	abstract public void caseOpLessEq(OpLessEq opLessEq);
	abstract public void caseOpGreaterEq(OpGreaterEq opGreaterEq);
	abstract public void caseOpGreater(OpGreater opGreater);
	abstract public void caseOpLess(OpLess opLess);
	public void doSwitch(OpComparison opComparison) {
		if (opComparison instanceof OpLessEq) { caseOpLessEq((OpLessEq)opComparison); return; }
		if (opComparison instanceof OpGreaterEq) { caseOpGreaterEq((OpGreaterEq)opComparison); return; }
		if (opComparison instanceof OpGreater) { caseOpGreater((OpGreater)opComparison); return; }
		if (opComparison instanceof OpLess) { caseOpLess((OpLess)opComparison); return; }
		throw new Error("Switch did not match any case.");
	}
}

