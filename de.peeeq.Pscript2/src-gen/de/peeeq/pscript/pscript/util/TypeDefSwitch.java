package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class TypeDefSwitch <T> {
	abstract public T caseNativeType(NativeType nativeType);
	abstract public T caseClassDef(ClassDef classDef);
	public T doSwitch(TypeDef typeDef) {
		if (typeDef instanceof NativeType) return caseNativeType((NativeType)typeDef);
		if (typeDef instanceof ClassDef) return caseClassDef((ClassDef)typeDef);
		throw new Error("Switch did not match any case.");
	}
}

