package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImVar;

public enum VarFlag {
    /** this is a variable from blizzard.j */
    BJ,
    /** this is a parameter that should be specialized:
     * for each subclass a copy of the function should be created by the optimizer */
    SPECIALIZE;

    public static boolean isBj(ImVar v) {
        return v.getVarFlags().contains(BJ);
    }
}
