package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElementWithLeft;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.utils.LineOffsets;

public class AttrImplicitParameter {

	public static OptExpr getImplicitParameter(ExprMemberVar e) {
		Expr result = getImplicitParameterUsingLeft(e);
		if (result == null) {
			return getImplicitParamterCaseNormalVar(e);
		} else {
			return result;
		}
	}
	
	public static OptExpr getImplicitParameter(ExprMemberArrayVar e) {
		Expr result = getImplicitParameterUsingLeft(e);
		if (result == null) {
			return getImplicitParamterCaseNormalVar(e);
		} else {
			return result;
		}
	}
	
	public static OptExpr getImplicitParameter(ExprVarAccess e) {
		return getImplicitParamterCaseNormalVar(e);
	}

	public static OptExpr getImplicitParameter(ExprVarArrayAccess e) {
		return getImplicitParamterCaseNormalVar(e);
	}
	
	

	public static OptExpr getImplicitParameter(ExprFunctionCall e) {
		return getImplicitParamterCaseNormalFunctionCall(e);
	}

	public static OptExpr getImplicitParameter(ExprMemberMethod e) {
		Expr result = getImplicitParameterUsingLeft(e);
		if (result == null) {
			return getImplicitParamterCaseNormalFunctionCall(e);
		} else {
			return result;
		}
	}
	
	private static Expr getImplicitParameterUsingLeft(AstElementWithLeft e) {
		PscriptType leftType = e.getLeft().attrTyp();
		if (leftType instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope ns = (PscriptTypeNamedScope) leftType;
			if (ns.isStaticRef()) {
				// we have a static ref like Math.sqrt()
				// this will be handled like if we just have sqrt()
				// if we have an implicit parameter depends on whether sqrt is static or not
				return null;
			}
		}
		return e.getLeft();
	}

	private static OptExpr getImplicitParamterCaseNormalFunctionCall(FunctionCall e) {
		FunctionDefinition calledFunc = e.attrFuncDef();
		if (calledFunc == null) {
			return Ast.NoExpr();
		}
		if (calledFunc.attrIsDynamicClassMember()) {
			// dynamic function call
			if (e.attrIsDynamicContext()) {		
				// dynamic context means we have a 'this':
				return Ast.ExprThis(Ast.WPos("{generated}", LineOffsets.dummy, 0, 0));
			} else {
				attr.addError(e.getSource(), "Cannot call dynamic function " + e.getFuncName() + " from static context." );
				return Ast.NoExpr();
			}
		} else {
			// static function:
			return Ast.NoExpr();
		}
	}

	private static OptExpr getImplicitParamterCaseNormalVar(NameRef e) {
		NameDef def = e.attrNameDef();
		if (def instanceof VarDef) {
			VarDef varDef = (VarDef) def;
			if (varDef.attrIsDynamicClassMember()) {
				// dynamic var access
				if (e.attrIsDynamicContext()) {		
					// dynamic context means we have a 'this':
					return Ast.ExprThis(Ast.WPos("{generated}", LineOffsets.dummy, 0, 0));
				} else {
					attr.addError(e.getSource(), "Cannot access dynamic variabe " + varDef.getName() + " from static context." );
					return Ast.NoExpr();
				}
			}
		}
		return Ast.NoExpr();
	}

	
	
}
