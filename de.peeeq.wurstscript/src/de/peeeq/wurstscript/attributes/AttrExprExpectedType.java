package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ConstructorDefs;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
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
			Element parent = expr.getParent();
			if (parent instanceof Arguments) {
				Arguments args = (Arguments) parent;
				Element parent2 = args.getParent();
				if (parent2 instanceof StmtCall) {
					StmtCall stmtCall = (StmtCall) parent2;
					return expectedType(expr, args, stmtCall);
				} else if (parent2 instanceof ConstructorDef) {
                    ConstructorDef constructorDef = (ConstructorDef) parent2;
				    return expectedTypeSuperCall(constructorDef, expr);
				}
			} else if (parent instanceof StmtSet) {
				StmtSet stmtSet = (StmtSet) parent;
				if (stmtSet.getRight() == expr) {
					WurstType leftType = stmtSet.getUpdatedExpr().attrTyp();
					return leftType;
				} else if (stmtSet.getUpdatedExpr() == expr) {
					return WurstTypeUnknown.instance();
				}
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

	private static WurstType expectedTypeSuperCall(ConstructorDef constr, Expr expr) {
	    ClassDef c = constr.attrNearestClassDef();
        if (c == null) {
            return null;
        }
        ClassDef superClass = c.attrExtendedClass();
        if (superClass == null) {
            return null;
        }
        // call super constructor
        ConstructorDefs constructors = superClass.getConstructors();
	    
        
        WurstType res = WurstTypeUnknown.instance();
        
        int paramIndex = constr.getSuperArgs().indexOf(expr);
        
        for (ConstructorDef superConstr : constructors) {
            if (superConstr.getParameters().size() == constr.getSuperArgs().size()) {
                res = res.typeUnion(superConstr.getParameters().get(paramIndex).getTyp().attrTyp());
            }
        }
        
        return res;
    }

    private static WurstType expectedType(Expr expr, Arguments args, StmtCall stmtCall) {
		Collection<FunctionSignature> sigs = stmtCall.attrPossibleFunctionSignatures();
		
		int index = args.indexOf(expr);
		
		WurstType res = WurstTypeUnknown.instance();
		
		for (FunctionSignature sig : sigs) {
			if (index < sig.getParamTypes().size()) {
				res = res.typeUnion(sig.getParamTypes().get(index));
			}
		}
		return res;
	}

	public static WurstType normalizedType(Expr e) {
		return e.attrExpectedTypRaw().normalize();
	}

}
