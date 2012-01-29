package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;

public class PscriptTypeBoundTypeParam extends PscriptType {

	
	private TypeParamDef typeParamDef;
	private PscriptType baseType;

	public PscriptTypeBoundTypeParam(TypeParamDef def, PscriptType baseType) {
		this.typeParamDef = def;
		this.baseType = baseType;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
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

	public PscriptType getBaseType() {
		return baseType;
	}

}
