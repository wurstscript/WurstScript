package de.peeeq.wurstscript.types;


public class PsciptFuncType extends PscriptType {


	private PscriptType returnType;
	private PscriptType[] paramTypes;

	public PsciptFuncType(PscriptType returnType, PscriptType ... paramTypes ) {
		this.returnType = returnType;
		this.paramTypes = paramTypes;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
		if (! (other instanceof PsciptFuncType)) {
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

}
