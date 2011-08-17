package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class TypeDefSwitch <T> {
	abstract public T caseClassDef(ClassDef classDef);
	abstract public T caseNativeType(NativeType nativeType);
	public T doSwitch(TypeDef typeDef) {
if ( typeDef == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (typeDef instanceof ClassDef) return caseClassDef((ClassDef)typeDef);
		if (typeDef instanceof NativeType) return caseNativeType((NativeType)typeDef);
		throw new Error("Switch did not match any case: " + typeDef);
	}
}

