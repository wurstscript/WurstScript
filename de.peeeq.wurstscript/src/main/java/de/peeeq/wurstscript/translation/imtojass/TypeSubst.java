package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.jassIm.ImTypeClassFunc;
import de.peeeq.wurstscript.jassIm.ImTypeVar;
import de.peeeq.wurstscript.jassIm.ImTypeVarRef;
import de.peeeq.wurstscript.jassIm.JassIm;
import io.vavr.control.Either;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces type variables by the type arguments bound to them.
 * <p>
 * Substitution used to be expressed as two parallel lists -- the type variables of a function or
 * class next to the type arguments given at a use of it -- paired up positionally at each point of
 * use. That form lost information: a {@link ImTypeArgument} is a type <i>plus</i> what is known
 * about it, but pairing the lists unwrapped the argument and returned the bare {@link ImType}, so
 * the type class binding never survived. Callers which needed it had to re-attach it by hand, and
 * the ones which did not know to do so silently produced an argument with no binding.
 * <p>
 * Keeping the argument whole fixes that: substituting into an argument position yields the argument
 * that was bound, so its binding travels with the type it belongs to.
 * <p>
 * Lookup is by identity. IM nodes do not override {@code equals}, and a type variable stands for one
 * particular declaration, so two variables which merely share a name are different variables.
 */
public final class TypeSubst {

    private static final TypeSubst EMPTY =
            new TypeSubst(Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    private final Map<ImTypeVar, ImTypeArgument> bindings;
    /** Variables this substitution covers but for which no argument was supplied. */
    private final List<ImTypeVar> unbound;
    /** Kept so that a missing argument is reported the same way it was before. */
    private final List<ImTypeVar> typeVars;
    private final List<ImTypeArgument> typeArguments;

    private TypeSubst(Map<ImTypeVar, ImTypeArgument> bindings, List<ImTypeVar> unbound,
                      List<ImTypeVar> typeVars, List<ImTypeArgument> typeArguments) {
        this.bindings = bindings;
        this.unbound = unbound;
        this.typeVars = typeVars;
        this.typeArguments = typeArguments;
    }

    public static TypeSubst empty() {
        return EMPTY;
    }

    /**
     * Binds {@code typeVars} to {@code typeArguments} by position.
     * <p>
     * Fewer arguments than variables is allowed: the surplus variables stay unbound, and only a type
     * which actually mentions one of them fails. Callers rely on that, because a use may legitimately
     * leave the arguments off when it does not name the variable.
     */
    public static TypeSubst of(List<ImTypeVar> typeVars, List<ImTypeArgument> typeArguments) {
        if (typeVars.isEmpty()) {
            return EMPTY;
        }
        // LinkedHashMap rather than IdentityHashMap: the keys already compare by identity, and this
        // keeps iteration order stable, which matters wherever substitution feeds emitted names.
        Map<ImTypeVar, ImTypeArgument> bindings = new LinkedHashMap<>();
        List<ImTypeVar> unbound = new ArrayList<>();
        for (int i = 0; i < typeVars.size(); i++) {
            ImTypeVar typeVar = typeVars.get(i);
            if (i < typeArguments.size()) {
                // A variable repeated in the list keeps its first argument, as positional lookup did.
                bindings.putIfAbsent(typeVar, typeArguments.get(i));
            } else if (!bindings.containsKey(typeVar)) {
                unbound.add(typeVar);
            }
        }
        return new TypeSubst(bindings, unbound, typeVars, typeArguments);
    }

    public boolean isEmpty() {
        return bindings.isEmpty() && unbound.isEmpty();
    }

    /** The argument bound to {@code typeVar}, or null when this substitution does not cover it. */
    public @Nullable ImTypeArgument get(ImTypeVar typeVar) {
        return bindings.get(typeVar);
    }

    /** Applies this substitution to a type. Any binding on the replacing argument is not part of a type. */
    public ImType apply(ImType type) {
        if (isEmpty()) {
            return type;
        }
        return type.match(new TypeRewriteMatcher() {
            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                ImTypeArgument replacement = resolve(t);
                return replacement == null ? t : replacement.getType();
            }

            @Override
            protected ImTypeArgument rewriteTypeArgument(ImTypeArgument argument) {
                return TypeSubst.this.apply(argument);
            }
        });
    }

    /**
     * Applies this substitution to a type argument.
     * <p>
     * When the argument is exactly a type variable this substitution binds, the bound argument
     * replaces it whole, so the binding recorded at the use site reaches the body being substituted
     * into. A binding already present on the argument is more specific and is kept.
     */
    public ImTypeArgument apply(ImTypeArgument argument) {
        if (isEmpty()) {
            return argument;
        }
        if (argument.getType() instanceof ImTypeVarRef ref) {
            ImTypeArgument replacement = resolve(ref);
            if (replacement != null) {
                return JassIm.ImTypeArgument(replacement.getType(), binding(argument, replacement));
            }
        }
        return JassIm.ImTypeArgument(apply(argument.getType()), argument.getTypeClassBinding());
    }

    private static Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> binding(ImTypeArgument argument,
                                                                              ImTypeArgument replacement) {
        return argument.getTypeClassBinding().isEmpty()
                ? replacement.getTypeClassBinding()
                : argument.getTypeClassBinding();
    }

    private @Nullable ImTypeArgument resolve(ImTypeVarRef ref) {
        ImTypeVar typeVar = ref.getTypeVariable();
        ImTypeArgument bound = bindings.get(typeVar);
        if (bound != null) {
            return bound;
        }
        if (unbound.contains(typeVar)) {
            throw new RuntimeException("Could not find replacement for " + ref
                    + " when replacing " + typeVars + " with " + typeArguments);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        bindings.forEach((typeVar, argument) -> {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(typeVar.getName()).append(" -> ").append(argument.getType());
        });
        return sb.append("]").toString();
    }
}
