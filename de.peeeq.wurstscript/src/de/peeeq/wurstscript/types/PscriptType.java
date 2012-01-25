package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public abstract class PscriptType {
	/**
	 * @param other
	 * @param location 
	 * @return is this type a subtype (or equal) to other type?
	 */
	public abstract boolean isSubtypeOf(PscriptType other, AstElement location);
	
	
	/**
	 * @param other
	 * @return is this type a supertype (or equal) to other type?
	 */
	public final boolean isSupertypeOf(PscriptType other, AstElement location) {
		return other.isSubtypeOf(this, location);
	}
	
	/**
	 * @return the name of the type
	 */
	public abstract String getName();
	
	/**
	 * @return the fully qualified name of the type
	 */
	public abstract String getFullName();
	
	
	public boolean equalsType(PscriptType otherType, AstElement location) {
		return otherType.isSubtypeOf(this, location) && this.isSubtypeOf(otherType, location);
	}
	
	@Override public String toString() {
		return getName();
	}
	
	@Override public boolean equals(Object other) {
		if (other instanceof PscriptType) {
			PscriptType otherType = (PscriptType) other;
			return equalsType(otherType, null);			
		} else {
			return false;
		}
	}
	
	@Deprecated
	@Override public int hashCode() {
		throw new Error("Hash code not implemented for types, because it could conflict with the custom equals operation.");
	}
	
	

	public PscriptType dynamic() {
		return this;
	}


	/**
	 * replaces all type parameters in t with the actual type
	 */
	public  PscriptType replaceBoundTypeVars(PscriptType t) {
		return t;
	}


	public abstract String[] jassTranslateType();
}
