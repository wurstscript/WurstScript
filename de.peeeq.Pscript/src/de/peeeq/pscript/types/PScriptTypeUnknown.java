package de.peeeq.pscript.types;

import de.peeeq.pscript.analysis.Definition;

public class PScriptTypeUnknown extends PscriptType {

	private static final Definition instance = new PScriptTypeUnknown("unknown");

	private String name = "unknown";
	
	public PScriptTypeUnknown(String name) {
		this.name = name;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return false;
	}

	@Override
	public boolean isSupertypeOf(PscriptType other) {
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

	public static Definition instance() {
		return instance;
	}

}
