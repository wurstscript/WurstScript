package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class TypeDefSwitchVoid {
	abstract public void caseClassDef(ClassDef classDef);
	abstract public void caseNativeType(NativeType nativeType);
	public void doSwitch(TypeDef typeDef) {
		if (typeDef instanceof ClassDef) { caseClassDef((ClassDef)typeDef); return; }
		if (typeDef instanceof NativeType) { caseNativeType((NativeType)typeDef); return; }
		throw new Error("Switch did not match any case.");
	}
}

