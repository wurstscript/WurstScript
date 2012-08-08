package de.peeeq.wurstscript.attributes;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
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
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PScriptTypeHandle;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeJassInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeBoundTypeParam;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeInterface;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.types.PscriptTypeModuleInstanciation;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.types.PscriptTypeTuple;
import de.peeeq.wurstscript.types.PscriptTypeTypeParam;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;



/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrExprType {



	public static  PscriptType calculate(ExprIntVal term)  {
		if (Utils.isJassCode(term)) {
			return PScriptTypeJassInt.instance();
		} else {
			if (term.attrExpectedTyp() instanceof PScriptTypeReal) {
				return PScriptTypeReal.instance();
			}
			return PScriptTypeInt.instance();
		}
	}


	public static  PscriptType calculate(ExprRealVal term)
	{
		return PScriptTypeReal.instance();
	}


	public static  PscriptType calculate(ExprStringVal term)
	{
		return PScriptTypeString.instance();
	}


	public static  PscriptType calculate(ExprBoolVal term)
	{
		return PScriptTypeBool.instance();
	}


	public static  PscriptType calculate(ExprFuncRef term)
	{
		return PScriptTypeCode.instance();
	}


	public static  PscriptType calculate(ExprVarAccess term)
	{
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return PScriptTypeUnknown.instance();
		}
		if (varDef instanceof VarDef) {
			return varDef.attrTyp().dynamic();
		}
		if (varDef instanceof FunctionDefinition) {
			term.addError("Missing parantheses for function call");
		}
		return varDef.attrTyp();
	}


	public static  PscriptType calculate(ExprVarArrayAccess term)  {
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return PScriptTypeUnknown.instance();
		}

		PscriptType varDefType = varDef.attrTyp().dynamic();
		if (varDefType instanceof PScriptTypeArray) {
			return ((PScriptTypeArray) varDefType).getBaseType();
		} else {
			term.addError("Variable " + varDef.getName() + " is no array variable.");
		}
		return PScriptTypeUnknown.instance();
	}


	public static  PscriptType calculate(ExprThis term)  {
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
			return PScriptTypeUnknown.instance();
		}

		// find nearest class-like thing
		NamedScope c = term.attrNearestNamedScope();
		if (c != null) {
			return c.match(new NamedScope.Matcher<PscriptType>() {

				@Override
				public PscriptType case_ModuleDef(ModuleDef moduleDef) {
					return new PscriptTypeModule(moduleDef, false);
				}

				@Override	
				public PscriptType case_ClassDef(ClassDef classDef) {
					
					return classDef.attrTyp().dynamic();
				}

				@Override
				public PscriptType case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
					return new PscriptTypeModuleInstanciation(moduleInstanciation, false);
				}

				@Override
				public PscriptType case_WPackage(WPackage wPackage) {
					// 'this' cannot be used on package level
					return PScriptTypeUnknown.instance();
				}

				@Override
				public PscriptType case_InterfaceDef(InterfaceDef interfaceDef) {
					return  interfaceDef.attrTyp().dynamic();
				}

			});
		} else {
			term.addError("The keyword 'this' can only be used inside methods.");
			return PScriptTypeUnknown.instance();
		}
	}


	public static  PscriptType calculate(final ExprBinary term)  {
		final PscriptType  leftType = term.getLeft().attrTyp();
		final PscriptType  rightType = term.getRight().attrTyp();
		return term.getOp().match(new OpBinary.Matcher<PscriptType>() {



			private PscriptType requireEqualTypes(
					PScriptTypeBool requiredType, PScriptTypeBool resultType) {
				if (!leftType.isSubtypeOf(requiredType, term)) {
					term.getLeft().addError("Operator " + term.getOp() + " requires two operands of " +
					"type " + requiredType + " but left type was " + leftType);
					return PScriptTypeUnknown.instance();
				}
				if (!leftType.isSubtypeOf(requiredType, term)) {
					term.getRight().addError("Operator " + term.getOp() + " requires two operands of " +
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

				if (leftType.equalsType(rightType, term)) {
					return PScriptTypeBool.instance();
				}

				if (leftType.isSubtypeOf(rightType, term) || rightType.isSubtypeOf(leftType, term)) {
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
				term.addError("Cannot compare types " + leftType + " with " + rightType);

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
					term.getLeft().addError("Can not compare with value of type " + leftType);
				}
				if (!(rightType instanceof PScriptTypeInt
						|| rightType instanceof PScriptTypeReal)) {
					term.getRight().addError("Can not compare with value of type " + rightType);
				}
				return PScriptTypeBool.instance();
			}
			
			public boolean bothTypesRealOrInt() {
				if ((leftType instanceof PScriptTypeInt || leftType instanceof PScriptTypeReal) && (rightType instanceof PScriptTypeInt || rightType instanceof PScriptTypeReal)) {
					return true;
				}
				return false;
				
			}

			@Override
			public PscriptType case_OpPlus(OpPlus op)  {
				if (leftType instanceof PScriptTypeString && rightType instanceof PScriptTypeString) {
					return PScriptTypeString.instance();
				} if (bothTypesRealOrInt()) {
					return caseMathOperation();
				} else {
					return handleOperatorOverloading(term);
				}
		
				
			}

			private PscriptType handleOperatorOverloading(final ExprBinary term) {
				FunctionDefinition def = term.attrFuncDef();
				if (def == null) {
					term.addError("No operator overloading function for operator " + term.getOp() +
					" was found for operands " + leftType + " and " + rightType + ". The overloading function has to be named: " + convertOpToOpol(term.getOp()));
					return PScriptTypeUnknown.instance();
				}
				return def.getReturnTyp().attrTyp();
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
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType case_OpMinus(OpMinus op)
			{
				if (bothTypesRealOrInt()) {
					return caseMathOperation();
				} else {
					return handleOperatorOverloading(term);
				}
			}

			@Override
			public PscriptType case_OpMult(OpMult op)  {
				if (bothTypesRealOrInt()) {
					return caseMathOperation();
				} else {
					return handleOperatorOverloading(term);
				}
			}

			@Override
			public PscriptType case_OpDivReal(OpDivReal op)
			{
				if (Utils.isJassCode(op)) {
					return caseMathOperation();
				} else if (bothTypesRealOrInt()) {
					return PScriptTypeReal.instance();			
				} else {
					return handleOperatorOverloading(term);
				}
//				attr.addError(term.getSource(), "Operator " + term.getOp() +" is not defined for " +
//						"operands " + leftType + " and " + rightType);
//				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType case_OpModReal(OpModReal op)
			{
				if (leftType instanceof PScriptTypeReal || leftType instanceof PScriptTypeInt) {
					if (rightType instanceof PScriptTypeReal || rightType instanceof PScriptTypeInt) {
						return PScriptTypeReal.instance();
					}
				}
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType case_OpModInt(OpModInt op)
			{
				if (leftType instanceof PScriptTypeInt || rightType instanceof PScriptTypeInt) {
					return leftType;
				}
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return PScriptTypeUnknown.instance();
			}

			@Override
			public PscriptType case_OpDivInt(OpDivInt op)
			{
				if (leftType instanceof PScriptTypeInt && rightType instanceof PScriptTypeInt) {
					return leftType;
				}
				term.addError("Operator " + term.getOp() +" is not defined for " +
				"operands " + leftType + " and " + rightType);
				return PScriptTypeUnknown.instance();
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


	public static  PscriptType calculate(final ExprUnary term)  {
		final PscriptType rightType = term.getRight().attrTyp();
		return term.getOpU().match(new OpUnary.Matcher<PscriptType>() {


			public PscriptType case_OpNot(OpNot op)  {
				if (!(rightType instanceof PScriptTypeBool)) {
					term.addError("Expected Boolean after not but found " + rightType);
				}
				return PScriptTypeBool.instance();
			}


			public PscriptType case_OpMinus(OpMinus op)
			{
				if (rightType instanceof PScriptTypeInt || rightType instanceof PScriptTypeReal) { 
					return rightType;
				}
				term.addError("Expected Int or Real after Minus but found " + rightType);
				return PScriptTypeReal.instance();
			}
		});
	}


	public static  PscriptType calculate(ExprMemberVar term)
	{
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return PScriptTypeUnknown.instance();
		}
		if (varDef instanceof FunctionDefinition) {
			term.addError("Missing parantheses for function call");
		}
		if (varDef.attrIsStatic() && term.getLeft().attrTyp() instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope ns = (PscriptTypeNamedScope) term.getLeft().attrTyp();
			if (!ns.isStaticRef()) {
				term.addError("Cannot access static variable " + term.getVarName() + " via a dynamic reference.");
			}
		}
		return varDef.attrTyp().setTypeArgs(term.getLeft().attrTyp().getTypeArgBinding());
	}


	public static  PscriptType calculate(ExprMemberArrayVar term)  {
		NameDef varDef = term.attrNameDef();
		if (varDef == null) {
			return PScriptTypeUnknown.instance();
		}
		PscriptType typ = varDef.attrTyp().dynamic();
		if (typ instanceof PScriptTypeArray) {
			PScriptTypeArray ar = (PScriptTypeArray) typ;
			return ar.getBaseType();			
		}
		term.addError("Variable " + term.getVarName() + " is not an array.");
		return typ;
	}


	public static  PscriptType calculate(ExprMemberMethod term)
	{
		FunctionDefinition f = term.attrFuncDef();
		if (f == null) {
			return PScriptTypeUnknown.instance();
		}
		if (f.getReturnTyp() instanceof NoTypeExpr) {
			return PScriptTypeVoid.instance();
		}
		PscriptType typ = f.getReturnTyp().attrTyp().dynamic();
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
				return leftType.dynamic();
			}
		} else if (typ instanceof PscriptTypeTypeParam) {
			PscriptTypeTypeParam typParam = (PscriptTypeTypeParam) typ;
			// typ referes to a type parameter so we have to get the binding
			
			// try function type args
			if (term.attrTypeParameterBindings().containsKey(typParam.getDef())) {
				return term.attrTypeParameterBindings().get(typParam.getDef());
			}
			// try left hand side type args	
			if (term.getLeft().attrTyp() instanceof PscriptTypeNamedScope) {
				PscriptTypeNamedScope ns = (PscriptTypeNamedScope) term.getLeft().attrTyp();
				return ns.getTypeParameterBinding(typParam.getDef()).dynamic();
			}
		}
		return typ.setTypeArgs(term.getLeft().attrTyp().getTypeArgBinding()).dynamic();
	}


	public static  PscriptType calculate(ExprFunctionCall term)
	{
		if (term.attrFuncDef() == null) {
			return PScriptTypeUnknown.instance();
		}

		FunctionDefinition f = term.attrFuncDef();
		if (f == null) {
			return PScriptTypeUnknown.instance();
		}
		if (f.getReturnTyp() instanceof NoTypeExpr) {
			if (f instanceof TupleDef) {
				return new PscriptTypeTuple((TupleDef) f);
			}
			return PScriptTypeVoid.instance();
		}
		PscriptType typ = f.getReturnTyp().attrTyp().dynamic();
		if (typ instanceof PscriptTypeModule) {
			ClassOrModule classOrModule = term.attrNearestClassOrModule();
			if (classOrModule != null) {
				return TypesHelper.typeOf(classOrModule, false);
			}
		} else if (typ instanceof PscriptTypeTypeParam) {
			PscriptTypeTypeParam typParam = (PscriptTypeTypeParam) typ;
			// typ referes to a type parameter so we have to get the binding
			
			// try function type args
			if (term.attrTypeParameterBindings().containsKey(typParam.getDef())) {
				return term.attrTypeParameterBindings().get(typParam.getDef());
			}
		}
		return typ;
	}


	public static  PscriptType calculate(ExprNewObject term)
	{

		TypeDef typeDef = term.attrTypeDef();
		if (typeDef instanceof ClassDef) {
			ClassDef c = (ClassDef) typeDef;
			final Map<TypeParamDef, PscriptType> bindings = term.attrTypeParameterBindings();
			List<PscriptType> types = Lists.newArrayList();
			for (TypeParamDef t: c.getTypeParameters()) {
				PscriptType r = bindings.get(t);
				if (r != null) {
					types.add(r);
				} else {
					types.add(PScriptTypeUnknown.instance());
				}
			}
			return new PscriptTypeClass(c, types, false);
		} else {
			term.addError("Can only create instances of classes.");
			return PScriptTypeUnknown.instance();
		}
	}


	public static  PscriptType calculate(ExprNull term)  {
		// null is a little bit tricky
		// it will have the type which you expect it to have, ...
		PscriptType t = term.attrExpectedTyp();
		if (t instanceof PScriptTypeUnknown) {
			term.addError("Could not determine type of null expression.");
		}
		// ... but of course not all types support null
		if (!   (  t instanceof PscriptTypeNamedScope
				|| t instanceof PScriptTypeHandle
				|| t instanceof PscriptTypeBoundTypeParam
				|| t instanceof PscriptTypeTypeParam
				|| t instanceof PscriptNativeType
				|| t instanceof PScriptTypeString
				|| t instanceof PScriptTypeCode)) {
			term.addError("Null is not a valid value for " + t);
		}
		return t;
	}


	public static  PscriptType calculate(ExprCast term)  {
		PscriptType targetTyp = term.getTyp().attrTyp().dynamic();
		PscriptType exprTyp = term.getExpr().attrTyp();
		if (targetTyp instanceof PScriptTypeInt && isCastableToInt(exprTyp)) {
			// cast from classtype to int: OK
		} else if (isCastableToInt(targetTyp) && exprTyp instanceof PScriptTypeInt) {
			// cast from int to classtype: OK
		} else {
			checkCastOrInstanceOf(term, exprTyp, targetTyp, "cast expression");
		}
		return targetTyp;
	}


	public static  PscriptType calculate(ExprIncomplete exprIncomplete) {
		return PScriptTypeUnknown.instance();
	}


	protected static boolean isCastableToInt(PscriptType typ) {
		return typ instanceof PscriptTypeClass 
				|| typ instanceof PscriptTypeModule
				|| typ instanceof PscriptTypeInterface
				|| typ instanceof PscriptTypeTypeParam
				|| typ instanceof PscriptTypeBoundTypeParam;
	}


	public static PscriptType calculate(ExprInstanceOf e) {
		PscriptType exprType = e.getExpr().attrTyp();
		PscriptType targetType = e.getTyp().attrTyp();
		checkCastOrInstanceOf(e, exprType, targetType, "instanceof expression");
		return PScriptTypeBool.instance();
	}


	private static void checkCastOrInstanceOf(AstElement e, PscriptType exprType, PscriptType targetType, String msgPre) {
		if (!(exprType instanceof PscriptTypeClass
				|| exprType instanceof PscriptTypeInterface)) {
			e.addError(msgPre + " not defined for expression type " + exprType);
		}
		if (!(targetType instanceof PscriptTypeClass
				|| targetType instanceof PscriptTypeInterface)) {
			e.addError(msgPre + "instanceof expression not defined for target type " + targetType);
		}
		if (exprType.isSubtypeOf(targetType, e)) {
			e.addError("This "+ msgPre + " is always true");
		} else if (!exprType.isSupertypeOf(targetType, e)) {
			e.addError(msgPre + " is not allowed because types " + exprType + " and " + targetType + " are not directly related.\n" +
					"Consider adding a cast to a common superType first.");
		}
	}

}
