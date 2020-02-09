package de.peeeq.wurstscript;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyPrinter;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeClasses {
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
     * @param location
     * @param errors
     * @param mapping
     * @param tp
     * @param matchedType
     * @param constraint1
     * @return the updated type var mapping if any was found
     *
     * TODO change to stream return type to allow backtracking
     */
    public static VariableBinding findTypeClass(Element location, List<CompileError> errors, VariableBinding mapping, TypeParamDef tp, WurstTypeBoundTypeParam matchedType, WurstTypeInterface constraint1) {
        WurstTypeInterface constraint = (WurstTypeInterface) constraint1.setTypeArgs(mapping);

        // option 1: the matched type is a type param that also has the right constraint:
        if (matchedType.getBaseType() instanceof WurstTypeTypeParam) {
            WurstTypeTypeParam wtp = (WurstTypeTypeParam) matchedType.getBaseType();
            Optional<TypeParamConstraint> matchingConstraint = wtp.getTypeConstraints().stream()
                .filter(c -> c.attrConstraintTyp().isSubtypeOf(constraint, location)).findFirst();
            if (matchingConstraint.isPresent()) {
                TypeClassInstance instance = TypeClassInstance.fromTypeParam(
                    location, matchingConstraint.get());
                return mapping.set(tp, matchedType.withTypeClassInstance(instance));
            }
        }
        // option 2: find instance declarations
        // TODO create index to make this faster and use normal scoped lookup (ony search imports)
        WurstModel model = location.getModel();
        List<TypeClassInstance> instances = model.stream()
            .flatMap(cu -> cu.getPackages().stream())
            .flatMap(p -> p.getElements().stream())
            .filter(e -> e instanceof InstanceDecl)
            .map(e -> (InstanceDecl) e)
            .flatMap(instance -> {
                WurstType instanceType;
                try {
                    instanceType = instance.getImplementedInterface().attrTyp();
                } catch (CyclicDependencyError e) {
                    return Stream.empty();
                }
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
                        VariableBinding match2 = findTypeClass(location, errors, match, instanceTp, mType, instanceConstraint);
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
            errors.add(new CompileError(location,
                "Type " + matchedType + " does not satisfy constraint " + tp.getName() + ": " + constraint.getName()));
            // "Could not find type class instance " + constraint.getName() + " for type " + matchedType));
            return null;
        } else {
            if (instances.size() > 1) {
                errors.add(new CompileError(location,
                    "There are multiple instances for type " + matchedType + " and constraint " + tp.getName() + ": " + constraint.getName() + "\n" +
                        Utils.printSep("\n", instances)));

            }
            TypeClassInstance instance = Utils.getFirst(instances);
            return mapping.set(tp, matchedType.withTypeClassInstance(instance));
        }
    }
}
