package de.peeeq.wurstscript.types;


public class PScriptTypeThis extends PscriptType {

	private static final PScriptTypeThis instance = new PScriptTypeThis();

	private String name = "thistype";

	private Error err;
	
	private PScriptTypeThis() {
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return other instanceof PScriptTypeThis;
	}


	@Override
	public String getName() {
		return "#" + name;
	}

	@Override
	public String getFullName() {
		return "#" + name;
	}

//	public static PScriptTypeThis instance() {
//		return instance;
//	}

	@Override
	public String printJass() {
		throw err;
//		return "unknown<"+name+">";
	}

}
