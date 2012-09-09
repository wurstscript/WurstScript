package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeReal extends WurstTypePrimitive {

	private static final WurstTypeReal instance = new WurstTypeReal();

	// make constructor private as we only need one instance
	private WurstTypeReal() {
		super("real");
	}
	
	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		return other instanceof WurstTypeReal;
	}



	public static WurstTypeReal instance() {
		return instance;
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImRealVal("0.");
	}


}
