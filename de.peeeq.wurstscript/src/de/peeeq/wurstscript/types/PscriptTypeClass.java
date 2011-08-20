package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ClassDefPos;

public class PscriptTypeClass extends PscriptType {

	private ClassDefPos classDef;

	public PscriptTypeClass(ClassDefPos classDef) {
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
		return classDef.name().term();
	}

	public ClassDefPos getClassDef() {
		return classDef;
	}
	
	@Override
	public String getFullName() {
		// TODO fully qualified name
		return getName();
	}

}
