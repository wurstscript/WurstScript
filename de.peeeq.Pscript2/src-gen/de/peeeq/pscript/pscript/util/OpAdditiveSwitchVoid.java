package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class OpAdditiveSwitchVoid {
	abstract public void caseOpMinus(OpMinus opMinus);
	abstract public void caseOpPlus(OpPlus opPlus);
	public void doSwitch(OpAdditive opAdditive) {
		if (opAdditive instanceof OpMinus) { caseOpMinus((OpMinus)opAdditive); return; }
		if (opAdditive instanceof OpPlus) { caseOpPlus((OpPlus)opAdditive); return; }
		throw new Error("Switch did not match any case.");
	}
}

