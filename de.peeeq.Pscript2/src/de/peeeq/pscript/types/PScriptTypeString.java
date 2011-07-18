package de.peeeq.pscript.types;


public class PScriptTypeString extends PscriptType {

	private static final PScriptTypeString instance = new PScriptTypeString();

	// make constructor private as we only need one instance
	private PScriptTypeString() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
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

}
