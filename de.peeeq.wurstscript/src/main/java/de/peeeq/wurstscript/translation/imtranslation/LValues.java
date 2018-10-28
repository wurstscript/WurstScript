package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;

/**
 *
 */
public class LValues {
    public static boolean isUsedAsLValue(ImLExpr e) {
        Element parent = e.getParent();
        if (parent != null) {
            if (parent instanceof ImTupleSelection) {
                ImTupleSelection ts = (ImTupleSelection) parent;
                return isUsedAsLValue(ts);
            } else if (parent instanceof ImSet) {
                ImSet set = (ImSet) parent;
                return set.getLeft() == e;
            } else if (parent instanceof ImStatementExpr) {
                ImStatementExpr se = (ImStatementExpr) parent;
                return isUsedAsLValue(se);
            }
        }
        return false;
    }
}
