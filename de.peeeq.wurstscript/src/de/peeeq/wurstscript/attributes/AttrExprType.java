package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NoTypeExpr;
import de.peeeq.wurstscript.ast.OpAnd;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpDivInt;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OpGreater;
import de.peeeq.wurstscript.ast.OpGreaterEq;
import de.peeeq.wurstscript.ast.OpLess;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpMult;
import de.peeeq.wurstscript.ast.OpNot;
import de.peeeq.wurstscript.ast.OpOr;
import de.peeeq.wurstscript.ast.OpPlus;
import de.peeeq.wurstscript.ast.OpUnary;
import de.peeeq.wurstscript.ast.OpUnequals;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeJassInt;
import de.peeeq.wurstscript.types.PScriptTypeNull;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrExprType {
	
	public static  PscriptType calculate(Expr node) {
		return node.match(new Expr.Matcher<PscriptType>() {

			@Override
			public PscriptType case_ExprIntVal(ExprIntVal term)  {
				if (Utils.isJassCode(term)) {
					return PScriptTypeJassInt.instance();
				} else {
					return PScriptTypeInt.instance();
				}
			}

			@Override
			public PscriptType case_ExprRealVal(ExprRealVal term)
					 {
				return PScriptTypeReal.instance();
			}

			@Override
			public PscriptType case_ExprStringVal(ExprStringVal term)
					 {
				return PScriptTypeString.instance();
			}

			@Override
			public PscriptType case_ExprBoolVal(ExprBoolVal term)
					 {
				return PScriptTypeBool.instance();
			}

			@Override
			public PscriptType case_ExprFuncRef(ExprFuncRef term)
					 {
				return PScriptTypeCode.instance();
			}

			@Override
			public PscriptType case_ExprVarAccess(ExprVarAccess term)
					 {
				NameDef varDef = term.attrNameDef();
				if (varDef == null) {
					return PScriptTypeUnknown.instance();
				}
				return varDef.attrTyp();
			}

			@Override
			public PscriptType case_ExprVarArrayAccess(
					ExprVarArrayAccess term)  {
				NameDef varDef = term.attrNameDef();
				if (varDef == null) {
					return PScriptTypeUnknown.instance();
				}
				
				PscriptType varDefType = varDef.attrTyp();
				if (varDefType instanceof PScriptTypeArray) {
					return ((PScriptTypeArray) varDefType).getBaseType();
				} else {
					attr.addError(term.getSource(), "Variable " + varDef.getName() + " is no array variable.");
				}
				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType case_ExprThis(ExprThis term)  {
				// find nearest class definition
				ClassOrModule pos = term.attrNearestClassOrModule();
				if (pos != null) {
					if (pos instanceof ClassDef) {
						return new PscriptTypeClass((ClassDef) pos);
					}
					if (pos instanceof ModuleDef) {
						return new PscriptTypeModule((ModuleDef) pos);
					}
				} else {
					FunctionImplementation func = term.attrNearestFuncDef();
					if (func instanceof ExtensionFuncDef) {
						ExtensionFuncDef extensionFuncDef = (ExtensionFuncDef) func;
						return extensionFuncDef.getExtendedType().attrTyp();
					}
				}
				
				attr.addError(term.getSource(), "The keyword 'this' can only be used inside methods.");
				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType case_ExprBinary(final ExprBinary term)  {
				final PscriptType  leftType = term.getLeft().attrTyp();
				final PscriptType  rightType = term.getRight().attrTyp();
				return term.getOp().match(new OpBinary.Matcher<PscriptType>() {

					

					private PscriptType requireEqualTypes(
							PScriptTypeBool requiredType, PScriptTypeBool resultType) {
						if (!leftType.isSubtypeOf(requiredType)) {
							attr.addError(term.getLeft().getSource(), "Operator " + term.getOp() + " requires two operands of " +
									"type " + requiredType + " but left type was " + leftType);
							return PScriptTypeUnknown.instance();
						}
						if (!leftType.isSubtypeOf(requiredType)) {
							attr.addError(term.getRight().getSource(), "Operator " + term.getOp() + " requires two operands of " +
									"type " + requiredType + " but right type was " + leftType);
							return PScriptTypeUnknown.instance();
						}
						return resultType;
					}
					
					@Override
					public PscriptType case_OpOr(OpOr op)  {
						return requireEqualTypes(PScriptTypeBool.instance(), PScriptTypeBool.instance());
					}

					@Override
					public PscriptType case_OpAnd(OpAnd op)  {
						return requireEqualTypes(PScriptTypeBool.instance(), PScriptTypeBool.instance());
					}

					@Override
					public PscriptType case_OpEquals(OpEquals op)
							 {
						return caseEquality();
						
					}
					
					@Override
					public PscriptType case_OpUnequals(OpUnequals term)
							 {
						return caseEquality();
					}

					private PscriptType caseEquality() {
						
						if (leftType.equals(rightType)) {
							return PScriptTypeBool.instance();
						}
						
						if (leftType.isSubtypeOf(rightType) || rightType.isSubtypeOf(leftType)) {
							return  PScriptTypeBool.instance();
						}
						
						if (Utils.isJassCode(term)) {
							if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
								if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
									return  PScriptTypeBool.instance();									
								}
							}
						}
						
						// TODO check if the intersection of the basetypes of lefttpye and righttype is
						// not empty. Example:
						// class A implements B,C
						// -> B and C should be comparable
						attr.addError(term.getSource(), "Cannot compare types " + leftType + " with " + rightType);
					
						return PScriptTypeBool.instance();
					}

					

					@Override
					public PscriptType case_OpLessEq(OpLessEq term)
							 {
						return caseCompare();
					}

					

					@Override
					public PscriptType case_OpLess(OpLess term)  {
						return caseCompare();
					}

					@Override
					public PscriptType case_OpGreaterEq(OpGreaterEq term)
							 {
						return caseCompare();
					}

					@Override
					public PscriptType case_OpGreater(OpGreater term)
							 {
						return caseCompare();
					}
					
					private PscriptType caseCompare() {
						if (!(leftType instanceof PScriptTypeInt
								|| leftType instanceof PScriptTypeReal)) {
							attr.addError(term.getLeft().getSource(), "Can not compare with value of type " + leftType);
						}
						if (!(rightType instanceof PScriptTypeInt
								|| rightType instanceof PScriptTypeReal)) {
							attr.addError(term.getRight().getSource(), "Can not compare with value of type " + rightType);
						}
						return PScriptTypeBool.instance();
					}

					@Override
					public PscriptType case_OpPlus(OpPlus op)  {
						if (leftType instanceof PScriptTypeString && rightType instanceof PScriptTypeString) {
							return PScriptTypeString.instance();
						}
						return caseMathOperation();
					}

					private PscriptType caseMathOperation() {
						if (leftType instanceof PScriptTypeInt && rightType instanceof PScriptTypeInt) {
							return leftType;
						}
						if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
							if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
								return PScriptTypeReal.instance();
							}
						}
						attr.addError(term.getSource(), "Operator " + term.getOp() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}

					@Override
					public PscriptType case_OpMinus(OpMinus op)
							 {
						return caseMathOperation();
					}

					@Override
					public PscriptType case_OpMult(OpMult term)  {
						return caseMathOperation();
					}

					@Override
					public PscriptType case_OpDivReal(OpDivReal op)
							 {
						if (Utils.isJassCode(op)) {
							return caseMathOperation();
						} else {
							if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
								if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
									return PScriptTypeReal.instance();
								}
							}
							attr.addError(term.getSource(), "Operator " + term.getOp() +" is not defined for " +
									"operands " + leftType + " and " + rightType);
							return PScriptTypeUnknown.instance();
						}
					}

					@Override
					public PscriptType case_OpModReal(OpModReal op)
							 {
						if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
							if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
								return PScriptTypeReal.instance();
							}
						}
						attr.addError(term.getSource(), "Operator " + term.getOp() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}

					@Override
					public PscriptType case_OpModInt(OpModInt op)
							 {
						if (leftType instanceof PScriptTypeInt || rightType instanceof PScriptTypeInt) {
							return leftType;
						}
						attr.addError(term.getSource(), "Operator " + term.getOp() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}

					@Override
					public PscriptType case_OpDivInt(OpDivInt op)
							 {
						if (leftType instanceof PScriptTypeInt && rightType instanceof PScriptTypeInt) {
							return leftType;
						}
						attr.addError(term.getSource(), "Operator " + term.getOp() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}
				});
			}

			@Override
			public PscriptType case_ExprUnary(final ExprUnary term)  {
				final PscriptType rightType = term.getRight().attrTyp();
				return term.getOp().match(new OpUnary.Matcher<PscriptType>() {

					@Override
					public PscriptType case_OpNot(OpNot op)  {
						if (!(rightType instanceof PScriptTypeBool)) {
							attr.addError(term.getSource(), "Expected Boolean after not but found " + rightType);
						}
						return PScriptTypeBool.instance();
					}

					@Override
					public PscriptType case_OpMinus(OpMinus op)
							 {
						if (rightType instanceof PScriptTypeInt || rightType instanceof PScriptTypeReal) { 
							return rightType;
						}
						attr.addError(term.getSource(), "Expected Int or Real after Minus but found " + rightType);
						return PScriptTypeReal.instance();
					}
				});
			}

			@Override
			public PscriptType case_ExprMemberVar(ExprMemberVar term)
					 {
				NameDef varDef = term.attrNameDef();
				if (varDef == null) {
					return PScriptTypeUnknown.instance();
				}
				return varDef.attrTyp();
			}

			@Override
			public PscriptType case_ExprMemberArrayVar(
					ExprMemberArrayVar term)  {
				NameDef varDef = term.attrNameDef();
				return varDef.attrTyp();
			}

			@Override
			public PscriptType case_ExprMemberMethod(ExprMemberMethod term)
					 {
				FunctionDefinition f = term.attrFuncDef();
				if (f == null) {
					return PScriptTypeUnknown.instance();
				}
				if (f.getSignature().getTyp() instanceof NoTypeExpr) {
					return PScriptTypeVoid.instance();
				}
				PscriptType typ = f.getSignature().getTyp().attrTyp();
				if (typ instanceof PscriptTypeModule) {
					// example:
					// module A 
					//    function foo() returns thistype
					// class C
					//    use A
					// ...
					// C c = new C()
					// c.foo() // this should return type c  
					PscriptType leftType = term.getLeft().attrTyp();
					if (leftType instanceof PscriptTypeClass ||
							leftType instanceof PscriptTypeModule) {
						typ = leftType;
					}
				}
				return typ;
			}

			@Override
			public PscriptType case_ExprFunctionCall(ExprFunctionCall term)
					 {
				if (term.attrFuncDef() == null) {
					return PScriptTypeUnknown.instance();
				}
				
				FunctionDefinition f = term.attrFuncDef();
				if (f == null) {
					return PScriptTypeUnknown.instance();
				}
				if (f.getSignature().getTyp() instanceof NoTypeExpr) {
					return PScriptTypeVoid.instance();
				}
				PscriptType typ = f.getSignature().getTyp().attrTyp();
				if (typ instanceof PscriptTypeModule) {
					ClassOrModule classOrModule = term.attrNearestClassOrModule();
					if (classOrModule != null) {
						typ = TypesHelper.typeOf(classOrModule);
					}
				}
				return typ;
			}

			@Override
			public PscriptType case_ExprNewObject(ExprNewObject term)
					 {
				
				TypeDef typeDef = term.attrTypeDef();
				if (typeDef instanceof ClassDef) {
					return new PscriptTypeClass((ClassDef) typeDef);
				} else {
					attr.addError(term.getSource(), "Can only create instances of classes.");
					return PScriptTypeUnknown.instance();
				}
			}

			@Override
			public PscriptType case_ExprNull(ExprNull term)  {
				return PScriptTypeNull.instance();
			}

			@Override
			public PscriptType case_ExprCast(ExprCast term)  {
				PscriptType typ = term.getTyp().attrTyp();
				PscriptType exprTyp = term.getExpr().attrTyp();
				if (typ instanceof PScriptTypeInt && isClassOrModule(exprTyp)) {
					// cast from classtype to int: OK
				} else if (isClassOrModule(typ) && exprTyp instanceof PScriptTypeInt) {
					// cast from int to classtype: OK
				} else {
					attr.addError(term.getSource(), "Cannot cast from " + exprTyp + " to " + typ + ".");
				}
				return typ;
			}
		});
	}

	protected static boolean isClassOrModule(PscriptType typ) {
		return typ instanceof PscriptTypeClass 
				|| typ instanceof PscriptTypeModule;
	}

}
