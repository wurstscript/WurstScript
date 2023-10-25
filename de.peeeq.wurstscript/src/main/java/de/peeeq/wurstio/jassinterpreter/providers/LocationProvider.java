package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.LocationMock;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class LocationProvider extends Provider {
    private static IlConstHandle lastExpiredMock = null;

    public LocationProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle Location(ILconstReal x, ILconstReal y) {
        return new IlConstHandle(NameProvider.getRandomName("location"), new LocationMock(x, y));
    }

    public void RemoveLocation(IlConstHandle location) {
    }

    public void MoveLocation(IlConstHandle location, ILconstReal x, ILconstReal y) {
        LocationMock locationMock = (LocationMock) location.getObj();
        locationMock.move(x, y);
    }

    public ILconstReal GetLocationX(IlConstHandle location) {
        LocationMock locationMock = (LocationMock) location.getObj();
        return locationMock.x;
    }

    public ILconstReal GetLocationY(IlConstHandle location) {
        LocationMock locationMock = (LocationMock) location.getObj();
        return locationMock.y;
    }
}
