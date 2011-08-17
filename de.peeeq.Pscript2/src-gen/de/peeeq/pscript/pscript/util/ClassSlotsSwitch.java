package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ClassSlotsSwitch <T> {
	abstract public T caseOnDestroyDef(OnDestroyDef onDestroyDef);
	abstract public T caseConstructorDef(ConstructorDef constructorDef);
	abstract public T caseClassMember(ClassMember classMember);
	public T doSwitch(ClassSlots classSlots) {
if ( classSlots == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (classSlots instanceof OnDestroyDef) return caseOnDestroyDef((OnDestroyDef)classSlots);
		if (classSlots instanceof ConstructorDef) return caseConstructorDef((ConstructorDef)classSlots);
		if (classSlots instanceof ClassMember) return caseClassMember((ClassMember)classSlots);
		throw new Error("Switch did not match any case: " + classSlots);
	}
}

