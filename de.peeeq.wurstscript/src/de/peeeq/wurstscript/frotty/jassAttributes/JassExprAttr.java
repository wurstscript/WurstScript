package de.peeeq.wurstscript.frotty.jassAttributes;

import de.peeeq.wurstscript.jassAst.JassExprBinary;
import de.peeeq.wurstscript.jassAst.JassExprBoolVal;
import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprIntVal;
import de.peeeq.wurstscript.jassAst.JassExprNull;
import de.peeeq.wurstscript.jassAst.JassExprRealVal;
import de.peeeq.wurstscript.jassAst.JassExprStringVal;
import de.peeeq.wurstscript.jassAst.JassExprUnary;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;

public class JassExprAttr {

	public static String getType(JassExprBinary e) {
		if ( e.getLeftExpr().getType().equals("TYPE_INTEGER")) {
			return "TYPE_INTEGER";
		}else if ( e.getLeftExpr().getType().equals("TYPE_REAL")) {
			return "TYPE_REAL";
		}else if ( e.getLeftExpr().getType().equals("TYPE_STRING")) {
			return "TYPE_STRING";
		}else 
		
		return null;
	}
	
	public static String getType(JassExprUnary e) {
		if ( e.getRight().getType().equals("TYPE_INTEGER")) {
			return "TYPE_INTEGER";
		}else if ( e.getRight().getType().equals("TYPE_REAL")) {
			return "TYPE_REAL";
		}
		
		return null;
	}
	
	public static String getType(JassExprBoolVal e) {
		return "TYPE_BOOLEAN";
	}
	
	public static String getType(JassExprFuncRef e) {
		return "TYPE_CODE";
	}
	
	public static String getType(JassExprFunctionCall e) {
		return "TYPE_CODE";
	}
	
	public static String getType(JassExprIntVal e) {
		return "TYPE_INTEGER";
	}
	
	public static String getType(JassExprRealVal e) {
		return "TYPE_REAL";
	}
	
	public static String getType(JassExprStringVal e) {
		return "TYPE_STRING";
	}
	
	public static String getType(JassExprNull e) {
		return "TYPE_NULL";
	}
	
	public static String getType(JassExprVarAccess e) {
		return e.attrVariableDefinition().getType();
	}
	
	public static String getType(JassExprVarArrayAccess e) {
		return e.attrVariableDefinition().getType();
	}

}
