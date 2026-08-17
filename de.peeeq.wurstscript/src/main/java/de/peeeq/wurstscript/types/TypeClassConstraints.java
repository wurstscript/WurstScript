package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
        FuncDef generic = firstGenericMethod(i);
        if (generic != null) {
            // Matching such a requirement means pairing the interface's method type parameters with
            // the implementation's, which this version does not do. Say so, rather than comparing
            // parameters that only look identical and reporting a mismatch between a name and itself.
            return i.getName() + "." + generic.getName() + " has its own type parameters, which is not"
                    + " supported for a bound: a requirement may only use the interface's type parameter.";
        }
        return null;
    }

    /** The first requirement declaring type parameters of its own, or null when none does. */
    public static @Nullable FuncDef firstGenericMethod(InterfaceDef iface) {
        for (FuncDef method : iface.getMethods()) {
            if (!method.getTypeParameters().isEmpty()) {
                return method;
            }
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
     * Surfaces the methods required by a type parameter's bounds as members of a receiver which
     * stands for that parameter, so that {@code T.f(args)} resolves.
     * <p>
     * Bounds are ordered and an earlier one wins, but only over the same signature: two bounds may
     * require the very same operation, and offering both would make every call ambiguous.
     * Differently shaped overloads are not in competition, so later bounds still contribute them and
     * overload resolution picks between them as usual.
     *
     * @param standsFor what the interface's own type parameter is bound to, which is what the
     *                  requirement's parameter and return types substitute to.
     * @param receiver  the type the call is written on.
     */
    public static void addRequirementMethods(TypeParamDef def, WurstType standsFor, WurstType receiver,
                                             Element node, String name, List<FuncLink> result) {
        List<FuncLink> supplied = new ArrayList<>();
        for (InterfaceDef bound : boundInterfaces(def)) {
            for (FuncDef method : bound.getMethods()) {
                if (!method.getName().equals(name)) {
                    continue;
                }
                FuncLink candidate = requirementLink(bound, method, standsFor, receiver, node);
                if (!alreadySupplied(supplied, candidate, node)) {
                    supplied.add(candidate);
                }
            }
        }
        result.addAll(supplied);
    }

    /** Every requirement of the bounds, for callers which want the whole set rather than one name. */
    public static Stream<FuncLink> requirementMethods(TypeParamDef def, WurstType standsFor,
                                                      WurstType receiver, Element node) {
        return boundInterfaces(def).stream()
                .flatMap(bound -> bound.getMethods().stream()
                        .map(method -> requirementLink(bound, method, standsFor, receiver, node)));
    }

    /**
     * Exposes one interface method as a requirement: the interface's own type parameter is
     * substituted by what the receiver stands for, so the call reads {@code T.f(args)} with the
     * arguments exactly as declared.
     */
    private static FuncLink requirementLink(InterfaceDef bound, FuncDef method, WurstType standsFor,
                                            WurstType receiver, Element node) {
        TypeParamDef ifaceParam = bound.getTypeParameters().get(0);
        VariableBinding binding = VariableBinding.emptyMapping()
                .set(ifaceParam, new WurstTypeBoundTypeParam(ifaceParam, standsFor, node));
        return FuncLink.create(method, bound)
                .withTypeArgBinding(node, binding)
                .withReceiverType(receiver);
    }

    /** True when an earlier bound already supplied a requirement of the same shape. */
    private static boolean alreadySupplied(List<FuncLink> supplied, FuncLink candidate, Element node) {
        for (FuncLink existing : supplied) {
            List<WurstType> a = existing.getParameterTypes();
            List<WurstType> b = candidate.getParameterTypes();
            if (a.size() != b.size()) {
                continue;
            }
            boolean same = true;
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).equalsType(b.get(i), node)) {
                    same = false;
                    break;
                }
            }
            if (same) {
                return true;
            }
        }
        return false;
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
