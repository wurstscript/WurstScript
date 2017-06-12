package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;

public class WurstTypeStaticTypeRef extends WurstType {

	private final WurstType base;
	
	public WurstTypeStaticTypeRef(WurstType base) {
		this.base = base;
	}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, Element location) {
		return false;
	}

	@Override
	public String getName() {
		return base.getName();
	}

	@Override
	public String getFullName() {
		return "static reference to " + base.getFullName();
	}

	@Override
	public ImType imTranslateType() {
		return base.imTranslateType();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return base.getDefaultValue();
	}
	
	@Override
	public WurstType dynamic() {
		return base;
	}

}
