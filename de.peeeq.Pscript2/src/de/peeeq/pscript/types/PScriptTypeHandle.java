package de.peeeq.pscript.types;


public class PScriptTypeHandle extends PscriptType {

	private static final PScriptTypeHandle instance = new PScriptTypeHandle();

	// make constructor private as we only need one instance
	private PScriptTypeHandle() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return other instanceof PScriptTypeHandle;
	}


	@Override
	public String getName() {
		return "Handle";
	}

	@Override
	public String getFullName() {
		return "Handle";
	}

	public static PScriptTypeHandle instance() {
		return instance;
	}

}
