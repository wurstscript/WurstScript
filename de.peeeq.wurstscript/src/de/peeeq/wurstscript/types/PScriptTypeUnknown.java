package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeUnknown extends PscriptType {

	private static final PScriptTypeUnknown instance = new PScriptTypeUnknown("unknown");

	private String name = "unknown";

	private Error err;
	
	public PScriptTypeUnknown(String name) {
		this.name = name;
		try {
			throw new Error("unknown type");
		} catch (Error e) {
			this.err = e; // store for later
		}
	}

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return false;
	}


	@Override
	public String getName() {
		return "#" + name;
	}

	@Override
	public String getFullName() {
		return "#" + name;
	}

	public static PScriptTypeUnknown instance() {
		return instance;
	}

	@Override
	public String printJass() {
		throw err;
//		return "unknown<"+name+">";
	}

}
