package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImArrayTypeMulti;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Option;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static de.peeeq.wurstscript.types.VariablePosition.NONE;
import static de.peeeq.wurstscript.types.VariablePosition.RIGHT;

public abstract class WurstType {

    /**
     * @param other
     * @param location
     * @return is this type a subtype (or equal) to other type?
     */
    public final boolean isSubtypeOf(WurstType other, @Nullable Element location) {
        return matchAgainstSupertype(other, location, VariableBinding.emptyMapping(), RIGHT) != null;
    }

    //    public final VariableBinding matchAgainstSupertype(WurstType other, @Nullable Element location) {
//        return this.matchAgainstSupertype(other, location, TreeMap.empty(TypeParamOrd.instance()));
//    }


    /**
     * Matches this type against another type.
     * <p>
     * Will try to instantiate type variables from the set typeParams
     */
    public final @Nullable VariableBinding matchAgainstSupertype(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstTypeUnknown || this instanceof WurstTypeUnknown) {
            // everything is a subtype of unknown (stops error cascades)
            return mapping;
        } else if (other instanceof WurstTypeInfer || this instanceof WurstTypeInfer) {
            // assume everything can match a type that is inferred later
            return mapping;
        } else if (other instanceof WurstTypeBoundTypeParam) {
            WurstTypeBoundTypeParam btp = (WurstTypeBoundTypeParam) other;
            return matchAgainstSupertype(btp.getBaseType(), location, mapping, NONE);
        } else if (other instanceof WurstTypeUnion) {
            WurstTypeUnion wtu = (WurstTypeUnion) other;
            mapping = matchAgainstSupertype(wtu.getTypeA(), location, mapping, variablePosition);
            if (mapping == null) {
                return null;
            }
            return matchAgainstSupertype(wtu.getTypeB(), location, mapping, variablePosition);
        } else if (other instanceof WurstTypeTypeParam) {
            WurstTypeTypeParam tp = (WurstTypeTypeParam) other;

            if (variablePosition == RIGHT) {
                Option<WurstTypeBoundTypeParam> bound = mapping.get(tp.getDef());
                if (bound.isDefined()) {
                    // already bound, use current bound
                    return matchAgainstSupertype(bound.get(), location, mapping, variablePosition);
                } else if (mapping.isVar(tp.getDef())) {
                    // match this type parameter
                    return mapping.set(tp.getDef(), new WurstTypeBoundTypeParam(tp.getDef(), this, location));
                }
            }

            if (this instanceof WurstTypeTypeParam) {
                WurstTypeTypeParam this2 = (WurstTypeTypeParam) this;
                if (this2.getDef() == tp.getDef()) {
                    // same type variable --> match without binding
                    return mapping;
                }
            }
        }
        return this.matchAgainstSupertypeIntern(other, location, mapping, variablePosition);
    }


    /**
     * Matches a type against a supertype .
     * Both sides can include type parameters, but only the type parameters given in typeParams are matched.
     * <p>
     * returns the type mapping required to make the types equal and null if they do not match.
     * <p>
     * Simple examples:
     * int match: int --> {} (the empty mapping}
     * int match: string --> null
     * ⋀T. A[int] match: A[T] --> {T -> int}
     * ⋀T. A[T] match A[int] --> {T -> int}
     * <p>
     * Nonlinear patterns are also possible (matches from left to right):
     * ⋀T. A[T, T] match A[int, int] --> {T -> int}
     * <p>
     * The complicated case is when we have subclasses:
     * e.g. conider two classes
     * class A[S,T]
     * class B[X,Y] extends A[X,List[X]]
     * <p>
     * B[int,string] match A[int, List[int]] --> {}
     * ⋀X Y. B[int,string] match A[X, Y] --> {X -> int, Y -> List[int]}
     * <p>
     * The given mapping are already mapped type parameters.
     */
    abstract VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition);


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
        return matchTypes(otherType, location, VariableBinding.emptyMapping(), RIGHT) != null;
    }

    /**
     * Bidirectional matching of types
     */
    public VariableBinding matchTypes(WurstType otherType, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        mapping = this.matchAgainstSupertype(otherType, location, mapping, variablePosition);
        if (mapping == null) {
            return null;
        }
        return otherType.matchAgainstSupertype(this, location, mapping, variablePosition.inverse());
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


    public WurstType setTypeArgs(VariableBinding typeParamMapping) {
        return this;
    }


    public VariableBinding getTypeArgBinding() {
        return VariableBinding.emptyMapping();
    }


    public abstract ImType imTranslateType(ImTranslator tr);


    public abstract ImExprOpt getDefaultValue(ImTranslator tr);

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

    public Stream<FuncLink> getMemberMethods(Element node) {
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
                || this instanceof WurstTypeNull
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


    protected abstract boolean isNullable();

    public boolean isArray() {
        return this instanceof WurstTypeArray;
    }

    /**
     * Prints the string as it would be in source code.
     */
    public String toPrettyString() {
        return toString();
    }
}
