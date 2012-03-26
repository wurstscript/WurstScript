package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public abstract class PscriptTypePrimitive extends PscriptType {
	
	private String name;
	private String[] jassType;
	private ImSimpleType imType;

	protected PscriptTypePrimitive(String name) {
		this.name = name;
		jassType = new String[] { name };
		imType = JassIm.ImSimpleType(name);
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
	
	@Override
	public ImSimpleType imTranslateType() {
		return imType;
	}
	
}
