package de.peeeq.wurstscript;

import com.google.common.collect.ImmutableMultimap;
import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Lazy;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeClasses {
    /**
     * How often an instance declaration can be used in a single derivation.
     */
    private static final int DERIVATION_MAX_INSTANCE_USES = 10;

    private static final ImmutableList<String> OBJ_INSTANCES = ImmutableList.of("FromIndex", "ToIndex", "AnyRef");

    public static Pair<FunctionSignature, List<CompileError>> findTypeClasses(FunctionSignature sig, StmtCall fc) {
        List<CompileError> errors = new ArrayList<>();
        VariableBinding mapping = sig.getMapping();
        for (TypeParamDef tp : sig.getDefinitionTypeVariables()) {
            Option<WurstTypeBoundTypeParam> matchedTypeOpt = mapping.get(tp);
            List<WurstTypeInterface> constraints = getConstraints(tp);
            if (matchedTypeOpt.isEmpty()) {
                if (!constraints.isEmpty()) {
                    errors.add(new CompileError(fc.attrSource(), "Type parameter " + tp.getName() + " is not bound, so type constraints cannot be solved."));
                }
                continue;
            }
            WurstTypeBoundTypeParam matchedType = matchedTypeOpt.get();
            for (WurstTypeInterface constraint : constraints) {
                VariableBinding mapping2 = findTypeClass(fc, errors, mapping, tp, matchedType, constraint);
                if (mapping2 == null) {
                    errors.add(new CompileError(fc.attrSource(), "Type " + matchedType + " does not satisfy constraint " + tp.getName() + ": " + constraint + "."));
                } else {
                    mapping = mapping2;
                }
            }
        }
        sig = sig.setTypeArgs(fc, mapping);
        return Pair.create(sig, errors);
    }

    @NotNull
    public static List<WurstTypeInterface> getConstraints(TypeParamDef tp) {
        List<WurstTypeInterface> constraints = new ArrayList<>();
        if (tp.getTypeParamConstraints() instanceof TypeParamConstraintList) {
            for (TypeParamConstraint c : ((TypeParamConstraintList) tp.getTypeParamConstraints())) {
                WurstType ct = c.attrConstraintTyp();
                if (ct instanceof WurstTypeInterface) {
                    WurstTypeInterface wti = (WurstTypeInterface) ct;
                    constraints.add(wti);


                }
            }
        }
        return constraints;
    }

    /**
     * Finds an instance of a type class
     *
     * @param location
     * @param errors
     * @param mapping
     * @param tp
     * @param matchedType
     * @param constraint
     * @return the updated type var mapping if any was found
     * <p>
     * TODO change to stream return type to allow backtracking
     */
    public static VariableBinding findTypeClass(Element location, List<CompileError> errors, VariableBinding mapping, TypeParamDef tp, WurstTypeBoundTypeParam matchedType, WurstTypeInterface constraint) {
        return findTypeClassH(location, errors, mapping, tp, matchedType, constraint, HashMap.empty());
    }

    private static VariableBinding findTypeClassH(Element location, List<CompileError> errors, VariableBinding mapping, TypeParamDef tp, WurstTypeBoundTypeParam matchedType, WurstTypeInterface constraint1, HashMap<InstanceDecl, Integer> uses) {
        WurstTypeInterface constraint = (WurstTypeInterface) constraint1.setTypeArgs(mapping);

        // option 1: the matched type is a type param that also has the right constraint:
        VariableBinding mapping2 = findDerived(location, matchedType, constraint, tp, mapping);
        if (mapping2 != null) {
            return mapping2;
        }
        // option 2: find instance declarations
        mapping2 = findInstanceDeclarations(location, errors, mapping, tp, matchedType, uses, constraint);
        if (mapping2 != null) {
            return mapping2;
        }
        // option 3: built-in instance for objects
        if (OBJ_INSTANCES.contains(constraint.getDef().getName())) {
            if (matchedType.getBaseType() instanceof WurstTypeClassOrInterface) {
                WurstTypeClassOrInterface objectType = (WurstTypeClassOrInterface) matchedType.getBaseType();

                TypeClassInstance instance = TypeClassInstance.fromObject(objectType, constraint.getDef());
                return mapping.withTypeClassInstance(tp, matchedType, instance);
            }
        }
        // not found:
        errors.add(new CompileError(location,
                        "Type " + matchedType + " does not satisfy constraint " + tp.getName() + ": " + constraint.getName()));
        return null;
    }

    private static VariableBinding findDerived(Element location, WurstTypeBoundTypeParam matchedType, WurstTypeInterface constraint, TypeParamDef tp, VariableBinding mapping) {
        if (matchedType.getBaseType() instanceof WurstTypeTypeParam) {
            WurstTypeTypeParam wtp = (WurstTypeTypeParam) matchedType.getBaseType();
            Optional<TypeParamConstraint> matchingConstraint = wtp.getTypeConstraints().stream()
                .filter(c -> c.attrConstraintTyp().isSubtypeOf(constraint, location)).findFirst();
            if (matchingConstraint.isPresent()) {
                TypeClassInstance instance = TypeClassInstance.fromTypeParam(
                    location, matchingConstraint.get());
                return mapping.withTypeClassInstance(tp, matchedType, instance);
            }
        }
        return null;
    }


    @org.jetbrains.annotations.Nullable
    private static VariableBinding findInstanceDeclarations(Element location, List<CompileError> errors, VariableBinding mapping, TypeParamDef tp, WurstTypeBoundTypeParam matchedType, HashMap<InstanceDecl, Integer> uses, WurstTypeInterface constraint) {
        // TODO create index to make this faster and use normal scoped lookup (ony search imports)

        @Nullable PackageOrGlobal wPackageG = location.attrNearestPackage();
        if (!(wPackageG instanceof WPackage)) {
            return null;
        }
        WPackage wPackage = (WPackage) wPackageG;
        List<TypeClassInstance> instances = wPackage.attrTypeClasses().stream()
            .flatMap(instance -> {
                int instanceUses = uses.getOrElse(instance, 0);
                if (instanceUses > 10) {
                    return Stream.empty();
                }
                Lazy<HashMap<InstanceDecl, Integer>> uses2 = Lazy.create(() -> uses.put(instance, instanceUses + 1));

                WurstType instanceType;
                try {
                    instanceType = instance.getImplementedInterface().attrTyp();
                } catch (CyclicDependencyError e) {
                    return Stream.empty();
                }
                // TODO instead of initialMapping should use previous mapping here
                // but that would require fresh variables (needs refactoring)
                VariableBinding initialMapping = VariableBinding.emptyMapping().withTypeVariables(instance.getTypeParameters());
                VariableBinding match = instanceType.matchAgainstSupertype(constraint, location, initialMapping, VariablePosition.LEFT);

                if (match == null) {
                    return Stream.empty();
                }
                instanceType = instanceType.setTypeArgs(match);


                for (Tuple2<TypeParamDef, WurstTypeBoundTypeParam> m : match) {
                    TypeParamDef instanceTp = m._1();
                    WurstTypeBoundTypeParam mType = m._2();
                    List<WurstTypeInterface> instanceConstraints = getConstraints(instanceTp);
                    for (WurstTypeInterface instanceConstraint : instanceConstraints) {
                        VariableBinding match2 = findTypeClassH(location, errors, match, instanceTp, mType, instanceConstraint, uses2.get());
                        if (match2 == null) {
                            return Stream.empty();
                        }
                        match = match2;
                    }
                }

                List<TypeClassInstance> deps = new ArrayList<>();
                List<WurstType> typeArgs = new ArrayList<>();
                for (TypeParamDef instanceTp : instance.getTypeParameters()) {
                    WurstTypeBoundTypeParam i = match.get(instanceTp).get();
                    @Nullable List<TypeClassInstance> is = i.getInstances();
                    if (is != null) {
                        deps.addAll(is);
                    }
                    if (instanceTp.getTypeParamConstraints() instanceof TypeParamConstraintList) {
                        typeArgs.add(i);
                    }
                }


                // TODO resolve dependencies

                TypeClassInstance result = TypeClassInstance.fromInstance(instance, typeArgs, deps, (WurstTypeInterface) instanceType);

                return Stream.of(result);

            }).collect(Collectors.toList());

        if (instances.isEmpty()) {
            return null;
        } else {
            if (instances.size() > 1) {
                errors.add(new CompileError(location,
                    "There are multiple instances for type " + matchedType + " and constraint " + tp.getName() + ": " + constraint.getName() + "\n" +
                        Utils.printSep("\n", instances)));

            }
            TypeClassInstance instance = Utils.getFirst(instances);
            return mapping.withTypeClassInstance(tp, matchedType, instance);
        }
    }

    public static List<InstanceDecl> availableTypeClasses(WPackage p) {
        return Stream.concat(
            p.attrDefinedTypeClasses().stream(),
            p.getImports().stream()
                .map(WImport::attrImportedPackage)
                .flatMap(ip -> {
                    if (ip == null) {
                        return Stream.empty();
                    } else {
                        return ip.attrDefinedTypeClasses().stream()
                            .filter(InstanceDecl::attrIsPublic);
                    }
                }))
            .collect(Collectors.toList());
    }

    public static List<InstanceDecl> definedTypeClasses(WPackage p) {
        return p.getElements().stream()
            .filter(e -> e instanceof InstanceDecl)
            .map(e -> (InstanceDecl) e)
            .collect(Collectors.toList());
    }

    public static void checkInstance(InstanceDecl c) {
        ImmutableMultimap<String, DefLink> nameLinks = c.attrNameLinks();
        if (!c.attrIsAbstract()) {
            StringBuilder toImplement = new StringBuilder();
            // should have no abstract methods
            for (DefLink link : nameLinks.values()) {
                NameDef f = link.getDef();
                if (f.attrIsAbstract()) {
                    if (f.attrNearestStructureDef() == c) {
                        Element loc = f.getModifiers().stream()
                            .filter(m -> m instanceof ModAbstract)
                            .<Element>map(x -> x)
                            .findFirst()
                            .orElse(f);
                        loc.addError(Utils.printElementWithSource(c) + " cannot have abstract functions like " + f.getName());
                    } else if (link instanceof FuncLink) {
                        toImplement.append("\n    ");
                        toImplement.append(((FuncLink) link).printFunctionTemplate());
                    }
                }
            }
            if (toImplement.length() > 0) {
                c.addError(Utils.printElementWithSource(c) + " must implement the following functions:" + toImplement);
            }
        }

    }
}
