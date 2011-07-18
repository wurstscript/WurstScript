package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpMultiplicativeSwitchVoid {
	abstract public void caseOpModReal(OpModReal opModReal);
	abstract public void caseOpModInt(OpModInt opModInt);
	abstract public void caseOpDivReal(OpDivReal opDivReal);
	abstract public void caseOpMult(OpMult opMult);
	public void doSwitch(OpMultiplicative opMultiplicative) {
		if (opMultiplicative instanceof OpModReal) { caseOpModReal((OpModReal)opMultiplicative); return; }
		if (opMultiplicative instanceof OpModInt) { caseOpModInt((OpModInt)opMultiplicative); return; }
		if (opMultiplicative instanceof OpDivReal) { caseOpDivReal((OpDivReal)opMultiplicative); return; }
		if (opMultiplicative instanceof OpMult) { caseOpMult((OpMult)opMultiplicative); return; }
		throw new Error("Switch did not match any case.");
	}
}

