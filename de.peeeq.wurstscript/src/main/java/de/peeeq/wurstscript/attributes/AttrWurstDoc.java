package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;

public class AttrWurstDoc {

    public static String getComment(NameDef nameDef) {
        if (nameDef instanceof HasModifier) {
            HasModifier HasModifier = nameDef;
            return getCommmentHelper(HasModifier);
        }
        return "";
    }

    private static String getCommmentHelper(
            HasModifier HasModifier) {
        Modifiers modifiers = HasModifier.getModifiers();
        for (Modifier m : modifiers) {
            if (m instanceof WurstDoc) {
                WurstDoc wurstDoc = (WurstDoc) m;
                return comment(wurstDoc);
            }
        }
        return "";
    }

    public static String getComment(ConstructorDef constructorDef) {
        return getCommmentHelper(constructorDef);
    }


    private static String comment(WurstDoc wurstDoc) {
        String result = wurstDoc.getRawComment();
        if (result.length() <= 5) {
            return "";
        }
        // strip of "/**" and */"
        result = result.replaceAll("^/\\*\\*+", "").replaceAll("\\*+/$", "");

        String[] lines = result.split("(\n|\r|\n\r|\r\n)[\t ]*(\\*)? ?");

        result = Utils.join(lines, "\n");

        return result;
    }


}
