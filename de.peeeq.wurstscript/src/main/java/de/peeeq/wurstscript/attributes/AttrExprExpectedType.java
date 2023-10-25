package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.NonNull;

import java.util.Collection;
import java.util.Optional;

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

    public static @NonNull WurstType calculate(Expr expr) {
        try {
            Element parent = expr.getParent();
            if (parent instanceof Arguments) {
                Arguments args = (Arguments) parent;
                Element parent2 = args.getParent();
                if (parent2 instanceof StmtCall) {
                    StmtCall stmtCall = (StmtCall) parent2;
                    return expectedType(expr, args, stmtCall);
                } else if (parent2 instanceof SuperConstructorCall) {
                    SuperConstructorCall constructorDef = (SuperConstructorCall) parent2;
                    return expectedTypeSuperCall(constructorDef, expr);
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
                    return nearestFuncDef.attrReturnTyp();
                }
            } else if (parent instanceof SwitchCase) {
                SwitchCase sc = (SwitchCase) parent;
                SwitchStmt s = (SwitchStmt) sc.getParent().getParent();
                return s.getExpr().attrTyp();
            } else if (parent instanceof ExprIfElse) {
                ExprIfElse ie = (ExprIfElse) parent;
                if (expr == ie.getCond()) {
                    return WurstTypeBool.instance();
                } else {
                    return ie.attrExpectedTypRaw();
                }
            } else if (parent instanceof ExprMemberMethod) {
                ExprMemberMethod m = (ExprMemberMethod) parent;
                if (m.getLeft() == expr) {
                    WurstType receiverType = m.attrFunctionSignature().getReceiverType();
                    if (receiverType == null) {
                        return WurstTypeUnknown.instance();
                    }
                    return receiverType;
                }
            }
        } catch (CyclicDependencyError | CompileError t) {
            WLogger.info("Something went wrong while computing the expected type for "
                    + Utils.printElementWithSource(Optional.of(expr))
                    + "\nThis is probably not a bug, but we are logging it anyway since it might help to improve error "
                    + "messages.");
            WLogger.info(t);
        }

        return WurstTypeUnknown.instance();
    }

    private static WurstType expectedTypeSuperCall(SuperConstructorCall sc, Expr expr) {
        ConstructorDef constr = (ConstructorDef) sc.getParent();
        ClassDef c = constr.attrNearestClassDef();
        if (c == null) {
            return WurstTypeUnknown.instance();
        }
        WurstTypeClass superClass = c.attrTypC().extendedClass();
        if (superClass == null) {
            return WurstTypeUnknown.instance();
        }
        // call super constructor
        ClassDef superClassDef = superClass.getDef();
        ConstructorDefs constructors = superClassDef.getConstructors();


        WurstType res = WurstTypeUnknown.instance();

        int paramIndex = SmallHelpers.superArgs(constr).indexOf(expr);

        for (ConstructorDef superConstr : constructors) {
            if (superConstr.getParameters().size() == SmallHelpers.superArgs(constr).size()) {
                res = res.typeUnion(superConstr.getParameters().get(paramIndex).getTyp().attrTyp(), expr);
            }
        }

        return res;
    }

    private static WurstType expectedType(Expr expr, Arguments args, StmtCall stmtCall) {
        Collection<FunctionSignature> sigs = stmtCall.attrPossibleFunctionSignatures();

        int index = args.indexOf(expr);

        WurstType res = WurstTypeUnknown.instance();

        for (FunctionSignature sig : sigs) {
            if (index < sig.getMaxNumParams()) {
                res = res.typeUnion(sig.getParamType(index), expr);
            }
        }
        return res;
    }

    private static WurstType expectedTypeAfterOverloading(Expr expr, Arguments args, StmtCall stmtCall) {
        FunctionSignature sig = stmtCall.attrFunctionSignature();
        int index = args.indexOf(expr);

        if (index < sig.getMaxNumParams()) {
            return sig.getParamType(index);
        }
        return WurstTypeUnknown.instance();
    }

    public static WurstType normalizedType(Expr e) {
        return e.attrExpectedTypRaw().normalize();
    }

    public static WurstType afterOverloading(Expr e) {
        Element parent = e.getParent();
        if (parent instanceof Arguments) {
            Arguments args = (Arguments) parent;
            Element parent2 = args.getParent();
            if (parent2 instanceof StmtCall) {
                StmtCall stmtCall = (StmtCall) parent2;
                return expectedTypeAfterOverloading(e, args, stmtCall).normalize();
            }
        }
        return e.attrExpectedTyp();
    }
}
