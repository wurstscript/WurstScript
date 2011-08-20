package de.peeeq.wurstscript.types;


public class PScriptTypePackage extends PscriptType {

	private String name;

	// make constructor private as we only need one instance
	private PScriptTypePackage(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return (other instanceof PScriptTypePackage && other.getFullName().equals(getFullName()));	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFullName() {
		return name;
	}

	public static PScriptTypePackage instance(String name) {
		return new PScriptTypePackage(name);
	}

}
