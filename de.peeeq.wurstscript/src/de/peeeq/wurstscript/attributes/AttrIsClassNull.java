package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithInitialExpr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;

public class AttrIsClassNull {

	public static boolean calculate(ExprNull expr) {
		AstElement parent = expr.getParent();
		if (parent instanceof Arguments) {
			Arguments args = (Arguments) parent;
			ExprFunctionCall efc = (ExprFunctionCall) args.getParent();
			FunctionDefinition calledFunc = efc.attrFuncDef();
			for (int i = 0; i < args.size(); i++) {
				if (args.get(i) == expr) {
					PscriptType paramType = calledFunc.getParameters().get(i).attrTyp();
					return paramType instanceof PscriptTypeNamedScope;
				}
			}
			throw new CompileError(expr.getSource(), "could not find expr in parent");
		} else if (parent instanceof StmtSet) {
			StmtSet stmtSet = (StmtSet) parent;
			if (stmtSet.getRight() == expr) {
				PscriptType leftType = stmtSet.getUpdatedExpr().attrTyp();
				return leftType instanceof PscriptTypeNamedScope;
			}
			throw new CompileError(expr.getSource(), "could not find expr in parent");
		} else if (parent instanceof VarDef) {
			VarDef varDef = (VarDef) parent;
			PscriptType leftType = varDef.attrTyp();
			return leftType instanceof PscriptTypeNamedScope;
		} else if (parent instanceof ExprBinary) {
			ExprBinary exprBinary = (ExprBinary) parent;
			if (exprBinary.getLeft() == expr) {
				return exprBinary.getRight().attrTyp() instanceof PscriptTypeNamedScope;
			} else if (exprBinary.getRight() == expr) {
				return exprBinary.getLeft().attrTyp() instanceof PscriptTypeNamedScope;
			}
			throw new CompileError(expr.getSource(), "could not find expr in parent");
		} else if (parent instanceof StmtReturn) {
			StmtReturn stmtReturn = (StmtReturn) parent;
			FunctionImplementation nearestFuncDef = stmtReturn.attrNearestFuncDef();
			return nearestFuncDef.getReturnTyp().attrTyp() instanceof PscriptTypeNamedScope;		
		}

		throw new CompileError(expr.getSource(), "not implemented for parent " + parent.getClass());
	}

}
