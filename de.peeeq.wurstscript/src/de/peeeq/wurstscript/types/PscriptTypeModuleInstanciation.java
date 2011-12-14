package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;


public class PscriptTypeModuleInstanciation extends PscriptType {

	private ModuleInstanciation moduleDef;

	public PscriptTypeModuleInstanciation(ModuleInstanciation moduleDef) {
		this.moduleDef = moduleDef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
		if (other instanceof PscriptTypeModuleInstanciation) {
			PscriptTypeModuleInstanciation otherClassType = (PscriptTypeModuleInstanciation) other;
			return otherClassType.moduleDef.equals(moduleDef);
		}
		// TODO implement subtyping
		return false;
	}

	@Override
	public String getName() {
		return moduleDef.getName();
	}

	public ModuleInstanciation getModuleDef() {
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
