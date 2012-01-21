package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeBool extends PscriptType {

	private static final PScriptTypeBool instance = new PScriptTypeBool();

	// make constructor private as we only need one instance
	private PScriptTypeBool() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeBool;
	}


	@Override
	public String getName() {
		return "boolean";
	}

	@Override
	public String getFullName() {
		return "boolean";
	}

	public static PScriptTypeBool instance() {
		return instance;
	}

	@Override
	public String printJass() {
		return getName();
	}

}
