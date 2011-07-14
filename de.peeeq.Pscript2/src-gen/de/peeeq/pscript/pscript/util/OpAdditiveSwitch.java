package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpAdditiveSwitch <T> {
	abstract public T caseOpPlus(OpPlus opPlus);
	abstract public T caseOpMinus(OpMinus opMinus);
	public T doSwitch(OpAdditive opAdditive) {
		if (opAdditive instanceof OpPlus) return caseOpPlus((OpPlus)opAdditive);
		if (opAdditive instanceof OpMinus) return caseOpMinus((OpMinus)opAdditive);
		throw new Error("Switch did not match any case.");
	}
}

