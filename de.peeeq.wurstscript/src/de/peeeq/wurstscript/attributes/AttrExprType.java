package de.peeeq.wurstscript.attributes;

import katja.common.NE;
import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.ExprBinaryPos;
import de.peeeq.wurstscript.ast.ExprBoolValPos;
import de.peeeq.wurstscript.ast.ExprFuncRefPos;
import de.peeeq.wurstscript.ast.ExprFunctionCallPos;
import de.peeeq.wurstscript.ast.ExprIntValPos;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarPos;
import de.peeeq.wurstscript.ast.ExprMemberMethodPos;
import de.peeeq.wurstscript.ast.ExprMemberVarPos;
import de.peeeq.wurstscript.ast.ExprNewObjectPos;
import de.peeeq.wurstscript.ast.ExprNullPos;
import de.peeeq.wurstscript.ast.ExprPos;
import de.peeeq.wurstscript.ast.ExprRealValPos;
import de.peeeq.wurstscript.ast.ExprStringValPos;
import de.peeeq.wurstscript.ast.ExprThisPos;
import de.peeeq.wurstscript.ast.ExprUnaryPos;
import de.peeeq.wurstscript.ast.ExprVarAccessPos;
import de.peeeq.wurstscript.ast.ExprVarArrayAccessPos;
import de.peeeq.wurstscript.ast.FunctionDefinitionPos;
import de.peeeq.wurstscript.ast.NoTypeExprPos;
import de.peeeq.wurstscript.ast.OpAndPos;
import de.peeeq.wurstscript.ast.OpBinaryPos;
import de.peeeq.wurstscript.ast.OpDivIntPos;
import de.peeeq.wurstscript.ast.OpDivRealPos;
import de.peeeq.wurstscript.ast.OpEqualsPos;
import de.peeeq.wurstscript.ast.OpGreaterEqPos;
import de.peeeq.wurstscript.ast.OpGreaterPos;
import de.peeeq.wurstscript.ast.OpLessEqPos;
import de.peeeq.wurstscript.ast.OpLessPos;
import de.peeeq.wurstscript.ast.OpMinusPos;
import de.peeeq.wurstscript.ast.OpModIntPos;
import de.peeeq.wurstscript.ast.OpModRealPos;
import de.peeeq.wurstscript.ast.OpMultPos;
import de.peeeq.wurstscript.ast.OpNotPos;
import de.peeeq.wurstscript.ast.OpOrPos;
import de.peeeq.wurstscript.ast.OpPlusPos;
import de.peeeq.wurstscript.ast.OpUnaryPos;
import de.peeeq.wurstscript.ast.OpUnequalsPos;
import de.peeeq.wurstscript.ast.TypeExprPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeNull;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrExprType extends Attribute<ExprPos, PscriptType> {

	public AttrExprType(Attributes attr) {
		super(attr);
	}

	@Override
	protected PscriptType calculate(ExprPos node) {
		return node.Switch(new ExprPos.Switch<PscriptType, NE>() {

			@Override
			public PscriptType CaseExprIntValPos(ExprIntValPos term) throws NE {
				return PScriptTypeInt.instance();
			}

			@Override
			public PscriptType CaseExprRealValPos(ExprRealValPos term)
					throws NE {
				return PScriptTypeReal.instance();
			}

			@Override
			public PscriptType CaseExprStringValPos(ExprStringValPos term)
					throws NE {
				return PScriptTypeString.instance();
			}

			@Override
			public PscriptType CaseExprBoolValPos(ExprBoolValPos term)
					throws NE {
				return PScriptTypeBool.instance();
			}

			@Override
			public PscriptType CaseExprFuncRefPos(ExprFuncRefPos term)
					throws NE {
				return PScriptTypeCode.instance();
			}

			@Override
			public PscriptType CaseExprVarAccessPos(ExprVarAccessPos term)
					throws NE {
				VarDefPos varDef = attr.varDef.get(term);
				if (varDef == null) {
					return PScriptTypeUnknown.instance();
				}
				return attr.varDefType.get(varDef);
			}

			@Override
			public PscriptType CaseExprVarArrayAccessPos(
					ExprVarArrayAccessPos term) throws NE {
				VarDefPos varDef = attr.varDef.get(term);
				if (varDef == null) {
					return PScriptTypeUnknown.instance();
				}
				
				PscriptType varDefType = attr.varDefType.get(varDef);
				if (varDefType instanceof PScriptTypeArray) {
					return ((PScriptTypeArray) varDefType).getBaseType();
				} else {
					attr.addError(term.source(), "Variable " + varDef.name() + " is no array variable.");
				}
				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType CaseExprThisPos(ExprThisPos term) throws NE {
				// find nearest class definition
				SortPos pos = term;
				while (pos != null) {
					if (pos instanceof ClassDefPos) {
						return new PscriptTypeClass((ClassDefPos) pos);
					}
					pos = pos.parent();
				}
				attr.addError(term.source(), "'this' can only be used inside methods");
				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType CaseExprBinaryPos(final ExprBinaryPos term) throws NE {
				final PscriptType  leftType = attr.exprType.get(term.left());
				final PscriptType  rightType = attr.exprType.get(term.right());
				return term.op().Switch(new OpBinaryPos.Switch<PscriptType, NE>() {

					

					private PscriptType requireEqualTypes(
							PScriptTypeBool requiredType, PScriptTypeBool resultType) {
						if (!leftType.isSubtypeOf(requiredType)) {
							attr.addError(term.left().source().term(), "Operator " + term.op().term() + " requires two operands of " +
									"type " + requiredType + " but left type was " + leftType);
							return PScriptTypeUnknown.instance();
						}
						if (!leftType.isSubtypeOf(requiredType)) {
							attr.addError(term.right().source().term(), "Operator " + term.op().term() + " requires two operands of " +
									"type " + requiredType + " but right type was " + leftType);
							return PScriptTypeUnknown.instance();
						}
						return resultType;
					}
					
					@Override
					public PscriptType CaseOpOrPos(OpOrPos op) throws NE {
						return requireEqualTypes(PScriptTypeBool.instance(), PScriptTypeBool.instance());
					}

					@Override
					public PscriptType CaseOpAndPos(OpAndPos op) throws NE {
						return requireEqualTypes(PScriptTypeBool.instance(), PScriptTypeBool.instance());
					}

					@Override
					public PscriptType CaseOpEqualsPos(OpEqualsPos op)
							throws NE {
						return caseEquality();
						
					}
					
					@Override
					public PscriptType CaseOpUnequalsPos(OpUnequalsPos term)
							throws NE {
						return caseEquality();
					}

					private PscriptType caseEquality() {
						
						if (leftType.equals(rightType)) {
							return PScriptTypeBool.instance();
						}
						
						if (leftType.isSubtypeOf(rightType) || rightType.isSubtypeOf(leftType)) {
							return  PScriptTypeBool.instance();
						}
						
						// FIXME check if the intersection of the basetypes of lefttpye and righttype is
						// not empty. Example:
						// class A with B,C
						// -> B and C should be comparable
						attr.addError(term.source(), "Cannot compare types " + leftType + " with " + rightType);
					
						return PScriptTypeBool.instance();
					}

					

					@Override
					public PscriptType CaseOpLessEqPos(OpLessEqPos term)
							throws NE {
						return caseCompare();
					}

					

					@Override
					public PscriptType CaseOpLessPos(OpLessPos term) throws NE {
						return caseCompare();
					}

					@Override
					public PscriptType CaseOpGreaterEqPos(OpGreaterEqPos term)
							throws NE {
						return caseCompare();
					}

					@Override
					public PscriptType CaseOpGreaterPos(OpGreaterPos term)
							throws NE {
						return caseCompare();
					}
					
					private PscriptType caseCompare() {
						if (!(leftType instanceof PScriptTypeInt
								|| leftType instanceof PScriptTypeReal)) {
							attr.addError(term.left().source(), "Can not compare with value of type " + leftType);
						}
						if (!(rightType instanceof PScriptTypeInt
								|| rightType instanceof PScriptTypeReal)) {
							attr.addError(term.right().source(), "Can not compare with value of type " + rightType);
						}
						return PScriptTypeBool.instance();
					}

					@Override
					public PscriptType CaseOpPlusPos(OpPlusPos op) throws NE {
						if (leftType instanceof PScriptTypeString && rightType instanceof PScriptTypeString) {
							return PScriptTypeString.instance();
						}
						return caseMathOperation();
					}

					private PscriptType caseMathOperation() {
						if (leftType instanceof PScriptTypeInt || rightType instanceof PScriptTypeInt) {
							return PScriptTypeInt.instance();
						}
						if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
							if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
								return PScriptTypeReal.instance();
							}
						}
						attr.addError(term.source(), "Operator " + term.op().term() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}

					@Override
					public PscriptType CaseOpMinusPos(OpMinusPos op)
							throws NE {
						return caseMathOperation();
					}

					@Override
					public PscriptType CaseOpMultPos(OpMultPos term) throws NE {
						return caseMathOperation();
					}

					@Override
					public PscriptType CaseOpDivRealPos(OpDivRealPos op)
							throws NE {
						if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
							if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
								return PScriptTypeReal.instance();
							}
						}
						attr.addError(term.source(), "Operator " + term.op().term() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}

					@Override
					public PscriptType CaseOpModRealPos(OpModRealPos op)
							throws NE {
						if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
							if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
								return PScriptTypeReal.instance();
							}
						}
						attr.addError(term.source(), "Operator " + term.op().term() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}

					@Override
					public PscriptType CaseOpModIntPos(OpModIntPos op)
							throws NE {
						if (leftType instanceof PScriptTypeInt || rightType instanceof PScriptTypeInt) {
							return PScriptTypeInt.instance();
						}
						attr.addError(term.source(), "Operator " + term.op().term() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}

					@Override
					public PscriptType CaseOpDivIntPos(OpDivIntPos op)
							throws NE {
						if (leftType instanceof PScriptTypeInt && rightType instanceof PScriptTypeInt) {
							return PScriptTypeInt.instance();
						}
						attr.addError(term.source(), "Operator " + term.op().term() +" is not defined for " +
								"operands " + leftType + " and " + rightType);
						return PScriptTypeUnknown.instance();
					}
				});
			}

			@Override
			public PscriptType CaseExprUnaryPos(final ExprUnaryPos term) throws NE {
				final PscriptType rightType = get(term.right());
				return term.op().Switch(new OpUnaryPos.Switch<PscriptType, NE>() {

					@Override
					public PscriptType CaseOpNotPos(OpNotPos op) throws NE {
						if (!(rightType instanceof PScriptTypeBool)) {
							attr.addError(term.source(), "Expected Boolean after not but found " + rightType);
						}
						return PScriptTypeBool.instance();
					}

					@Override
					public PscriptType CaseOpMinusPos(OpMinusPos op)
							throws NE {
						if (rightType instanceof PScriptTypeInt) { 
							return PScriptTypeInt.instance();
						}
						if (rightType instanceof PScriptTypeReal) { 
							return PScriptTypeReal.instance();
						}
						attr.addError(term.source(), "Expected Int or Real after Minus but found " + rightType);
						return PScriptTypeReal.instance();
					}
				});
			}

			@Override
			public PscriptType CaseExprMemberVarPos(ExprMemberVarPos term)
					throws NE {
				VarDefPos varDef = attr.varDef.get(term);
				return attr.varDefType.get(varDef );
			}

			@Override
			public PscriptType CaseExprMemberArrayVarPos(
					ExprMemberArrayVarPos term) throws NE {
				VarDefPos varDef = attr.varDef.get(term);
				return attr.varDefType.get(varDef );
			}

			@Override
			public PscriptType CaseExprMemberMethodPos(ExprMemberMethodPos term)
					throws NE {
				FunctionDefinitionPos f = attr.funcDef.get(term);
				if (f.signature().typ() instanceof NoTypeExprPos) {
					return PScriptTypeVoid.instance();
				}
				return attr.typeExprType.get((TypeExprPos) f.signature().typ());
			}

			@Override
			public PscriptType CaseExprFunctionCallPos(ExprFunctionCallPos term)
					throws NE {
				FunctionDefinitionPos f = attr.funcDef.get(term);
				if (f == null) {
					return PScriptTypeUnknown.instance();
				}
				if (f.signature().typ() instanceof NoTypeExprPos) {
					return PScriptTypeVoid.instance();
				}
				// TODO check parameters?
				return attr.typeExprType.get((TypeExprPos) f.signature().typ());
			}

			@Override
			public PscriptType CaseExprNewObjectPos(ExprNewObjectPos term)
					throws NE {
				// TODO new expression type
				throw new Error("not implemented");
			}

			@Override
			public PscriptType CaseExprNullPos(ExprNullPos term) throws NE {
				return PScriptTypeNull.instance();
			}
		});
	}

}
