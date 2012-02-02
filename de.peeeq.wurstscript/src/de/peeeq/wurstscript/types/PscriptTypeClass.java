package de.peeeq.wurstscript.types;

import java.util.Collection;
import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeClass extends PscriptTypeNamedScope {

	ClassDef classDef;


	public PscriptTypeClass(ClassDef classDef, List<PscriptType> typeParameters, boolean staticRef) {
		super(typeParameters, staticRef);
		if (classDef == null) throw new IllegalArgumentException();
		this.classDef = classDef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType obj, AstElement location) {
		if (super.isSubtypeOf(obj, location)) {
			return true;
		}
		if (obj instanceof PscriptTypeInterface) {
			PscriptTypeInterface pti = (PscriptTypeInterface) obj;
			for (PscriptTypeInterface implementedInterface : classDef.attrImplementedInterfaces()) {
				if (implementedInterface.setTypeArgs(getTypeArgBinding()).isSubtypeOf(pti, location)) {
					return true;
				}
			}
			return false;
		}
		return false;
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
		return new PscriptTypeClass(getClassDef(), getTypeParameters(), false);
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeClass(classDef, newTypes, isStaticRef());
	}

	@Override
	public String[] jassTranslateType() {
		return PScriptTypeInt.instance().jassTranslateType();
	}
	
}
