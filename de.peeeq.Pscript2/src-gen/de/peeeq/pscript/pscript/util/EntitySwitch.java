package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class EntitySwitch <T> {
	abstract public T caseInitBlock(InitBlock initBlock);
	abstract public T caseFuncDef(FuncDef funcDef);
	abstract public T caseVarDef(VarDef varDef);
	abstract public T caseTypeDef(TypeDef typeDef);
	public T doSwitch(Entity entity) {
if ( entity == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (entity instanceof InitBlock) return caseInitBlock((InitBlock)entity);
		if (entity instanceof FuncDef) return caseFuncDef((FuncDef)entity);
		if (entity instanceof VarDef) return caseVarDef((VarDef)entity);
		if (entity instanceof TypeDef) return caseTypeDef((TypeDef)entity);
		throw new Error("Switch did not match any case: " + entity);
	}
}

