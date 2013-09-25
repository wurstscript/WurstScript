package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;


public abstract class WurstType {
	/**
	 * @param other
	 * @param location 
	 * @return is this type a subtype (or equal) to other type?
	 */
	public final boolean isSubtypeOf(WurstType other, AstElement location) {
		if (other instanceof WurstTypeBoundTypeParam) {
			WurstTypeBoundTypeParam btp = (WurstTypeBoundTypeParam) other;
			return isSubtypeOf(btp.getBaseType(), location);
		}
		if (this.isSubtypeOfIntern(other, location)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param other
	 * @param location 
	 * @return is this type a subtype (or equal) to other type?
	 */
	public abstract boolean isSubtypeOfIntern(WurstType other, AstElement location);
	
	
	/**
	 * @param other
	 * @return is this type a supertype (or equal) to other type?
	 */
	public final boolean isSupertypeOf(WurstType other, AstElement location) {
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
	
	
	public boolean equalsType(WurstType otherType, AstElement location) {
		return otherType.isSubtypeOf(this, location) && this.isSubtypeOf(otherType, location);
	}
	
	@Override public String toString() {
		return getName();
	}
	/**
	 * @deprecated  use {@link #equalsType(WurstType, AstElement)}
	 */
	@Deprecated
	@Override public boolean equals(Object other) {
		throw new Error("operation not supported");
	}
	
	@Deprecated
	@Override public int hashCode() {
		throw new Error("Hash code not implemented for types, because it could conflict with the custom equals operation.");
	}
	
	

	public WurstType dynamic() {
		return this;
	}



	public abstract String[] jassTranslateType();


	public WurstType setTypeArgs(Map<TypeParamDef, WurstType> typeParamMapping) {
		return this;
	}


	public Map<TypeParamDef, WurstType> getTypeArgBinding() {
		return Collections.emptyMap();
	}


	public abstract ImType imTranslateType();


	public abstract ImExprOpt getDefaultValue();

	public boolean isVoid() {
		return false;
	}

	public boolean canBeUsedInInstanceOf() {
		return false;
	}

	public boolean allowsDynamicDispatch() {
		return false;
	}

	public void addMemberMethods(AstElement node, String name,
			List<NameLink> result) {
	}

	public boolean isStaticRef() {
		return false;
	}

	public boolean isCastableToInt() {
		return this instanceof WurstTypeClass 
				|| this instanceof WurstTypeModule
				|| this instanceof WurstTypeClassOrInterface
				|| this instanceof WurstTypeTypeParam
				|| this instanceof WurstTypeBoundTypeParam
				|| this instanceof WurstTypeEnum;
	}

	public WurstType normalize() {
		return this;
	}


}
