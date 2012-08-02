package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.List;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpMult;
import de.peeeq.wurstscript.ast.OpPlus;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrFuncDef {
	final static String overloadingPlus = "op_plus";
	final static String overloadingMinus = "op_minus";
	final static String overloadingMult = "op_mult";
	final static String overloadingDiv = "op_div";

	public static  FunctionDefinition calculate(final ExprFuncRef node) {
		FunctionDefinition result;
		if (node.getScopeName().length() > 0) {
			result = searchFunctionWithScope(node);
		} else {
			result = searchFunction(node.getFuncName(), node);
		}
		if (result == null) {
			attr.addError(node.getSource(), "Could not resolve reference to function " + node.getFuncName());
		}
		return result;
	}

	private static FunctionDefinition searchFunctionWithScope(final ExprFuncRef node) {
		WScope scope = NameResolution.searchTypedNameGetOne(WScope.class, node.getScopeName(), node, true);
		if (scope == null) {
			return null;
		}
		List<NotExtensionFunction> functions = NameResolution.searchTypedName(NotExtensionFunction.class, node.getFuncName(), scope, true);
		if (functions.size() == 0) {
			return null;
		} else {
			if (functions.size() > 1) {
				attr.addError(node.getSource(), "Reference to function " + node.getFuncName() + " is ambigious.");
			}
			return functions.get(0);
		}
	}
	
	public static FunctionDefinition calculate(ExprBinary node) {
		return getExtensionFunction(node.getLeft(), node.getRight(), node.getOp());
	}

	public static FunctionDefinition getExtensionFunction(Expr left, Expr right, OpBinary op) {
		String funcName = getOperatorFuncName(op);
		if (funcName == null || nativeOperator(left.attrTyp(), right.attrTyp())) {
			return null;
		}
		return getMemberFunc(left, left.attrTyp(), funcName);
	}
	
	
	private static String getOperatorFuncName(OpBinary op) {
		if ( op instanceof OpPlus) {
			return overloadingPlus;
		} else if ( op instanceof OpMinus) {
			return overloadingMinus;
		} else if ( op instanceof OpMult) {
			return overloadingMult;
		} else if ( op instanceof OpDivReal) {
			return overloadingDiv;
		} else {
			return null;
		}
	}

	/**
	 * chcks if operator is a native operator like for 1+2
	 */
	private static boolean nativeOperator(PscriptType leftType, PscriptType rightType) {
		return
			// numeric
			((leftType instanceof PScriptTypeInt || leftType instanceof PScriptTypeReal) && (rightType instanceof PScriptTypeInt || rightType instanceof PScriptTypeReal))
			// strings
			|| (leftType instanceof PScriptTypeString && rightType instanceof PScriptTypeString);
	}

	public static  FunctionDefinition calculate(final ExprFunctionCall node) {
		FunctionDefinition result = searchFunction(node.getFuncName(), node);

		if (result == null) {
			String funcName = node.getFuncName();
			if (funcName.startsWith("InitTrig_") 
					&& node.attrNearestFuncDef() != null
					&& node.attrNearestFuncDef().getName().equals("InitCustomTriggers")) {
				// ignore missing InitTrig functions
			} else {
				attr.addError(node.getSource(), "Could not resolve reference to called function " + funcName);
			}
		}
		return result;
	}



	private static FunctionDefinition searchFunction(String funcName, FuncRef node) {
		if (node == null) {
			return null;
		}
		
		List<NotExtensionFunction> functions = NameResolution.searchTypedName(NotExtensionFunction.class, funcName, node, true);
		if (functions.size() > 0) {
			// TODO ambiguous function 
			return functions.get(0).attrRealFuncDef();
		} else {
			return null;
		}
	}


	public static  FunctionDefinition calculate(final ExprMemberMethod node) {
		
		Expr left = node.getLeft();
		PscriptType leftType = left.attrTyp();
		String funcName = node.getFuncName();
		
		FunctionDefinition result = getMemberFunc(node, leftType, funcName);
		if (result == null) {
			attr.addError(node.getSource(), "The method " + funcName + " is undefined for receiver of type " + leftType);
		}
		return result;
	}

	private static FunctionDefinition getMemberFunc(Expr context, PscriptType leftType, String funcName) {
		FunctionDefinition result = null;
		if (leftType instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope sr = (PscriptTypeNamedScope) leftType;
			result = null;
			Collection<FunctionDefinition> funcs = NameResolution.getTypedNameFromNamedScope(FunctionDefinition.class, context, funcName, sr);
			if (funcs.size() > 0) {
				result = Utils.getFirst(funcs);
				if (funcs.size() > 1) {
					attr.addError(context.getSource(), "Reference to function " + funcName + " is ambigious. " +
							"Alternatives are: " + NameResolution.printAlternatives(funcs));
				}
			}
			// get real implementation funcDef (wrt override)
			if (result != null && !sr.isStaticRef()) {
				result = result.attrRealFuncDef();
			}
		}

		// check extension methods:
		if (result == null) {
			PackageOrGlobal scope = context.attrNearestPackage();
			if (scope instanceof WPackage) {
				WPackage pack = (WPackage) scope;
				Collection<ExtensionFuncDef> functions = NameResolution.searchTypedName(ExtensionFuncDef.class, funcName, pack, false);
				result = selectExtensionFunction(leftType, functions);
			}
		}
		return result;
	}

	protected static FunctionDefinition selectExtensionFunction(PscriptType receiverTyp, Collection<ExtensionFuncDef> collection) {
		FunctionDefinition result = null;
		PscriptType resultType = null;
		for (ExtensionFuncDef extensionFuncDef : collection) {
			PscriptType fTyp = extensionFuncDef.getExtendedType().attrTyp();
			if (receiverTyp.isSubtypeOf(fTyp, extensionFuncDef)) {
				// function is suitable
				if (result == null) {
					result = extensionFuncDef;
					resultType = fTyp;
				} else {
					// we have already found an other function, check if this one is more specific:
					if (fTyp.isSubtypeOf(resultType, extensionFuncDef)) {
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
