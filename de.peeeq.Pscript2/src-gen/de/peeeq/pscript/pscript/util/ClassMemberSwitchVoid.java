package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class ClassMemberSwitchVoid {
	abstract public void caseVarDef(VarDef varDef);
	abstract public void caseFuncDef(FuncDef funcDef);
	public void doSwitch(ClassMember classMember) {
		if (classMember instanceof VarDef) { caseVarDef((VarDef)classMember); return; }
		if (classMember instanceof FuncDef) { caseFuncDef((FuncDef)classMember); return; }
		throw new Error("Switch did not match any case.");
	}
}

