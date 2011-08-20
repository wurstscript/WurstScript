package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ExprSwitchVoid {
	abstract public void caseExprMult(ExprMult exprMult);
	abstract public void caseExprStrval(ExprStrval exprStrval);
	abstract public void caseExprFunctioncall(ExprFunctioncall exprFunctioncall);
	abstract public void caseExprOr(ExprOr exprOr);
	abstract public void caseExprAdditive(ExprAdditive exprAdditive);
	abstract public void caseExprNot(ExprNot exprNot);
	abstract public void caseExprAnd(ExprAnd exprAnd);
	abstract public void caseExprComparison(ExprComparison exprComparison);
	abstract public void caseExprIntVal(ExprIntVal exprIntVal);
	abstract public void caseExprEquality(ExprEquality exprEquality);
	abstract public void caseExprNumVal(ExprNumVal exprNumVal);
	abstract public void caseExprThis(ExprThis exprThis);
	abstract public void caseExprIdentifier(ExprIdentifier exprIdentifier);
	abstract public void caseExprNewObject(ExprNewObject exprNewObject);
	abstract public void caseExprFuncRef(ExprFuncRef exprFuncRef);
	abstract public void caseExprSign(ExprSign exprSign);
	abstract public void caseExprBoolVal(ExprBoolVal exprBoolVal);
	abstract public void caseExprMember(ExprMember exprMember);
	public void doSwitch(Expr expr) {
if ( expr == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (expr instanceof ExprMult) { caseExprMult((ExprMult)expr); return; }
		if (expr instanceof ExprStrval) { caseExprStrval((ExprStrval)expr); return; }
		if (expr instanceof ExprFunctioncall) { caseExprFunctioncall((ExprFunctioncall)expr); return; }
		if (expr instanceof ExprOr) { caseExprOr((ExprOr)expr); return; }
		if (expr instanceof ExprAdditive) { caseExprAdditive((ExprAdditive)expr); return; }
		if (expr instanceof ExprNot) { caseExprNot((ExprNot)expr); return; }
		if (expr instanceof ExprAnd) { caseExprAnd((ExprAnd)expr); return; }
		if (expr instanceof ExprComparison) { caseExprComparison((ExprComparison)expr); return; }
		if (expr instanceof ExprIntVal) { caseExprIntVal((ExprIntVal)expr); return; }
		if (expr instanceof ExprEquality) { caseExprEquality((ExprEquality)expr); return; }
		if (expr instanceof ExprNumVal) { caseExprNumVal((ExprNumVal)expr); return; }
		if (expr instanceof ExprThis) { caseExprThis((ExprThis)expr); return; }
		if (expr instanceof ExprIdentifier) { caseExprIdentifier((ExprIdentifier)expr); return; }
		if (expr instanceof ExprNewObject) { caseExprNewObject((ExprNewObject)expr); return; }
		if (expr instanceof ExprFuncRef) { caseExprFuncRef((ExprFuncRef)expr); return; }
		if (expr instanceof ExprSign) { caseExprSign((ExprSign)expr); return; }
		if (expr instanceof ExprBoolVal) { caseExprBoolVal((ExprBoolVal)expr); return; }
		if (expr instanceof ExprMember) { caseExprMember((ExprMember)expr); return; }
		throw new Error("Switch did not match any case: " + expr);
	}
}

