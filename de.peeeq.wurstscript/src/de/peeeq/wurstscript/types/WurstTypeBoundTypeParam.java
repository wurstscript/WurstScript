package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImType;

public class WurstTypeBoundTypeParam extends WurstType {

	
	private TypeParamDef typeParamDef;
	private WurstType baseType;

	public WurstTypeBoundTypeParam(TypeParamDef def, WurstType baseType) {
		this.typeParamDef = def;
		this.baseType = baseType;
	}

	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		return baseType.isSubtypeOf(other, location);
	}

	@Override
	public String getName() {
		return typeParamDef.getName() + "<--" + baseType.getName();
	}

	@Override
	public String getFullName() {
		return typeParamDef.getName() + "<--" + baseType.getName();
	}

	@Override
	public String[] jassTranslateType() {
		return new String[] { "integer", "integer" };
	}

	public WurstType getBaseType() {
		return baseType;
	}

	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

}
