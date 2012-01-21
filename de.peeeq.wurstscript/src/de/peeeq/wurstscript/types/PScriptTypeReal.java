package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeReal extends PscriptType {

	private static final PScriptTypeReal instance = new PScriptTypeReal();

	// make constructor private as we only need one instance
	private PScriptTypeReal() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeReal;
	}


	@Override
	public String getName() {
		return "real";
	}

	@Override
	public String getFullName() {
		return "real";
	}

	public static PScriptTypeReal instance() {
		return instance;
	}

	@Override
	public String printJass() {
		return getName();
	}

}
