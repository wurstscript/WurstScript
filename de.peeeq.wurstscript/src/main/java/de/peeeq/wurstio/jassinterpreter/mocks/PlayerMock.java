package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;

import java.util.HashMap;

public class PlayerMock {
    public final ILconstInt id;
    public ILconst playerColor = ILconstNull.instance();
    public final HashMap<Integer, ILconstInt> techMaxAllowed = new HashMap<>();

    public PlayerMock(ILconstInt p) {
        this.id = p;
    }
}
