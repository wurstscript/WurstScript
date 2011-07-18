package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ClassMemberSwitch <T> {
	abstract public T caseFuncDef(FuncDef funcDef);
	abstract public T caseVarDef(VarDef varDef);
	public T doSwitch(ClassMember classMember) {
if ( classMember == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (classMember instanceof FuncDef) return caseFuncDef((FuncDef)classMember);
		if (classMember instanceof VarDef) return caseVarDef((VarDef)classMember);
		throw new Error("Switch did not match any case: " + classMember);
	}
}

