package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeBool extends PscriptTypePrimitive {

	private static final PScriptTypeBool instance = new PScriptTypeBool();

	// make constructor private as we only need one instance
	private PScriptTypeBool() {
		super("boolean");
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeBool;
	}

	public static PScriptTypeBool instance() {
		return instance;
	}

	

	


}
