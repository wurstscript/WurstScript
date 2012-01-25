package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeReal extends PscriptTypePrimitive {

	private static final PScriptTypeReal instance = new PScriptTypeReal();

	// make constructor private as we only need one instance
	private PScriptTypeReal() {
		super("real");
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeReal;
	}



	public static PScriptTypeReal instance() {
		return instance;
	}


}
