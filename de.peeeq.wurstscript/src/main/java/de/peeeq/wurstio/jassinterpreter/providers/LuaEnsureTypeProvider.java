package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class LuaEnsureTypeProvider extends Provider {
    public LuaEnsureTypeProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstInt intEnsure(ILconstInt x) {
        return x;
    }

    public ILconstString stringEnsure(ILconstString x) {
        return x;
    }

    public ILconstBool boolEnsure(ILconstBool x) {
        return x;
    }

    public ILconstReal realEnsure(ILconstReal x) {
        return x;
    }

    public ILconstString stringConcat(ILconstString x, ILconstString y) { return new ILconstString(x.getVal() + y.getVal()); }
}
