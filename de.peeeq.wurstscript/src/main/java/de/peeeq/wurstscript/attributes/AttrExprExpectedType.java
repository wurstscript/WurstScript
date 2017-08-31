package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.*;

import java.util.Collection;

/**
 * This attribute calculates the expected type for an expression
 * for example if you have:
 * <p>
 * function foo(A a)
 * function bar() returns B
 * <p>
 * and call it with foo(bar()), then the expected type of the expected type
 * of the expression bar() will be A and the actual type (see attrExprType) will be B.
 */
public class AttrExprExpectedType {


    public static WurstType calculate(Expr expr) {
        try {
            Element parent = expr.getParent();
            if (parent instanceof Argument) {
                Argument arg = (Argument) parent;
                // TODO expected type should consider name in argument, not just position
                Arguments args = (Arguments) parent.getParent();
                Element parent2 = args.getParent();
                if (parent2 instanceof StmtCall) {
                    StmtCall stmtCall = (StmtCall) parent2;
                    return expectedType(arg, args, stmtCall);
                }
            } else if (parent instanceof StmtSet) {
                StmtSet stmtSet = (StmtSet) parent;
                if (stmtSet.getRight() == expr) {
                    return stmtSet.getUpdatedExpr().attrTyp();
                } else if (stmtSet.getUpdatedExpr() == expr) {
                    return WurstTypeUnknown.instance();
                }
            } else if (parent instanceof VarDef) {
                VarDef varDef = (VarDef) parent;
                return varDef.attrTyp();
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
                if (nearestFuncDef != null) {
                    return nearestFuncDef.getReturnTyp().attrTyp();
                }
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

    private static WurstType expectedType(Argument expr, Arguments args, StmtCall stmtCall) {
        Collection<FunctionSignature> sigs = stmtCall.attrPossibleFunctionSignatures();

        int index = args.indexOf(expr);

        WurstType res = WurstTypeUnknown.instance();

        for (FunctionSignature sig : sigs) {
            if (index < sig.getParamTypes().paramCount()) {
                res = res.typeUnion(sig.getParamTypes().get(index).getType(), expr);
            }
        }
        return res;
    }

    public static WurstType normalizedType(Expr e) {
        return e.attrExpectedTypRaw().normalize();
    }

}
