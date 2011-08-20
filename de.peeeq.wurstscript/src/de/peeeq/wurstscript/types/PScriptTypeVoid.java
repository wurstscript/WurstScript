package de.peeeq.wurstscript.types;


public class PScriptTypeVoid extends PscriptType {

	private static final PScriptTypeVoid instance = new PScriptTypeVoid();

	// make constructor private as we only need one instance
	private PScriptTypeVoid() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return false;
	}

	@Override
	public String getName() {
		return "Void";
	}

	@Override
	public String getFullName() {
		return "Void";
	}

	public static PScriptTypeVoid instance() {
		return instance;
	}

}
