package de.peeeq.wurstscript.attributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithTypeArgs;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;
import de.peeeq.wurstscript.utils.Utils;

public class Generics {

	public static Map<TypeParamDef, WurstType> getTypeParameterBindings(FunctionCall fc) {
		FunctionDefinition def = fc.attrFuncDef();
		if (def == null) {
			return Collections.emptyMap();
		}
		TypeParamDefs typeParams = getTypeParameters(def);
		if (hasTypeParams(fc, typeParams)) {
			return givenBinding(fc, typeParams);
		}
		
		Map<TypeParamDef, WurstType> result = Maps.newLinkedHashMap();
		if (fc instanceof ExprMemberMethod) {
			ExprMemberMethod emm = (ExprMemberMethod) fc;
			inferTypeParameterUsingReceiver(result, emm, typeParams);
		}
		inferTypeParametersUsingArguments(result, fc.getArgs(), def.getParameters(), typeParams);
		return result;
	}

	private static void inferTypeParameterUsingReceiver(
			Map<TypeParamDef, WurstType> result, ExprMemberMethod emm, TypeParamDefs typeParams) {
		FunctionDefinition def = emm.attrFuncDef();
		if (def instanceof ExtensionFuncDef) {
			ExtensionFuncDef ef = (ExtensionFuncDef) def;
			WurstType argType = emm.getLeft().attrTyp();
			WurstType paramType = ef.attrReceiverType();
			inferTypeParameters(result, emm, argType, paramType, typeParams);
		}
	}

	// TODO in the future this should also take return type into account
	// e.g. List<String> = new List() // infer String here
	private static void inferTypeParametersUsingArguments(Map<TypeParamDef, WurstType> result, Arguments args, WParameters params,	TypeParamDefs typeParams) {
		// calculate (most general) unifier
		for (int i = 0; i < args.size() && i < params.size(); i++) {
			inferTypeParameters(result, args, args.get(i).attrTyp(), params.get(i).attrTyp(), typeParams);
		}
		if (result.size() < typeParams.size()) {
			for (TypeParamDef tp : typeParams) {
				if (!result.containsKey(tp)) {
					args.getParent().addError("Could not infer type parameter " + tp.getName() + ".");
				}
			}
		}
	}

	private static void inferTypeParameters(Map<TypeParamDef, WurstType> result, AstElement pos, WurstType argType, WurstType paramTyp,
			TypeParamDefs typeParams) {
		if (paramTyp instanceof WurstTypeTypeParam) {
			WurstTypeTypeParam paramTyp2 = (WurstTypeTypeParam) paramTyp;
			if (typeParams.contains(paramTyp2.getDef())) {
				WurstType previousType = result.put(paramTyp2.getDef(), argType);
				if (previousType != null && !previousType.equalsType(argType, pos)) {
					pos.addError("Cannot infer type parameters, there is a conflict between "
					+ previousType + " and " + argType + " for type parameter " + paramTyp2.getName());
				}
			}
		}
		if (paramTyp instanceof WurstTypeNamedScope && argType instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope paramTyp2 = (WurstTypeNamedScope) paramTyp;
			WurstTypeNamedScope argTyp2 = (WurstTypeNamedScope) argType;
			List<WurstType> paramTyp2childs = paramTyp2.getTypeParameters();
			List<WurstType> argTyp2childs = argTyp2.getTypeParameters();
			for (int i = 0; i < paramTyp2childs.size() && i < argTyp2childs.size(); i++) {
				inferTypeParameters(result, pos, argTyp2childs.get(i), paramTyp2childs.get(i), typeParams);
			}
			
		}
	}

	public static Map<TypeParamDef, WurstType> getTypeParameterBindings(ExprNewObject e) {
		ConstructorDef constrDef = e.attrConstructorDef();
		if (constrDef == null) {
			return Collections.emptyMap();
		}
		ClassOrModule classDef = constrDef.attrNearestClassOrModule();
		TypeParamDefs typeParams = getTypeParameters(classDef);
		if (hasTypeParams(e, typeParams)) {
			return givenBinding(e, typeParams);
		}
		
		Map<TypeParamDef, WurstType> result = Maps.newLinkedHashMap();
		inferTypeParametersUsingArguments(result, e.getArgs(), constrDef.getParameters(), typeParams);
		return result;
	}
	
	public static Map<TypeParamDef, WurstType> getTypeParameterBindings(ModuleUse m) {
		ModuleDef usedModule = m.attrModuleDef();
		TypeParamDefs typeParams = getTypeParameters(usedModule);
		if (hasTypeParams(m, typeParams)) {
			return givenBinding(m, typeParams);
		}
		m.addError("Missing type arguments for module " + m.getModuleName());
		return Collections.emptyMap();
	}

	public static Map<TypeParamDef, WurstType> getTypeParameterBindings(TypeExprSimple t) {
		TypeDef def = t.attrTypeDef();
		TypeParamDefs typeParams = getTypeParameters(def);
		if (hasTypeParams(t, typeParams)) {
			return givenBinding(t, typeParams);
		}
		t.addError("Missing type arguments for " + Utils.printElement(t));
		return Collections.emptyMap();
	}
	
	
	/**
	 * returns the binding given by the user
	 */
	private static Map<TypeParamDef, WurstType> givenBinding(AstElementWithTypeArgs fc, TypeParamDefs typeParams) {
		Map<TypeParamDef, WurstType> result = Maps.newLinkedHashMap();
		for (int i = 0; i < typeParams.size(); i++) {
			result.put(typeParams.get(i), fc.getTypeArgs().get(i).attrTyp().dynamic());
		}
		return result;
	}

	/**
	 * returns wether the user has given all necessary bindings 
	 */
	private static boolean hasTypeParams(AstElementWithTypeArgs fc, TypeParamDefs typeParams) {
		if (typeParams.size() == fc.getTypeArgs().size()) {
			return true;
		}
		if (fc.getTypeArgs().size() > 0) {
			fc.addError("Wrong number of type arguments. Expected " + typeParams.size());
		}
		return false;
	}

	private static TypeParamDefs getTypeParameters(AstElement def) {
		if (def instanceof AstElementWithTypeParameters) {
			AstElementWithTypeParameters wtp = (AstElementWithTypeParameters) def;
			return wtp.getTypeParameters();
		} else {
			return Ast.TypeParamDefs();
		}
	}

	

	

	

}
