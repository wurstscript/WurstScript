package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.UnitMock;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.LinkedHashMap;

public class UnitProvider extends Provider {
    private final LinkedHashMap<IlConstHandle, ILconstInt> userDataMap = new LinkedHashMap<>();

    public UnitProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateUnit(IlConstHandle owner, ILconstInt unitid, ILconstReal x, ILconstReal y, ILconstReal face) {
        return new IlConstHandle(NameProvider.getRandomName("unit"), new UnitMock(owner, unitid, x, y, face));
    }

    public ILconstInt GetUnitTypeId(IlConstHandle unit) {
        return ((UnitMock)unit.getObj()).unitid;
    }

    public ILconstInt GetUnitUserData(IlConstHandle unit) {
        return userDataMap.get(unit);
    }

    public void SetUnitUserData(IlConstHandle unit, ILconstInt userData) {
        userDataMap.put(unit, userData);
    }
}
