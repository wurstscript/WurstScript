package de.peeeq.pscript.attributes;

import de.peeeq.pscript.attributes.infrastructure.AbstractAttribute;
import de.peeeq.pscript.attributes.infrastructure.AttributeDependencies;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.ClassMember;
import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprAdditive;
import de.peeeq.pscript.pscript.ExprAnd;
import de.peeeq.pscript.pscript.ExprBoolVal;
import de.peeeq.pscript.pscript.ExprComparison;
import de.peeeq.pscript.pscript.ExprEquality;
import de.peeeq.pscript.pscript.ExprFuncRef;
import de.peeeq.pscript.pscript.ExprFunctioncall;
import de.peeeq.pscript.pscript.ExprIdentifier;
import de.peeeq.pscript.pscript.ExprIntVal;
import de.peeeq.pscript.pscript.ExprMember;
import de.peeeq.pscript.pscript.ExprMemberRight;
import de.peeeq.pscript.pscript.ExprMult;
import de.peeeq.pscript.pscript.ExprNot;
import de.peeeq.pscript.pscript.ExprNumVal;
import de.peeeq.pscript.pscript.ExprOr;
import de.peeeq.pscript.pscript.ExprSign;
import de.peeeq.pscript.pscript.ExprStrval;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.OpDivInt;
import de.peeeq.pscript.pscript.OpDivReal;
import de.peeeq.pscript.pscript.OpModInt;
import de.peeeq.pscript.pscript.OpModReal;
import de.peeeq.pscript.pscript.OpMult;
import de.peeeq.pscript.pscript.OpMultiplicative;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.ParameterDef;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.pscript.util.ClassMemberSwitch;
import de.peeeq.pscript.pscript.util.ExprSwitch;
import de.peeeq.pscript.types.PScriptTypeArray;
import de.peeeq.pscript.types.PScriptTypeBool;
import de.peeeq.pscript.types.PScriptTypeCode;
import de.peeeq.pscript.types.PScriptTypeInt;
import de.peeeq.pscript.types.PScriptTypePackage;
import de.peeeq.pscript.types.PScriptTypeReal;
import de.peeeq.pscript.types.PScriptTypeString;
import de.peeeq.pscript.types.PScriptTypeVoid;
import de.peeeq.pscript.types.PsciptFuncType;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeError;

public class AttrExprType extends AbstractAttribute<Expr, PscriptType> {


