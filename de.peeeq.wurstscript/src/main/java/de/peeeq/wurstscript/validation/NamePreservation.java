package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstTypeTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Metadata for names which are part of the Warcraft III-facing API. */
public final class NamePreservation {

    public static final String ANNOTATION = "@preserveName";
    private static final String SYNTHETIC_MARKER = "__wurst_trve_preserve_name";

    private NamePreservation() {
    }

    public static boolean isPreserved(ImFunction function) {
        return function.hasFlag(FunctionFlagEnum.PRESERVE_NAME);
    }

    public static boolean isPreserved(ImVar variable) {
        return variable.getTrace() instanceof NameDef
            && ((NameDef) variable.getTrace()).hasAnnotation(ANNOTATION);
    }

    public static void preserve(ImFunction function) {
        if (!isPreserved(function)) {
            function.getFlags().add(FunctionFlagEnum.PRESERVE_NAME);
        }
    }

    /**
     * Marks a resolved global used by TriggerRegisterVariableEvent without maintaining a
     * name-based side table. The marker remains attached to the AST definition and is copied to
     * the corresponding IM variable through its trace.
     */
    public static void preserve(GlobalVarDef variable) {
        if (variable.hasAnnotation(ANNOTATION)) {
            return;
        }
        Annotation marker = Ast.Annotation(variable.getSource(),
            Ast.Identifier(variable.getSource(), ANNOTATION.substring(1)),
            Ast.Arguments(Ast.ExprStringVal(variable.getSource(), SYNTHETIC_MARKER)));
        variable.getModifiers().add(marker);
    }

    /**
     * Resolves globals by their emitted runtime name, without consulting lexical name resolution.
     * The index is scoped to one validation run; the preservation marker itself remains attached to
     * the AST definition and is copied to the corresponding IM variables through their trace.
     */
    public static RuntimeNameIndex indexGlobals(WurstModel model) {
        RuntimeNameIndex result = new RuntimeNameIndex();
        model.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(GlobalVarDef variable) {
                super.visit(variable);
                String name = runtimeName(variable);
                result.add(name, variable);
                addTupleComponentNames(result, name, variable.attrTyp(), variable,
                    Collections.newSetFromMap(new IdentityHashMap<>()));
            }
        });
        return result;
    }

    /** Removes markers synthesized for TRVE during an earlier validation run. */
    public static void clearSyntheticMarkers(WurstModel model) {
        model.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(GlobalVarDef variable) {
                super.visit(variable);
                variable.getModifiers().removeIf(modifier -> modifier instanceof Annotation annotation
                    && annotation.getAnnotationType().equalsIgnoreCase(ANNOTATION)
                    && annotation.getArgs().size() == 1
                    && annotation.getArgs().get(0) instanceof ExprStringVal value
                    && value.getValS().equals(SYNTHETIC_MARKER));
            }
        });
    }

    private static void addTupleComponentNames(RuntimeNameIndex index, String name, WurstType type,
                                               GlobalVarDef variable, Set<TupleDef> expandedTuples) {
        if (type instanceof WurstTypeArray array) {
            type = array.getBaseType();
        }
        if (!(type instanceof WurstTypeTuple tuple)) {
            return;
        }
        if (!expandedTuples.add(tuple.getTupleDef())) {
            return;
        }
        for (WParameter parameter : tuple.getTupleDef().getParameters()) {
            String componentName = name + "_" + parameter.getName();
            index.add(componentName, variable);
            addTupleComponentNames(index, componentName, parameter.attrTyp(), variable, expandedTuples);
        }
    }

    public static final class RuntimeNameIndex {
        private final Map<String, List<GlobalVarDef>> globalsByName = new LinkedHashMap<>();

        private void add(String name, GlobalVarDef variable) {
            globalsByName.computeIfAbsent(name, ignored -> new ArrayList<>()).add(variable);
        }

        public void preserve(String runtimeName) {
            for (GlobalVarDef variable : globalsByName.getOrDefault(runtimeName, List.of())) {
                NamePreservation.preserve(variable);
            }
        }
    }

    private static String runtimeName(GlobalVarDef variable) {
        if (variable.getParent() != null && variable.getParent().getParent() instanceof NamedScope scope) {
            return runtimeName(scope) + "_" + variable.getName();
        }
        return variable.getName();
    }

    private static String runtimeName(NamedScope scope) {
        if (scope instanceof ModuleInstanciation instantiation) {
            return runtimeName(instantiation.getParent().attrNearestNamedScope()) + "_" + instantiation.getName();
        }
        return scope.getName();
    }

    public static boolean isPreserveAnnotation(String annotation) {
        return annotation.equalsIgnoreCase(ANNOTATION);
    }
}
