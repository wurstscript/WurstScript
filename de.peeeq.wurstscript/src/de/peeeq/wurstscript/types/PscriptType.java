package de.peeeq.wurstscript.types;


public abstract class PscriptType {
	/**
	 * @param other
	 * @return is this type a subtype (or equal) to other type?
	 */
	public abstract boolean isSubtypeOf(PscriptType other);
	
	
	/**
	 * @param other
	 * @return is this type a supertype (or equal) to other type?
	 */
	public final boolean isSupertypeOf(PscriptType other) {
		return other.isSubtypeOf(this);
	}
	
	/**
	 * @return the name of the type
	 */
	public abstract String getName();
	
	/**
	 * @return the fully qualified name of the type
	 */
	public abstract String getFullName();
	
	
	public boolean equalsType(PscriptType otherType) {
		return otherType.isSubtypeOf(this) && this.isSubtypeOf(otherType);
	}
	
	@Override public String toString() {
		return getName();
	}
	
	@Override public boolean equals(Object other) {
		if (other instanceof PscriptType) {
			PscriptType otherType = (PscriptType) other;
			return equalsType(otherType);			
		} else {
			return false;
		}
	}
	
	@Deprecated
	@Override public int hashCode() {
		throw new Error("Hash code not implemented for types, because it could conflict with the custom equals operation.");
	}
}
