package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpAdditiveSwitch <T> {
	abstract public T caseOpMinus(OpMinus opMinus);
	abstract public T caseOpPlus(OpPlus opPlus);
	public T doSwitch(OpAdditive opAdditive) {
if ( opAdditive == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (opAdditive instanceof OpMinus) return caseOpMinus((OpMinus)opAdditive);
		if (opAdditive instanceof OpPlus) return caseOpPlus((OpPlus)opAdditive);
		throw new Error("Switch did not match any case: " + opAdditive);
	}
}

