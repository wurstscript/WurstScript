package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the type class instance which satisfies a bound.
 * <p>
 * Resolution deliberately does not search the import graph. An instance may only be declared in
 * the package that declares the interface or in the package that declares the type, so a lookup
 * only ever has to inspect those two packages. That keeps instance selection independent of which
 * packages happen to be imported at the use site, which in turn guarantees that a given
 * {@code (interface, type)} pair means the same thing everywhere in the program.
 */
public final class TypeClassInstances {

    private TypeClassInstances() {
    }

    /**
     * The single instance binding {@code iface} to {@code type}, or null when there is none.
     * Ambiguity is impossible by construction here; the validator rejects duplicate instances at
     * their declaration sites instead, where the error can point at both offenders.
     */
    public static @Nullable InstanceDecl find(InterfaceDef iface, WurstType type) {
        for (WPackage p : candidatePackages(iface, type)) {
            InstanceDecl found = findIn(p, iface, type);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /** The only two packages an instance for this pair is permitted to live in. */
    public static List<WPackage> candidatePackages(InterfaceDef iface, WurstType type) {
        List<WPackage> result = new ArrayList<>(2);
        WPackage ifacePackage = packageOf(iface);
        if (ifacePackage != null) {
            result.add(ifacePackage);
        }
        WPackage typePackage = packageOfType(type);
        if (typePackage != null && !result.contains(typePackage)) {
            result.add(typePackage);
        }
        return result;
    }

    private static @Nullable InstanceDecl findIn(WPackage p, InterfaceDef iface, WurstType type) {
        for (InstanceDecl decl : declaredIn(p)) {
            if (matches(decl, iface, type)) {
                return decl;
            }
        }
        return null;
    }

    /** The instance declarations written directly in the given package. */
    public static List<InstanceDecl> declaredIn(WPackage p) {
        List<InstanceDecl> result = new ArrayList<>();
        for (WEntity e : p.getElements()) {
            if (e instanceof InstanceDecl decl) {
                result.add(decl);
            }
        }
        return result;
    }

    /** True when this declaration is the instance for exactly {@code iface} applied to {@code type}. */
    public static boolean matches(InstanceDecl decl, InterfaceDef iface, WurstType type) {
        InterfaceDef declared = declaredInterface(decl);
        if (declared != iface) {
            return false;
        }
        WurstType declaredFor = instanceType(decl);
        return declaredFor != null && declaredFor.equalsType(type, decl);
    }

    /** The interface named by an instance declaration, e.g. {@code Indexable} in {@code instance Indexable<vec2>}. */
    public static @Nullable InterfaceDef declaredInterface(InstanceDecl decl) {
        if (!(decl.getImplementedInterface() instanceof TypeExprSimple simple)) {
            return null;
        }
        TypeDef def = decl.lookupType(simple.getTypeName(), false);
        return def instanceof InterfaceDef i ? i : null;
    }

    /** The concrete type an instance is declared for, e.g. {@code vec2} in {@code instance Indexable<vec2>}. */
    public static @Nullable WurstType instanceType(InstanceDecl decl) {
        if (!(decl.getImplementedInterface() instanceof TypeExprSimple simple)) {
            return null;
        }
        if (simple.getTypeArgs().size() != 1) {
            return null;
        }
        return simple.getTypeArgs().get(0).attrTyp();
    }

    /**
     * The instance method implementing the given requirement, or null when none matches.
     * <p>
     * An interface may overload a requirement name, so the match is on the substituted signature
     * rather than the name alone. Validation and lowering both go through here, so a rejected
     * instance and a selected implementation can never disagree.
     */
    public static @Nullable FuncDef findImplementation(InstanceDecl decl, FuncDef requirement,
                                                       WurstType instanceType) {
        List<FuncDef> sameName = new ArrayList<>();
        for (FuncDef provided : decl.getMethods()) {
            if (provided.getName().equals(requirement.getName())) {
                sameName.add(provided);
            }
        }
        if (sameName.size() == 1) {
            // The common case: one candidate, so let the signature check report any mismatch
            // against this one rather than silently finding nothing.
            return sameName.get(0);
        }
        for (FuncDef provided : sameName) {
            if (signatureMatches(provided, requirement, instanceType, decl)) {
                return provided;
            }
        }
        return null;
    }

    /** True when the implementation matches the requirement with the interface parameter substituted. */
    public static boolean signatureMatches(FuncDef provided, FuncDef requirement, WurstType instanceType,
                                           Element context) {
        VariableBinding binding = requirementBinding(requirement, instanceType, context);
        if (binding == null || provided.getParameters().size() != requirement.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < requirement.getParameters().size(); i++) {
            WurstType expected = requirement.getParameters().get(i).attrTyp().setTypeArgs(binding);
            if (!provided.getParameters().get(i).attrTyp().equalsType(expected, context)) {
                return false;
            }
        }
        WurstType expectedReturn = requirement.attrReturnTyp().setTypeArgs(binding);
        return provided.attrReturnTyp().equalsType(expectedReturn, context);
    }

    /** Replaces the interface's own type parameter by the type an instance is declared for. */
    public static @Nullable VariableBinding requirementBinding(FuncDef requirement, WurstType instanceType,
                                                               Element context) {
        ClassOrInterface owner = requirement.attrNearestClassOrInterface();
        if (!(owner instanceof InterfaceDef iface) || iface.getTypeParameters().size() != 1) {
            return null;
        }
        TypeParamDef ifaceParam = iface.getTypeParameters().get(0);
        return VariableBinding.emptyMapping()
                .set(ifaceParam, new WurstTypeBoundTypeParam(ifaceParam, instanceType, context));
    }

    private static @Nullable WPackage packageOf(Element e) {
        PackageOrGlobal p = e.attrNearestPackage();
        return p instanceof WPackage w ? w : null;
    }

    /**
     * The package which declares the given type, if it has one. Primitives and Jass handle types
     * have no declaring Wurst package, so instances for those must live with the interface.
     */
    private static @Nullable WPackage packageOfType(WurstType type) {
        WurstType t = type.normalize();
        if (t instanceof WurstTypeNamedScope ns && ns.getDef() != null) {
            return packageOf(ns.getDef());
        }
        if (t instanceof WurstTypeTuple tuple && tuple.getTupleDef() != null) {
            return packageOf(tuple.getTupleDef());
        }
        return null;
    }
}
