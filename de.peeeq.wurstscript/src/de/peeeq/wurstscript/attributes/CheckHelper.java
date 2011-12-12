package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.PscriptType;

public class CheckHelper {

	/**
	 * check if the signature of "f" is a refinement of the signature of "of"
	 * @param f
	 * @param of
	 */
	public static void checkIfIsRefinement(FunctionDefinition f,
			FunctionDefinition of) {
		String funcName = f.getSignature().getName();
		// check static-ness
		if (f.attrIsStatic() && !of.attrIsStatic()) {
			attr.addError(f.getSource(), "Function " + funcName + " must not be static.");
		}
		if (!f.attrIsStatic() && of.attrIsStatic()) {
			attr.addError(f.getSource(), "Function " + funcName + " must be static.");
		}
		// check returntype
		PscriptType f_type = f.getSignature().getTyp().attrTyp();
		PscriptType of_type = of.getSignature().getTyp().attrTyp();
		if (! f_type.isSubtypeOf(of_type)) { 
			attr.addError(f.getSource(), "Cannot override function " + funcName + ": The return type is " + f_type + 
					" but it should be " + of_type + ".");
		}
		
		// check parameter count
		int f_count = f.getSignature().getParameters().size();
		int of_count = of.getSignature().getParameters().size(); 
		// check parameters
		if (f_count != of_count) {
			attr.addError(f.getSource(), "Cannot override function " + funcName + ": The number of parameters of function " + funcName + " must be equal to " + of_count + 
					", as defined by the overriden function.");
			return;
		}
		int i = 0;
		for (WParameter f_p : f.getSignature().getParameters()) {
			WParameter of_p = of.getSignature().getParameters().get(i);
			PscriptType f_p_type = f_p.attrTyp();
			PscriptType of_p_type = of_p.attrTyp();
			if (! f_p_type.isSupertypeOf(of_p_type)) {
				attr.addError(f.getSource(), "Cannot override function " + funcName + ": The type of parameter " + f_p.getName() + " is " + f_p_type + 
						" but it should be " + of_p_type );
			}
			i++;
		}
	}

}
