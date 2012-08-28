package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeInt extends WurstTypePrimitive {

	private static final WurstTypeInt instance = new WurstTypeInt();

	// make constructor private as we only need one instance
	protected WurstTypeInt() {
		super("integer");
	}
	
	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		if (other instanceof WurstTypeBoundTypeParam) {
			return isSubtypeOf(((WurstTypeBoundTypeParam) other).getBaseType(), location);
		}
		return other instanceof WurstTypeInt;
	}


	public static WurstTypeInt instance() {
		return instance;
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}
	

}
