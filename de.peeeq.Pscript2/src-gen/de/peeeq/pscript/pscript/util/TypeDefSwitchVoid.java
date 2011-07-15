package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class TypeDefSwitchVoid {
	abstract public void caseNativeType(NativeType nativeType);
	abstract public void caseClassDef(ClassDef classDef);
	public void doSwitch(TypeDef typeDef) {
		if (typeDef instanceof NativeType) { caseNativeType((NativeType)typeDef); return; }
		if (typeDef instanceof ClassDef) { caseClassDef((ClassDef)typeDef); return; }
		throw new Error("Switch did not match any case.");
	}
}

