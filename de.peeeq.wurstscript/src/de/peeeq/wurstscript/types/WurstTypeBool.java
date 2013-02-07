package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeBool extends WurstTypePrimitive {

	private static final WurstTypeBool instance = new WurstTypeBool();

	// make constructor private as we only need one instance
	private WurstTypeBool() {
		super("boolean");
	}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		return other instanceof WurstTypeBool;
	}

	public static WurstTypeBool instance() {
		return instance;
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImBoolVal(false);
	}

	

	


}
