package de.peeeq.pscript.types;


public class PScriptTypeCode extends PscriptType {

	private static final PScriptTypeCode instance = new PScriptTypeCode();

	// make constructor private as we only need one instance
	private PScriptTypeCode() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return other instanceof PScriptTypeCode;
	}


	@Override
	public String getName() {
		return "code";
	}

	@Override
	public String getFullName() {
		return "code";
	}

	public static PScriptTypeCode instance() {
		return instance;
	}

}
