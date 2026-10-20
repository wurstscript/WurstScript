package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;

/**
 *
 */
public class LValues {
    public static boolean isUsedAsLValue(ImLExpr e) {
        Element parent = e.getParent();
        if (parent != null) {
            if (parent instanceof ImTupleSelection ts) {
                return isUsedAsLValue(ts);
            } else if (parent instanceof ImSet set) {
                return set.getLeft() == e;
            } else if (parent instanceof ImStatementExpr se) {
                return isUsedAsLValue(se);
            }
        }
        return false;
    }
}
