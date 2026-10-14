package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstTypeTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
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
        for (de.peeeq.wurstscript.jassIm.Element owner = variable; owner != null; owner = owner.getParent()) {
            if (owner instanceof ImProg prog) {
                return prog.getPreservedVariables().contains(variable.getTrace());
            }
        }
        return false;
    }

    /** Immutable source identities copied into private IM before scoped AST metadata is restored. */
    public static Set<NameDef> preservedVariables(Collection<CompilationUnit> units) {
        Set<NameDef> result = Collections.newSetFromMap(new IdentityHashMap<>());
        java.util.ArrayDeque<Element> todo = new java.util.ArrayDeque<>(units);
        while (!todo.isEmpty()) {
            Element element = todo.removeLast();
            if (element instanceof NameDef definition && definition.hasAnnotation(ANNOTATION)) {
                result.add(definition);
            }
            for (int i = 0; i < element.size(); i++) todo.add(element.get(i));
        }
        return Collections.unmodifiableSet(result);
    }

    public static SavedMarkers saveSyntheticMarkers(WurstModel model) {
        return new SavedMarkers(model);
    }

    public static final class SavedMarkers implements AutoCloseable {
        private record Marker(GlobalVarDef variable, int position, Annotation annotation) {}
        private final WurstModel model;
        private final List<Marker> markers = new ArrayList<>();

        private SavedMarkers(WurstModel model) {
            this.model = model;
            model.accept(new Element.DefaultVisitor() {
                @Override public void visit(GlobalVarDef variable) {
                    super.visit(variable);
                    for (int i = 0; i < variable.getModifiers().size(); i++) {
                        Modifier modifier = variable.getModifiers().get(i);
                        if (isSyntheticMarker(modifier)) markers.add(new Marker(variable, i, (Annotation) modifier));
                    }
                }
            });
        }

        @Override public void close() {
            CompilationCancellation.run(() -> false, () -> {
                clearSyntheticMarkers(model);
                for (Marker marker : markers) {
                    marker.variable().getModifiers().add(marker.position(), marker.annotation());
                }
            });
        }
    }

    private static boolean isSyntheticMarker(Modifier modifier) {
        return modifier instanceof Annotation annotation
            && annotation.getAnnotationType().equalsIgnoreCase(ANNOTATION)
            && annotation.getArgs().size() == 1
            && annotation.getArgs().get(0) instanceof ExprStringVal value
            && value.getValS().equals(SYNTHETIC_MARKER);
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
    public static RuntimeNameIndex indexGlobals(Collection<CompilationUnit> units) {
        RuntimeNameIndex result = new RuntimeNameIndex();
        Element.DefaultVisitor visitor = new Element.DefaultVisitor() {
            @Override
            public void visit(GlobalVarDef variable) {
                super.visit(variable);
                String name = runtimeName(variable);
                result.add(name, variable);
                addTupleComponentNames(result, name, variable.attrTyp(), variable,
                    Collections.newSetFromMap(new IdentityHashMap<>()));
            }
        };
        units.forEach(cu -> cu.accept(visitor));
        return result;
    }

    /** Removes markers synthesized for TRVE during an earlier validation run. */
    public static void clearSyntheticMarkers(WurstModel model) {
        model.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(GlobalVarDef variable) {
                super.visit(variable);
                variable.getModifiers().removeIf(NamePreservation::isSyntheticMarker);
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
