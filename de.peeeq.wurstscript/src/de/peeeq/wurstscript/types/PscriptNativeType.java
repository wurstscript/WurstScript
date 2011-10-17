package de.peeeq.wurstscript.types;

public class PscriptNativeType extends PscriptType {

	private String name;
	private PscriptType superType;

	private PscriptNativeType() {
	}
	
	@Override
	public boolean isSubtypeOf(PscriptType other) {
		if (other instanceof PscriptNativeType) {
			return ((PscriptNativeType)other).name.equals(name)
				|| superType.isSubtypeOf(other);
		}
		return superType.isSubtypeOf(other);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFullName() {
		return name;
	}

	public static PscriptNativeType instance(String name, PscriptType superType) {
		PscriptNativeType t = new PscriptNativeType();
		t.name = name;
		t.superType = superType;
		return t;
	}

	@Override
	public String printJass() {
		return name;
	}

}
