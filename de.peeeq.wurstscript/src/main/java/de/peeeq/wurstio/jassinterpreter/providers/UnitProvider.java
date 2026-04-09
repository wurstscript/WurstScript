package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.objectreader.ObjectHelper;
import de.peeeq.wurstio.jassinterpreter.mocks.UnitMock;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
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

    public ILconstString GetUnitName(IlConstHandle unit) {
        if (unit == null) {
            return new ILconstString("");
        }
        UnitMock unitMock = (UnitMock) unit.getObj();
        return new ILconstString(ObjectHelper.objectIdIntToString(unitMock.unitid.getVal()));
    }

    public ILconstInt GetUnitGoldCost(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetUnitWoodCost(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetUnitPointValueByType(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetFoodUsed(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetUnitBuildTime(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt BlzGetUnitIntegerField(IlConstHandle whichUnit, IlConstHandle whichField) {
        return ILconstInt.create(0);
    }

    public ILconstInt BlzGetUnitWeaponIntegerField(IlConstHandle whichUnit, IlConstHandle whichField, ILconstInt index) {
        return ILconstInt.create(0);
    }

    public ILconstBool IsUnitType(IlConstHandle whichUnit, IlConstHandle whichUnitType) {
        return ILconstBool.FALSE;
    }

    public void RemoveUnit(IlConstHandle unit) {
        userDataMap.remove(unit);
    }

    public ILconstInt GetUnitUserData(IlConstHandle unit) {
        return unit == null ? ILconstInt.create(0) : userDataMap.getOrDefault(unit, ILconstInt.create(0));
    }

    public void SetUnitUserData(IlConstHandle unit, ILconstInt userData) {
        if (unit == null) {
            return;
        }
        userDataMap.put(unit, userData);
    }
}
