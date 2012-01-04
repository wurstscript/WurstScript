package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithArgs;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;

public class AttrExprExpectedType {

	public static PscriptType calculate(Expr expr) {
		try {
			AstElement parent = expr.getParent();
			if (parent instanceof Arguments) {
				Arguments args = (Arguments) parent;
				WParameters params = null;
				if (args.getParent() instanceof ExprFunctionCall) {
					ExprFunctionCall efc = (ExprFunctionCall) args.getParent();
					params = efc.attrFuncDef().getParameters();
				} else if (args.getParent() instanceof ExprNewObject) {
					ExprNewObject eno = (ExprNewObject) args.getParent();
					params = eno.attrConstructorDef().getParameters();
				}
				for (int i = 0; i < args.size(); i++) {
					if (args.get(i) == expr) {
						PscriptType paramType = params.get(i).attrTyp();
						return paramType;
					}
				}
				throw new CompileError(expr.getSource(), "could not find expr in parent");
			} else if (parent instanceof StmtSet) {
				StmtSet stmtSet = (StmtSet) parent;
				if (stmtSet.getRight() == expr) {
					PscriptType leftType = stmtSet.getUpdatedExpr().attrTyp();
					return leftType;
				}
				throw new CompileError(expr.getSource(), "could not find expr in parent");
			} else if (parent instanceof VarDef) {
				VarDef varDef = (VarDef) parent;
				PscriptType leftType = varDef.attrTyp();
				return leftType;
			} else if (parent instanceof ExprBinary) {
				ExprBinary exprBinary = (ExprBinary) parent;
				if (exprBinary.getLeft() == expr) {
					return exprBinary.getRight().attrTyp();
				} else if (exprBinary.getRight() == expr) {
					return exprBinary.getLeft().attrTyp();
				}
				throw new CompileError(expr.getSource(), "could not find expr in parent");
			} else if (parent instanceof StmtReturn) {
				StmtReturn stmtReturn = (StmtReturn) parent;
				FunctionImplementation nearestFuncDef = stmtReturn.attrNearestFuncDef();
				return nearestFuncDef.getReturnTyp().attrTyp();		
			}
		} catch (Throwable t) {
			System.out.println("ignorable error: ");
			System.out.println(t);
		}
		return PScriptTypeUnknown.instance();
	}

}
