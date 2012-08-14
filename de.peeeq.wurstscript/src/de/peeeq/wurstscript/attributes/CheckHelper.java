package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.WurstType;

public class CheckHelper {

	/**
	 * check if the signature of "f" is a refinement of the signature of "of"
	 * @param f
	 * @param of
	 * @param b 
	 */
	public static void checkIfIsRefinement(Map<TypeParamDef, WurstType> typeParamMapping, FunctionDefinition f, FunctionDefinition of, String errorMessage, boolean reverseErrorMessage) {
		String funcName = f.getName();
		// check static-ness
		if (f.attrIsStatic() && !of.attrIsStatic()) {
			f.addError("Function " + funcName + " must not be static.");
		}
		if (!f.attrIsStatic() && of.attrIsStatic()) {
			f.addError("Function " + funcName + " must be static.");
		}
		// check returntype
		WurstType f_type = getRealType(typeParamMapping, f.getReturnTyp().attrTyp());
		WurstType of_type = getRealType(typeParamMapping, of.getReturnTyp().attrTyp());
		if (! f_type.isSubtypeOf(of_type, f)) { 
			f.addError(errorMessage + funcName + ": The return type is " + f_type + 
			" but it should be " + of_type + ".");
		}
		
		// check parameter count
		int f_count = f.getParameters().size();
		int of_count = of.getParameters().size(); 
		// check parameters
		if (f_count != of_count) {
			f.addError(errorMessage + funcName + ": The number of parameters of function " + funcName + " must be equal to " + of_count + 
			", as defined by the overriden function.");
			return;
		}
		int i = 0;
		for (WParameter f_p : f.getParameters()) {
			WParameter of_p = of.getParameters().get(i);
			WurstType f_p_type = getRealType(typeParamMapping, f_p.attrTyp());
			WurstType of_p_type = getRealType(typeParamMapping, of_p.attrTyp());
			if (! f_p_type.isSupertypeOf(of_p_type, f)) {
				if (reverseErrorMessage) {
					WurstType temp = f_p_type;
					f_p_type = of_p_type;
					of_p_type = temp;
				}
				
				f.addError(errorMessage + funcName + ": The type of parameter " + f_p.getName() + " is " + f_p_type + 
				" but it should be " + of_p_type);
			}
			i++;
		}
	}

	private static WurstType getRealType(Map<TypeParamDef, WurstType> typeParamMapping, WurstType t) {
		return t.setTypeArgs(typeParamMapping);
	}

	/**
	 * checks if overridingFunc is a refinement of all the overriddenFuntions
	 * @param overridingFunc
	 * @param overriddenFuntions
	 */
	public static void checkIfIsRefinement(FuncDef overridingFunc,	Collection<FuncDef> overriddenFuntions, String errorMessage) {
		Map<TypeParamDef, WurstType> typeParamBinding = Collections.emptyMap();		
		for (FuncDef f: overriddenFuntions) {
			checkIfIsRefinement(typeParamBinding, overridingFunc, f, errorMessage, false);
		}
		
	}

}
