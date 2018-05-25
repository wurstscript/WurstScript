package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.RectMock;
import de.peeeq.wurstio.jassinterpreter.mocks.RegionMock;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

public class RegionProvider extends Provider {


    public RegionProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateRegion() {
        return new IlConstHandle(NameProvider.getRandomName("region"), new RegionMock());
    }

    public void RemoveRegion(IlConstHandle region) {
    }

    public void RegionAddRect(IlConstHandle region, IlConstHandle rect) {
        // TODO
    }

    public ILconstReal GetRectCenterY(IlConstHandle rect) {
        RectMock rectMock = (RectMock) rect.getObj();
        return new ILconstReal((rectMock.miny.getVal() + rectMock.maxy.getVal() / 2.0));
    }

    public ILconstReal GetRectMinX(IlConstHandle rect) {
        RectMock rectMock = (RectMock) rect.getObj();
        return rectMock.minx;
    }

    public ILconstReal GetRectMinY(IlConstHandle rect) {
        RectMock rectMock = (RectMock) rect.getObj();
        return rectMock.miny;
    }

    public ILconstReal GetRectMaxX(IlConstHandle rect) {
        RectMock rectMock = (RectMock) rect.getObj();
        return rectMock.maxx;
    }

    public ILconstReal GetRectMaxY(IlConstHandle rect) {
        RectMock rectMock = (RectMock) rect.getObj();
        return rectMock.maxy;
    }

}
