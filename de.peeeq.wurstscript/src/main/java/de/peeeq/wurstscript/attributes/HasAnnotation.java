package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.NameDef;
import org.jetbrains.annotations.NotNull;

public class HasAnnotation {
    @NotNull
    public static String normalizeAnnotation(String string) {
        String normalizedAnnotation = string.toLowerCase();
        if (! normalizedAnnotation.startsWith("@")) {
            normalizedAnnotation = "@" + normalizedAnnotation;
        }
        return normalizedAnnotation;
    }

    public static boolean hasAnnotation(NameDef e, String annotation) {
        String norm = normalizeAnnotation(annotation);
        if (e.getModifiers().size() > 0) {
            for (Modifier m : e.getModifiers()) {
                if (m instanceof Annotation) {
                    Annotation a = (Annotation) m;
                    if (normalizeAnnotation(a.getAnnotationType()).equals(norm)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Annotation getAnnotation(NameDef e, String annotation) {
        String norm = normalizeAnnotation(annotation);
        if (e.getModifiers().size() > 0) {
            for (Modifier m : e.getModifiers()) {
                if (m instanceof Annotation) {
                    Annotation a = (Annotation) m;
                    if (normalizeAnnotation(a.getAnnotationType()).equals(norm)) {
                        return a;
                    }
                }
            }
        }
        return null;
    }

}
