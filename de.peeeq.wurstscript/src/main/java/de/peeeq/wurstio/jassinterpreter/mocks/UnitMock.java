package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;

import java.util.HashMap;

public class UnitMock {
    public IlConstHandle owner;
    public ILconstInt unitid;
    public ILconstReal x;
    public ILconstReal y;
    public ILconstReal face;
    public boolean removed;
    public final HashMap<String, ILconstReal> states = new HashMap<>();
    public final HashMap<Integer, ILconstInt> abilityLevels = new HashMap<>();
    public ILconstInt currentOrder = ILconstInt.create(0);

    public UnitMock(IlConstHandle owner, ILconstInt unitid, ILconstReal x, ILconstReal y, ILconstReal face) {
        this.owner = owner;
        this.unitid = unitid;
        this.x = x;
        this.y = y;
        this.face = face;
        states.put("unitstate0", ILconstReal.create(100));
        states.put("unitstate1", ILconstReal.create(100));
        states.put("unitstate2", ILconstReal.create(0));
        states.put("unitstate3", ILconstReal.create(0));
    }
}
