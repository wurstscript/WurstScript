package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithArgs;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.StmtCall;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.SwitchCase;
import de.peeeq.wurstscript.ast.SwitchStmt;
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
					throw new CompileError(expr.getSource(), "could not calculate expected type, arguments in " + args.getParent() + " " + args.attrSource().print());
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
				WurstType leftType = exprBinary.getLeft().attrTyp();
				WurstType rightType = exprBinary.getRight().attrTyp();
				if (leftType.equalsType(rightType, expr)) {
					// if both types are equal, result is clear:
					return leftType;
				} else {
					// otherwise, take the more specific type
					if (leftType.isSubtypeOf(rightType, expr)) {
						return rightType;
					} else if (rightType.isSubtypeOf(leftType, expr)) {
						return leftType;
					}
				}
				// no type is more specific. Not really clear what we want here...
				return WurstTypeUnknown.instance();
			} else if (parent instanceof ExprUnary) {
				ExprUnary exprUnary = (ExprUnary) parent;
				if (exprUnary.attrExpectedTyp().isSubtypeOf(WurstTypeInt.instance(), expr)) {
					return WurstTypeInt.instance();
				} else if (exprUnary.attrExpectedTyp().isSubtypeOf(WurstTypeReal.instance(), expr)) {
					return WurstTypeReal.instance();
				} else if (exprUnary.attrExpectedTyp().isSubtypeOf(WurstTypeBool.instance(), expr)) {
					return WurstTypeBool.instance();
				}
			} else if (parent instanceof StmtReturn) {
				StmtReturn stmtReturn = (StmtReturn) parent;
				FunctionImplementation nearestFuncDef = stmtReturn.attrNearestFuncDef();
				return nearestFuncDef.getReturnTyp().attrTyp();	
			} else if (parent instanceof SwitchCase) {
				SwitchCase sc = (SwitchCase) parent;
				SwitchStmt s = (SwitchStmt) sc.getParent().getParent();
				return s.getExpr().attrTyp();
//			} else if (parent instanceof ExprMemberMethod) {
//				ExprMemberMethod m = (ExprMemberMethod) parent;
//				if (m.getLeft() == expr) {
//					return m.attrFunctionSignature().getReceiverType();
//				}
			}
		} catch (CompileError t) {
			WLogger.info(t);
		}
		return WurstTypeUnknown.instance();
	}

	public static WurstType normalizedType(Expr e) {
		return e.attrExpectedTypRaw().normalize();
	}

}
