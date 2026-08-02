package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unchanged API. No eager map allocations unless you actually set/get vars/arrays.
 */
public class LocalState extends State {

    private @Nullable ILconst returnVal;

    public LocalState() {
        // no eager allocations
    }

    public LocalState(ILconst returnVal) {
        this.returnVal = returnVal;
    }

    public @Nullable ILconst getReturnVal() {
        return returnVal;
    }

    public LocalState setReturnVal(@Nullable ILconst returnVal) {
        this.returnVal = returnVal;
        return this;
    }
}
