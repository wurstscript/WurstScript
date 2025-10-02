package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.NameDef;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HasAnnotation {
    // OPTIMIZATION 1: Cache normalized annotations
    private static final Map<String, String> normalizationCache = new HashMap<>();

    @NotNull
    public static String normalizeAnnotation(String string) {
        // OPTIMIZATION 2: Check cache first
        String cached = normalizationCache.get(string);
        if (cached != null) {
            return cached;
        }

        // OPTIMIZATION 3: Avoid string concatenation for common case
        String normalized;
        if (string.charAt(0) == '@') {
            normalized = string.toLowerCase();
        } else {
            // Use StringBuilder for concatenation
            normalized = "@" + string.toLowerCase();
        }

        // Cache for future use
        normalizationCache.put(string, normalized);
        return normalized;
    }

    public static boolean hasAnnotation(NameDef e, String annotation) {
        // OPTIMIZATION 4: Early exit for no modifiers
        if (e.getModifiers().isEmpty()) {
            return false;
        }

        String norm = normalizeAnnotation(annotation);

        // OPTIMIZATION 5: Direct iteration without size check
        for (Modifier m : e.getModifiers()) {
            if (m instanceof Annotation) {
                Annotation a = (Annotation) m;
                // OPTIMIZATION 6: Cache annotation type normalization
                if (getNormalizedType(a).equals(norm)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Annotation getAnnotation(NameDef e, String annotation) {
        // OPTIMIZATION 7: Early exit
        if (e.getModifiers().isEmpty()) {
            return null;
        }

        String norm = normalizeAnnotation(annotation);

        for (Modifier m : e.getModifiers()) {
            if (m instanceof Annotation) {
                Annotation a = (Annotation) m;
                if (getNormalizedType(a).equals(norm)) {
                    return a;
                }
            }
        }
        return null;
    }

    // OPTIMIZATION 8: Cache normalized annotation types per Annotation object
    private static final Map<Annotation, String> annotationTypeCache = new HashMap<>();

    private static String getNormalizedType(Annotation a) {
        return annotationTypeCache.computeIfAbsent(a,
            ann -> normalizeAnnotation(ann.getAnnotationType()));
    }
}
