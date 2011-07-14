package de.peeeq.pscript.types;


public class PScriptTypeUnknown extends PscriptType {

	private static final PScriptTypeUnknown instance = new PScriptTypeUnknown("unknown");

	private String name = "unknown";
	
	public PScriptTypeUnknown(String name) {
		this.name = name;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
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

}
