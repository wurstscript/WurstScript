package de.peeeq.pscript.types;


public class PScriptTypeInt extends PscriptType {

	private static final PScriptTypeInt instance = new PScriptTypeInt();

	// make constructor private as we only need one instance
	private PScriptTypeInt() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return other instanceof PScriptTypeInt;
	}

	@Override
	public boolean isSupertypeOf(PscriptType other) {
		return false;
	}

	@Override
	public String getName() {
		return "Int";
	}

	@Override
	public String getFullName() {
		return "Int";
	}

	public static PScriptTypeInt instance() {
		return instance;
	}

}
