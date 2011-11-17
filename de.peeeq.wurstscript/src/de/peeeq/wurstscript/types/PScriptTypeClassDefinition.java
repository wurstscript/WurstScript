package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ClassDef;


public class PScriptTypeClassDefinition extends PscriptType {

	private ClassDef classDef;

	// make constructor private as we only need one instance
	private PScriptTypeClassDefinition(ClassDef classDef) {
		this.classDef = classDef;
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return (other instanceof PScriptTypeClassDefinition && other.getFullName().equals(getFullName()));	}

	@Override
	public String getName() {
		return "class definition of " + classDef.getName();
	}

	@Override
	public String getFullName() {
		return getName();
	}

	public static PScriptTypeClassDefinition instance(ClassDef classDef) {
		return new PScriptTypeClassDefinition(classDef);
	}

	@Override
	public String printJass() {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	public ClassDef getClassDef() {
		return classDef;
	}

}
