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

    public double getWidth() {
        return maxx.getVal() - minx.getVal();
    }

    public double getHeight() {
        return maxy.getVal() - miny.getVal();
    }
}
