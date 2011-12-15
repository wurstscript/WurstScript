package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.utils.Utils;


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
		if (node == null) {
			return null;
		}
		NamedScope scope = node.attrNearestNamedScope();
		if (scope == null) {
			// no more named scope exists -> search in global scope
			return searchGlobalScope(funcName, node);
		}
		Map<String, NotExtensionFunction> functions = scope.attrScopeFunctions();
		if (functions.containsKey(funcName)) {
			return functions.get(funcName);
		}
		// not found yet? -> search in parent scope
		return searchFunction(funcName, scope.getParent());
	}

	private static FunctionDefinition searchGlobalScope(String funcName, AstElement node) {
		CompilationUnit root = (CompilationUnit) Utils.getRoot(node);
		return root.attrScopeFunctions().get(funcName);
	}

	public static  FunctionDefinition calculate(final ExprMemberMethod node) {
		FunctionDefinition result = null;
		Expr left = node.getLeft();
		PscriptType leftType = left.attrTyp();
		String funcName = node.getFuncName();
		if (leftType instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope sr = (PscriptTypeNamedScope) leftType;
			result = getFunctionFromNamedScopeRef(left, funcName, sr);
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

	private static FunctionDefinition getFunctionFromNamedScopeRef(Expr context, String funcName, PscriptTypeNamedScope sr) {
		if (sr.isStaticRef() && (sr.getDef() instanceof ClassOrModuleOrModuleInstanciation)) {
			// if we have a static reference to a class, module etc. we have to ignore
			// the overriding functions and thus cannot use the normal scope attributes...
			ClassOrModuleOrModuleInstanciation c = (ClassOrModuleOrModuleInstanciation) sr.getDef();
			FuncDef func = c.attrAllFunctions().get(funcName);
			if (isSameClass(context, sr)) {
				// no problem
			} else if (isSamePackage(context, sr)) {
				if (func.attrIsPrivate()) {
					attr.addError(context.getSource(), "Function " + funcName + " is private.");
				}
			} else {
				if (func.attrIsPrivate() || func.attrIsProtected()) {
					attr.addError(context.getSource(), "Function " + funcName + " is protected. It cannot be accessed from other packages.");
				}
			}
			return func;
		} else { // dynamic ref
			Map<String, NotExtensionFunction> functions;
			if (isSameClass(context, sr)) {
				functions = sr.getDef().attrScopeFunctions();
			} else if (isSamePackage(context, sr)) {
				functions = sr.getDef().attrScopePackageFunctions();
			} else {
				// different package
				functions = sr.getDef().attrScopePublicFunctions();
			}
			return functions.get(funcName);
		}
	}

	

	private static boolean isSamePackage(AstElement context, PscriptTypeNamedScope sr) {
		return sr.getDef().attrNearestPackage() == context.attrNearestPackage();
	}

	private static boolean isSameClass(AstElement context, PscriptTypeNamedScope sr) {
		return sr.getDef() == context.attrNearestNamedScope();
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
