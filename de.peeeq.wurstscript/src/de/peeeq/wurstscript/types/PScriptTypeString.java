package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeString extends PscriptType {

	private static final PScriptTypeString instance = new PScriptTypeString();

	// make constructor private as we only need one instance
	private PScriptTypeString() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeString;
	}


	@Override
	public String getName() {
		return "string";
	}

	@Override
	public String getFullName() {
		return "string";
	}

	public static PScriptTypeString instance() {
		return instance;
	}

	
	@Override
	public String printJass() {
		return getName();
	}
}
