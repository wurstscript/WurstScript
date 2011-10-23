package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ClassDef;


public class PscriptTypeClass extends PscriptType {

	private ClassDef classDef;

	public PscriptTypeClass(ClassDef classDef) {
		this.classDef = classDef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
		if (other instanceof PscriptTypeClass) {
			PscriptTypeClass otherClassType = (PscriptTypeClass) other;
			return otherClassType.classDef.equals(classDef);
		}
		// TODO implement subtyping
		return false;
	}

	@Override
	public String getName() {
		return classDef.getName();
	}

	public ClassDef getClassDef() {
		return classDef;
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
