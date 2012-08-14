package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class WurstTypeBool extends WurstTypePrimitive {

	private static final WurstTypeBool instance = new WurstTypeBool();

	// make constructor private as we only need one instance
	private WurstTypeBool() {
		super("boolean");
	}
	
	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		return other instanceof WurstTypeBool;
	}

	public static WurstTypeBool instance() {
		return instance;
	}

	

	


}
