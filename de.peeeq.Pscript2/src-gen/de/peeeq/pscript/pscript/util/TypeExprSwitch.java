package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class TypeExprSwitch <T> {
	abstract public T caseTypeExprRef(TypeExprRef typeExprRef);
	abstract public T caseTypeExprBuildin(TypeExprBuildin typeExprBuildin);
	public T doSwitch(TypeExpr typeExpr) {
if ( typeExpr == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (typeExpr instanceof TypeExprRef) return caseTypeExprRef((TypeExprRef)typeExpr);
		if (typeExpr instanceof TypeExprBuildin) return caseTypeExprBuildin((TypeExprBuildin)typeExpr);
		throw new Error("Switch did not match any case: " + typeExpr);
	}
}

