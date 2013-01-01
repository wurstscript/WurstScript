package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstFuncType extends WurstType {


	private WurstType returnType;
	private WurstType[] paramTypes;

	public WurstFuncType(WurstType returnType, WurstType ... paramTypes ) {
		this.returnType = returnType;
		this.paramTypes = paramTypes;
	}

	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		if (! (other instanceof WurstFuncType)) {
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
	public String[] jassTranslateType() {
		return new String[] { "code" };
	}

	@Override
	public ImType imTranslateType() {
		return JassIm.ImSimpleType("code");
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImNull();
	}


}
