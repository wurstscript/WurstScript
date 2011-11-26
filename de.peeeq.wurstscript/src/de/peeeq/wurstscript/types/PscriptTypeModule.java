package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ModuleDef;


public class PscriptTypeModule extends PscriptType {

	private ModuleDef moduleDef;

	public PscriptTypeModule(ModuleDef moduleDef) {
		this.moduleDef = moduleDef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
		if (other instanceof PscriptTypeModule) {
			PscriptTypeModule otherClassType = (PscriptTypeModule) other;
			return otherClassType.moduleDef.equals(moduleDef);
		}
		// TODO implement subtyping
		return false;
	}

	@Override
	public String getName() {
		return moduleDef.getName();
	}

	public ModuleDef getModuleDef() {
		return moduleDef;
	}
	
	@Override
	public String getFullName() {
		// TODO fully qualified name
		return getName();
	}

	
	@Override
	public String printJass() {
		// classes are just translated to integers:
		return "integer";
	}
}
