package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.ImLExpr;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;

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
            }
        }
        return false;
    }
}
