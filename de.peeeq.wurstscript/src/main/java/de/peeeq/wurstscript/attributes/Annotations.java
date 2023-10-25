package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.ExprStringVal;

/**
 *
 */
public class Annotations {
    public static String annotationType(Annotation a) {
        return "@" + a.getFuncNameId().getName();
    }

    public static String annotationMessage(Annotation a) {
        if (a.getArgs().size() == 1 && a.getArgs().get(0) instanceof ExprStringVal) {
            ExprStringVal s = (ExprStringVal) a.getArgs().get(0);
            return s.getValS();
        }
        return null;
    }
}
