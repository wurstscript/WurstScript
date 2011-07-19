package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class TypeExprSwitch <T> {
	abstract public T caseTypeExprBuildin(TypeExprBuildin typeExprBuildin);
	abstract public T caseTypeExprRef(TypeExprRef typeExprRef);
	public T doSwitch(TypeExpr typeExpr) {
if ( typeExpr == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (typeExpr instanceof TypeExprBuildin) return caseTypeExprBuildin((TypeExprBuildin)typeExpr);
		if (typeExpr instanceof TypeExprRef) return caseTypeExprRef((TypeExprRef)typeExpr);
		throw new Error("Switch did not match any case: " + typeExpr);
	}
}

