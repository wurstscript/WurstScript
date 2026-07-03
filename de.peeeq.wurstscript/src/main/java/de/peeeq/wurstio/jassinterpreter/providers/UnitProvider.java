package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.objectreader.ObjectHelper;
import de.peeeq.wurstio.jassinterpreter.mocks.DestructableMock;
import de.peeeq.wurstio.jassinterpreter.mocks.UnitMock;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;
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

    public ILconst GetOwningPlayer(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstNull.instance() : unitMock.owner;
    }

    public ILconstInt GetUnitTypeId(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstInt.create(0) : unitMock.unitid;
    }

    public ILconstReal GetUnitX(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.x;
    }

    public ILconstReal GetUnitY(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.y;
    }

    public void SetUnitX(IlConstHandle unit, ILconstReal x) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.x = x;
        }
    }

    public void SetUnitY(IlConstHandle unit, ILconstReal y) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.y = y;
        }
    }

    public ILconstReal GetUnitFacing(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.face;
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
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.removed = true;
        }
        userDataMap.remove(unit);
    }

    public void KillUnit(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.states.put("unitstate0", ILconstReal.create(0));
        }
    }

    public ILconstReal GetUnitState(IlConstHandle unit, IlConstHandle unitstate) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstReal.create(0);
        }
        return unitMock.states.getOrDefault(unitStateKey(unitstate), ILconstReal.create(0));
    }

    public void SetUnitState(IlConstHandle unit, IlConstHandle unitstate, ILconstReal value) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.states.put(unitStateKey(unitstate), value);
        }
    }

    public ILconstReal GetWidgetLife(IlConstHandle widget) {
        UnitMock unitMock = unitOrNull(widget);
        if (unitMock != null) {
            return unitMock.states.getOrDefault("unitstate0", ILconstReal.create(0));
        }
        DestructableMock destructableMock = destructableOrNull(widget);
        return destructableMock == null ? ILconstReal.create(0) : destructableMock.life;
    }

    public void SetWidgetLife(IlConstHandle widget, ILconstReal newLife) {
        UnitMock unitMock = unitOrNull(widget);
        if (unitMock != null) {
            unitMock.states.put("unitstate0", newLife);
            return;
        }
        DestructableMock destructableMock = destructableOrNull(widget);
        if (destructableMock != null) {
            destructableMock.life = newLife;
        }
    }

    public ILconstBool UnitAddAbility(IlConstHandle unit, ILconstInt abilityId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstBool.FALSE;
        }
        unitMock.abilityLevels.putIfAbsent(abilityId.getVal(), ILconstInt.create(1));
        return ILconstBool.TRUE;
    }

    public ILconstBool UnitRemoveAbility(IlConstHandle unit, ILconstInt abilityId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstBool.FALSE;
        }
        return ILconstBool.instance(unitMock.abilityLevels.remove(abilityId.getVal()) != null);
    }

    public ILconstInt GetUnitAbilityLevel(IlConstHandle unit, ILconstInt abilityId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstInt.create(0);
        }
        return unitMock.abilityLevels.getOrDefault(abilityId.getVal(), ILconstInt.create(0));
    }

    public ILconstInt SetUnitAbilityLevel(IlConstHandle unit, ILconstInt abilityId, ILconstInt level) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstInt.create(0);
        }
        unitMock.abilityLevels.put(abilityId.getVal(), level);
        return level;
    }

    public ILconstBool IssueImmediateOrderById(IlConstHandle unit, ILconstInt orderId) {
        return issueOrder(unit, orderId);
    }

    public ILconstBool IssuePointOrderById(IlConstHandle unit, ILconstInt orderId, ILconstReal x, ILconstReal y) {
        return issueOrder(unit, orderId);
    }

    public ILconstBool IssueTargetOrderById(IlConstHandle unit, ILconstInt orderId, IlConstHandle target) {
        return issueOrder(unit, orderId);
    }

    public ILconstInt GetUnitCurrentOrder(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstInt.create(0) : unitMock.currentOrder;
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

    private ILconstBool issueOrder(IlConstHandle unit, ILconstInt orderId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstBool.FALSE;
        }
        unitMock.currentOrder = orderId;
        return ILconstBool.TRUE;
    }

    private UnitMock unitOrNull(IlConstHandle unit) {
        if (unit == null || !(unit.getObj() instanceof UnitMock)) {
            return null;
        }
        return (UnitMock) unit.getObj();
    }

    private DestructableMock destructableOrNull(IlConstHandle destructable) {
        if (destructable == null || !(destructable.getObj() instanceof DestructableMock)) {
            return null;
        }
        return (DestructableMock) destructable.getObj();
    }

    private String unitStateKey(IlConstHandle unitstate) {
        return unitstate == null ? "unitstate0" : unitstate.print();
    }
}
