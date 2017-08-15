package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import org.eclipse.jdt.annotation.Nullable;


public class LocalState extends State {

    private @Nullable ILconst returnVal = null;

    public LocalState(ILconst returnVal) {
        this.setReturnVal(returnVal);
    }

    public LocalState() {
    }

    public @Nullable ILconst getReturnVal() {
        return returnVal;
    }

    public LocalState setReturnVal(@Nullable ILconst returnVal) {
        this.returnVal = returnVal;
        return this;
    }


}
