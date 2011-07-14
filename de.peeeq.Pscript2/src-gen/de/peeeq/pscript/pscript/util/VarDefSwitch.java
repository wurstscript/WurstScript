package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class VarDefSwitch <T> {
	abstract public T caseParameterDef(ParameterDef parameterDef);
	public T doSwitch(VarDef varDef) {
		if (varDef instanceof ParameterDef) return caseParameterDef((ParameterDef)varDef);
		throw new Error("Switch did not match any case.");
	}
}

