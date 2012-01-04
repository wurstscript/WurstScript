package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.TypeParamDef;

public class PscriptTypeTypeParam extends PscriptType {

	private TypeParamDef def;

	public PscriptTypeTypeParam(TypeParamDef t) {
		this.def = t;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
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

	@Override
	public String printJass() {
		return "integer";
	}

	public TypeParamDef  getDef() {
		return def;
	}

}
