package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.RectMock;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

public class RectProvider extends Provider {


    public RectProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle Rect(ILconstReal minx, ILconstReal miny, ILconstReal maxx, ILconstReal maxy) {
        return new IlConstHandle(NameProvider.getRandomName("rect"), new RectMock(minx, miny, maxx, maxy));
    }

    public void RemoveRect(IlConstHandle rect) {
    }

}
