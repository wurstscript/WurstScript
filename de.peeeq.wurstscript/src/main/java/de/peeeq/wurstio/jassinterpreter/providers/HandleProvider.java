package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.LinkedHashMap;

public class HandleProvider extends Provider {
    /** Ids start at 1: the game reserves 0 for a null handle, so nothing may be given it. */
    private int handleCounter = 1;
    private final LinkedHashMap<IlConstHandle, ILconstInt> handleMap = new LinkedHashMap<>();

    public HandleProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    /**
     * Takes ILconst rather than IlConstHandle so a null handle reaches us: natives are dispatched
     * by name and arity, so a null argument used to fail the reflective invoke with an Error. The
     * game answers 0 for one, which is why no real handle is given that id.
     */
    public ILconstInt GetHandleId(ILconst handle) {
        if (handle instanceof ILconstNull) {
            return ILconstInt.create(0);
        }
        if (!(handle instanceof IlConstHandle h)) {
            throw new InterpreterException("GetHandleId expects a handle, got " + handle.print() + ".");
        }
        return handleMap.computeIfAbsent(h, (_key) -> ILconstInt.create(handleCounter++));
    }


}
