package de.peeeq.wurstscript.attributes;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprDestroy;
import de.peeeq.wurstscript.ast.ExprEmpty;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprIncomplete;
import de.peeeq.wurstscript.ast.ExprInstanceOf;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarDot;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarDotDot;
import de.peeeq.wurstscript.ast.ExprMemberMethodDotDot;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprMemberVarDot;
import de.peeeq.wurstscript.ast.ExprMemberVarDotDot;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStatementsBlock;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprSuper;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExprTypeId;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.StmtCall;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstTypeBool;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import de.peeeq.wurstscript.types.WurstTypeClosure;
import de.peeeq.wurstscript.types.WurstTypeCode;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeIntLiteral;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeModule;
import de.peeeq.wurstscript.types.WurstTypeNull;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;



/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrExprType {



	public static  WurstType calculate(ExprIntVal term)  {
		return WurstTypeIntLiteral.instance();
	}


	public static  WurstType calculate(ExprRealVal term)
	{
		return WurstTypeReal.instance();
	}


	public static  WurstType calculate(ExprStringVal term)
	{
		return WurstTypeString.instance();
	}


	public static  WurstType calculate(ExprBoolVal term)
	{
		return WurstTypeBool.instance();
	}


	public static  WurstType calculate(ExprFuncRef term)
	{
		return WurstTypeCode.instance();
	}


	public static  WurstType calculate(ExprVarAccess term)
	{
		NameDef varDef = term.attrNameDef();
		
		if (varDef == null) {
			String varName = term.getVarName();
			if (varName.startsWith("gg_rct_")) {
				return getHandleType(term, "rect");
			} else if (varName.startsWith("gg_trg_")) {
				return getHandleType(term, "trigger");
			} else if (varName.startsWith("gg_unit_")) {
				return getHandleType(term, "unit");
			} else if (varName.startsWith("gg_dest_")) {
				return getHandleType(term, "destructable");
			} else if (varName.startsWith("gg_cam_")) {
				return getHandleType(term, "camerasetup");
			} else if (varName.startsWith("gg_snd_")) {
				return getHandleType(term, "sound");
			} else if (varName.startsWith("gg_item_")) {
				return getHandleType(term, "item");
			}
			
			return WurstTypeUnknown.instance();
		}
		if (varDef instanceof VarDef) {
			if (Utils.getParentVarDef(term) == varDef) {
				term.addError("Recursive variable definition is not allowed.");
				return WurstTypeUnknown.instance();
			}
			return varDef.attrTyp().dynamic();
		}
		if (varDef instanceof FunctionDefinition) {
			term.addError("Missing parantheses for function call");
		}
		WurstType typ = varDef.attrTyp();
		return typ;
	}


	private static WurstType getHandleType(AstElement node, String typeName) {
		TypeDef def = node.lookupType(typeName);
		if (def != null) {
			return def.attrTyp().dynamic();
		} else {
			return WurstTypeUnknown.instance();
		}
	}


	public static  WurstType calculate(ExprVarArrayAccess term)  {
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return WurstTypeUnknown.instance();
		}

		WurstType varDefType = varDef.attrTyp().dynamic();
		if (varDefType instanceof WurstTypeArray) {
			WurstType typ = ((WurstTypeArray) varDefType).getBaseType();
			return typ;
		} else {
			term.addError("Variable " + varDef.getName() + " is no array variable.");
		}
		return WurstTypeUnknown.instance();
	}

	
	public static  WurstType calculate(ExprThis term)  {
		return caclulateThistype(term, true, "this");
	}
	
	/**
	 * calculates the type of 'thistype' or 'this'
	 * 
	 *  @param term where the expr 'this' or 'thistype' is used
	 *  @param dynamic true when searching for 'this'
	 *  @param searchedTerm what to display in error messages, null for no error messages
	 */
	public static  WurstType caclulateThistype(AstElement term, boolean dynamic, String searchedTerm)  {
		boolean showErrors = searchedTerm != null;
		if (term.getParent() == null) {
			// not attached to the tree -> generated
			throw new CompileError(term.attrSource(), "Expression 'this' not attached to AST.");
		}

		// check if we are in an extension function
		FunctionImplementation func = term.attrNearestFuncDef();
		if (func instanceof ExtensionFuncDef) {
			ExtensionFuncDef extensionFuncDef = (ExtensionFuncDef) func;
			return extensionFuncDef.getExtendedType().attrTyp().dynamic();
		}
		if (dynamic && !term.attrIsDynamicContext()) {
			if (showErrors) {
				term.addError("Cannot use '"+searchedTerm+"' in static methods.");
			}
			return WurstTypeUnknown.instance();
		}

		// find nearest class-like thing
		NamedScope c = term.attrNearestNamedScope();
		if (c != null) {
			return c.match(new NamedScope.Matcher<WurstType>() {

				@Override
				public WurstType case_ModuleDef(ModuleDef moduleDef) {
					return new WurstTypeModule(moduleDef, false);
				}

				@Override	
				public WurstType case_ClassDef(ClassDef classDef) {
					WurstTypeClass result = (WurstTypeClass) classDef.attrTyp().dynamic();
					return result.replaceTypeVars(classDef.getTypeParameters().attrTypes());
				}

				@Override
				public WurstType case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
					ClassOrModule parent = moduleInstanciation.attrNearestClassOrModule();
					return parent.attrTyp().dynamic();
				}

				@Override
				public WurstType case_WPackage(WPackage wPackage) {
					// 'this' cannot be used on package level
					return WurstTypeUnknown.instance();
				}

				@Override
				public WurstType case_InterfaceDef(InterfaceDef interfaceDef) {
					WurstTypeInterface result = (WurstTypeInterface) interfaceDef.attrTyp().dynamic();
					return result.replaceTypeVars(interfaceDef.getTypeParameters().attrTypes());
				}

				@Override
				public WurstType case_EnumDef(EnumDef enumDef) {
					// 'this' cannot be used in enums
					return WurstTypeUnknown.instance();
				}

			});
		} else {
			if (showErrors) {
				term.addError("The keyword '"+searchedTerm+"' can only be used inside methods.");
			}
			return WurstTypeUnknown.instance();
		}
	}


	public static  WurstType calculate(final ExprBinary term)  {
		final WurstType  leftType = term.getLeft().attrTyp();
		final WurstType  rightType = term.getRight().attrTyp();
		switch (term.getOp()) {
		case AND:
		case OR:
			return requireEqualTypes(term, WurstTypeBool.instance(), WurstTypeBool.instance());
		case EQ:
		case NOTEQ: {
			if (leftType.equalsType(rightType, term)) {
				return WurstTypeBool.instance();
			}

			if (leftType.isSubtypeOf(rightType, term) || rightType.isSubtypeOf(leftType, term)) {
				return  WurstTypeBool.instance();
			}

			if (Utils.isJassCode(term)) {
				if (leftType.isSubtypeOf(WurstTypeReal.instance(), term) || leftType.isSubtypeOf(WurstTypeInt.instance(), term)) {
					if (rightType.isSubtypeOf(WurstTypeReal.instance(), term) || rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
						// in jass code it is allowed to compare reals and ints
						return  WurstTypeBool.instance();									
					}
				}
				
				if (leftType.isSubtypeOf(WurstTypeNull.instance(), term) && rightType.isSubtypeOf(WurstTypeInt.instance(), term)
					|| leftType.isSubtypeOf(WurstTypeInt.instance(), term) && rightType.isSubtypeOf(WurstTypeNull.instance(), term)) {
					// in jass code it is allowed to compare an integer with 'null'. wat?
					return  WurstTypeBool.instance();		
				}
			}

			// TODO check if the intersection of the basetypes of lefttpye and righttype is
			// not empty. Example:
			// class A implements B,C
			// -> B and C should be comparable
			term.addError("Cannot compare types " + leftType + " with " + rightType);

			return WurstTypeBool.instance();
		}

		case GREATER:
		case GREATER_EQ:
		case LESS:
		case LESS_EQ: {
			if (!(leftType.isSubtypeOf(WurstTypeInt.instance(), term)
					|| leftType.isSubtypeOf(WurstTypeReal.instance(), term))) {
				term.getLeft().addError("Can not compare with value of type " + leftType);
			}
			if (!(rightType.isSubtypeOf(WurstTypeInt.instance(), term)
					|| rightType.isSubtypeOf(WurstTypeReal.instance(), term))) {
				term.getRight().addError("Can not compare with value of type " + rightType);
			}
			return WurstTypeBool.instance();
		}
		case PLUS:
			if (leftType instanceof WurstTypeString && rightType instanceof WurstTypeString) {
				return WurstTypeString.instance();
			} if (bothTypesRealOrInt(term)) {
				return caseMathOperation(term);
			} else {
				return handleOperatorOverloading(term);
			}
		case MINUS:
		case MULT:
			if (bothTypesRealOrInt(term)) {
				return caseMathOperation(term);
			} else {
				return handleOperatorOverloading(term);
			}
		case DIV_REAL:
			if (Utils.isJassCode(term)) {
				return caseMathOperation(term);
			} else if (bothTypesRealOrInt(term)) {
				return WurstTypeReal.instance();			
			} else {
				return handleOperatorOverloading(term);
			}
		case MOD_REAL:
			if (leftType.isSubtypeOf(WurstTypeReal.instance(), term) || leftType.isSubtypeOf(WurstTypeInt.instance(), term)) {
				if (rightType.isSubtypeOf(WurstTypeReal.instance(), term) || rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
					return WurstTypeReal.instance();
				}
			}
			term.addError("Operator " + term.getOp() +" is not defined for " +
			"operands " + leftType + " and " + rightType);
			return WurstTypeUnknown.instance();
		case MOD_INT:
		case DIV_INT:
			if (leftType.isSubtypeOf(WurstTypeInt.instance(), term) && rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
				return leftType;
			}
			term.addError("Operator " + term.getOp() +" is not defined for " +
			"operands " + leftType + " and " + rightType);
			return WurstTypeUnknown.instance();
		
		case NOT:
		case UNARY_MINUS:
		}
		throw new Error("unhandled op " + term.getOp());
	}


	private static WurstType caseMathOperation(ExprBinary term) {
		WurstType leftType = term.getLeft().attrTyp();
		WurstType  rightType = term.getRight().attrTyp();
		if (leftType.isSubtypeOf(WurstTypeInt.instance(), term) && rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
			return WurstTypeInt.instance();
		}
		if (leftType.isSubtypeOf(WurstTypeReal.instance(), term) || leftType.isSubtypeOf(WurstTypeInt.instance(), term)) {
			if (rightType.isSubtypeOf(WurstTypeReal.instance(), term) || rightType.isSubtypeOf(WurstTypeInt.instance(), term)) {
				return WurstTypeReal.instance();
			}
		}
		term.addError("Operator " + term.getOp() +" is not defined for " +
		"operands " + leftType + " and " + rightType);
		return WurstTypeUnknown.instance();
	}
	
	private static WurstType handleOperatorOverloading(ExprBinary term) {
		WurstType leftType = term.getLeft().attrTyp();
		WurstType  rightType = term.getRight().attrTyp();
		FunctionDefinition def = term.attrFuncDef();
		if (def == null) {
			term.addError("No operator overloading function for operator " + term.getOp() +
			" was found for operands " + leftType + " and " + rightType + ". The overloading function has to be named: " + term.getOp().getOverloadingFuncName());
			return WurstTypeUnknown.instance();
		}
		return def.getReturnTyp().attrTyp().dynamic();
	}
	
	private static WurstType requireEqualTypes(ExprBinary term,
			WurstTypeBool requiredType, WurstTypeBool resultType) {
		final WurstType  leftType = term.getLeft().attrTyp();
		final WurstType  rightType = term.getRight().attrTyp();
		if (!leftType.isSubtypeOf(requiredType, term)) {
			term.getLeft().addError("Operator " + term.getOp() + " requires two operands of " +
			"type " + requiredType + " but left type was " + leftType);
			return WurstTypeUnknown.instance();
		}
		if (!rightType.isSubtypeOf(requiredType, term)) {
			term.getRight().addError("Operator " + term.getOp() + " requires two operands of " +
			"type " + requiredType + " but right type was " + leftType);
			return WurstTypeUnknown.instance();
		}
		return resultType;
	}
	
	private static boolean bothTypesRealOrInt(ExprBinary term) {
		WurstType  leftType = term.getLeft().attrTyp();
		WurstType  rightType = term.getRight().attrTyp();
		return ((leftType.isSubtypeOf(WurstTypeInt.instance(), term) || leftType .isSubtypeOf(WurstTypeReal.instance(), term)) 
				&& (rightType.isSubtypeOf(WurstTypeInt.instance(), term) || rightType.isSubtypeOf(WurstTypeReal.instance(), term)));
	}


	public static  WurstType calculate(final ExprUnary term)  {
		final WurstType rightType = term.getRight().attrTyp();
		if (term.getOpU() == WurstOperator.NOT) {
			if (!(rightType instanceof WurstTypeBool)) {
				term.addError("Expected Boolean after not but found " + rightType);
			}
			return WurstTypeBool.instance();
		} else if (term.getOpU() == WurstOperator.UNARY_MINUS) {
			if (rightType.isSubtypeOf(WurstTypeInt.instance(), term) || rightType.isSubtypeOf(WurstTypeReal.instance(), term)) { 
				return rightType;
			}
			term.addError("Expected Int or Real after Minus but found " + rightType);
			return WurstTypeReal.instance();
		}
		throw new Error("unhandled case: " + term.getOpU());
	}


	public static  WurstType calculate(ExprMemberVarDot term)
	{
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return WurstTypeUnknown.instance();
		}
		if (varDef instanceof FunctionDefinition) {
			term.addError("Missing parantheses for function call");
		}
		if (varDef.attrIsStatic() && !term.getLeft().attrTyp().isStaticRef()) {
			term.addError("Cannot access static variable " + term.getVarName() + " via a dynamic reference.");
		}
		return varDef.attrTyp().setTypeArgs(term.getLeft().attrTyp().getTypeArgBinding());
	}


	public static  WurstType calculate(ExprMemberArrayVarDot term)  {
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return WurstTypeUnknown.instance();
		}
		WurstType typ = varDef.attrTyp().dynamic();
		if (typ instanceof WurstTypeArray) {
			WurstTypeArray ar = (WurstTypeArray) typ;
			return ar.getBaseType();			
		}
		term.addError("Variable " + term.getVarName() + " is not an array.");
		return typ;
	}

	
	public static  WurstType calculate(StmtCall term) {
		return term.attrFunctionSignature().getReturnType();
	}


	public static  WurstType calculate(ExprNull term)  {
		return WurstTypeNull.instance();
	}

	public static  WurstType calculate(ExprCast term)  {
		WurstType targetTyp = term.getTyp().attrTyp().dynamic();
		WurstType exprTyp = term.getExpr().attrTyp();
		if (targetTyp.isSubtypeOf(WurstTypeInt.instance(), term) && isCastableToInt(exprTyp)) {
			// cast from classtype to int: OK
		} else if (isCastableToInt(targetTyp) && exprTyp.isSubtypeOf(WurstTypeInt.instance(), term)) {
			// cast from int to classtype: OK
		} else {
			checkCastOrInstanceOf(term, exprTyp, targetTyp, "cast expression");
		}
		return targetTyp;
	}


	public static  WurstType calculate(ExprIncomplete exprIncomplete) {
		return WurstTypeUnknown.instance();
	}


	protected static boolean isCastableToInt(WurstType typ) {
		return typ.isCastableToInt();
	}


	public static WurstType calculate(ExprInstanceOf e) {
		WurstType exprType = e.getExpr().attrTyp();
		WurstType targetType = e.getTyp().attrTyp();
		checkCastOrInstanceOf(e, exprType, targetType, "instanceof expression");
		return WurstTypeBool.instance();
	}


	private static void checkCastOrInstanceOf(AstElement e, WurstType exprType, WurstType targetType, String msgPre) {
		if (!exprType.canBeUsedInInstanceOf()) {
			e.addError(msgPre + " not defined for expression type " + exprType);
		}
		if (!targetType.canBeUsedInInstanceOf()) {
			e.addError(msgPre + " not defined for target type " + targetType);
		}
		if (exprType.isSubtypeOf(targetType, e)) {
			e.addError("This "+ msgPre + " is useless");
		} else if (!exprType.isSupertypeOf(targetType, e)) {
			e.addError(msgPre + " is not allowed because types " + exprType + " and " + targetType + " are not directly related.\n" +
					"Consider adding a cast to a common superType first.");
		}
	}


	public static WurstType calculate(ExprSuper e) {
		ClassDef cd = e.attrNearestClassDef();
		if (cd == null) {
			e.addError("'super' can only be used inside classes.");
		} else if (cd.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
			WurstTypeClass superType = (WurstTypeClass) cd.getExtendedClass().attrTyp();
			assert superType.isStaticRef();
			return superType;
		} else {
			e.addError("No super class found.");
		}
		return WurstTypeUnknown.instance();
	}


	public static WurstType calculate(ExprTypeId e) {
		WurstType exprTyp = e.getLeft().attrTyp();
		if (exprTyp instanceof WurstTypeClassOrInterface) {
			WurstTypeClassOrInterface t = (WurstTypeClassOrInterface) exprTyp;
			if (t.isStaticRef()) {
				// static reference to a type --> only concrete classes allowed
				if (t instanceof WurstTypeClass) {
					WurstTypeClass wtc = (WurstTypeClass) exprTyp;
					if (wtc.getClassDef().attrIsAbstract()) {
						e.addError("abstract classes do not have a typeId");
					}		
				} else {
					e.addError(t + " does not have a typeId. Only classes have one.");
				}				
			}
		} else {
			e.addError(exprTyp + " does not have a typeId, because it is not an object type.");
		}
		return WurstTypeInt.instance();
	}


	public static WurstType normalizedType(Expr e) {
		return e.attrTypRaw().normalize();
	}


	public static WurstType calculate(ExprClosure e) {
		WurstType returnType = e.getImplementation().attrTyp();
		List<WurstType> paramTypes = Lists.newArrayList();
		for (WParameter p : e.getParameters()) {
			paramTypes.add(p.attrTyp());
		}
		return new WurstTypeClosure(paramTypes, returnType);
	}


	public static WurstType calculate(ExprStatementsBlock e) {
		StmtReturn r = (StmtReturn) e.getReturnStmt();
		if (r != null) {
			if (r.getReturnedObj() instanceof Expr) {
				Expr re = (Expr) r.getReturnedObj();
				return re.attrTyp();
			}
		}
		return WurstTypeVoid.instance();
	}


	public static WurstType calculate(ExprDestroy e) {
		return WurstTypeVoid.instance();
	}


	public static WurstType calculate(ExprMemberArrayVarDotDot e) {
		e.addError("The dot-dot-operator can only be used with methods.");
		return WurstTypeUnknown.instance();
	}


	public static WurstType calculate(ExprMemberVarDotDot e) {
		e.addError("The dot-dot-operator can only be used with methods.");
		return WurstTypeUnknown.instance();
	}
	
	public static WurstType calculate(ExprMemberMethodDotDot e) {
		e.attrFunctionSignature();
		return e.getLeft().attrTyp();
	}


	public static WurstType calculate(ExprEmpty e) {
		return new WurstTypeUnknown("empty");
	}

}
