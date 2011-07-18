package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class TypeExprSwitchVoid {
	abstract public void caseTypeExprRef(TypeExprRef typeExprRef);
	abstract public void caseTypeExprBuildin(TypeExprBuildin typeExprBuildin);
	public void doSwitch(TypeExpr typeExpr) {
		if (typeExpr instanceof TypeExprRef) { caseTypeExprRef((TypeExprRef)typeExpr); return; }
		if (typeExpr instanceof TypeExprBuildin) { caseTypeExprBuildin((TypeExprBuildin)typeExpr); return; }
		throw new Error("Switch did not match any case.");
	}
}

