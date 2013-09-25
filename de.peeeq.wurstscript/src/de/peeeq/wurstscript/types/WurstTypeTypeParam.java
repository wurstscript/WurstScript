package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.Map;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public class WurstTypeTypeParam extends WurstType {

	private TypeParamDef def;

	public WurstTypeTypeParam(TypeParamDef t) {
		this.def = t;
	}

	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		if (other instanceof WurstTypeTypeParam) {
			WurstTypeTypeParam other2 = (WurstTypeTypeParam) other;
			return other2.def == this.def;
		}
		return false;
	}

	@Override
	public String getName() {
		return def.getName() + " (type parameter)";
	}

	@Override
	public String getFullName() {
		return getName() + " (type parameter)";
	}

	public TypeParamDef  getDef() {
		return def;
	}

	@Override
	public String[] jassTranslateType() {
		return new String[] { "integer", "integer" };
	}
	
	public Map<TypeParamDef, WurstType> getTypeArgBinding() {
		return Collections.emptyMap();
	}

	public WurstType setTypeArgs(Map<TypeParamDef, WurstType> typeParamBounds) {
		if (typeParamBounds.containsKey(def)) {
			WurstType t = typeParamBounds.get(def);
			return new WurstTypeBoundTypeParam(def, t);
		} 
		return this;
	}

	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImNull();
	}
	

	@Override
	public boolean isCastableToInt() {
		return true;
	}
	
}
