package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.NameLink;
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


	public WurstType getBaseType() {
		return baseType;
	}

	@Override
	public ImType imTranslateType() {
		return baseType.imTranslateType();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}

	
	@Override
	public WurstType dynamic() {
		return baseType.dynamic();
	}
	
	@Override
	public boolean canBeUsedInInstanceOf() {
		return baseType.canBeUsedInInstanceOf();
	}
	
	@Override
	public boolean allowsDynamicDispatch() {
		return baseType.allowsDynamicDispatch();
	}
	
	@Override
	public void addMemberMethods(AstElement node, String name,
			List<NameLink> result) {
		baseType.addMemberMethods(node, name, result);
	}
	
	@Override
	public boolean isStaticRef() {
		return baseType.isStaticRef();
	}
	
	@Override
	public boolean isCastableToInt() {
		return true; // because baseType must always be castable to int 
		//return baseType.isCastableToInt();
	}
	
	@Override
	public WurstType normalize() {
		return baseType.normalize();
	}
}
