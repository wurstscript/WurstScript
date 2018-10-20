package de.peeeq.wurstscript.translation.imtranslation.purity;

import de.peeeq.wurstscript.jassIm.*;

public class PurityLevels {

    public static PurityLevel calculate(ImStmt s) {
        return mergeWithChildren(s, Pure.instance);
    }

    private static PurityLevel mergeWithChildren(Element e, PurityLevel level) {
        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (child instanceof ImStmt) {
                ImStmt imStmt = (ImStmt) child;
                level = level.merge(imStmt.attrPurity());
            } else {
                level = mergeWithChildren(child, level);
            }
        }
        return level;
    }

    public static PurityLevel calculate(ImCall s) {
        return ChangesTheWorld.instance;
    }

    public static PurityLevel calculate(ImSet s) {
        return mergeWithChildren(s, WritesGlobals.instance);
    }

    public static PurityLevel calculate(ImVarAccess s) {
        return mergeWithChildren(s, ReadsGlobals.instance);
    }

    public static PurityLevel calculate(ImVarArrayAccess s) {
        return mergeWithChildren(s, ReadsGlobals.instance);
    }

}
