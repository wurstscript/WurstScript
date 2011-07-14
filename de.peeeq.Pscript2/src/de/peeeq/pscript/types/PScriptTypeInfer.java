package de.peeeq.pscript.types;

/**
 * the exact type is not known but it will be whatever you want it to be ;)
 * (used for the buildin/native functions, where we cannot check the types) 
 */
public class PScriptTypeInfer extends PscriptType {

	private static PscriptType instance = new PScriptTypeInfer();

	private PScriptTypeInfer() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return true;
	}

	@Override
	public String getName() {
		return "<Infer type>";
	}

	@Override
	public String getFullName() {
		return getName();
	}

	public static PscriptType instance() {
		return instance ;
	}

}
