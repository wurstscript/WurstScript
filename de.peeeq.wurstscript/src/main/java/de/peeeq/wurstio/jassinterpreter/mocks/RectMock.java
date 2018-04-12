package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstReal;

public class RectMock {
    public ILconstReal minx;
    public ILconstReal miny;
    public ILconstReal maxx;
    public ILconstReal maxy;

    public RectMock(ILconstReal minx, ILconstReal miny, ILconstReal maxx, ILconstReal maxy) {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
    }
}
