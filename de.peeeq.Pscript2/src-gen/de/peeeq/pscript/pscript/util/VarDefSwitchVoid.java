package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class VarDefSwitchVoid {
	abstract public void caseParameterDef(ParameterDef parameterDef);
	public void doSwitch(VarDef varDef) {
if ( varDef == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (varDef instanceof ParameterDef) { caseParameterDef((ParameterDef)varDef); return; }
		throw new Error("Switch did not match any case: " + varDef);
	}
}

