package de.peeeq.wurstscript.attributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.jassAst.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;
import java.util.Set;

public class JassAttributes {

    public static Set<JassVar> attrIgnoredVariables(JassProg jassProgImpl) {
        return Sets.newLinkedHashSet();
    }

    public static Set<JassFunction> attrIgnoredFunctions(JassProg jassProgImpl) {
        return Sets.newLinkedHashSet();
    }

    public static @Nullable JassFunction attrFuncDef(JassFuncRef f) {
        String funcName = f.getFuncName();
        JassProg root = getRoot(f);
        for (JassFunction func : root.getFunctions()) {
            if (func.getName().equals(funcName)) {
                return func;
            }
        }

        return null;
    }

    private static JassProg getRoot(@Nullable Element e) {
        while (e != null) {
            if (e instanceof JassProg) {
                return (JassProg) e;
            }
            e = e.getParent();
        }
        throw new Error("Not attached to root.");
    }

    public static Map<Element, String> attrComments(JassProg jassProgImpl) {
        return Maps.newLinkedHashMap();
    }

}