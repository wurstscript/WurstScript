package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;

public class ILvar implements ILexpr {
	private String name;
	private PscriptType type;
	
	
	
	public ILvar(String name, PscriptType type) {
		if (type instanceof PScriptTypeArray) {
			PScriptTypeArray arType = (PScriptTypeArray) type;
			if (arType.getBaseType() instanceof PscriptTypeClass) {
				throw new Error("Type error for variable " + name + " and " + type);
			}
		}
		if (type instanceof PscriptTypeClass) {
			throw new Error("Type error for variable " + name + " and " + type);
		}
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PscriptType getType() {
		return type;
	}
	public void setType(PscriptType type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ILvar) {
			ILvar v = (ILvar) obj;
			return v.name.equals(name) ; //&& v.type.equals(type);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return "ILvar(" + type + " " + name + ")";
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append(type.printJass() + " " + name);
	}

	@Override
	public void printJassExpr(StringBuilder sb, int indent) {
		sb.append(name);
	}
}
