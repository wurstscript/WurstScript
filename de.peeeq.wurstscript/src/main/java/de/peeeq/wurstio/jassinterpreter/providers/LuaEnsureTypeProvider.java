package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.*;
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
}
