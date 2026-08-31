package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;

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
    public static void preserve(GlobalVarDef variable) {
        if (!variable.hasAnnotation(ANNOTATION)) {
            Annotation marker = Ast.Annotation(variable.getSource(),
                Ast.Identifier(variable.getSource(), ANNOTATION.substring(1)), Ast.Arguments());
            variable.getModifiers().add(marker);
        }
    }

    public static boolean isPreserveAnnotation(String annotation) {
        return annotation.equalsIgnoreCase(ANNOTATION);
    }
}
