package de.peeeq.wurstscript.types;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;

public class WurstTypeUnion extends WurstType {

	WurstType typeA;
	WurstType typeB;
	
	private WurstTypeUnion(WurstType typeA, WurstType typeB) {
		this.typeA = typeA;
		this.typeB = typeB;
	}
	
	public static WurstType create(WurstType a, WurstType b) {
		if (a instanceof WurstTypeUnknown) return b;
		if (b instanceof WurstTypeUnknown) return a;
		// TODO simplify types
//		if (a.isSubtypeOf(b, null)) return b;
//		if (b.isSubtypeOf(a, null)) return a;
		return new WurstTypeUnion(a, b);
	}

	@Override
	public boolean isSubtypeOfIntern(WurstType other, @Nullable AstElement location) {
		return typeA.isSubtypeOf(other, location)
				&& typeB.isSubtypeOf(other, location);
	}

	@Override
	public String getName() {
		return typeA.getName() + " & " + typeB.getName();
	}

	@Override
	public String getFullName() {
		return typeA.getFullName() + " & " + typeB.getFullName();
	}

	@Override
	public ImType imTranslateType() {
		return typeA.imTranslateType();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return typeA.getDefaultValue();
	}

	public WurstType getTypeA() {
		return typeA;
	}

	public WurstType getTypeB() {
		return typeB;
	}

}
