package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public class WurstNativeType extends WurstType {

	private String name;
	private WurstType superType;

	private WurstNativeType() {
	}
	
	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		if (other instanceof WurstNativeType) {
			return ((WurstNativeType)other).name.equals(name)
				|| superType.isSubtypeOf(other, location);
		}
		return superType.isSubtypeOf(other, location);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFullName() {
		return name;
	}

	public static WurstNativeType instance(String name, WurstType superType) {
		WurstNativeType t = new WurstNativeType();
		t.name = name;
		t.superType = superType;
		return t;
	}

	@Override
	public String[] jassTranslateType() {
		return new String[] {name};
	}

	@Override
	public ImType imTranslateType() {
		return JassIm.ImSimpleType(name);
	}

}
