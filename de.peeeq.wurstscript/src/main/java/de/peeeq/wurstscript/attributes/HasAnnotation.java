package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.NameDef;

public class HasAnnotation {

    public static boolean hasAnnotation(NameDef e, String annotation) {
        Preconditions.checkArgument(annotation.startsWith("@"));
        for (Modifier m : e.getModifiers()) {
            if (m instanceof Annotation) {
                Annotation a = (Annotation) m;
                if (a.getAnnotationType().equals(annotation)) {
                    return true;
                }
            }
        }
        return false;
    }

}
