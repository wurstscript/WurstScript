package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeInfer;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstNull extends ILconstAbstract {


	@Override
	public String print() {
		return "null";
	}
	
	public PscriptType getType() {
		return PScriptTypeInfer.instance();
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		return other instanceof ILconstNull;
	}

}
