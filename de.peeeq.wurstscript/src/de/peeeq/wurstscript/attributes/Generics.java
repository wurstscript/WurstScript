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
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.types.PscriptTypeTypeParam;
import de.peeeq.wurstscript.utils.Utils;

public class Generics {

	public static Map<TypeParamDef, PscriptType> getTypeParameterBindings(FunctionCall fc) {
		FunctionDefinition def = fc.attrFuncDef();
		TypeParamDefs typeParams = getTypeParameters(def);
		if (hasTypeParams(fc, typeParams)) {
			return givenBinding(fc, typeParams);
		}
		
		return inferTypeParametersUsingArguments(fc.getArgs(), def.getParameters(), typeParams);
	}

	// TODO in the future this should also take return type into account
	// e.g. List<String> = new List() // infer String here
	private static Map<TypeParamDef, PscriptType> inferTypeParametersUsingArguments(Arguments args, WParameters params,	TypeParamDefs typeParams) {
		Map<TypeParamDef, PscriptType> result = Maps.newHashMap();
		// calculate (most general) unifier
		for (int i = 0; i < args.size() && i < params.size(); i++) {
			inferTypeParameters(result, args, args.get(i).attrTyp(), params.get(i).attrTyp(), typeParams);
		}
		if (result.size() < typeParams.size()) {
			attr.addError(args.attrSource(), "Could not infer all type parameters.");
		}
		return result;
	}

	private static void inferTypeParameters(Map<TypeParamDef, PscriptType> result, AstElement pos, PscriptType argType, PscriptType paramTyp,
			TypeParamDefs typeParams) {
		if (paramTyp instanceof PscriptTypeTypeParam) {
			PscriptTypeTypeParam paramTyp2 = (PscriptTypeTypeParam) paramTyp;
			if (typeParams.contains(paramTyp2.getDef())) {
				PscriptType previousType = result.put(paramTyp2.getDef(), argType);
				if (previousType != null && !previousType.equalsType(argType, pos)) {
					attr.addError(pos.attrSource(), "Cannot infer type parameters, there is a conflict between "
							+ previousType + " and " + argType + " for type parameter " + paramTyp2.getName());
				}
			}
		}
		if (paramTyp instanceof PscriptTypeNamedScope && argType instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope paramTyp2 = (PscriptTypeNamedScope) paramTyp;
			PscriptTypeNamedScope argTyp2 = (PscriptTypeNamedScope) argType;
			List<PscriptType> paramTyp2childs = paramTyp2.getTypeParameters();
			List<PscriptType> argTyp2childs = argTyp2.getTypeParameters();
			for (int i = 0; i < paramTyp2childs.size() && i < argTyp2childs.size(); i++) {
				inferTypeParameters(result, pos, argTyp2childs.get(i), paramTyp2childs.get(i), typeParams);
			}
			
		}
	}

	public static Map<TypeParamDef, PscriptType> getTypeParameterBindings(ExprNewObject e) {
		ConstructorDef constrDef = e.attrConstructorDef();
		if (constrDef == null) {
			return Collections.emptyMap();
		}
		ClassOrModule classDef = constrDef.attrNearestClassOrModule();
		TypeParamDefs typeParams = getTypeParameters(classDef);
		if (hasTypeParams(e, typeParams)) {
			return givenBinding(e, typeParams);
		}
		
		return inferTypeParametersUsingArguments(e.getArgs(), constrDef.getParameters(), typeParams);
	}
	
	public static Map<TypeParamDef, PscriptType> getTypeParameterBindings(ModuleUse m) {
		ModuleDef usedModule = m.attrModuleDef();
		TypeParamDefs typeParams = getTypeParameters(usedModule);
		if (hasTypeParams(m, typeParams)) {
			return givenBinding(m, typeParams);
		}
		attr.addError(m.getSource(), "Missing type arguments for module " + m.getModuleName());
		return Collections.emptyMap();
	}

	public static Map<TypeParamDef, PscriptType> getTypeParameterBindings(TypeExprSimple t) {
		TypeDef def = t.attrTypeDef();
		TypeParamDefs typeParams = getTypeParameters(def);
		if (hasTypeParams(t, typeParams)) {
			return givenBinding(t, typeParams);
		}
		attr.addError(t.getSource(), "Missing type arguments for " + Utils.printElement(t));
		return Collections.emptyMap();
	}
	
	
	/**
	 * returns the binding given by the user
	 */
	private static Map<TypeParamDef, PscriptType> givenBinding(AstElementWithTypeArgs fc, TypeParamDefs typeParams) {
		Map<TypeParamDef, PscriptType> result = Maps.newHashMap();
		for (int i = 0; i < typeParams.size(); i++) {
			result.put(typeParams.get(i), fc.getTypeArgs().get(i).attrTyp());
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
			attr.addError(fc.attrSource(), "Wrong number of type arguments. Expected " + typeParams.size());
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
