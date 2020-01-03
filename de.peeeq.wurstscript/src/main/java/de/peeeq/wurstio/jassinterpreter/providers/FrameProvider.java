package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.RectMock;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

/**
 * Some functions for frames, so that StdLib tests work.
 */
public class FrameProvider extends Provider {


    public FrameProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle BlzGetOriginFrame(IlConstHandle frameType, ILconstInt index) {
        return new IlConstHandle("framehandle", new FrameHandle());
    }

    public IlConstHandle BlzCreateFrameByType(ILconstString typeName, ILconstString name, IlConstHandle owner, ILconstString inherits, ILconstInt createContext) {
        return new IlConstHandle("framehandle", new FrameHandle());
    }

    public void BlzFrameSetSize(IlConstHandle frame, ILconstReal width, ILconstReal height) {
    }

    static class FrameHandle {
    }


    public IlConstHandle ConvertOriginFrameType(ILconstInt i) {
        return new IlConstHandle("frameType", i.getVal());
    }

}
