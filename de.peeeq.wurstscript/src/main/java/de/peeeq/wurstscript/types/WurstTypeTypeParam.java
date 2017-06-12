package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.Map;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.ImplicitFuncs;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public class WurstTypeTypeParam extends WurstType {

	private TypeParamDef def;

	public WurstTypeTypeParam(TypeParamDef t) {
		this.def = t;
	}

	@Override
	public boolean isSubtypeOfIntern(WurstType other, Element location) {
		if (other instanceof WurstTypeTypeParam) {
			WurstTypeTypeParam other2 = (WurstTypeTypeParam) other;
			return other2.def == this.def;
		}
		return false;
	}

	@Override
	public String getName() {
		return def.getName();
	}

	@Override
	public String getFullName() {
		return getName() + " (type parameter)";
	}

	public TypeParamDef  getDef() {
		return def;
	}

	@Override
	public Map<TypeParamDef, WurstTypeBoundTypeParam> getTypeArgBinding() {
		return Collections.emptyMap();
	}

	@Override
	public WurstType setTypeArgs(Map<TypeParamDef, WurstTypeBoundTypeParam> typeParamBounds) {
		if (typeParamBounds.containsKey(def)) {
			return typeParamBounds.get(def);
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
