package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.TypeDef;
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
	public final boolean isSubtypeOf(WurstType other, @Nullable AstElement location) {
		if (other instanceof WurstTypeBoundTypeParam) {
			WurstTypeBoundTypeParam btp = (WurstTypeBoundTypeParam) other;
			return isSubtypeOf(btp.getBaseType(), location);
		}
		if (other instanceof WurstTypeUnion) {
			WurstTypeUnion wtu = (WurstTypeUnion) other;
			return this.isSubtypeOf(wtu.getTypeA(), location)
					&& this.isSubtypeOf(wtu.getTypeB(), location);
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
	public abstract boolean isSubtypeOfIntern(WurstType other, @Nullable AstElement location);
	
	
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
	
	
	public boolean equalsType(WurstType otherType, @Nullable AstElement location) {
		return otherType.isSubtypeOf(this, location) && this.isSubtypeOf(otherType, location);
	}
	
	@Override public String toString() {
		return getName();
	}
	/**
	 * @deprecated  use {@link #equalsType(WurstType, AstElement)}
	 */
	@Deprecated
	@Override public boolean equals(@Nullable Object other) {
		throw new Error("operation not supported");
	}
	
	@Deprecated
	@Override public int hashCode() {
		throw new Error("Hash code not implemented for types, because it could conflict with the custom equals operation.");
	}
	
	

	public WurstType dynamic() {
		return this;
	}



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
	
	public boolean isTranslatedToInt() {
		return this instanceof WurstTypeInt
				|| this instanceof WurstTypeIntLiteral
				|| this instanceof WurstTypeNamedScope
				|| this instanceof WurstTypeTypeParam
				|| this instanceof WurstTypeFreeTypeParam
				|| this instanceof WurstTypeBoundTypeParam;
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
	
	public boolean supportsGenerics() {
		WurstType t = this.normalize();
		return t instanceof WurstTypeNamedScope 
				|| t instanceof WurstTypeNull
				|| t instanceof WurstTypeInt
				|| t instanceof WurstTypeTypeParam
				|| t instanceof WurstTypeFreeTypeParam
				|| t instanceof WurstTypeIntLiteral;
	}

	public TypeDef tryGetTypeDef() {
		throw new Error("not implemented");
	}

	public WurstType typeUnion(WurstType t) {
		return WurstTypeUnion.create(this, t);
	}

	/**
	 * checks if this type is nested inside another type or the same class
	 * for example inner classes are nested inside the outer classes 
	 */
	public boolean isNestedInside(WurstType other) {
		return false;
	}


}
