package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;

public class UnitMock {
    public IlConstHandle owner;
    public ILconstInt unitid;
    public ILconstReal x;
    public ILconstReal y;
    public ILconstReal face;

    public UnitMock(IlConstHandle owner, ILconstInt unitid, ILconstReal x, ILconstReal y, ILconstReal face) {
        this.owner = owner;
        this.unitid = unitid;
        this.x = x;
        this.y = y;
        this.face = face;
    }
}
