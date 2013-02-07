package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public class WurstTypeBoundTypeParam extends WurstType {

	
	private TypeParamDef typeParamDef;
	private WurstType baseType;

	public WurstTypeBoundTypeParam(TypeParamDef def, WurstType baseType) {
		this.typeParamDef = def;
		this.baseType = baseType;
	}

	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		return baseType.isSubtypeOfIntern(other, location);
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

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}

	
	@Override
	public WurstType dynamic() {
		return baseType.dynamic();
	}
}
