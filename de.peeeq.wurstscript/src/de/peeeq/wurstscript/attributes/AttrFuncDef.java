package de.peeeq.wurstscript.attributes;

import java.util.Collection;

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
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PScriptTypeClassDefinition;
import de.peeeq.wurstscript.types.PScriptTypeModuleDefinition;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrFuncDef {

	public static  FuncDefInstance calculate(final FuncRef node) {
		final String funcName = node.getFuncName();
		final Expr[] receiverType = new Expr[1];
		FuncDefInstance result = node.match(new FuncRef.Matcher<FuncDefInstance>() {

			private FuncDefInstance defaultCase() {
				WScope scope = Scoping.getNearestScope(node);
				while (scope != null) {
					Multimap<String, FuncDefInstance> functions = scope.attrScopeFunctions();
					if (functions.containsKey(funcName)) {
						return selectOverloadedFunction(node, functions.get(funcName));
					}
					scope = Scoping.getNearestScope(scope);
				}
				return null;
			}


			private FuncDefInstance memberCase(Expr left) {
				receiverType[0] = left;
				FuncDefInstance r = null;
				PscriptType leftType = left.attrTyp();
				if (leftType instanceof PscriptTypeClass) {
					// receiver has a class type

					PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
					ClassDef classDef = leftTypeC.getClassDef();

					Multimap<String, FuncDefInstance> functions = getVisibleClassFunctions(left, classDef);


					if (functions.containsKey(funcName)) {
						FuncDefInstance f = selectOverloadedFunction(node, functions.get(funcName));
						if (((FuncDef) f.getDef()).attrIsStatic()) {
							attr.addError(left.getSource(), "Cannot call static function " + funcName + " in dynamic context.");
						}
						r = f;
					} 
				} else if (leftType instanceof PscriptTypeModule) {
					// receiver has a class type

					PscriptTypeModule leftTypeM = (PscriptTypeModule) leftType;
					ModuleDef moduleDef = leftTypeM.getModuleDef();

					Multimap<String, FuncDefInstance> functions = getVisibleClassFunctions(left, moduleDef);


					if (functions.containsKey(funcName)) {
						FuncDefInstance f = selectOverloadedFunction(node, functions.get(funcName));
						if (((FuncDef) f.getDef()).attrIsStatic()) {
							attr.addError(left.getSource(), "Cannot call static function " + funcName + " in dynamic context.");
						}
						r = f;
					} 	


				} else if (leftType instanceof PScriptTypeClassDefinition) {
					PScriptTypeClassDefinition leftTypeC = (PScriptTypeClassDefinition) leftType;
					// receiver is a classDefinition. this means we have a static method call
					ClassDef classDef = leftTypeC.getClassDef(); 
					Multimap<String, FuncDefInstance> functions = getVisibleClassFunctions(left, classDef);
					if (functions.containsKey(funcName)) {
						FuncDefInstance f = selectOverloadedFunction(node, functions.get(funcName));
						if (! ((FuncDef) f.getDef()).attrIsStatic()) {
							attr.addError(left.getSource(), "Cannot call dynamic function " + funcName + " in static context.");
						}
						r = f;
					}
				} else if (leftType instanceof PScriptTypeModuleDefinition) {
					PScriptTypeModuleDefinition leftTypeM = (PScriptTypeModuleDefinition) leftType;
					ModuleDef moduleDef = leftTypeM.getModuleDef();
					Multimap<String, FuncDefInstance> functions = getVisibleClassFunctions(left, moduleDef);
					if (functions.containsKey(funcName)) {
						FuncDefInstance f = selectOverloadedFunction(node, functions.get(funcName));
						r = f;
					}
				} 
				
				//				else {
				//					// only valid as long as there are no extension functions 
				//					attr.addError(left.getSource(), "Cannot use the dot operator on receiver of type " + leftType.getClass() + " " + leftType);
				//				}

				// check extension methods:
				WScope scope = Scoping.getNearestScope(node);
				while (r == null && scope != null) {
					Multimap<String, FuncDefInstance> functions = scope.attrScopeFunctions();
					if (functions.containsKey(funcName)) {
						r = selectExtensionFunction(left.attrTyp(), functions.get(funcName));
					}
					scope = Scoping.getNearestScope(scope);
				}
				if (r == null) {
					attr.addError(node.getSource(), "The method " + funcName + " is undefined for receiver of type " + leftType);
				}
				return r;
			}


			/**
			 * get a list of visible functions of a class for a given context
			 * @param context
			 * @param classDef
			 * @return
			 */
			private Multimap<String, FuncDefInstance> getVisibleClassFunctions(AstElement context, ClassOrModule classDef) {
				Multimap<String, FuncDefInstance> functions;
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

			@Override
			public FuncDefInstance case_ExprFuncRef(ExprFuncRef term)  {
				return defaultCase();
			}



			@Override
			public FuncDefInstance case_ExprMemberMethod(ExprMemberMethod term)
			{
				return memberCase(term.getLeft());
			}



			@Override
			public FuncDefInstance case_ExprFunctionCall(ExprFunctionCall term)
			{
				return defaultCase();
			}
		});

		if (result == null) {
			if (funcName.startsWith("InitTrig_") 
					&& node.attrNearestFuncDef() != null
					&& node.attrNearestFuncDef().getSignature().getName().equals("InitCustomTriggers")) {
				// ignore missing InitTrig functions
			} else {
				if (receiverType[0] == null) {
					attr.addError(node.getSource(), "Could not resolve reference to function " + funcName);
				} else {
					attr.addError(node.getSource(), "Could not resolve reference to function " + funcName + " for receiver " + receiverType[0] + 
							" of type "+ receiverType[0].attrTyp());
				}
			}
		}
		return result;
	}

	protected static FuncDefInstance selectExtensionFunction(PscriptType receiverTyp, Collection<FuncDefInstance> functions) {
		FuncDefInstance result = null;
		PscriptType resultType = null;
		for (FuncDefInstance f : functions) {
			if (f.getDef() instanceof ExtensionFuncDef) {
				ExtensionFuncDef extensionFuncDef = (ExtensionFuncDef) f.getDef();
				PscriptType fTyp = extensionFuncDef.getExtendedType().attrTyp();
				if (receiverTyp.isSubtypeOf(fTyp)) {
					// function is suitable
					if (result == null) {
						result = f;
						resultType = fTyp;
					} else {
						// we have already found an other function, check if this one is more specific:
						if (fTyp.isSubtypeOf(resultType)) {
							// -> this function is more specific
							result = f;
							resultType = fTyp;
						}
					}
				}
			}
		}
		return result;
	}

	protected static FuncDefInstance selectOverloadedFunction(
			FuncRef funcCall, Collection<FuncDefInstance> collection) {
		FuncDefInstance funcDef = OverloadingResolver.resolveFuncCall(collection, funcCall);
		if (funcDef != null) {
			return funcDef;
		}
		// if no method matches exactly, just choose the first method
		for (FuncDefInstance f : collection) {
			if (f == null) throw new Error();
			return f;
		}
		attr.addError(funcCall.getSource(), "Unknown Function " + funcCall.getFuncName());
		return null;
	}

}
