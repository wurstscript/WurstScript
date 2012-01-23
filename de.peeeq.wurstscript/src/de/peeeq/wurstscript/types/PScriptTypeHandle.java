package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeHandle extends PscriptTypePrimitive {

	private static final PScriptTypeHandle instance = new PScriptTypeHandle();

	// make constructor private as we only need one instance
	private PScriptTypeHandle() {
		super("handle");
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeHandle;
	}



	public static PScriptTypeHandle instance() {
		return instance;
	}

	
}
