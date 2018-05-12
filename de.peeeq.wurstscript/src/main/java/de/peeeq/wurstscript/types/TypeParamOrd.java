package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.parser.WPos;
import fj.Ord;
import fj.Ordering;

/**
 *
 */
public class TypeParamOrd {

    private static final Ord<TypeParamDef> instance = Ord.ord((TypeParamDef x, TypeParamDef y) -> {
        int c1 = x.getName().compareTo(y.getName());
        if (c1 < 0) {
            return Ordering.LT;
        } else if (c1 > 0) {
            return Ordering.GT;
        }
        return compareSource(x.getSource(), y.getSource());
    });

    public static Ord<TypeParamDef> instance() {
        return instance;
    }

    private static Ordering compareSource(WPos x, WPos y) {
        int c1 = x.getFile().compareTo(y.getFile());
        if (c1 < 0) {
            return Ordering.LT;
        } else if (c1 > 0) {
            return Ordering.GT;
        }
        return Ordering.fromInt(y.getLeftPos() - x.getLeftPos());
    }
}
