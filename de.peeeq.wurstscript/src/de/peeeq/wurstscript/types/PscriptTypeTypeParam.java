package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.Map;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;

public class PscriptTypeTypeParam extends PscriptType {

	private TypeParamDef def;

	public PscriptTypeTypeParam(TypeParamDef t) {
		this.def = t;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		if (other instanceof PscriptTypeBoundTypeParam) {
			PscriptTypeBoundTypeParam b = (PscriptTypeBoundTypeParam) other;
			return isSubtypeOf(b.getBaseType(), location);
		}
		
		if (other instanceof PscriptTypeTypeParam) {
			PscriptTypeTypeParam other2 = (PscriptTypeTypeParam) other;
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
	
	public Map<TypeParamDef, PscriptType> getTypeArgBinding() {
		return Collections.emptyMap();
	}

	public PscriptType setTypeArgs(Map<TypeParamDef, PscriptType> typeParamBounds) {
		if (typeParamBounds.containsKey(def)) {
			PscriptType t = typeParamBounds.get(def);
			return new PscriptTypeBoundTypeParam(def, t);
		} 
		return this;
	}

}
