package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;

public class PlayerMock {
    public final ILconstInt id;
    public ILconst playerColor = ILconstNull.instance();

    public PlayerMock(ILconstInt p) {
        this.id = p;
    }
}
