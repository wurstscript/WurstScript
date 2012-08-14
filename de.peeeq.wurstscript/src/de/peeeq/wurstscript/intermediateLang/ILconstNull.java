package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.WurstTypeInfer;
import de.peeeq.wurstscript.types.WurstType;

public class ILconstNull extends ILconstAbstract {


	private static ILconstNull instance = new ILconstNull();

	private ILconstNull() {
	}
	
	@Override
	public String print() {
		return "null";
	}
	
	public WurstType getType() {
		return WurstTypeInfer.instance();
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		return other instanceof ILconstNull;
	}

	public static ILconstNull instance() {
		return instance;
	}

}
