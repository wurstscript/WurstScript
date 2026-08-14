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
     * Bounds which do not resolve to a single-parameter interface are skipped here; the validator
     * reports those separately so that a bad bound does not cascade into every use site.
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
        if (!(boundExpr instanceof TypeExprSimple simple)) {
            return null;
        }
        // A bound names the interface unapplied: writing the type argument would be redundant,
        // because it is always the type parameter being constrained.
        if (!simple.getTypeArgs().isEmpty()) {
            return null;
        }
        TypeDef def = boundExpr.lookupType(simple.getTypeName(), false);
        if (!(def instanceof InterfaceDef i)) {
            return null;
        }
        if (i.getTypeParameters().size() != 1) {
            return null;
        }
        return i;
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
