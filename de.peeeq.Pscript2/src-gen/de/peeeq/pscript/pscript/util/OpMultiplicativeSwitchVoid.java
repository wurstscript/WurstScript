package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpMultiplicativeSwitchVoid {
	abstract public void caseOpDivReal(OpDivReal opDivReal);
	abstract public void caseOpModInt(OpModInt opModInt);
	abstract public void caseOpMult(OpMult opMult);
	abstract public void caseOpDivInt(OpDivInt opDivInt);
	abstract public void caseOpModReal(OpModReal opModReal);
	public void doSwitch(OpMultiplicative opMultiplicative) {
if ( opMultiplicative == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (opMultiplicative instanceof OpDivReal) { caseOpDivReal((OpDivReal)opMultiplicative); return; }
		if (opMultiplicative instanceof OpModInt) { caseOpModInt((OpModInt)opMultiplicative); return; }
		if (opMultiplicative instanceof OpMult) { caseOpMult((OpMult)opMultiplicative); return; }
		if (opMultiplicative instanceof OpDivInt) { caseOpDivInt((OpDivInt)opMultiplicative); return; }
		if (opMultiplicative instanceof OpModReal) { caseOpModReal((OpModReal)opMultiplicative); return; }
		throw new Error("Switch did not match any case: " + opMultiplicative);
	}
}

