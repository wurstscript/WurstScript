package de.peeeq.pscript.types;


public class PScriptTypeReal extends PscriptType {

	private static final PScriptTypeReal instance = new PScriptTypeReal();

	// make constructor private as we only need one instance
	private PScriptTypeReal() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
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

}
