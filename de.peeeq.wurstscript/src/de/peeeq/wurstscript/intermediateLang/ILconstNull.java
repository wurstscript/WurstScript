package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeNull;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstNull extends ILconstAbstract {


	@Override
	public String print() {
		return "null";
	}
	
	public PscriptType getType() {
		return PScriptTypeNull.instance();
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		return other instanceof ILconstNull;
	}

}
