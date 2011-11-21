package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ModuleDef;


public class PScriptTypeModuleDefinition extends PscriptType {


	private ModuleDef moduleDef;

	// make constructor private as we only need one instance
	private PScriptTypeModuleDefinition(ModuleDef moduleDef) {
		this.moduleDef = moduleDef;
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return (other instanceof PScriptTypeModuleDefinition && other.getFullName().equals(getFullName()));	}

	@Override
	public String getName() {
		return "module definition of " + moduleDef.getName();
	}

	@Override
	public String getFullName() {
		return getName();
	}

	public static PScriptTypeModuleDefinition instance(ModuleDef m) {
		return new PScriptTypeModuleDefinition(m);
	}

	@Override
	public String printJass() {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	public ModuleDef getModuleDef() {
		return moduleDef;
	}

}
