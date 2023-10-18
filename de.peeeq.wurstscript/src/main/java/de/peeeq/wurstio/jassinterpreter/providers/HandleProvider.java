package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class HandleProvider extends Provider {
    private int handleCounter = 0;

    public HandleProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstInt GetHandleId(IlConstHandle handle) {
        return ILconstInt.create(handleCounter++);
    }


}
