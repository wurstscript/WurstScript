package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.LinkedHashMap;

public class HandleProvider extends Provider {
    private int handleCounter = 0;
    private final LinkedHashMap<IlConstHandle, ILconstInt> handleMap = new LinkedHashMap<>();

    public HandleProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstInt GetHandleId(IlConstHandle handle) {
        return handleMap.computeIfAbsent(handle,(_key) -> ILconstInt.create(handleCounter++));
    }


}
