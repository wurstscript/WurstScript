package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeVoid extends PscriptType {

	private static final PScriptTypeVoid instance = new PScriptTypeVoid();

	// make constructor private as we only need one instance
	private PScriptTypeVoid() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeVoid;
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

	@Override
	public String[] jassTranslateType() {
		return new String[] { "nothing" };
	}

}
