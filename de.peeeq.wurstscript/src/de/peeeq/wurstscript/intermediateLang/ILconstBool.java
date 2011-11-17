package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstBool extends ILconstAbstract {

	private boolean val;

	public final static ILconstBool FALSE = new ILconstBool(false);
	public final static ILconstBool TRUE = new ILconstBool(true);
	
	public static ILconstBool instance(boolean value) {
		return value ? TRUE : FALSE;
	}
	
	private ILconstBool(boolean b) {
		val = b;
	}

	public boolean getVal() {
		return val;
	}

	@Override
	public String print() {
		return val ? "true" : "false";
	}

	public boolean not() {
		return !val;
	}


	public PscriptType getType() {
		return PScriptTypeBool.instance();
	}

	public ILconst negate() {
		return val ? FALSE : TRUE;
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		return other == this;
	}

	
	

}
