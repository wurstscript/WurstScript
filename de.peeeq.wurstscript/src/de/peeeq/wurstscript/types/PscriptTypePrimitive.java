package de.peeeq.wurstscript.types;

public abstract class PscriptTypePrimitive extends PscriptType {
	
	private String name;
	private String[] jassType;

	protected PscriptTypePrimitive(String name) {
		this.name = name;
		jassType = new String[] { name };
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getFullName() {
		return name;
	}
	
	@Override
	public String[] jassTranslateType() {
		return jassType;
	}
	
}