	@Override
	public
	PscriptType calculate(final AttributeManager attributeManager,
			AttributeDependencies dependencies, Expr node) {

//		return new PscriptSwitch<PscriptType>() {
		return new ExprSwitch<PscriptType>() {
//			@Override
//			public PscriptType defaultCase(EObject e) {
//				return new PscriptTypeError("Typing not implemented for " + e);
//			}
			
			@Override
			public PscriptType caseExprIntVal(ExprIntVal e) {
				return PScriptTypeInt.instance();
			}
			
			@Override
			public PscriptType caseExprAdditive(ExprAdditive e) {
				// A) left + right
				// B) left -r right
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (leftType.isSubtypeOf(PScriptTypeInt.instance())
						&& rightType.isSubtypeOf(PScriptTypeInt.instance())) {
					return PScriptTypeInt.instance();
				} else if ((leftType.isSubtypeOf(PScriptTypeReal.instance()) || leftType.isSubtypeOf(PScriptTypeInt.instance()))
						&& (rightType.isSubtypeOf(PScriptTypeReal.instance()) || rightType.isSubtypeOf(PScriptTypeInt.instance()))) {
					return PScriptTypeReal.instance();
				} else if (leftType.isSubtypeOf(PScriptTypeString.instance())
						&& rightType.isSubtypeOf(PScriptTypeString.instance())) {
					return PScriptTypeString.instance();
				} else {
					return new PscriptTypeError("incompatible types " + leftType + " and " + rightType);
				}
				
			}
			
			@Override
			public PscriptType caseExprMult(ExprMult e) {
				// A) left * right
				// B) left / right
				// '*'|'/'|'%'|'mod'|'div'
				OpMultiplicative op = e.getOp();
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (leftType.isSubtypeOf(PScriptTypeInt.instance())
						&& rightType.isSubtypeOf(PScriptTypeInt.instance())) {
					// int , int
					if (op instanceof OpMult || op instanceof OpModInt || op instanceof OpDivInt) {
						return PScriptTypeInt.instance();
					} else if (op instanceof OpDivReal || op instanceof OpModReal) {
						return PScriptTypeReal.instance();
					} else {
						return new PscriptTypeError("Operator " + op + " not applicable for ints.");
					}
				} else if ((leftType.isSubtypeOf(PScriptTypeReal.instance()) || leftType.isSubtypeOf(PScriptTypeInt.instance()))
						&& (rightType.isSubtypeOf(PScriptTypeReal.instance()) || rightType.isSubtypeOf(PScriptTypeInt.instance()))) {
					// both are reals or ints but not both of them are ints					
					if (op instanceof OpMult ||  op instanceof OpDivReal || op instanceof OpModReal) {
						return PScriptTypeReal.instance();
					} else {
						return new PscriptTypeError("Operator " + op + " not applicable for reals.");
					}
				} else {
					return new PscriptTypeError("incompatible types " + leftType + " and " + rightType);
				}
				
			}
			
			@Override public PscriptType caseExprAnd(ExprAnd e) {
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (leftType.isSubtypeOf(PScriptTypeBool.instance()) && rightType.isSubtypeOf(PScriptTypeBool.instance())) {
					return PScriptTypeBool.instance();
				}
				return new PscriptTypeError("Operator not compatible with types " + leftType + " and " + rightType);
			}
			
			@Override public PscriptType caseExprOr(ExprOr e) {
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (leftType.isSubtypeOf(PScriptTypeBool.instance()) && rightType.isSubtypeOf(PScriptTypeBool.instance())) {
					return PScriptTypeBool.instance();
				}
				return new PscriptTypeError("Operator not compatible with types " + leftType + " and " + rightType);
			}
			
			@Override public PscriptType caseExprEquality(ExprEquality e) {
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (!leftType.equals(rightType)) {
					return new PscriptTypeError("Cannot compare types " + leftType + " and " + rightType);
				}
				return PScriptTypeBool.instance();
			}
			
			@Override public PscriptType caseExprComparison(ExprComparison e) {
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (!leftType.equals(rightType)) {
					return new PscriptTypeError("Cannot compare types " + leftType + " and " + rightType);
				}
				if (  !leftType.isSubtypeOf(PScriptTypeInt.instance())
				   && !leftType.isSubtypeOf(PScriptTypeReal.instance())) {
					return new PscriptTypeError("Comparison " + e.getOp() + "in only applicable for ints and reals but " +
							"found " + leftType);
				}
				return PScriptTypeBool.instance();
			}
			
			@Override public PscriptType caseExprNot(ExprNot e) {
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (!rightType.isSubtypeOf(PScriptTypeBool.instance())) {
					return new PscriptTypeError("Operator not can not be used with type " + rightType);
				}
				return PScriptTypeBool.instance();
			}
			
			@Override public PscriptType caseExprSign(ExprSign e) {
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (  !rightType.isSubtypeOf(PScriptTypeInt.instance())
				   && !rightType.isSubtypeOf(PScriptTypeReal.instance())) {
					return new PscriptTypeError("Negation is only applicable for ints and reals, but found " + rightType);
				}
				return rightType;
			}
			
			@Override
			public PscriptType caseExprFunctioncall(ExprFunctioncall e) {
				FuncDef funcDef = e.getNameVal();
				if (funcDef.getType() != null) {
					return attributeManager.getAttValue(AttrTypeExprType.class, funcDef.getType());
				} else {
					return PScriptTypeVoid.instance();
				}
			}
			
			@Override
			public PscriptType caseExprMember(ExprMember e) {
				// the type of the overall expression equals the type of the right expression
				if (e.getMessage() == null) {
					return new PscriptTypeError("right side is null");
				}
				
				ClassMember classMember = e.getMessage().getNameVal();
				
				if (classMember == null) {
					return new PscriptTypeError("right side (2) is null");
				}
				
				return new ClassMemberSwitch<PscriptType>() {

					@Override
					public PscriptType caseVarDef(VarDef varDef) {
						return attributeManager.getAttValue(AttrVarDefType.class, varDef);
					}

					@Override
					public PscriptType caseFuncDef(FuncDef funcDef) {
						if (funcDef.getType() == null) {
							return PScriptTypeVoid.instance();
						}						
						return attributeManager.getAttValue(AttrTypeExprType.class, funcDef.getType());
					}
					
				}.doSwitch(classMember);
			}
			
			
			@Override
			public PscriptType caseExprIdentifier(ExprIdentifier e) {
				VarDef decl = e.getNameVal();
				
				
				// VarDef
				if (decl instanceof VarDef) {
					VarDef v = (VarDef) decl;
					PscriptType varDefType = attributeManager.getAttValue(AttrVarDefType.class, (VarDef)decl);
					if (varDefType instanceof PScriptTypeArray) {
						return ((PScriptTypeArray)varDefType).getBaseType();
					} else {
						return varDefType;
					}
				}
				
				// PackageDeclaration
				if (decl instanceof PackageDeclaration) {
					return PScriptTypePackage.instance(decl.getName());
				}
				// ParameterDef
				if (decl instanceof ParameterDef) {
					ParameterDef pd = (ParameterDef) decl;
					return attributeManager.getAttValue(AttrTypeExprType.class, pd.getType());
				}
				
				
				// FuncDef
				if (decl instanceof FuncDef) {
					FuncDef f = (FuncDef) decl;
					PscriptType[] parameterTypes = new PscriptType[f.getParameters().size()];
					PscriptType retType = null;
					if (f.getType() != null) {
						retType = attributeManager.getAttValue(AttrTypeExprType.class, f.getType());
					}
					for (int i=0; i<parameterTypes.length; i++) {
						ParameterDef p = (ParameterDef) f.getParameters().get(i);
						parameterTypes[i] = attributeManager.getAttValue(AttrTypeExprType.class, p.getType());
					}
					
					return new PsciptFuncType(retType, parameterTypes);
				}
				
				// InitBlock - not possible
				// TypeDef - not possible
				
				return new PscriptTypeError("unexpected identifier reference : " + decl);
			}
			
			@Override
			public PscriptType caseExprStrval(ExprStrval e) {
				return PScriptTypeString.instance();
			}
			
			@Override
			public PscriptType caseExprBoolVal(ExprBoolVal e) {
				return PScriptTypeBool.instance();
			}
			
			
			@Override 
			public PscriptType caseExprNumVal(ExprNumVal e) {
				return PScriptTypeReal.instance();
			}


			@Override
			public PscriptType caseExprFuncRef(ExprFuncRef exprFuncRef) {
				return PScriptTypeCode.instance();
			}
			
				
		}.doSwitch(node);
		
	}

	

}
