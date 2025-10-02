package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TypeRewriter {

    public static void rewriteTypes(Element root, Function<ImType, ImType> rewriteFunc) {
        // Memoization is the key performance optimization
        Map<ImType, ImType> cache = new HashMap<>();
        Function<ImType, ImType> memoizedRewrite = t -> cache.computeIfAbsent(t, rewriteFunc);

        rewrite(root, memoizedRewrite);
    }

    private static void rewrite(Element e, Function<ImType, ImType> rewriteFunc) {
        if (e == null) {
            return;
        }

        // Apply rewrite logic using modern pattern matching switch
        switch (e) {
            case ImVar v -> v.setType(rewriteFunc.apply(v.getType()));
            case ImFunction f -> f.setReturnType(rewriteFunc.apply(f.getReturnType()));
            case ImNull n -> n.setType(rewriteFunc.apply(n.getType()));
            case ImTypeArgument ta -> ta.setType(rewriteFunc.apply(ta.getType()));
            case ImCast c -> c.setToType(rewriteFunc.apply(c.getToType()));
            case ImAlloc a -> a.setClazz((ImClassType) rewriteFunc.apply(a.getClazz()));
            case ImDealloc d -> d.setClazz((ImClassType) rewriteFunc.apply(d.getClazz()));
            case ImInstanceof i -> i.setClazz((ImClassType) rewriteFunc.apply(i.getClazz()));
            case ImTypeIdOfObj t -> t.setClazz((ImClassType) rewriteFunc.apply(t.getClazz()));
            case ImTypeIdOfClass t -> t.setClazz((ImClassType) rewriteFunc.apply(t.getClazz()));
            case ImMethod m -> m.setMethodClass((ImClassType) rewriteFunc.apply(m.getMethodClass()));
            case ImClass c -> {
                List<ImClassType> newSuperClasses = new ArrayList<>();
                for (ImClassType tt : c.getSuperClasses()) {
                    newSuperClasses.add((ImClassType) rewriteFunc.apply(tt));
                }
                c.setSuperClasses(newSuperClasses);
            }
            default -> {
                // No specific action for this node type
            }
        }

        // Recurse to children
        for (int i = 0; i < e.size(); i++) {
            rewrite(e.get(i), rewriteFunc);
        }
    }
}
