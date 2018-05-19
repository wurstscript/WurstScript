package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import fj.data.Option;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


public abstract class WurstType {
    /**
     * @param other
     * @param location
     * @return is this type a subtype (or equal) to other type?
     */
    public final boolean isSubtypeOf(WurstType other, @Nullable Element location) {
        return matchAgainstSupertype(other, location, Collections.emptySet(), emptyMapping()) != null;
    }

    @NotNull
    public static TreeMap<TypeParamDef, WurstTypeBoundTypeParam> emptyMapping() {
        return TreeMap.empty(TypeParamOrd.instance());
    }

//    public final TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertype(WurstType other, @Nullable Element location) {
//        return this.matchAgainstSupertype(other, location, TreeMap.empty(TypeParamOrd.instance()));
//    }

    /**
     * Matches this type against another type.
     * <p>
     * Will try to instantiate type variables from the set typeParams
     */
    public final @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertype(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        if (other instanceof WurstTypeBoundTypeParam) {
            WurstTypeBoundTypeParam btp = (WurstTypeBoundTypeParam) other;
            return matchAgainstSupertype(btp.getBaseType(), location, typeParams, mapping);
        } else if (other instanceof WurstTypeUnion) {
            WurstTypeUnion wtu = (WurstTypeUnion) other;
            mapping = matchAgainstSupertype(wtu.getTypeA(), location, typeParams, mapping);
            if (mapping == null) {
                return null;
            }
            return matchAgainstSupertype(wtu.getTypeB(), location, typeParams, mapping);
        } else if (other instanceof WurstTypeUnknown) {
            // everything is a subtype of unknown (stops error cascades)
            return mapping;
        } else if (other instanceof WurstTypeDeferred) {
            return matchAgainstSupertype(((WurstTypeDeferred) other).force(), location, typeParams, mapping);
        } else if (other instanceof WurstTypeTypeParam) {
            WurstTypeTypeParam tp = (WurstTypeTypeParam) other;
            Option<WurstTypeBoundTypeParam> bound = mapping.get(tp.getDef());
            if (bound.isSome()) {
                // already bound, use current bound
                return matchAgainstSupertypeIntern(bound.some(), location, typeParams, mapping);
            } else if (typeParams.contains(tp.getDef())) {
                // match this type parameter
                return mapping.set(tp.getDef(), new WurstTypeBoundTypeParam(tp.getDef(), this, location));
            }

        }
        return this.matchAgainstSupertypeIntern(other, location, typeParams, mapping);
    }


    /**
     * @param other
     * @param location
     * @return is this type a subtype (or equal) to other type?
     */
    abstract @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping);


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
        return matchTypes(otherType, location, Collections.emptySet(), emptyMapping()) != null;
    }

    /**
     * Bidirectional matching of types
     */
    public TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchTypes(WurstType otherType, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        mapping = this.matchAgainstSupertype(otherType, location, typeParams, mapping);
        if (mapping == null) {
            return null;
        }
        return otherType.matchAgainstSupertype(this, location, typeParams, mapping);
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


    public WurstType setTypeArgs(TreeMap<TypeParamDef, WurstTypeBoundTypeParam> typeParamMapping) {
        return this;
    }


    public TreeMap<TypeParamDef, WurstTypeBoundTypeParam> getTypeArgBinding() {
        return WurstType.emptyMapping();
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
