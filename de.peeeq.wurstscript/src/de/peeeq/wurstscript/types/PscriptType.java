package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.Map;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;


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
	/**
	 * @deprecated  use {@link #equalsType(PscriptType, AstElement)}
	 */
	@Deprecated
	@Override public boolean equals(Object other) {
		throw new Error("operation not supported");
	}
	
	@Deprecated
	@Override public int hashCode() {
		throw new Error("Hash code not implemented for types, because it could conflict with the custom equals operation.");
	}
	
	

	public PscriptType dynamic() {
		return this;
	}



	public abstract String[] jassTranslateType();


	public PscriptType setTypeArgs(Map<TypeParamDef, PscriptType> typeParamMapping) {
		return this;
	}


	public Map<TypeParamDef, PscriptType> getTypeArgBinding() {
		return Collections.emptyMap();
	}
}
