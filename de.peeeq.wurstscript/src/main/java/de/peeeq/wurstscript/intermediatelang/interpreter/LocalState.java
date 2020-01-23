package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.jassIm.*;
import io.vavr.control.Either;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;


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
