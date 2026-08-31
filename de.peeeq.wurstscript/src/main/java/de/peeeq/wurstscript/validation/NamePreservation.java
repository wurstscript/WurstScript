package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstTypeTuple;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Metadata for names which are part of the Warcraft III-facing API. */
public final class NamePreservation {

    public static final String ANNOTATION = "@preserveName";

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
    public static @Nullable Annotation preserve(GlobalVarDef variable) {
        if (variable.hasAnnotation(ANNOTATION)) {
            return null;
        }
        Annotation marker = Ast.Annotation(variable.getSource(),
            Ast.Identifier(variable.getSource(), ANNOTATION.substring(1)), Ast.Arguments());
        variable.getModifiers().add(marker);
        return marker;
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
                addTupleComponentNames(result, name, variable.attrTyp(), variable);
            }
        });
        return result;
    }

    private static void addTupleComponentNames(RuntimeNameIndex index, String name, WurstType type,
                                               GlobalVarDef variable) {
        if (type instanceof WurstTypeArray array) {
            type = array.getBaseType();
        }
        if (!(type instanceof WurstTypeTuple tuple)) {
            return;
        }
        for (WParameter parameter : tuple.getTupleDef().getParameters()) {
            String componentName = name + "_" + parameter.getName();
            index.add(componentName, variable);
            addTupleComponentNames(index, componentName, parameter.attrTyp(), variable);
        }
    }

    public static final class RuntimeNameIndex {
        private final Map<String, List<GlobalVarDef>> globalsByName = new LinkedHashMap<>();
        private final Map<GlobalVarDef, Annotation> syntheticMarkers = new LinkedHashMap<>();

        private void add(String name, GlobalVarDef variable) {
            globalsByName.computeIfAbsent(name, ignored -> new ArrayList<>()).add(variable);
        }

        public void preserve(String runtimeName) {
            for (GlobalVarDef variable : globalsByName.getOrDefault(runtimeName, List.of())) {
                Annotation marker = NamePreservation.preserve(variable);
                if (marker != null) {
                    syntheticMarkers.put(variable, marker);
                }
            }
        }

        public void clearSyntheticMarkers() {
            for (Map.Entry<GlobalVarDef, Annotation> entry : syntheticMarkers.entrySet()) {
                entry.getKey().getModifiers().remove(entry.getValue());
            }
            syntheticMarkers.clear();
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
