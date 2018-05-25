package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstReal;

public class LocationMock {
    public ILconstReal x;
    public ILconstReal y;

    public LocationMock(ILconstReal x, ILconstReal y) {
        this.x = x;
        this.y = y;
    }

    public void move(ILconstReal x, ILconstReal y) {
        // TODO
    }
}
