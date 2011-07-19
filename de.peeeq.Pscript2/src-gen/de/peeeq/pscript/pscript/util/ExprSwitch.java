package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ExprSwitch <T> {
	abstract public T caseExprComparison(ExprComparison exprComparison);
	abstract public T caseExprSign(ExprSign exprSign);
	abstract public T caseExprBoolVal(ExprBoolVal exprBoolVal);
	abstract public T caseExprMult(ExprMult exprMult);
	abstract public T caseExprNot(ExprNot exprNot);
	abstract public T caseExprEquality(ExprEquality exprEquality);
	abstract public T caseExprStrval(ExprStrval exprStrval);
	abstract public T caseExprIdentifier(ExprIdentifier exprIdentifier);
	abstract public T caseExprAdditive(ExprAdditive exprAdditive);
	abstract public T caseExprNumVal(ExprNumVal exprNumVal);
	abstract public T caseExprOr(ExprOr exprOr);
	abstract public T caseExprMember(ExprMember exprMember);
	abstract public T caseExprFunctioncall(ExprFunctioncall exprFunctioncall);
	abstract public T caseExprFuncRef(ExprFuncRef exprFuncRef);
	abstract public T caseExprIntVal(ExprIntVal exprIntVal);
	abstract public T caseExprAnd(ExprAnd exprAnd);
	public T doSwitch(Expr expr) {
if ( expr == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (expr instanceof ExprComparison) return caseExprComparison((ExprComparison)expr);
		if (expr instanceof ExprSign) return caseExprSign((ExprSign)expr);
		if (expr instanceof ExprBoolVal) return caseExprBoolVal((ExprBoolVal)expr);
		if (expr instanceof ExprMult) return caseExprMult((ExprMult)expr);
		if (expr instanceof ExprNot) return caseExprNot((ExprNot)expr);
		if (expr instanceof ExprEquality) return caseExprEquality((ExprEquality)expr);
		if (expr instanceof ExprStrval) return caseExprStrval((ExprStrval)expr);
		if (expr instanceof ExprIdentifier) return caseExprIdentifier((ExprIdentifier)expr);
		if (expr instanceof ExprAdditive) return caseExprAdditive((ExprAdditive)expr);
		if (expr instanceof ExprNumVal) return caseExprNumVal((ExprNumVal)expr);
		if (expr instanceof ExprOr) return caseExprOr((ExprOr)expr);
		if (expr instanceof ExprMember) return caseExprMember((ExprMember)expr);
		if (expr instanceof ExprFunctioncall) return caseExprFunctioncall((ExprFunctioncall)expr);
		if (expr instanceof ExprFuncRef) return caseExprFuncRef((ExprFuncRef)expr);
		if (expr instanceof ExprIntVal) return caseExprIntVal((ExprIntVal)expr);
		if (expr instanceof ExprAnd) return caseExprAnd((ExprAnd)expr);
		throw new Error("Switch did not match any case: " + expr);
	}
}

