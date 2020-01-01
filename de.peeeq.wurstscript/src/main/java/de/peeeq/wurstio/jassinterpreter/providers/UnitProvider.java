package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.UnitMock;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class UnitProvider extends Provider {
    public UnitProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateUnit(IlConstHandle owner, ILconstInt unitid, ILconstReal x, ILconstReal y, ILconstReal face) {
        return new IlConstHandle(NameProvider.getRandomName("unit"), new UnitMock(owner, unitid, x, y, face));
    }

    public ILconstInt GetUnitTypeId(IlConstHandle unit) {
        return ((UnitMock)unit.getObj()).unitid;
    }
}
