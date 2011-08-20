package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ClassMemberSwitch <T> {
	abstract public T caseVarDef(VarDef varDef);
	abstract public T caseFuncDef(FuncDef funcDef);
	public T doSwitch(ClassMember classMember) {
if ( classMember == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (classMember instanceof VarDef) return caseVarDef((VarDef)classMember);
		if (classMember instanceof FuncDef) return caseFuncDef((FuncDef)classMember);
		throw new Error("Switch did not match any case: " + classMember);
	}
}

