package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class IntegerProvider extends Provider {

    public IntegerProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstInt BlzBitOr(ILconstInt x, ILconstInt y) {
        return ILconstInt.create(x.getVal() | y.getVal());
    }

    public ILconstInt BlzBitAnd(ILconstInt x, ILconstInt y) {
        return ILconstInt.create(x.getVal() & y.getVal());
    }

    public ILconstInt BlzBitXor(ILconstInt x, ILconstInt y) {
        return ILconstInt.create(x.getVal() ^ y.getVal());
    }

}
