package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PScriptTypeClassDefinition;
import de.peeeq.wurstscript.types.PScriptTypeModuleDefinition;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrFuncDef {

	public static  FunctionDefinition calculate(final ExprFuncRef node) {
		FunctionDefinition result = searchFunction(node.getFuncName(), node);
		if (result == null) {
			attr.addError(node.getSource(), "Could not resolve reference to function " + node.getFuncName());
		}
		return result;
	}

	public static  FunctionDefinition calculate(final ExprFunctionCall node) {
		FunctionDefinition result = searchFunction(node.getFuncName(), node);

		if (result == null) {
			String funcName = node.getFuncName();
			if (funcName.startsWith("InitTrig_") 
					&& node.attrNearestFuncDef() != null
					&& node.attrNearestFuncDef().getSignature().getName().equals("InitCustomTriggers")) {
				// ignore missing InitTrig functions
			} else {
				attr.addError(node.getSource(), "Could not resolve reference to function " + funcName);
			}
		}
		return result;
	}



	private static FunctionDefinition searchFunction(String funcName, AstElement node) {
		WScope scope = Scoping.getNearestScope(node);
		while (scope != null) {
			Map<String, NotExtensionFunction> functions = scope.attrScopeFunctions();
			if (functions.containsKey(funcName)) {
				return functions.get(funcName);
			}
			scope = Scoping.getNearestScope(scope);
		}
		return null;
	}

	public static  FunctionDefinition calculate(final ExprMemberMethod node) {
		FunctionDefinition result = null;
		Expr left = node.getLeft();
		PscriptType leftType = left.attrTyp();
		String funcName = node.getFuncName();
		if (leftType instanceof PscriptTypeClass) {
			PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
			ClassDef classDef = leftTypeC.getClassDef();

			result = getFunctionFromClassOrModule(left, funcName, classDef, true);

		} else if (leftType instanceof PscriptTypeModule) {
			PscriptTypeModule leftTypeM = (PscriptTypeModule) leftType;
			ModuleDef moduleDef = leftTypeM.getModuleDef();

			result = getFunctionFromClassOrModule(left, funcName, moduleDef, true);
		} else if (leftType instanceof PScriptTypeClassDefinition) {
			PScriptTypeClassDefinition leftTypeC = (PScriptTypeClassDefinition) leftType;
			// receiver is a classDefinition. this means we have a static method call
			ClassDef classDef = leftTypeC.getClassDef(); 
			result = getFunctionFromClassOrModule(left, funcName, classDef, false);
		} else if (leftType instanceof PScriptTypeModuleDefinition) {
			PScriptTypeModuleDefinition leftTypeM = (PScriptTypeModuleDefinition) leftType;
			ModuleDef moduleDef = leftTypeM.getModuleDef();
			result = getFunctionFromClassOrModule(left, funcName, moduleDef, false);
		} 

		// check extension methods:
		if (result == null) {
			PackageOrGlobal scope = node.attrNearestPackage();
			if (scope instanceof WPackage) {
				WPackage pack = (WPackage) scope;
				Multimap<String, ExtensionFuncDef> functions = pack.attrScopeExtensionFunctions();
				if (functions.containsKey(funcName)) {
					result = selectExtensionFunction(left.attrTyp(), functions.get(funcName));
				}
			}
		}
		if (result == null) {
			attr.addError(node.getSource(), "The method " + funcName + " is undefined for receiver of type " + leftType);
		}
		return result;
	}

	private static FunctionDefinition getFunctionFromClassOrModule(Expr left,	String funcName, ClassOrModule classDef, boolean isDynamicContext) {
		Map<String, NotExtensionFunction> functions = getVisibleClassFunctions(left, classDef);
		FunctionDefinition result = functions.get(funcName);
		if (result != null) {
			if (result.attrIsStatic() && isDynamicContext) {
				attr.addError(left.getSource(), "Cannot call static function " + funcName + " in dynamic context.");
			} else if (!result.attrIsStatic() && !isDynamicContext) {
				attr.addError(left.getSource(), "Cannot call dynamic function " + funcName + " in static context.");
			}
		}
		return result;
	}


	/**
	 * get a list of visible functions of a class for a given context
	 * @param context
	 * @param classDef
	 * @return
	 */
	private static Map<String, NotExtensionFunction> getVisibleClassFunctions(AstElement context, ClassOrModule classDef) {
		Map<String, NotExtensionFunction> functions;
		if (classDef == context.attrNearestClassDef()) {
			// same class
			functions = classDef.attrScopeFunctions();
		} else if (classDef.attrNearestPackage() == context.attrNearestPackage()) {
			// same package
			functions = classDef.attrScopePackageFunctions();
		} else {
			// different package
			functions = classDef.attrScopePublicFunctions();
		}
		return functions;
	}


	protected static FunctionDefinition selectExtensionFunction(PscriptType receiverTyp, Collection<ExtensionFuncDef> collection) {
		FunctionDefinition result = null;
		PscriptType resultType = null;
		for (ExtensionFuncDef extensionFuncDef : collection) {
			PscriptType fTyp = extensionFuncDef.getExtendedType().attrTyp();
			if (receiverTyp.isSubtypeOf(fTyp)) {
				// function is suitable
				if (result == null) {
					result = extensionFuncDef;
					resultType = fTyp;
				} else {
					// we have already found an other function, check if this one is more specific:
					if (fTyp.isSubtypeOf(resultType)) {
						// -> this function is more specific
						result = extensionFuncDef;
						resultType = fTyp;
					}
				}
			}
		}
		return result;
	}


}
