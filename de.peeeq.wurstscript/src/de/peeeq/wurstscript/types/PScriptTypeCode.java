package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeCode extends PscriptTypePrimitive {

	private static final PScriptTypeCode instance = new PScriptTypeCode();

	// make constructor private as we only need one instance
	private PScriptTypeCode() {
		super("code");
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeCode;
	}

	public static PScriptTypeCode instance() {
		return instance;
	}


}
