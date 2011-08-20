package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpMultiplicativeSwitch <T> {
	abstract public T caseOpDivReal(OpDivReal opDivReal);
	abstract public T caseOpModInt(OpModInt opModInt);
	abstract public T caseOpMult(OpMult opMult);
	abstract public T caseOpDivInt(OpDivInt opDivInt);
	abstract public T caseOpModReal(OpModReal opModReal);
	public T doSwitch(OpMultiplicative opMultiplicative) {
if ( opMultiplicative == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (opMultiplicative instanceof OpDivReal) return caseOpDivReal((OpDivReal)opMultiplicative);
		if (opMultiplicative instanceof OpModInt) return caseOpModInt((OpModInt)opMultiplicative);
		if (opMultiplicative instanceof OpMult) return caseOpMult((OpMult)opMultiplicative);
		if (opMultiplicative instanceof OpDivInt) return caseOpDivInt((OpDivInt)opMultiplicative);
		if (opMultiplicative instanceof OpModReal) return caseOpModReal((OpModReal)opMultiplicative);
		throw new Error("Switch did not match any case: " + opMultiplicative);
	}
}

