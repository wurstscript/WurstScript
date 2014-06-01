package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.JassIm;

public abstract class WurstTypePrimitive extends WurstType {
	
	private String name;
	private ImSimpleType imType;

	protected WurstTypePrimitive(String name) {
		this.name = name;
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
	public ImSimpleType imTranslateType() {
		return imType;
	}
	
}
