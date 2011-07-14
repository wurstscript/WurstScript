package de.peeeq.pscript.attributes;

import org.eclipse.emf.common.util.EList;

import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.ParameterDef;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.types.PscriptType;

public class AttrUtil {
	
//	/**
//	 * 
//	 * @param typeName
//	 * @return a type for the build in type or null if it is not a build in type
//	 */
//	public static PscriptType getBuildInType(String typeName) {
//		if (typeName.equals("Int")) {
//			return PScriptTypeInt.instance();
//		}
//		return null;
//	}
	

	static String printFuncDef(FuncDef f) {
		return f.getName() + "(" + AttrUtil.printParams(f.getParameters()) + "):" + f.getType().getName();
	}
	
	static String printParams(EList<VarDef> eList) {
		String result = "";
		boolean first = true;
		for (VarDef n : eList) {
			ParameterDef p = (ParameterDef) n;
			if (!first) {
				result += ", ";
			}
			result += p.getName();
			result += ":";
			result += p.getType().getName();
			
			first = false;
		}
		return result;
	}

	public static  PscriptType returnType(final AttributeManager attributeManager, FuncDef f) {
		return attributeManager.getAttValue(AttrTypeExprType.class, f.getType());
	}

//	/**
//	 * @param A
//	 * @param B
//	 * @return is A a subtype of B
//	 */
//	public static boolean isSubType(PscriptType A, PscriptType B) {
//		// TODO maybe move this to some other class
//		if (A.equals(B)) {
//			return true;
//		}
//		// TODO other types
//		
//		return false;
//	}

}
