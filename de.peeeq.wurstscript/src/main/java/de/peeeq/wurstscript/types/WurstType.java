package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public abstract class WurstType {
    /**
     * @param other
     * @param location
     * @return is this type a subtype (or equal) to other type?
     */
    public final boolean isSubtypeOf(WurstType other, @Nullable Element location) {
        if (other instanceof WurstTypeBoundTypeParam) {
            WurstTypeBoundTypeParam btp = (WurstTypeBoundTypeParam) other;
            return isSubtypeOf(btp.getBaseType(), location);
        }
        if (other instanceof WurstTypeUnion) {
            WurstTypeUnion wtu = (WurstTypeUnion) other;
            return this.isSubtypeOf(wtu.getTypeA(), location)
                    && this.isSubtypeOf(wtu.getTypeB(), location);
        }
        if (other instanceof WurstTypeUnknown) {
            // everything is a subtype of unknown (stops error cascades)
            return true;
        }
        if (other instanceof WurstTypeDeferred) {
            return isSubtypeOf(((WurstTypeDeferred) other).force(), location);
        }
        return this.isSubtypeOfIntern(other, location);
    }

    /**
     * @param other
     * @param location
     * @return is this type a subtype (or equal) to other type?
     */
    public abstract boolean isSubtypeOfIntern(WurstType other, @Nullable Element location);


    /**
     * @param other
     * @return is this type a supertype (or equal) to other type?
     */
    public final boolean isSupertypeOf(WurstType other, Element location) {
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


    public boolean equalsType(WurstType otherType, @Nullable Element location) {
        return otherType.isSubtypeOf(this, location) && this.isSubtypeOf(otherType, location);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @deprecated use {@link #equalsType(WurstType, Element)}
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object other) {
        throw new Error("operation not supported");
    }

    @Deprecated
    @Override
    public int hashCode() {
        throw new Error("Hash code not implemented for types, because it could conflict with the custom equals operation.");
    }


    public WurstType dynamic() {
        return this;
    }


    public WurstType setTypeArgs(Map<TypeParamDef, WurstTypeBoundTypeParam> typeParamMapping) {
        return this;
    }


    public Map<TypeParamDef, WurstTypeBoundTypeParam> getTypeArgBinding() {
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

    public void addMemberMethods(Element node, String name,
                                 List<FuncLink> result) {
    }

    public Stream<NameLink> getMemberMethods(Element node) {
        return Stream.empty();
    }

    public boolean isStaticRef() {
        return false;
    }

    public boolean isTranslatedToInt() {
        return this instanceof WurstTypeInt
                || this instanceof WurstTypeIntLiteral
                || this instanceof WurstTypeNamedScope
                || this instanceof WurstTypeTypeParam
                || this instanceof WurstTypeBoundTypeParam; //  WurstTypeBoundTypeParam overrides this method
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
                || t instanceof WurstTypeIntLiteral;
    }


    public WurstType typeUnion(WurstType t, Element loc) {
        return WurstTypeUnion.create(this, t, loc);
    }

    /**
     * checks if this type is nested inside another type or the same class
     * for example inner classes are nested inside the outer classes
     */
    public boolean isNestedInside(WurstType other) {
        return false;
    }


}
