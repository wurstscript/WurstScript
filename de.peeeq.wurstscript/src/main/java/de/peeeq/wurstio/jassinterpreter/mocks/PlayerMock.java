package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;

public class PlayerMock {
    public final ILconstInt id;
    public IlConstHandle playerColor;

    public PlayerMock(ILconstInt p) {
        this.id = p;
    }
}
