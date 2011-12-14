package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeClass extends PscriptTypeNamedScope {

	ClassDef classDef;

	public PscriptTypeClass(ClassDef classDef, boolean staticRef) {
		super(staticRef);
		this.classDef = classDef;
	}

	@Override
	public NamedScope getDef() {
		return classDef;
	}

	public ClassDef getClassDef() {
		return classDef;
	}
}
