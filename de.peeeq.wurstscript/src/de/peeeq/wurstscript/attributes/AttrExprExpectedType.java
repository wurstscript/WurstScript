package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.StmtCall;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBool;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeUnknown;

/**
 * This attribute calculates the expected type for an expression
 * for example if you have:
 * 
 * 	function foo(A a)
 *  function bar() returns B
 * 
 * and call it with foo(bar()), then the expected type of the expected type 
 * of the expression bar() will be A and the actual type (see attrExprType) will be B.
 */
public class AttrExprExpectedType {

	public static WurstType calculate(Expr expr) {
		try {
			AstElement parent = expr.getParent();
			if (parent instanceof Arguments) {
				Arguments args = (Arguments) parent;
				if (args.getParent() instanceof StmtCall) {
					StmtCall stmtCall = (StmtCall) args.getParent();
					FunctionSignature sig = stmtCall.attrFunctionSignature();
					for (int i = 0; i < args.size(); i++) {
						if (args.get(i) == expr) {
							return sig.getParamTypes().get(i);
						}
					}
					throw new CompileError(expr.getSource(), "a) could not find expr " + expr + " in parent " + parent);
				} else {
					throw new CompileError(expr.getSource(), "could not calculate expected type");
				}
			} else if (parent instanceof StmtSet) {
				StmtSet stmtSet = (StmtSet) parent;
				if (stmtSet.getRight() == expr) {
					WurstType leftType = stmtSet.getUpdatedExpr().attrTyp();
					return leftType;
				} else if (stmtSet.getUpdatedExpr() == expr) {
					return WurstTypeUnknown.instance();
				}
				throw new CompileError(expr.getSource(), "b) could not find expr " + expr + " in parent " + parent);
			} else if (parent instanceof VarDef) {
				VarDef varDef = (VarDef) parent;
				WurstType leftType = varDef.attrTyp();
				return leftType;
			} else if (parent instanceof ExprBinary) {
				ExprBinary exprBinary = (ExprBinary) parent;
				if (exprBinary.getLeft() == expr) {
					return exprBinary.getRight().attrTyp();
				} else if (exprBinary.getRight() == expr) {
					return exprBinary.getLeft().attrTyp();
				}
				throw new CompileError(expr.getSource(), "c) could not find expr " + expr + " in parent " + parent);
			} else if (parent instanceof ExprUnary) {
				ExprUnary exprUnary = (ExprUnary) parent;
				if (exprUnary.attrExpectedTyp().isSubtypeOf(WurstTypeInt.instance(), expr)) {
					return WurstTypeInt.instance();
				} else if (exprUnary.attrExpectedTyp().isSubtypeOf(WurstTypeReal.instance(), expr)) {
					return WurstTypeReal.instance();
				} else if (exprUnary.attrExpectedTyp() instanceof WurstTypeBool) {
					return WurstTypeBool.instance();
				}
			} else if (parent instanceof StmtReturn) {
				StmtReturn stmtReturn = (StmtReturn) parent;
				FunctionImplementation nearestFuncDef = stmtReturn.attrNearestFuncDef();
				return nearestFuncDef.getReturnTyp().attrTyp();		
//			} else if (parent instanceof ExprMemberMethod) {
//				ExprMemberMethod m = (ExprMemberMethod) parent;
//				if (m.getLeft() == expr) {
//					return m.attrFunctionSignature().getReceiverType();
//				}
			}
		} catch (Throwable t) {
			WLogger.info(t);
		}
		return WurstTypeUnknown.instance();
	}

}
