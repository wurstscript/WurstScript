package de.peeeq.parseq.ast;

public final class Parameter {

	public final String typ;
	public final  String name;
	public final boolean isRef;

	public Parameter(boolean isRef, String typ, String name) {
		this.isRef = isRef;
		this.typ = typ;
		this.name = name;
	}
	
	public Parameter(String typ, String name) {
		this.isRef = false;
		this.typ = typ;
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Parameter) {
			Parameter parameter = (Parameter) obj;
			return typ.equals(parameter.typ)
					&& name.equals(parameter.name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() ^ typ.hashCode();
	}
	
	@Override
	public String toString() {
		return typ + " " + name;
	}

}
