package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeClass extends PscriptTypeNamedScope {

	ClassDef classDef;

	public PscriptTypeClass(ClassDef classDef, boolean staticRef) {
		super(staticRef);
		this.classDef = classDef;
	}

	public PscriptTypeClass(ClassDef classDef2, List<PscriptType> newTypes) {
		super(newTypes);
		this.classDef = classDef2;
	}

	@Override
	public NamedScope getDef() {
		return classDef;
	}

	public ClassDef getClassDef() {
		return classDef;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + printTypeParams() + " (class)";
	}
	
	@Override
	public PscriptType dynamic() {
		if (isStaticRef()) {
			return new PscriptTypeClass(getClassDef(), false);
		}
		return this;
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeClass(classDef, newTypes);
	}
	
}
