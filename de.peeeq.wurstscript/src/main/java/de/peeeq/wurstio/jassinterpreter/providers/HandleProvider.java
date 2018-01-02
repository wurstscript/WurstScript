package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

public class HandleProvider extends Provider {

    public HandleProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstInt GetHandleId(IlConstHandle handle) {
        return ILconstInt.create(handle.hashCode());
    }


}
