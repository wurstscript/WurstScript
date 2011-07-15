package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ClassMemberSwitchVoid {
	abstract public void caseFuncDef(FuncDef funcDef);
	abstract public void caseVarDef(VarDef varDef);
	public void doSwitch(ClassMember classMember) {
		if (classMember instanceof FuncDef) { caseFuncDef((FuncDef)classMember); return; }
		if (classMember instanceof VarDef) { caseVarDef((VarDef)classMember); return; }
		throw new Error("Switch did not match any case.");
	}
}

