package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;

public class AttrImplicitParameter {

	public static OptExpr getImplicitParameter(ExprFunctionCall e) {
		return getImplicitParamterCaseNormalFunctionCall(e);
	}

	public static OptExpr getImplicitParameter(ExprMemberMethod e) {
		PscriptType leftType = e.getLeft().attrTyp();
		if (leftType instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope ns = (PscriptTypeNamedScope) leftType;
			if (ns.isStaticRef()) {
				// we have a static ref like Math.sqrt()
				// this will be handled like if we just have sqrt()
				// if we have an implicit parameter depends on wether sqrt is static or not
				return getImplicitParamterCaseNormalFunctionCall(e);
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
				return Ast.ExprThis(Ast.WPos("{generated}", 0, 0));
			} else {
				attr.addError(e.getSource(), "Cannot call dynamic function " + e.getFuncName() + " from static context." );
				return Ast.NoExpr();
			}
		} else {
			// static function:
			return Ast.NoExpr();
		}
	}
	
}
