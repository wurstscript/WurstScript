package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ClassSlotsSwitchVoid {
	abstract public void caseClassMember(ClassMember classMember);
	abstract public void caseOnDestroyDef(OnDestroyDef onDestroyDef);
	abstract public void caseConstructorDef(ConstructorDef constructorDef);
	public void doSwitch(ClassSlots classSlots) {
if ( classSlots == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (classSlots instanceof ClassMember) { caseClassMember((ClassMember)classSlots); return; }
		if (classSlots instanceof OnDestroyDef) { caseOnDestroyDef((OnDestroyDef)classSlots); return; }
		if (classSlots instanceof ConstructorDef) { caseConstructorDef((ConstructorDef)classSlots); return; }
		throw new Error("Switch did not match any case: " + classSlots);
	}
}

