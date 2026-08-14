package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads the type class bounds written on a new-style type parameter.
 * <p>
 * A bound names the interface without applying it, so {@code <T: Indexable>} means
 * "there is an instance of {@code Indexable<T>}". v1 only supports interfaces with exactly one
 * type parameter, which keeps the bound unambiguous and instance search a direct lookup.
 */
public final class TypeClassConstraints {

    private TypeClassConstraints() {
    }

    /**
     * The interfaces named as bounds of the given type parameter, in source order.
     * Unusable bounds are skipped; {@link #invalidBoundReason} explains why, and the validator
     * reports it at the bound itself so a bad bound does not cascade into every use site.
     */
    public static List<InterfaceDef> boundInterfaces(TypeParamDef tp) {
        List<TypeExpr> exprs = boundExprs(tp);
        if (exprs.isEmpty()) {
            return Collections.emptyList();
        }
        List<InterfaceDef> result = new ArrayList<>(exprs.size());
        for (TypeExpr e : exprs) {
            InterfaceDef def = resolveBound(e);
            if (def != null) {
                result.add(def);
            }
        }
        return result;
    }

    /** The raw bound type expressions, so callers can attach diagnostics to the right source range. */
    public static List<TypeExpr> boundExprs(TypeParamDef tp) {
        TypeParamConstraints constraints = tp.getTypeParamConstraints();
        if (!(constraints instanceof TypeExprList list) || list.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(list);
    }

    /** True if this type parameter carries at least one bound, i.e. it is more than a bare {@code <T:>}. */
    public static boolean hasBounds(TypeParamDef tp) {
        return !boundExprs(tp).isEmpty();
    }

    /**
     * Resolves one bound expression to the interface it names, or null when the bound is not a
     * plain reference to a single-parameter interface.
     */
    public static @Nullable InterfaceDef resolveBound(TypeExpr boundExpr) {
        return invalidBoundReason(boundExpr) == null ? namedInterface(boundExpr) : null;
    }

    /**
     * Explains why a bound cannot be used as a type class, or null when it is usable.
     * <p>
     * A bound must name an interface, unapplied, with exactly one type parameter, and that
     * interface must not extend another. The last restriction keeps the set of requirements equal
     * to the interface's own methods, so an instance cannot silently miss an inherited one.
     */
    public static @Nullable String invalidBoundReason(TypeExpr boundExpr) {
        if (!(boundExpr instanceof TypeExprSimple simple)) {
            return "A bound must name an interface.";
        }
        // A bound names the interface unapplied: writing the type argument would be redundant,
        // because it is always the type parameter being constrained.
        if (!simple.getTypeArgs().isEmpty()) {
            return "A bound must name the interface without type arguments, because the argument is"
                    + " always the type parameter being constrained.";
        }
        TypeDef def = boundExpr.lookupType(simple.getTypeName(), false);
        if (def == null) {
            return "Could not find " + simple.getTypeName() + ".";
        }
        if (!(def instanceof InterfaceDef i)) {
            return simple.getTypeName() + " is not an interface, so it cannot be used as a bound.";
        }
        if (i.getTypeParameters().size() != 1) {
            return i.getName() + " must have exactly one type parameter to be used as a bound, but has "
                    + i.getTypeParameters().size() + ".";
        }
        if (!i.getExtendsList().isEmpty()) {
            return i.getName() + " extends another interface, which is not supported for bounds:"
                    + " the requirements of a bound are the interface's own functions.";
        }
        return null;
    }

    private static @Nullable InterfaceDef namedInterface(TypeExpr boundExpr) {
        if (!(boundExpr instanceof TypeExprSimple simple)) {
            return null;
        }
        return boundExpr.lookupType(simple.getTypeName(), false) instanceof InterfaceDef i ? i : null;
    }

    /**
     * Looks up a method required by any bound of the given type parameter.
     * Earlier bounds win, matching the left-to-right order the bounds were written in.
     */
    public static @Nullable FuncDef findRequiredMethod(TypeParamDef tp, String name) {
        for (InterfaceDef bound : boundInterfaces(tp)) {
            for (FuncDef m : bound.getMethods()) {
                if (m.getName().equals(name)) {
                    return m;
                }
            }
        }
        return null;
    }
}
