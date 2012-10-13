package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprIncomplete;
import de.peeeq.wurstscript.ast.ExprInstanceOf;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprSuper;
import de.peeeq.wurstscript.ast.ExprThis;
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
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstNativeType;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstTypeBool;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeCode;
import de.peeeq.wurstscript.types.WurstTypeEnum;
import de.peeeq.wurstscript.types.WurstTypeHandle;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeJassInt;
import de.peeeq.wurstscript.types.WurstTypeModule;
import de.peeeq.wurstscript.types.WurstTypeModuleInstanciation;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.types.WurstTypeTuple;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;



/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrExprType {



	public static  WurstType calculate(ExprIntVal term)  {
		if (Utils.isJassCode(term)) {
			return WurstTypeJassInt.instance();
		} else {
			if (term.attrExpectedTyp() instanceof WurstTypeReal) {
				return WurstTypeReal.instance();
			}
			return WurstTypeInt.instance();
		}
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
			}
			
			return WurstTypeUnknown.instance();
		}
		if (varDef instanceof VarDef) {
			if (Utils.getParentOfType(VarDef.class, term) == varDef) {
				term.addError("Recursive variable definition is not allowed.");
				return WurstTypeUnknown.instance();
			}
			return varDef.attrTyp().dynamic();
		}
		if (varDef instanceof FunctionDefinition) {
			term.addError("Missing parantheses for function call");
		}
		return varDef.attrTyp();
	}


	private static WurstType getHandleType(AstElement node, String typeName) {
		Set<WScope> ignoredScopes = Collections.emptySet();
		//		Collection<NameDef> defs = term.attrCompilationUnit().attrDefinedNames().get(typeName);
		List<NameDef> defs = NameResolution.searchTypedName(NameDef.class, typeName, node, false, ignoredScopes);
		if (defs.size() > 0) {
			return Utils.getFirst(defs).attrTyp();
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
			return ((WurstTypeArray) varDefType).getBaseType();
		} else {
			term.addError("Variable " + varDef.getName() + " is no array variable.");
		}
		return WurstTypeUnknown.instance();
	}


	public static  WurstType calculate(ExprThis term)  {
		if (term.getParent() == null) {
			// not attached to the tree -> generated
			throw new CompileError(term.getSource(), "Expression 'this' not attached to AST.");
		}

		// check if we are in an extension function
		FunctionImplementation func = term.attrNearestFuncDef();
		if (func instanceof ExtensionFuncDef) {
			ExtensionFuncDef extensionFuncDef = (ExtensionFuncDef) func;
			return extensionFuncDef.getExtendedType().attrTyp().dynamic();
		}
		if (!term.attrIsDynamicContext()) {
			term.addError("Cannot use 'this' in static methods.");
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
					
					return classDef.attrTyp().dynamic();
				}

				@Override
				public WurstType case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
					return new WurstTypeModuleInstanciation(moduleInstanciation, false);
				}

				@Override
				public WurstType case_WPackage(WPackage wPackage) {
					// 'this' cannot be used on package level
					return WurstTypeUnknown.instance();
				}

				@Override
				public WurstType case_InterfaceDef(InterfaceDef interfaceDef) {
					return  interfaceDef.attrTyp().dynamic();
				}

				@Override
				public WurstType case_EnumDef(EnumDef enumDef) {
					// 'this' cannot be used in enums
					return WurstTypeUnknown.instance();
				}

			});
		} else {
			term.addError("The keyword 'this' can only be used inside methods.");
			return WurstTypeUnknown.instance();
		}
	}


	public static  WurstType calculate(final ExprBinary term)  {
		final WurstType  leftType = term.getLeft().attrTyp();
		final WurstType  rightType = term.getRight().attrTyp();
		return term.getOp().match(new OpBinary.Matcher<WurstType>() {



			private WurstType requireEqualTypes(
					WurstTypeBool requiredType, WurstTypeBool resultType) {
				if (!leftType.isSubtypeOf(requiredType, term)) {
					term.getLeft().addError("Operator " + term.getOp() + " requires two operands of " +
					"type " + requiredType + " but left type was " + leftType);
					return WurstTypeUnknown.instance();
				}
				if (!leftType.isSubtypeOf(requiredType, term)) {
					term.getRight().addError("Operator " + term.getOp() + " requires two operands of " +
					"type " + requiredType + " but right type was " + leftType);
					return WurstTypeUnknown.instance();
				}
				return resultType;
			}

			@Override
			public WurstType case_OpOr(OpOr op)  {
				return requireEqualTypes(WurstTypeBool.instance(), WurstTypeBool.instance());
			}


			@Override
			public WurstType case_OpAnd(OpAnd op)  {
				return requireEqualTypes(WurstTypeBool.instance(), WurstTypeBool.instance());
			}


			@Override
			public WurstType case_OpEquals(OpEquals op)
			{
				return caseEquality();

			}


			@Override
			public WurstType case_OpUnequals(OpUnequals term)
			{
				return caseEquality();
			}

			private WurstType caseEquality() {

				if (leftType.equalsType(rightType, term)) {
					return WurstTypeBool.instance();
				}

				if (leftType.isSubtypeOf(rightType, term) || rightType.isSubtypeOf(leftType, term)) {
					return  WurstTypeBool.instance();
				}

				if (Utils.isJassCode(term)) {
					if (leftType instanceof WurstTypeReal || leftType instanceof WurstTypeInt) {
						if (rightType instanceof WurstTypeReal || rightType instanceof WurstTypeInt) {
							return  WurstTypeBool.instance();									
						}
					}
				}

				// TODO check if the intersection of the basetypes of lefttpye and righttype is
				// not empty. Example:
				// class A implements B,C
				// -> B and C should be comparable
				term.addError("Cannot compare types " + leftType + " with " + rightType);

				return WurstTypeBool.instance();
			}




			@Override
			public WurstType case_OpLessEq(OpLessEq term)
			{
				return caseCompare();
			}




			@Override
			public WurstType case_OpLess(OpLess term)  {
				return caseCompare();
			}

			@Override
			public WurstType case_OpGreaterEq(OpGreaterEq term)
			{
				return caseCompare();
			}

			@Override
			public WurstType case_OpGreater(OpGreater term)
			{
				return caseCompare();
			}

			private WurstType caseCompare() {
				if (!(leftType instanceof WurstTypeInt
						|| leftType instanceof WurstTypeReal)) {
					term.getLeft().addError("Can not compare with value of type " + leftType);
				}
				if (!(rightType instanceof WurstTypeInt
						|| rightType instanceof WurstTypeReal)) {
					term.getRight().addError("Can not compare with value of type " + rightType);
				}
				return WurstTypeBool.instance();
			}
			
			public boolean bothTypesRealOrInt() {
				if ((leftType instanceof WurstTypeInt || leftType instanceof WurstTypeReal) && (rightType instanceof WurstTypeInt || rightType instanceof WurstTypeReal)) {
					return true;
				}
				return false;
				
			}

			@Override
			public WurstType case_OpPlus(OpPlus op)  {
				if (leftType instanceof WurstTypeString && rightType instanceof WurstTypeString) {
					return WurstTypeString.instance();
				} if (bothTypesRealOrInt()) {
					return caseMathOperation();
				} else {
					return handleOperatorOverloading(term);
				}
		
				
			}

			private WurstType handleOperatorOverloading(final ExprBinary term) {
				FunctionDefinition def = term.attrFuncDef();
				if (def == null) {
					term.addError("No operator overloading function for operator " + term.getOp() +
					" was found for operands " + leftType + " and " + rightType + ". The overloading function has to be named: " + convertOpToOpol(term.getOp()));
					return WurstTypeUnknown.instance();
				}
				return def.getReturnTyp().attrTyp();
			}

			private WurstType caseMathOperation() {
				if (leftType instanceof WurstTypeInt && rightType instanceof WurstTypeInt) {
					return leftType;
				}
				if (leftType instanceof WurstTypeReal || leftType instanceof WurstTypeInt) {
					if (rightType instanceof WurstTypeReal || rightType instanceof WurstTypeInt) {
						return WurstTypeReal.instance();
					}
				}
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return WurstTypeUnknown.instance();
			}

			@Override
			public WurstType case_OpMinus(OpMinus op)
			{
				if (bothTypesRealOrInt()) {
					return caseMathOperation();
				} else {
					return handleOperatorOverloading(term);
				}
			}

			@Override
			public WurstType case_OpMult(OpMult op)  {
				if (bothTypesRealOrInt()) {
					return caseMathOperation();
				} else {
					return handleOperatorOverloading(term);
				}
			}

			@Override
			public WurstType case_OpDivReal(OpDivReal op)
			{
				if (Utils.isJassCode(op)) {
					return caseMathOperation();
				} else if (bothTypesRealOrInt()) {
					return WurstTypeReal.instance();			
				} else {
					return handleOperatorOverloading(term);
				}
//				attr.addError(term.getSource(), "Operator " + term.getOp() +" is not defined for " +
//						"operands " + leftType + " and " + rightType);
//				return PScriptTypeUnknown.instance();
			}

			@Override
			public WurstType case_OpModReal(OpModReal op)
			{
				if (leftType instanceof WurstTypeReal || leftType instanceof WurstTypeInt) {
					if (rightType instanceof WurstTypeReal || rightType instanceof WurstTypeInt) {
						return WurstTypeReal.instance();
					}
				}
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return WurstTypeUnknown.instance();
			}

			@Override
			public WurstType case_OpModInt(OpModInt op)
			{
				if (leftType instanceof WurstTypeInt || rightType instanceof WurstTypeInt) {
					return leftType;
				}
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return WurstTypeUnknown.instance();
			}

			@Override
			public WurstType case_OpDivInt(OpDivInt op)
			{
				if (leftType instanceof WurstTypeInt && rightType instanceof WurstTypeInt) {
					return leftType;
				}
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return WurstTypeUnknown.instance();
			}
		});
	}


	protected static String convertOpToOpol(OpBinary op) {
		String result = "";
		if ( op instanceof OpPlus) {
			result = AttrFuncDef.overloadingPlus;
		}else if ( op instanceof OpMinus) {
			result = AttrFuncDef.overloadingMinus;
		}else if ( op instanceof OpDivReal) {
			result = AttrFuncDef.overloadingDiv;
		}else if ( op instanceof OpMult) {
			result = AttrFuncDef.overloadingMult;
		}
		return result;
	}


	public static  WurstType calculate(final ExprUnary term)  {
		final WurstType rightType = term.getRight().attrTyp();
		return term.getOpU().match(new OpUnary.Matcher<WurstType>() {


			public WurstType case_OpNot(OpNot op)  {
				if (!(rightType instanceof WurstTypeBool)) {
					term.addError("Expected Boolean after not but found " + rightType);
				}
				return WurstTypeBool.instance();
			}


			public WurstType case_OpMinus(OpMinus op)
			{
				if (rightType instanceof WurstTypeInt || rightType instanceof WurstTypeReal) { 
					return rightType;
				}
				term.addError("Expected Int or Real after Minus but found " + rightType);
				return WurstTypeReal.instance();
			}
		});
	}


	public static  WurstType calculate(ExprMemberVar term)
	{
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return WurstTypeUnknown.instance();
		}
		if (varDef instanceof FunctionDefinition) {
			term.addError("Missing parantheses for function call");
		}
		if (varDef.attrIsStatic() && term.getLeft().attrTyp() instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope ns = (WurstTypeNamedScope) term.getLeft().attrTyp();
			if (!ns.isStaticRef()) {
				term.addError("Cannot access static variable " + term.getVarName() + " via a dynamic reference.");
			}
		}
		return varDef.attrTyp().setTypeArgs(term.getLeft().attrTyp().getTypeArgBinding());
	}


	public static  WurstType calculate(ExprMemberArrayVar term)  {
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


	public static  WurstType calculate(ExprMemberMethod term)
	{
		FunctionDefinition f = term.attrFuncDef();
		if (f == null) {
			return WurstTypeUnknown.instance();
		}
		if (f.getReturnTyp() instanceof NoTypeExpr) {
			return WurstTypeVoid.instance();
		}
		WurstType typ = f.getReturnTyp().attrTyp().dynamic();
		if (term.getSource().getFile().endsWith("Test.wurst")) {
			System.out.println("typ = " + typ);
		}
		if (typ instanceof WurstTypeModule) {
			// example:
			// module A 
			//    function foo() returns thistype
			// class C
			//    use A
			// ...
			// C c = new C()
			// c.foo() // this should return type c  
			WurstType leftType = term.getLeft().attrTyp();
			if (leftType instanceof WurstTypeClass ||
					leftType instanceof WurstTypeModule) {
				return leftType.dynamic();
			}
		} else if (typ instanceof WurstTypeTypeParam) {
			WurstTypeTypeParam typParam = (WurstTypeTypeParam) typ;
			// typ referes to a type parameter so we have to get the binding
			
			// try function type args
			if (term.attrTypeParameterBindings().containsKey(typParam.getDef())) {
				return term.attrTypeParameterBindings().get(typParam.getDef());
			}
			// try left hand side type args	
			if (term.getLeft().attrTyp() instanceof WurstTypeNamedScope) {
				WurstTypeNamedScope ns = (WurstTypeNamedScope) term.getLeft().attrTyp();
				return ns.getTypeParameterBinding(typParam.getDef()).dynamic();
			}
		}
		return typ.setTypeArgs(term.getLeft().attrTyp().getTypeArgBinding()).dynamic();
	}


	public static  WurstType calculate(ExprFunctionCall term)
	{
		if (term.attrFuncDef() == null) {
			return WurstTypeUnknown.instance();
		}

		FunctionDefinition f = term.attrFuncDef();
		if (f == null) {
			return WurstTypeUnknown.instance();
		}
		if (f.getReturnTyp() instanceof NoTypeExpr) {
			if (f instanceof TupleDef) {
				return new WurstTypeTuple((TupleDef) f);
			}
			return WurstTypeVoid.instance();
		}
		WurstType typ = f.getReturnTyp().attrTyp().dynamic();
		if (typ instanceof WurstTypeModule) {
			ClassOrModule classOrModule = term.attrNearestClassOrModule();
			if (classOrModule != null) {
				return TypesHelper.typeOf(classOrModule, false);
			}
		} else if (typ instanceof WurstTypeTypeParam) {
			WurstTypeTypeParam typParam = (WurstTypeTypeParam) typ;
			// typ referes to a type parameter so we have to get the binding
			
			// try function type args
			if (term.attrTypeParameterBindings().containsKey(typParam.getDef())) {
				return term.attrTypeParameterBindings().get(typParam.getDef());
			}
		}
		return typ;
	}


	public static  WurstType calculate(ExprNewObject term)
	{

		TypeDef typeDef = term.attrTypeDef();
		if (typeDef instanceof ClassDef) {
			ClassDef c = (ClassDef) typeDef;
			final Map<TypeParamDef, WurstType> bindings = term.attrTypeParameterBindings();
			List<WurstType> types = Lists.newArrayList();
			for (TypeParamDef t: c.getTypeParameters()) {
				WurstType r = bindings.get(t);
				if (r != null) {
					types.add(r);
				} else {
					types.add(WurstTypeUnknown.instance());
				}
			}
			return new WurstTypeClass(c, types, false);
		} else {
			term.addError("Can only create instances of classes.");
			return WurstTypeUnknown.instance();
		}
	}


	public static  WurstType calculate(ExprNull term)  {
		// null is a little bit tricky
		// it will have the type which you expect it to have, ...
		WurstType t = term.attrExpectedTyp();
		if (t instanceof WurstTypeUnknown) {
			term.addError("Could not determine type of null expression.");
		}
		// ... but of course not all types support null
		if (!   (  t instanceof WurstTypeNamedScope
				|| t instanceof WurstTypeHandle
				|| t instanceof WurstTypeBoundTypeParam
				|| t instanceof WurstTypeTypeParam
				|| t instanceof WurstNativeType
				|| t instanceof WurstTypeString
				|| t instanceof WurstTypeCode)) {
			term.addError("Null is not a valid value for " + t);
		}
		return t;
	}


	public static  WurstType calculate(ExprCast term)  {
		WurstType targetTyp = term.getTyp().attrTyp().dynamic();
		WurstType exprTyp = term.getExpr().attrTyp();
		if (targetTyp instanceof WurstTypeInt && isCastableToInt(exprTyp)) {
			// cast from classtype to int: OK
		} else if (isCastableToInt(targetTyp) && exprTyp instanceof WurstTypeInt) {
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
		return typ instanceof WurstTypeClass 
				|| typ instanceof WurstTypeModule
				|| typ instanceof WurstTypeInterface
				|| typ instanceof WurstTypeTypeParam
				|| typ instanceof WurstTypeBoundTypeParam
				|| typ instanceof WurstTypeEnum;
	}


	public static WurstType calculate(ExprInstanceOf e) {
		WurstType exprType = e.getExpr().attrTyp();
		WurstType targetType = e.getTyp().attrTyp();
		checkCastOrInstanceOf(e, exprType, targetType, "instanceof expression");
		return WurstTypeBool.instance();
	}


	private static void checkCastOrInstanceOf(AstElement e, WurstType exprType, WurstType targetType, String msgPre) {
		if (!(exprType instanceof WurstTypeClass
				|| exprType instanceof WurstTypeInterface)) {
			e.addError(msgPre + " not defined for expression type " + exprType);
		}
		if (!(targetType instanceof WurstTypeClass
				|| targetType instanceof WurstTypeInterface)) {
			e.addError(msgPre + "instanceof expression not defined for target type " + targetType);
		}
		if (exprType.isSubtypeOf(targetType, e)) {
			e.addError("This "+ msgPre + " is always true");
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

}
