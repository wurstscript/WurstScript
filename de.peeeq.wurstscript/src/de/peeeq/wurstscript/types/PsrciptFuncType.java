package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PsrciptFuncType extends PscriptType {


	private PscriptType returnType;
	private PscriptType[] paramTypes;

	public PsrciptFuncType(PscriptType returnType, PscriptType ... paramTypes ) {
		this.returnType = returnType;
		this.paramTypes = paramTypes;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		if (! (other instanceof PsrciptFuncType)) {
			return false;
		}
//		PsciptFuncType f = (PsciptFuncType) other;
		
		// TODO PsciptFuncType
		return false;
	}

	@Override
	public String getName() {
		String result = "function (";
		for (int i=0; i<paramTypes.length; i++) {
			if (i>0) {
				result += ",";
			}
			result += paramTypes[i].getName();
		}
		result += ":"+returnType.getName();
		return result ;
	}

	@Override
	public String getFullName() {
		String result = "function(";
		for (int i=0; i<paramTypes.length; i++) {
			if (i>0) {
				result += ",";
			}
			result += paramTypes[i].getFullName();
		}
		result += "):"+returnType.getFullName();
		return result ;
	}

	@Override
	public String printJass() {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

}
