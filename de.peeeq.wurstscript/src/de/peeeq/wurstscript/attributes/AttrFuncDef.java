package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrFuncDef {
	
	public static  FunctionDefinition calculate(final FuncRef node) {
		final String funcName = node.getFuncName();
		FunctionDefinition result = node.match(new FuncRef.Matcher<FunctionDefinition>() {

			private FunctionDefinition defaultCase() {
				WScope scope = Scoping.getNearestScope(node);
				while (scope != null) {
					Multimap<String, FunctionDefinition> functions = scope.attrScopeFunctions();
					if (functions.containsKey(funcName)) {
						return selectOverloadedFunction(node, functions.get(funcName));
					}
					scope = Scoping.getNearestScope(scope);
				}
				return null;
			}
			
			
			private FunctionDefinition memberCase(Expr left) {
				PscriptType leftType = left.attrTyp();
				if (leftType instanceof PscriptTypeClass) {
					PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
					ClassDef classDef = leftTypeC.getClassDef();
					
					Multimap<String, FunctionDefinition> functions;
					if (classDef == left.attrNearestClassDef()) {
						// same class
						functions = classDef.attrScopeFunctions();
					} else if (classDef.attrNearestPackage() == left.attrNearestPackage()) {
						// same package
						functions = classDef.attrScopePackageFunctions();
					} else {
						// different package
						functions = classDef.attrScopePublicFunctions();
					}
					if (functions.containsKey(funcName)) {
						return selectOverloadedFunction(node, functions.get(funcName));
					}
				}
				// TODO check extension functions
				return null;
			}
			
			@Override
			public FunctionDefinition case_ExprFuncRef(ExprFuncRef term)  {
				return defaultCase();
			}

			

			@Override
			public FunctionDefinition case_ExprMemberMethod(ExprMemberMethod term)
					 {
				return memberCase(term.getLeft());
			}

			

			@Override
			public FunctionDefinition case_ExprFunctionCall(ExprFunctionCall term)
					 {
				return defaultCase();
			}
		});
		
		if (result == null) {
			attr.addError(node.getSource(), "Could not resolve reference to function " + funcName);
		}
		return result;
	}

	protected static FunctionDefinition selectOverloadedFunction(
			FuncRef funcCall, Collection<FunctionDefinition> functions) {
		OverloadingResolver.resolveFuncCall(functions, funcCall);
		// TODO overloading - select the right method
		for (FunctionDefinition f : functions) {
			return f;
		}
		attr.addError(funcCall.getSource(), "Unknown Function " + funcCall.getFuncName());
		return null;
	}

}
